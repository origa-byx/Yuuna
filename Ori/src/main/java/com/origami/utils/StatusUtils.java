package com.origami.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

/**
 * @author xiao gan
 * @date 2020/12/2
 * @description:
 **/
public class StatusUtils {

    /**
     * 设置全屏的沉浸式状态栏,自己处理状态栏
     * @param activity
     */
    public static void setImmerseStatus(Activity activity){
        Window window = activity.getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) { //5.0以上
//            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            activity.getWindow().setStatusBarColor(Color.TRANSPARENT);
            activity.getWindow()
                    .getDecorView()
                    .setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) { //4.4~5.0之间
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }

    /**
     * 全屏,设置一个颜色
     * @param activity
     * @param color
     */
    public static void setStatusBarColor(Activity activity,int color){
        Window window = activity.getWindow();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            setImmerseStatus(activity);
        }
        else if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.KITKAT){
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        View view = new View(activity);
        int barHeight = getStatusBarHeight(activity);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, barHeight));
        view.setBackgroundColor(color);
        ViewGroup decorView = (ViewGroup) window.getDecorView();
        decorView.addView(view);
        window.getDecorView()
                .findViewById(android.R.id.content)
                .setPadding(0, barHeight,0,0);
    }

    public static void setStatusBarResource(Activity activity,int resourceId){
        Window window = activity.getWindow();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            setImmerseStatus(activity);
        }
        else if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.KITKAT){
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        int barHeight = getStatusBarHeight(activity);
        View view = new View(activity);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, barHeight));
        view.setBackgroundResource(resourceId);
        ViewGroup decorView = (ViewGroup) window.getDecorView();
        decorView.addView(view);
        window.getDecorView()
                .findViewById(android.R.id.content)
                .setPadding(0, barHeight,0,0);
    }


    /**
     * 更改字体亮暗色
     * @param activity
     * @param dark
     */
    public static void setLightStatusBar(Activity activity, boolean dark) {
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            View decor = activity.getWindow().getDecorView();
            if (dark) {
                decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            } else {
                decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            }
        }
    }

    /**
     * 获取状态栏高度
     * @param context
     * @return
     */
    public static int getStatusBarHeight(Context context){
        //先获取资源ID，再根据ID获取资源
        Resources resources = context.getResources();
        int identifier = resources.getIdentifier("status_bar_height", "dimen", "android");
        return resources.getDimensionPixelOffset(identifier);
    }


    /**
     * 获取底部导航栏高度
     * @param context
     * @return
     */
    public static int getNavigationBarHeight(Context context) {
        if (context == null)
            return 0;
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        int height = resources.getDimensionPixelSize(resourceId);
        return height;
    }

}
