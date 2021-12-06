package com.ori.origami.recoder;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import com.ori.origami.jni.Mp3encode;
import com.origami.utils.Ori;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;


/**
 * @by: origami
 * @date: {2021-11-29}
 * @info:
 **/
public class OriAudio implements Runnable {

    private OriAudio(){ }
    private static OriAudio oriAudio;
    public synchronized static OriAudio instance(){
        if(oriAudio == null){ oriAudio = new OriAudio(); }
        return oriAudio;
    }
    //采用频率
    //44100是目前的标准，但是某些设备仍然支持22050，16000，11025
    //采样频率一般共分为22.05KHz、44.1KHz、48KHz三个等级
    private final static int AUDIO_SAMPLE_RATE = 16000;
    //采用16位深pcm
    private final static int AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    //pcm缓冲区字节大小
    private int bufferSizeInBytes = 0;

    //声道 双通道可采用 AudioFormat.CHANNEL_IN_STEREO 但不保证所有设备都支持
    private int audio_channel = AudioFormat.CHANNEL_IN_STEREO;
    private final Object lock = new Object();
    private final LinkedBlockingQueue<BufferData> pcmBufferQueue = new LinkedBlockingQueue<>();
    private AudioState currentState = AudioState.NO_INIT;
    //录音对象
    private AudioRecord audioRecord;
    private Mp3encode mp3encode;
    private Mp3EncodeThread mp3EncodeThread;
    private byte[] mp3Buffer;
    private Handler threadHandler;

    /**
     * init by def
     * @param record_stereo  是否是录制立体声，需要设备支持
     */
    public void init(boolean record_stereo){
        init(MediaRecorder.AudioSource.MIC, AUDIO_SAMPLE_RATE,
                record_stereo? AudioFormat.CHANNEL_IN_STEREO : AudioFormat.CHANNEL_IN_MONO, AUDIO_ENCODING);
    }

    /**
     * @see AudioRecord#AudioRecord(int, int, int, int, int)
     * @param audioSource {@link MediaRecorder.AudioSource}
     * @param sampleRateInHz 以赫兹表示的采样率。 44100Hz 是目前唯一保证适用于所有设备的速率，
     *      *                    但其他速率（例如 22050、16000 和 11025）可能适用于某些设备
     * @param channelConfig 描述音频通道的配置
     * @param audioFormat
     */
    public void init(int audioSource, int sampleRateInHz, int channelConfig, int audioFormat){
        if(audioRecord == null){
            audioRecord = new AudioRecord(audioSource, sampleRateInHz, audio_channel = channelConfig, audioFormat,
                    bufferSizeInBytes = AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat));
            Ori.v("INIT", String.format("pcm缓冲区大小：%s  单通道缓冲区大小：%s", bufferSizeInBytes, bufferSizeInBytes / 2));
            if((currentState.status & 0b10) == 0b10){
                currentState = AudioState.NO_INIT_MP3;
            }else {
                currentState = AudioState.INIT;
            }
        }
    }

    public void initMp3encode(int mp3SampleRate, int mp3Channel, int mp3BitRate, int quality){
        if(mp3encode != null){ mp3encode.release(); mp3encode = null; }
        mp3encode = Mp3encode.newInstance(audioRecord.getSampleRate(), mp3SampleRate, mp3Channel, mp3BitRate, quality);
        mp3Buffer = new byte[(int) (mp3SampleRate * 1.25f) + 7200];
        mp3EncodeThread = new Mp3EncodeThread();
        if((currentState.status & 0b01) == 0b01){
            currentState = AudioState.NO_INIT;
        }else {
            currentState = AudioState.INIT;
        }
    }

    public Mp3encode mp3encode(){
        return mp3encode;
    }

    public void reStartRecord(){
        startRecord(null);
    }
    /**
     * 开始录音 或者 继续
     * @param fileName 保存的文件地址(全路径) ->: .../.../../../.../.._.mp3  暂停在开始的情况下传 null 就行
     */
    public void startRecord(String fileName) {
        AudioState old = currentState;
        if ((old.status & 0b11) != 0) {
            throw new RuntimeException("录音尚未初始化完毕~");
        }
        if(old == AudioState.RELEASE){
            throw new RuntimeException("尝试使用一个已经释放资源的录音类录制");
        }
        if (old == AudioState.RECORDING) {
            Ori.w("OriAudio", "正在录音中");
            return;
        }
        audioRecord.startRecording();
        currentState = AudioState.RECORDING;
        if(old == AudioState.STOP || old == AudioState.INIT) {
            if (mp3EncodeThread == null) {
                mp3EncodeThread = new Mp3EncodeThread();
            }
            mp3EncodeThread.initAndRun(fileName);
            new Thread(this).start();
        }else if(old == AudioState.PAUSE){
            synchronized (lock){ lock.notifyAll(); }
        }
        Ori.v("ORI","开始录音: " + fileName);
    }

    /**
     * 暂停录音
     */
    public void pauseRecord() {
        Log.d("AudioRecorder", "===pauseRecord===");
        if (currentState != AudioState.RECORDING) {
            throw new IllegalStateException("当前没有在录音");
        } else {
            audioRecord.stop();
            currentState = AudioState.PAUSE;
        }
        Ori.v("ORI","暂停录音");
    }

    /**
     * 停止录音 将保存mp3完整文件 可重新开始录音
     */
    public void stopRecord() {
        Log.d("AudioRecorder", "===stopRecord===");
        if (currentState != AudioState.RECORDING && currentState != AudioState.PAUSE) {
            throw new IllegalStateException("录音尚未开始");
        } else {
            currentState = AudioState.STOP;
            audioRecord.stop();
            if(threadHandler != null){
                Message message = Message.obtain(threadHandler, 0, null);
                threadHandler.sendMessage(message);
            }
        }
        Ori.v("ORI","停止录音");
    }

    public void release(){
        if(threadHandler != null){
            threadHandler.sendEmptyMessage(2);
        }else {
            if(mp3encode != null) {
                mp3encode.release();
            }
            pcmBufferQueue.clear();
        }
        audioRecord.release();
        audioRecord = null;
        oriAudio = null;
    }

    @Override
    public void run() {
        pcmBufferQueue.offer(new BufferData(new short[bufferSizeInBytes]));
        while (currentState == AudioState.RECORDING || currentState == AudioState.PAUSE){
            if(currentState == AudioState.PAUSE){
                synchronized (lock){
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        continue;
                    }
                }
            }
            BufferData buffer = pcmBufferQueue.poll();
            if(buffer == null){
                Ori.d("ORI", "无可用缓冲区, 开始新建缓冲区");
                buffer = new BufferData(new short[bufferSizeInBytes]);
            }
            int size = audioRecord.read(buffer.pcmDates, 0, bufferSizeInBytes);
            if(audio_channel == AudioFormat.CHANNEL_IN_STEREO){
                buffer.readSize = size / 2;
            }else {
                buffer.readSize = size;
            }
            Ori.d("ORI", String.format("读取成功字节：%s  单个通道：%s", size, buffer.readSize));
            if(threadHandler != null && buffer.readSize > 0){
                Message message = Message.obtain(threadHandler, 1, buffer);
                threadHandler.sendMessage(message);
            }
        }
    }

    //描述状态
    enum AudioState{
        NEW(0b11),
        NO_INIT(0b01),
        NO_INIT_MP3(0b10),
        INIT(0),
        RECORDING(0b1100),
        PAUSE(0b1000),
        STOP(0b100),
        RELEASE(0b10000);
        int status;
        AudioState(int status) {
            this.status = status;
        }
    }

    static class BufferData{
        short[] pcmDates;
        int readSize = 0;

        public BufferData(short[] pcmDates) { this.pcmDates = pcmDates; }
    }

    class Mp3EncodeThread implements Runnable{

        boolean isOver = false;
        private String fileName;
        private short[] pcm_l;
        private short[] pcm_r;
        private Handler m_threadHandler;

        public void initAndRun(String fileName) {
            isOver = false;
            File file = new File(fileName);
            if(file.getParentFile() != null && !file.getParentFile().exists()){
                file.getParentFile().mkdirs();
            }
            if(audio_channel == AudioFormat.CHANNEL_IN_STEREO){
                pcm_l = new short[bufferSizeInBytes / 2];
                pcm_r = new short[bufferSizeInBytes / 2];
            }
            this.fileName = fileName;
            new Thread(this).start();
        }

        @Override
        public void run() {
            FileOutputStream outputStream;
            try {
                outputStream = new FileOutputStream(fileName, false);
            } catch (FileNotFoundException e) {
                Ori.e("OriAudio", e);
                return;
            }
            Looper.prepare();
            m_threadHandler = new Handler(Looper.myLooper()){
                @Override
                public void handleMessage(@NonNull Message msg) {
                    if(isOver){ return; }
                    if(msg.what == 1) {
                        if (msg.obj instanceof BufferData) {
                            BufferData data = (BufferData) msg.obj;
                            int encodeSize;
                            if(audio_channel == AudioFormat.CHANNEL_IN_STEREO){
                                for (int i = 0; i < pcm_l.length; i++) {
                                    pcm_l[i] = data.pcmDates[2 * i];
                                    pcm_r[i] = data.pcmDates[2 * i + 1];
                                }
                                encodeSize = mp3encode.encode(pcm_l, pcm_r, data.readSize, mp3Buffer);
                            }else {
                                encodeSize = mp3encode.encode(data.pcmDates, data.pcmDates, data.readSize, mp3Buffer);
                            }
                            pcmBufferQueue.offer(data);
                            if (encodeSize > 0) {
                                Ori.d("ORI", "编码成功长度：" + encodeSize);
                                try {
                                    outputStream.write(mp3Buffer, 0, encodeSize);
                                } catch (IOException e) {
                                    Ori.w("OriAudio", "写入mp3帧发生错误");
                                    Ori.e("OriAudio", e);
                                    release();
                                }
                            }else {
                                Ori.w("OriAudio", "编码mp3帧失败: code-> " + encodeSize);
                            }
                        }
                    }else {
                        isOver = true;
                        int end = mp3encode.end(mp3Buffer, mp3Buffer.length);
                        if(end > 0){
                            try {
                                outputStream.write(mp3Buffer, 0, end);
                            } catch (IOException e) {
                                Ori.w("OriAudio", "写入mp3文件尾发生错误");
                                Ori.e("OriAudio", e);
                            }
                        }else {
                            Ori.w("OriAudio", "缓冲区写入mp3文件尾失败: code-> " + end);
                        }
                        release();
                        if(msg.what != 0){ releaseAll(); }
                    }

                }
            };
            threadHandler = m_threadHandler;
            Looper.loop();
        }


        public void release(){
            if(m_threadHandler != null){
                m_threadHandler.getLooper().quit();
                m_threadHandler = null;
            }
        }

        public void releaseAll(){
            if(mp3encode != null) {
                mp3encode.release();
            }
            pcmBufferQueue.clear();
        }

    }

}
