package com.origami.log;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Color;

import androidx.annotation.NonNull;

import com.origami.utils.Ori;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @by: origami
 * @date: {2021-07-22}
 * @info:
 **/
public class OriLogBean {

    private static final String[] safeArr = new String[]{"&","<",">"};
    private static final String[] replaceArr = new String[]{"&amp;","&lt;","&gt;"};

    public static final String BASE_HTML = "<style>" +
            "body{padding:20px}" +
            "div{color:#000000;text-shadow: 1px 1px 5px #ffffff;border-radius:5px;word-break:break-all}" +
            "img {width: 30%;height: auto;float:left;z-index:-1;margin-right:5px}" +
            ".init{background-color:#fdfdfd;width:98%;float:left;padding:30px 0px;margin-top:30px;" +
                "margin-bottom:3px;box-sizing:border-box;text-align:center;border:5px dashed #2d85f0;border-radius:5px;}" +
            ".d{background-color:#eeeeee}" +
            ".v{background-color:#0aa858}" +
            ".e{background-color:#f4433c;}" +
            ".w{background-color:#ffbc32;}" +
            ".t{ width:20%;margin-right:5px;text-align:center;float:left;padding:5px 0px;margin-bottom:3px; }" +
            ".n{ width:10%;margin-right:5px;text-align:center;float:left;padding:5px 0px;margin-bottom:3px; }" +
            ".m{ width:67%;float:left;padding:5px 10px;margin-bottom:3px;box-sizing: border-box; }" +
            "</style>\n";

    @SuppressLint("SimpleDateFormat")
    public static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    //d v e w
    private final String level;
    public String tag;
    public String txt;
    public String msg;
    public Throwable throwable;

    private OriLogBean(String msg) {
        this.level = null;
        this.txt = toSafeString(msg);
        this.msg = msg;
        this.tag = "ORI";
        this.throwable = null;
    }

    private OriLogBean(String tag, String level, String msg, Throwable throwable, Bitmap... bitmap) {
        this.level = level;
        this.txt = toSafeString(msg) + getBitmapLogString(bitmap);
        this.msg = msg;
        this.tag = tag == null? "NULL": tag;
        this.throwable = throwable;
    }

    private static String toSafeString(String msg){
        for (int i = 0; i < safeArr.length; i++) {
            msg = msg.replaceAll(safeArr[i], replaceArr[i]);
        }
        return msg;
    }

    protected static OriLogBean i(String msg){
        return new OriLogBean(msg);
    }
    public static OriLogBean d(@NonNull String tag, @NonNull String msg, Bitmap... bitmaps){
        return new OriLogBean(tag, "d", msg, null, bitmaps);
    }
    public static OriLogBean v(@NonNull String tag, @NonNull String msg, Bitmap... bitmaps){
        return new OriLogBean(tag, "v", msg, null, bitmaps);
    }
    public static OriLogBean w(@NonNull String tag, @NonNull String msg, Bitmap... bitmaps){
        return new OriLogBean(tag, "w", msg, null, bitmaps);
    }
    public static OriLogBean e(@NonNull String tag, @NonNull String msg, Throwable throwable, Bitmap... bitmaps){
        return new OriLogBean(tag, "e", msg + "::" + getThrowableMsg(throwable), throwable, bitmaps);
    }
    public static OriLogBean e(@NonNull String tag, Throwable throwable){
        return new OriLogBean(tag, "e", getThrowableMsg(throwable), throwable);
    }

    private static String getThrowableMsg(Throwable throwable){
        return throwable == null? "" : throwable.getMessage();
    }

    private static String getBitmapLogString(Bitmap... bitmaps){
        if(bitmaps != null && bitmaps.length > 0){
            StringBuilder builder = new StringBuilder("<img src=\"data:image/jpeg;base64,");
            for (Bitmap bitmap : bitmaps) {
                builder.append(Ori.bitmapToBase64(bitmap, 10));
            }
            builder.append("\"/>");
            return builder.toString();
        }
        return "";
    }

    public String toHtml(){
        if(level == null){ return String.format("<div class=\"init\">%s</div>", toSafeString(txt)); }
        StringBuilder placeString = new StringBuilder(txt);
        if(throwable != null){
            StackTraceElement[] stackTrace = throwable.getStackTrace();
            if(stackTrace != null && stackTrace.length > 0){
                for (StackTraceElement traceElement : stackTrace) {
                    if(traceElement == null){ continue; }
                    placeString.append(String.format("</br><font color=\"#3b50ce\">(%s&lt;%s&gt;:%s)</font>"
                            ,traceElement.getClassName()
                            ,traceElement.getMethodName()
                            ,traceElement.getLineNumber()
                    ));
                }
            }
        }
        return String.format(
                "<div class=\"t %s\">%s</div>" +
                "<div class=\"n %s\">%s</div>" +
                "<div class=\"m %s\">%s</div>",
                level, dateFormat.format(new Date()),
                level, tag,
                level, placeString );
    }

}
