package com.ori.origami.jni;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * @by: origami
 * @date: {2021-11-29}
 * @info:
 **/
public class Mp3encode {

    static {
        System.loadLibrary("mp3encode");
    }
    /**
     *初始化编码器
     * @param pcm_sampleRate pcm 采样率
     * @param mp3_sampleRate mp3 采样率
     * @param mp3_channel    mp3 通道数
     * @param mp3_bitRate    mp3 比特率
     * @param quality      内部算法选择。 真正的质量是由比特率决定的
     * 但是这个变量会通过选择昂贵或廉价的算法来影响质量。
     * 质量=0..9。 0=最好（非常慢）。 9=最差。
     * 推荐：2 接近最好的质量，不要太慢
     *                5 质量好，速度快
     *                7 ok 质量，真的很快
     * @return
     */
    public static Mp3encode newInstance(int pcm_sampleRate, int mp3_sampleRate,
                                        int mp3_channel, int mp3_bitRate, int quality){
        Mp3encode mp3encode = new Mp3encode();
        mp3encode.init(pcm_sampleRate, mp3_sampleRate, mp3_channel, mp3_bitRate, quality);
        return mp3encode;
    }
    private Mp3encode() { }

    private long obj_ptr;

    private native void init(int pcm_sampleRate, int mp3_sampleRate,
                             int mp3_channel, int mp3_bitRate, int quality);

    public void setImage(String path){
        try {
            int size;
            File file = new File(path);
            FileInputStream stream = new FileInputStream(file);
            byte[] bytes = new byte[(int) file.length()];
            size = stream.read(bytes);
            Log.e("ORI-Image", "size : " + size + "bytes: " + bytes.length);
            setImage(bytes, size);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 128位id3v1 tag尾  好像也没什么用这个 api（好玩） = =
     * @param title    标题
     * @param artist   作者
     * @param album     专辑
     * @param year      年 只能4位数！
     */
    public native void setId3V1Tag(String title, String artist, String album, String year);
    /**
     *
     * @param buffer_l  左声道数据  --in
     * @param buffer_r  右声道数据  --in
     * @param size   取样数据大小 size --in
     * @param mp3buf    输出 --out
     * @return
     */
    public native int encode(short[] buffer_l, short[] buffer_r, int size, byte[] mp3buf);

    /**
     * 设置专辑封面
     * @param imageSrc
     */
    private native void setImage(byte[] imageSrc, int size);

    public native int end(byte[] mp3buffer, int mp3buffer_size);

    public native void release();

}
