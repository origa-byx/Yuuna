package com.origami.log;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.origami.utils.Ori;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @by: origami
 * @date: {2021-07-22}
 * @info:       30 api 以下在 根目录，以上在 android/data/包名 之中  （target_api 28的话可在根目录创建文件夹）
 **/
public class OriLog implements Runnable {

    private final String VER;//在日志命名上加入版本
    private final static int MAX_SIZE = 5 * 1024 * 1024;
    private final boolean debug;
    private static OriLog instance;
    private final String path;
    private String dateTime = "0000-00-00";
    private int index = 0;

    @SuppressLint("SimpleDateFormat")
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

    private final Object lock = new Object();//锁对象
    private final AtomicBoolean lock_flag = new AtomicBoolean(false);
    private final LinkedBlockingQueue<OriLogBean> logQueue = new LinkedBlockingQueue<>();

    /**
     * @param context
     * @param buildConfig_DEBUG     BuildConfig.DEBUG
     */
    public static void init(Context context, boolean buildConfig_DEBUG){
        if(instance == null){ instance = new OriLog(context, buildConfig_DEBUG); }else { return; }
        new Thread(instance).start();
        instance.log_print(OriLogBean.i("日志模块初始化：" + OriLogBean.dateFormat.format(new Date())));
    }
    public synchronized static OriLog getInstance(){
        if(instance == null){ throw new RuntimeException("you must be init<OriLog> at first"); }
        return instance;
    }
    private OriLog(Context context, boolean debug){
        this.VER = Ori.getVersion(context).version;
        this.debug = debug;
        this.path = Ori.getSaveFilePath(context) + "log";
    }

    public void log_print(OriLogBean bean){
        if(bean == null){ return; }
        if(debug && !TextUtils.isEmpty(bean.msg)){ Log.e(bean.tag, bean.msg); }
        logQueue.offer(bean);
        if(lock_flag.get()){
            synchronized (lock){
                lock_flag.set(false);
                lock.notifyAll();
            }
        }
    }

    @Override
    public void run() {
        while (true){
            OriLogBean poll = logQueue.poll();
            if(poll != null){
                print(poll);
            }else {
                synchronized (lock){
                    try {
                        lock_flag.set(true);
                        lock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        Log.e("LOG","WAIT EXCEPTION -> " + e.getMessage());
                    }
                }
            }
        }
    }

    private void print(OriLogBean logBean){
        print(logBean.toHtml());
    }

    private void print(String html){
        FileWriter fileWriter = null;
        try {
            File file = new File(getPath());
            boolean exists = createIfNotExists(file);
            fileWriter = new FileWriter(file, true);
            if(exists){
                fileWriter.write(OriLogBean.BASE_HTML);
            }else if(file.length() > MAX_SIZE){//当前文件大于5M了，下一次创建新的log文件
                index++;
            }
            fileWriter.write(html);
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
            if (fileWriter != null) {
                try {
                    fileWriter.flush();
                    fileWriter.close();
                } catch (IOException e1) { /* fail silently */ }
            }
        }
    }

    private String getPath(){
        String formatTime = format.format(new Date());
        if(!formatTime.equals(dateTime)){
            dateTime = formatTime;
            index = 0;
            deleteOverDateFile();
        }
        return path + File.separator + formatTime + File.separator + "log-" + VER + "-" + index + ".ori";
    }

    private void deleteOverDateFile() {
        File file = new File(path);
        if(file.exists() && file.isDirectory()){
            File[] files = file.listFiles();
            if(files == null || files.length <= 0){ return; }
            for (File listFile : files) {
                String file_name = listFile.getName().replaceAll("-", "");
                try {
                    long currentTime = Long.parseLong(dateTime.replaceAll("-", ""));
                    long fileTime = Long.parseLong(file_name);
                    if(currentTime - fileTime > 100){//超过一个月的文件夹删掉
                        try {
                            listFile.delete();
                        }catch (SecurityException e0){ /* do nothing*/ }
                    }
                }catch (NumberFormatException e){
                    try {
                        listFile.delete();
                    }catch (SecurityException e0){ /* do nothing*/ }
                }
            }
        }
    }

    private boolean createIfNotExists(File file) throws IOException {
        if (!file.exists()) {
            if(file.getParentFile() != null) {
                file.getParentFile().mkdirs();
            }
            file.createNewFile();
            return true;
        }
        return false;
    }

}
