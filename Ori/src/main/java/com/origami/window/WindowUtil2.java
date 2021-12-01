package com.origami.window;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.FloatRange;
import androidx.annotation.LayoutRes;

import com.origami.origami.R;
import com.origami.utils.Dp2px;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * @by: origami
 * @date: {2021-08-30}
 * @info:
 **/
public class WindowUtil2 {

    int p_x = 0, p_y = 0;
    float p_dark = 0;
    int valS = 0;

    private final int showAnimatorTY = Dp2px.dp2px(100);

    private final WeakReference<Activity> mActivity;
    private View bindView;

    private final WindowManager window;
    private boolean showFlag = false;
    private final WindowManager.LayoutParams paramsWindow = new WindowManager.LayoutParams();

    private ValueAnimator showAnimator;

    private Runnable dismissListener;

    private WindowUtil2(Activity activity, int window_layoutParams_flags, boolean system_window){
        window = activity.getWindowManager();
        paramsWindow.width = WindowManager.LayoutParams.MATCH_PARENT;
        paramsWindow.height = WindowManager.LayoutParams.WRAP_CONTENT;
        paramsWindow.format = PixelFormat.TRANSPARENT;
        paramsWindow.flags = window_layoutParams_flags |
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH |
                WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        if(system_window){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                //26及以上必须使用TYPE_APPLICATION_OVERLAY   @deprecated TYPE_PHONE
                paramsWindow.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            } else {
                paramsWindow.type = WindowManager.LayoutParams.TYPE_PHONE;
            }
        }else {
            paramsWindow.type = WindowManager.LayoutParams.TYPE_APPLICATION;
        }
        paramsWindow.gravity = Gravity.CENTER;
        paramsWindow.dimAmount = p_dark;
        mActivity = new WeakReference<>(activity);
    }

    public static WindowUtil2 build(Activity activity){
        return new WindowUtil2(activity, 0, false);
    }

    public static WindowUtil2 build(Activity activity, int window_layoutParams_flags){
        return new WindowUtil2(activity, window_layoutParams_flags, false);
    }

    /**
     * 独立于Activity的全局悬浮框   需指引用户手动开启权限
     * @param activity
     * @return
     */
    public static WindowUtil2 build_systemWindow(Activity activity){
        return new WindowUtil2(activity, 0, true);
    }
    public static WindowUtil2 build_systemWindow(Activity activity, int window_layoutParams_flags){
        return new WindowUtil2(activity, window_layoutParams_flags, true);
    }

    /**
     * 绑定UI
     * @param res    布局资源
     * @param width  布局宽 例如 {@link WindowManager.LayoutParams#MATCH_PARENT}
     * @param height 布局高
     * @return
     */
    public WindowUtil2 bindView(@LayoutRes int res, int width, int height){
        Activity activity = mActivity.get();
        paramsWindow.width = width;
        paramsWindow.height = height;
        if(activity == null){ return this; }
        this.bindView = LayoutInflater.from(activity).inflate(res, null, false);
        return this;
    }

    public WindowUtil2 setMarginH(int h){
        if(paramsWindow.width == WindowManager.LayoutParams.MATCH_PARENT){
            Point point = new Point();
            window.getDefaultDisplay().getSize(point);
            paramsWindow.width = point.x - h * 2;
        }
        return this;
    }

    public WindowUtil2 setGravity(int gravity){
        paramsWindow.gravity = gravity;
        return this;
    }

    public void setIsBottomMir(boolean f) {
        this.valS = f ? -1 : 1;
    }

    /**
     * 看 {@link #bindView(int, int, int)}
     * @return
     */
    public WindowUtil2 bindView(View view, int width, int height){
        paramsWindow.width = width;
        paramsWindow.height = height;
        this.bindView = view;
        return this;
    }

    /**
     * 默认配置绑定布局
     * @param res
     * @return
     */
    public WindowUtil2 bindView(@LayoutRes int res){
        Activity activity = mActivity.get();
        if(activity == null){ return this; }
        this.bindView = LayoutInflater.from(activity).inflate(res, null, false);
        return this;
    }

    /**
     * 默认配置绑定view
     * @param view
     * @return
     */
    public WindowUtil2 bindView(View view){
        this.bindView = view;
        return this;
    }

    /**
     * 控制背景变暗（透明）程度
     * @param val 1.0不透明, 0.0完全透明
     * @return
     */
    public WindowUtil2 setBackDark(@FloatRange(from = 0, to = 1f) float val){
        p_dark = paramsWindow.dimAmount = val;
        return this;
    }

    public WindowUtil2 setFullScreen(){
        paramsWindow.flags = paramsWindow.flags |
                WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR |
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
                WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS;
        return this;
    }


    public WindowUtil2 setLocation(int gravity, int x, int y, boolean gravityIsYBottom, boolean gravityIsXEnd){
        paramsWindow.gravity = gravity;
        p_x = paramsWindow.x = gravityIsXEnd? -x : x;
        p_y = paramsWindow.y = gravityIsYBottom? -y : y;
        return this;
    }

    /**
     * 为布局内元素绑定事件
     * @param listener
     * @param resId
     * @return
     */
    public WindowUtil2 setOnclickListener(View.OnClickListener listener, int... resId){
        if(bindView == null){ throw new RuntimeException("you must be bind the view at first"); }
        if(resId == null || resId.length == 0){ return this; }
        for (int id : resId) {
            bindView.findViewById(id).setOnClickListener(listener);
        }
        return this;
    }

    public WindowManager.LayoutParams getParamsWindow() {
        return paramsWindow;
    }

    public void updateWindow(){
        window.updateViewLayout(bindView, paramsWindow);
    }

    /**
     * 获取View
     * @param resId
     * @return
     */
    public View getViewById(int resId){
        if(bindView == null){ throw new RuntimeException("you must be bind the view at first"); }
        return bindView.findViewById(resId);
    }

    /**
     * 设置可以点击外部取消
     * @return
     */
    @SuppressLint("ClickableViewAccessibility")
    public WindowUtil2 setCanCancel(){
        bindView.setOnTouchListener((v, event) -> {
            if(event.getAction() == MotionEvent.ACTION_OUTSIDE){
                dismiss();
            }
            return false;
        });
        return this;
    }

    private void keyBack(){
        bindView.setOnKeyListener((v, keyCode, event) -> {
            if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
                dismiss();
                return true;
            }
            return false;
        });
        bindView.setFocusable(true);
        bindView.setFocusableInTouchMode(true);
        bindView.requestFocus();
    }

    public void showWithAnimator(){
        if(showFlag){ return; }
        bindView.setAlpha(0);
        window.addView(bindView, paramsWindow);
        keyBack();
        showFlag = true;
        if(valS == 0){
            int valS = paramsWindow.gravity == Gravity.BOTTOM? -1 : 1;
        }
        if(showAnimator == null){
            showAnimator = ValueAnimator.ofFloat(0,1f);
            showAnimator.setDuration(400);
            showAnimator.setInterpolator(new DecelerateInterpolator());
            showAnimator.addUpdateListener(animation -> {
                float value = (float) animation.getAnimatedValue();
                bindView.setAlpha(value);
                paramsWindow.dimAmount = p_dark * value;
                paramsWindow.y = p_y + ((int) (showAnimatorTY * (1f - value)) * valS);
                updateWindow();
            });
        }
        showAnimator.start();
    }

    public void show(){
        if(showFlag){ return; }
        window.addView(bindView, paramsWindow);
        keyBack();
        showFlag = true;
    }

    public WindowUtil2 setDismissListener(Runnable dismissListener) {
        this.dismissListener = dismissListener;
        return this;
    }

    public void dismiss(){
        if(!showFlag){ return; }
        if(showAnimator != null && showAnimator.isRunning()){ showAnimator.cancel(); }
        if(dismissListener != null){ dismissListener.run(); }
        bindView.clearFocus();
        window.removeView(bindView);
        showFlag = false;
    }

    public void dismissWithNotDoListener(){
        if(!showFlag){ return; }
        if(showAnimator != null && showAnimator.isRunning()){ showAnimator.cancel(); }
        bindView.clearFocus();
        window.removeView(bindView);
        showFlag = false;
    }

    public View getBindView(){
        return bindView;
    }

    public boolean isShowing() {
        return showFlag;
    }

    //spd  static
    public interface OnSelectListener{
        /**
         * 选择事件
         * @param txt    选择的文本
         * @param index  选择的文本的数组下标
         */
        void onSelect(String txt, int index);
    }

    /**
     * 显示选项弹出框
     * @param activity  activity
     * @param texts   可选列表
     * @param selectListener  可选列表的点击事件
     * @param showCancelView  是否含有取消选项
     */
    public static void showSelect(Activity activity,
                                  String[] texts,
                                  final OnSelectListener selectListener,
                                  boolean showCancelView){
        if(activity == null || selectListener == null){ return; }
        View selectView = LayoutInflater.from(activity)
                .inflate(
                        R.layout._base_show_select,
                        activity.getWindow().getDecorView().findViewById(android.R.id.content),
                        false);
        LinearLayout showLayout = selectView.findViewById(R.id.select_list);
        WindowUtil2 windowUtil = WindowUtil2
                .build(activity)
                .bindView(selectView)
                .setLocation(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0, true, false)
                .setBackDark(0.6f)
                .setCanCancel();
        View.OnClickListener listener = v -> {
            if(v instanceof  TextView){
                int index =(int) v.getTag();
                windowUtil.dismiss();
                selectListener.onSelect(texts[index], index);
            }
        };
        for (int i = 0; i < texts.length; i++) {
            TextView textView = new TextView(activity);
            textView.setTag(i);
            textView.setTextSize(16);
            textView.setBackground(activity.getDrawable(R.drawable._select_white_gray));
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Dp2px.dp2px(45));
            layoutParams.gravity = Gravity.CENTER;
            textView.setTextColor(Color.BLACK);
            textView.setGravity(Gravity.CENTER);
            textView.setText(texts[i]);
            textView.setOnClickListener(listener);
            showLayout.addView(textView, layoutParams);
        }
        TextView cancel_view = selectView.findViewById(R.id.select_cancel);
        if(showCancelView){
            cancel_view.setVisibility(View.VISIBLE);
            cancel_view.setOnClickListener(v -> windowUtil.dismiss());
        }else {
            cancel_view.setVisibility(View.GONE);
        }
        windowUtil.showWithAnimator();
    }


    /**
     * 显示选项确认框
     * @param activity  activity
     * @param msg       内容
     * @param text      eg->  { "确认","取消" } or { "确认" }
     * @param selectListener    点击事件
     */
    public static void showMakeSure(Activity activity,
                                    CharSequence msg, String[] text,
                                    final OnSelectListener selectListener){
        showMakeSure(activity, msg, text, selectListener, 0.3f);
    }

    /**
     * 显示选项确认框
     * @param activity  activity
     * @param msg       内容
     * @param text      eg->  { "确认","取消" } or { "确认" }
     * @param selectListener    点击事件
     * @param backDart      弹出后背景变暗度 0为不变暗
     */
    public static void showMakeSure(Activity activity,
                                    CharSequence msg, String[] text,
                                    final OnSelectListener selectListener,
                                    @FloatRange(from = 0, to = 1) float backDart){
        if(activity == null || text == null || text.length == 0 || selectListener == null){ return; }
        View makeSureView = LayoutInflater.from(activity)
                .inflate(
                        R.layout._base_show_makesure,
                        activity.getWindow().getDecorView().findViewById(android.R.id.content),
                        false);
        TextView okView = makeSureView.findViewById(R.id._base_makesure_ok);
        okView.setTag(0);
        okView.setText(text[0]);
        TextView cancelView = makeSureView.findViewById(R.id._base_makesure_cancel);
        if(text.length >= 2){
            cancelView.setVisibility(View.VISIBLE);
            cancelView.setText(text[1]);
            cancelView.setTag(1);
        }else {
            cancelView.setVisibility(View.INVISIBLE);
        }
        TextView textView = makeSureView.findViewById(R.id._base_makesure_text);
        textView.setText(msg);
        WindowUtil2 windowUtil2 = WindowUtil2.build(activity)
                .bindView(makeSureView)
                .setLocation(Gravity.CENTER, 0, 0, false, false)
                .setBackDark(backDart)
                .setCanCancel();
        View.OnClickListener listener = v -> {
            if(v instanceof TextView){
                int index =(int) v.getTag();
                windowUtil2.dismiss();
                selectListener.onSelect(text[index], index);
            }
        };
        okView.setOnClickListener(listener);
        cancelView.setOnClickListener(listener);
        windowUtil2.showWithAnimator();
    }

    /***
     * 检查悬浮窗开启权限
     * @param context
     * @return
     */
    public static boolean checkFloatWindowPermission(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT)
            return true;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            try {
                Class cls = Class.forName("android.content.Context");
                Field declaredField = cls.getDeclaredField("APP_OPS_SERVICE");
                declaredField.setAccessible(true);
                Object obj = declaredField.get(cls);
                if (!(obj instanceof String)) {
                    return false;
                }
                String str2 = (String) obj;
                obj = cls.getMethod("getSystemService", String.class).invoke(context, str2);
                cls = Class.forName("android.app.AppOpsManager");
                Field declaredField2 = cls.getDeclaredField("MODE_ALLOWED");
                declaredField2.setAccessible(true);
                Method checkOp = cls.getMethod("checkOp", Integer.TYPE, Integer.TYPE, String.class);
                int result = (Integer) checkOp.invoke(obj, 24, Binder.getCallingUid(), context.getPackageName());
                return result == declaredField2.getInt(cls);
            } catch (Exception e) {
                return false;
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                AppOpsManager appOpsMgr = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
                if (appOpsMgr == null)
                    return false;
                int mode;
//                if(Build.VERSION.SDK_INT > Build.VERSION_CODES.R) {
//                    mode= appOpsMgr.unsafeCheckOpNoThrow("android:system_alert_window", android.os.Process.myUid(), context
//                            .getPackageName());
//                }else {
                mode= appOpsMgr.checkOpNoThrow("android:system_alert_window", android.os.Process.myUid(), context
                        .getPackageName());
//                }
                return Settings.canDrawOverlays(context) || mode == AppOpsManager.MODE_ALLOWED || mode == AppOpsManager.MODE_IGNORED;
            } else {
                return Settings.canDrawOverlays(context);
            }
        }
    }

    /**
     * 悬浮窗开启权限
     * @param context
     * @param requestCode
     */
    public static void requestFloatPermission(Activity context, int requestCode){
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
        intent.setData(Uri.parse("package:" + context.getPackageName()));
        context.startActivityForResult(intent, requestCode);
    }

}
