package com.origami.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.StateListDrawable;
import android.media.MediaScannerConnection;
import android.os.Build;
import android.os.Environment;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.widget.EditText;

import androidx.annotation.AttrRes;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;

import com.origami.App;
import com.origami.log.OriLog;
import com.origami.log.OriLogBean;
import com.origami.origami.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

/**
 * @by: origami
 * @date: {2021-06-07}
 * @info:
 **/
public class Ori {

    /**
     * 获取版本信息
     */
    public static Version getVersion(Context context) {
        PackageManager packageManager = context.getPackageManager();
        // getPackageName()是你当前类的包名，0代表是获取版本信息
        PackageInfo packInfo;
        try {
            packInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            long versionCode;
            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.R){
                versionCode = packInfo.getLongVersionCode();
            }else {
                versionCode = packInfo.versionCode;
            }
            return new Version(packInfo.versionName, versionCode);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return new Version("0.0.0", Long.MAX_VALUE);
        }
    }

    /**
     * md5加密
     *
     * @param string
     * @return
     */
    public static String md5(String string) {
        if (TextUtils.isEmpty(string)) {
            return "";
        }
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
            byte[] bytes = md5.digest(string.getBytes());
            String result = "";
            for (byte b : bytes) {
                String temp = Integer.toHexString(b & 0xff);
                if (temp.length() == 1) {
                    temp = "0" + temp;
                }
                result += temp;
            }
            return result;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 震动
     * @param context
     * @param time  震动时长
     */
    public static void vibrator(Context context, long time){
        Vibrator vibrator = getVibrator(context);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            vibrator.vibrate(VibrationEffect.createOneShot(time, VibrationEffect.DEFAULT_AMPLITUDE));
        }else {
            vibrator.vibrate(time);
        }
    }

    /**
     * 获取震动控制类
     * @param context
     * @return  震动类
     */
    public static Vibrator getVibrator(Context context){
        return (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
    }

    /**
     * 获取渐变色 Drawable
     * @param colors 渐变颜色组
     * @return 渐变色
     */
    public static GradientDrawable getGradientDrawable(int[] colors){
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setColors(colors);
        drawable.setGradientType(GradientDrawable.LINEAR_GRADIENT);
        drawable.setOrientation(GradientDrawable.Orientation.TOP_BOTTOM);
        drawable.setSize(50,50);
        return drawable;
    }

    /**
     * selector Drawable
     * @param drawable_ok
     * @param status
     * @param drawable_notOk
     * @return
     */
    public static StateListDrawable getStateListDrawable(Drawable drawable_ok, @AttrRes int status, Drawable drawable_notOk){
        StateListDrawable drawable = new StateListDrawable();
        drawable.addState(new int[]{ status }, drawable_ok);
        drawable.addState(new int[]{ -status }, drawable_notOk);
        return drawable;
    }

    /**
     * 大概就是生成默认的selector drawable 给 一个按钮
     * @param context
     * @param color_out     边框
     * @param color_in      内部
     * @param okIsHasBor   true： 按下是切换为 有边框的
     * @return
     */
    public static StateListDrawable getDefSelectorBackgroundButtonDrawable(Context context, int color_out, int color_in, boolean okIsHasBor){
        Drawable drawable = context.getResources().getDrawable(R.drawable._ori_back_blue_per)
                .mutate();
        Drawable drawable_anti = context.getResources().getDrawable(R.drawable._ori_back_blue_nor)
                .mutate();
        if(drawable instanceof LayerDrawable){
            Drawable drawable_son1 = ((LayerDrawable) drawable).getDrawable(0);
            Drawable drawable_son2 = ((LayerDrawable) drawable).getDrawable(1);
            if(drawable_son1 instanceof GradientDrawable){
                ((GradientDrawable) drawable_son1).setColor(color_out);
            }
            if(drawable_son2 instanceof GradientDrawable){
                ((GradientDrawable) drawable_son2).setColor(color_in);
            }
        }
        if(drawable_anti instanceof GradientDrawable){
            ((GradientDrawable) drawable_anti).setColor(color_out);
        }
        if(okIsHasBor){
            return getStateListDrawable(drawable_anti, android.R.attr.state_pressed, drawable);
        }else {
            return getStateListDrawable(drawable, android.R.attr.state_pressed, drawable_anti);
        }
    }

    public static ColorStateList getSelectorColorStateList(int color_nor, int color_per){
        return getSelectorColorStateList(color_nor, color_per, false);
    }
    /**
     * 配合{@link #getDefSelectorBackgroundButtonDrawable(Context, int, int, boolean)} }
     *      用来生成颜色的
     * @param color_nor     正常
     * @param color_per     按下时
     * @param okIsHasBor    false 按照上面的情况  true  反过来
     * @return
     */
    public static ColorStateList getSelectorColorStateList(int color_nor, int color_per, boolean okIsHasBor){
        int[] colors = new int[2];
        if(okIsHasBor){
            colors[1] = color_nor; colors[0] = color_per;
        }else {
            colors[0] = color_nor; colors[1] = color_per;
        }
        return new ColorStateList(new int[][]{{ android.R.attr.state_pressed }, {}}, colors);
    }

    /**
     * 生成随机字符串 带时间
     * @param length    时间附加的随机字符长度
     * @return
     */
    public static String getRandomString(int length){
        String ran = "abcdefghijkmlnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder builder = new StringBuilder();
        Calendar date = Calendar.getInstance();
        builder.append(date.get(Calendar.YEAR))
                .append(date.get(Calendar.MONTH) < 9 ? "0" + (date.get(Calendar.MONTH) + 1) : (date.get(Calendar.MONTH) + 1))
                .append(date.get(Calendar.DATE) < 10 ? "0" + date.get(Calendar.DATE) : date.get(Calendar.DATE));
        for (int i = 0; i < length; i++) {
            builder.append(ran.charAt(random.nextInt(ran.length())));
        }
        return builder.toString();
    }


    /**
     * 产生随机文件名，对于可能会有大量文件的情况下，防止单一文件夹内文件过多导致的读取缓慢生成随机文件夹
     * @param obj  任意对象进行随机文件夹的生成  -> ran charAt -> obj.hashCode() % ran.length()
     * @return  eg.   b/{@link #getRandomString(int)}
     */
    public static String getRandomFileString(Object obj, @IntRange(from = 6) int length){
        String ran = "abcdefghijkmlnopqrstuvwxyz0123456789";
        StringBuilder builder = new StringBuilder();
        int index = obj.hashCode() % ran.length();
        builder.append(ran.charAt(index)).append(File.separator).append(getRandomString(length));
        return builder.toString();
    }

    /**
     * 保存位图
     * @param path 例如: "test/image/head"
     * @return null ：保存失败
     */
    public static String saveBitmap(Context context, Bitmap mBitmap, String path) {
        return saveBitmap(context, mBitmap, path, null);
    }

    public static String saveBitmap(Context context, Bitmap mBitmap, String path, String fileName) {
        String savePath;
        File filePic;
        savePath = getSaveFilePath(context, Environment.DIRECTORY_PICTURES) + path + File.separator;
        if(TextUtils.isEmpty(fileName)){
            savePath += (getRandomString(8) + ".jpg");
        }else {
            savePath += fileName;
        }
        try {
            filePic = new File(savePath);
            if (!filePic.exists()) {
                if(filePic.getParentFile() != null) {
                    filePic.getParentFile().mkdirs();
                }
                filePic.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(filePic, false);
            mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            Log.e("ORI", "saveBitmap : " + e.getMessage());
            return null;
        }
        return savePath;
    }

    /**
     * 保存位图  默认根目录下以app名命名的image文件夹下
     * @param isRandomFile  是否生成随机文件夹防止单一文件夹类子节点过多
     * @return null ：保存失败
     */
    public static String saveBitmapWithAppNamePath(Bitmap mBitmap, Context context, boolean isRandomFile) {
        return saveBitmapWithAppNamePath(mBitmap, context, isRandomFile, Bitmap.CompressFormat.JPEG);
    }

    public static String saveBitmapWithAppNamePath(Bitmap mBitmap, Context context , boolean isRandomFile, Bitmap.CompressFormat format) {
        File filePic;
        StringBuilder savePath = new StringBuilder(getSaveFilePath(context, Environment.DIRECTORY_PICTURES));
        String type;
        if(format == Bitmap.CompressFormat.JPEG){
            type = ".jpg";
        }else if(format == Bitmap.CompressFormat.PNG){
            type = ".png";
        }else{
            type = ".webp";
        }
        if(isRandomFile){
            savePath.append(getRandomFileString(mBitmap, 8))
                    .append(type);
        }else {
            savePath.append(getRandomString(8))
                    .append(type);
        }
        String save_path = savePath.toString();
        try {
            filePic = new File(save_path);
            if (!filePic.exists()) {
                if(filePic.getParentFile() != null) {
                    filePic.getParentFile().mkdirs();
                }
                filePic.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(filePic);
            mBitmap.compress(format, 100, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            Log.e("ORI", "saveBitmap : " + e.getMessage());
            return null;
        }
        return save_path;
    }

    /**
     * 获取统一保存路径
     * @param context
     * @return
     */
    public static String getSaveFilePath(Context context){
        return getSaveFilePath(context, Environment.DIRECTORY_DOCUMENTS);
    }

    /**
     * 获取统一保存路径
     *   sdk-version > 29 即 android 10 以上时 不允许在根目录 创建文件夹
     *      解决：targetApi 28 及以下 可以 {@link android.content.pm.ApplicationInfo#targetSdkVersion}
     *            或者换地方 {@link Context#getExternalFilesDir(String)}
     * @param context
     * @param type      文件类型  比如 {@link Environment#DIRECTORY_MUSIC}
     * @return
     */
    public static String getSaveFilePath(Context context, String type){
        String rootPath;
        if((Build.VERSION.SDK_INT < Build.VERSION_CODES.Q && Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
                || context.getApplicationInfo().targetSdkVersion < Build.VERSION_CODES.Q) {
            int labelRes = context.getApplicationInfo().labelRes;
            if (labelRes == 0) {
                rootPath = Environment.getExternalStorageDirectory().getPath() +
                        File.separator + "ori" +
                        File.separator + type + File.separator;
            } else {
                rootPath = Environment.getExternalStorageDirectory().getPath() +
                        File.separator + context.getResources().getString(labelRes) +
                        File.separator + type + File.separator;
            }
        }else {
            rootPath = context.getExternalFilesDir(type).getPath() + File.separator;
        }
        return rootPath;
    }


    /**
     * bitmap转化位base64
     * @param bitmap
     * @return
     */
    public static String bitmapToBase64(Bitmap bitmap, int... quality) {
        int qualityValue;
        if(quality != null && quality.length > 0){
            qualityValue = quality[0];
        }else {
            qualityValue = 100;
        }
        String result = null;
        ByteArrayOutputStream baos = null;
        try {
            if (bitmap != null) {
                baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, qualityValue, baos);

                baos.flush();
                baos.close();

                byte[] bitmapBytes = baos.toByteArray();
                result = Base64.encodeToString(bitmapBytes, Base64.DEFAULT);

            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (baos != null) {
                    baos.flush();
                    baos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public static Bitmap base64ToBitmap(String base64Data) {
        try {
            byte[] bytes = Base64.decode(base64Data, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } catch (Exception e) { /* do nothing */ }
        return null;
    }


    /**
     * 获取输入控件的值
     * @param editText
     * @return def or String
     */
    public static String getEditTextContent(EditText editText, String def){
        Editable editable = editText.getText();
        if(editable != null && !TextUtils.isEmpty(editable.toString())){
            return editable.toString();
        }
        return def;
    }

    /**
     * 获取输入控件的值
     * @param editText
     * @return null ： 无值 反之 String
     */
    public static String getEditTextContent(EditText editText){
        return getEditTextContent(editText, null);
    }

    /**
     * 获取输入控件的值
     * @param editText
     * @return def or int
     */
    public static int getEditIntContent(EditText editText, int def){
        Integer intContent = getEditIntContent(editText);
        return intContent == null? def : intContent;
    }

    /**
     * 获取输入控件的值
     * @param editText
     * @return null ： 无值 反之 int
     */
    public static Integer getEditIntContent(EditText editText){
        Editable editable = editText.getText();
        if(editable != null && !TextUtils.isEmpty(editable.toString())){
            try {
                return Integer.parseInt(editable.toString());
            }catch (NumberFormatException e){
                return null;
            }
        }
        return null;
    }

    /** 判断是否处于后台
     * @param context
     * @return true：处于后台, false：不处于后台
     */
    public static boolean isRunningOnBackground(Context context) {
        ActivityManager activityManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager
                .getRunningAppProcesses();
        if(appProcesses == null){ return false; }
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.processName.equals(context.getPackageName())) {
                return appProcess.importance != ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND;
            }
        }
        return false;
    }

    /**
     * 检测服务是否正在运行
     * @param mContext
     * @param className  -> {@link Class#getName()}
     * @return
     */
    public static boolean isServiceRunning(Context mContext, String className) {
        ActivityManager activityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> serviceList = activityManager.getRunningServices(30);
        if (serviceList.isEmpty()) { return false; }
        for (int i = 0; i < serviceList.size(); i++) {
            if (serviceList.get(i).service.getClassName().equals(className)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 转换成16进制字符串
     * @param bytes
     * @return
     */
    public static String ByteArrayToHexString(byte[] bytes) {
        final char[] hexArray = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
        char[] hexChars = new char[bytes.length * 2];
        int v;
        for ( int j = 0; j < bytes.length; j++ ) {
            v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    /**
     * 16进制字符串转换为字节数组
     * @param s
     * @return
     */
    public static byte[] HexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    //同步文件
    public void syncFile(Context context, List<String> paths) {
        if(paths == null || paths.isEmpty()){ return; }
        MediaScannerConnection.scanFile(context, paths.toArray(new String[0]), null, null);
    }

    //同步文件
    public void syncFile(Context context, String... path) {
        if(path == null || path.length == 0){ return; }
        MediaScannerConnection.scanFile(context, path, null, null);
    }

    public static void d(@NonNull String tag, @NonNull String msg, Bitmap... bitmaps){
        OriLog.getInstance().log_print(OriLogBean.d(tag, msg, bitmaps));
    }
    public static void v(@NonNull String tag, @NonNull String msg, Bitmap... bitmaps){
        OriLog.getInstance().log_print(OriLogBean.v(tag, msg, bitmaps));
    }
    public static void w(@NonNull String tag, @NonNull String msg, Bitmap... bitmaps){
        OriLog.getInstance().log_print(OriLogBean.w(tag, msg, bitmaps));
    }
    public static void e(@NonNull String tag, @NonNull String msg, Throwable throwable, Bitmap... bitmaps){
        OriLog.getInstance().log_print(OriLogBean.e(tag, msg, throwable, bitmaps));
    }
    public static void e(@NonNull String tag, Throwable throwable){
        OriLog.getInstance().log_print(OriLogBean.e(tag, throwable));
    }

    public static class Version{

        public String version;
        public long verCode;

        public Version(String version, long verCode) {
            this.version = version;
            this.verCode = verCode;
        }
    }

}
