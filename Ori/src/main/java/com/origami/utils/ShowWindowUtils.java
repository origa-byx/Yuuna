package com.origami.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.PixelFormat;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import androidx.appcompat.app.AlertDialog;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author xiao gan
 * @date 2020/11/24
 * @description: 弹框，两个静态方法弹dialog形式的弹框
 *          {@link #getFullScreenDialog(View, Context,int, int...)} or {@link #getFullScreenDialogAtBot(View, Context, int...)}
 *
 *     或者 实例化此类弹出一个新的window来自定义弹窗{@link #bindView(View)} or {@link #bindView(View, FrameLayout.LayoutParams)}
 *         最后{@link #show()}  , 对外暴露{@link #dismiss()}方法
 **/
public class ShowWindowUtils {
    private WindowManager mWindowManager;
    private Window mWindow;
    private FrameLayout mLayout;
    private View mView;

    private WindowManager.LayoutParams params;

    //管理所有的动画
//    private AnimatorSet mAnimatorSet;

    //弹出的动画 与 window的背景alpha动画
    private ValueAnimator mAnimator,mAnimatorWindow;

    //统一时间，两个动画同步执行
    private long duration = 500;

    AtomicBoolean isDismiss = new AtomicBoolean(true);

    private float mAlpha = 1.0f;

    public ShowWindowUtils(Activity activity){
        mWindowManager = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
        mWindow = activity.getWindow();
        //这里重写键盘事件分发,对返回键进行处理
        mLayout = new FrameLayout(activity){
            @Override
            public boolean dispatchKeyEvent(KeyEvent event) {
                if(event.getKeyCode() == KeyEvent.KEYCODE_BACK){
                    dismiss();
                }
                return super.dispatchKeyEvent(event);
            }
        };
    }

    /**
     * 通过在主窗口添加(addView)一个全屏布局达到弹出一个弹窗的效果
     *          这里需要自己手动设置window的弹框背景黑色
     * @param view
     */
    public ShowWindowUtils bindView(View view){
        return bindView(view,null);
    }

    public ShowWindowUtils bindView(View view, FrameLayout.LayoutParams relativeParams){
        if(mWindowManager==null){ throw new RuntimeException("this windowManager is null"); }
        if(mWindow == null){ throw new RuntimeException("this window is null"); }
        this.mView = view;

        params = new WindowManager.LayoutParams();
        params.format = PixelFormat.TRANSPARENT;
        params.flags = WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS |
                        WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR |
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        params.type = WindowManager.LayoutParams.LAST_APPLICATION_WINDOW;
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        if(relativeParams!=null){
            mLayout.addView(mView,relativeParams);
        }else {
            mLayout.addView(mView);
        }
        mLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        return this;
    }

    /**
     * show
     */
    public void show(){
        if(!isDismiss.getAndSet(false)){ return; }
        if(mAlpha != 1.0f){ changeWindowAlpha(true); }
        mWindowManager.addView(mLayout,params);
        if(mAnimator!=null){ mAnimator.start(); }
    }


    /**
     * dismiss ->内部有判断是否处于show的状态
     */
    public synchronized void dismiss(){
        if(!isDismiss.getAndSet(true)){
            if(mAnimator!=null && mAnimator.isRunning()){ mAnimator.cancel(); }
            if(mAnimator!=null){
                mAnimator.reverse();
                mAnimator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mLayout.removeAllViews();
                        mWindowManager.removeView(mLayout);
                        super.onAnimationEnd(animation);
                    }
                });
            }else{
                mLayout.removeAllViews();
                mWindowManager.removeView(mLayout);
            }
            mAnimator = null;
            if(mAlpha != 1.0f){ changeWindowAlpha(false); }
        }
    }

    private void changeWindowAlpha(boolean show){
        if(mAnimatorWindow != null){
            if(show){
                mAnimatorWindow.start();
            }else{
                mAnimatorWindow.reverse();
            }
        }else {
            if(show){
                changeWindowAlpha(mAlpha);
            }else{
                changeWindowAlpha(1.0f);
            }
        }
    }

    private void changeWindowAlpha(float a){
        WindowManager.LayoutParams attributes = mWindow.getAttributes();
        attributes.alpha = a;
        mWindow.setAttributes(attributes);
    }

    /**
     * 为自定义view设置动画
     * @param mAnimator
     * @return
     */
//    @SuppressLint("ObjectAnimatorBinding")
    public ShowWindowUtils setAnimator(ValueAnimator mAnimator) {
        if(mAnimator==null){
//            this.mAnimator = ObjectAnimator.ofInt(mView, "translationY", DpiUtils.dp2px(300),0);
            ValueAnimator animator = ObjectAnimator.ofInt(Dp2px.dp2px(300),0);
            animator.setDuration(duration);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int value = (int) animation.getAnimatedValue();
                    mView.setTranslationY(value);
                }
            });
            this.mAnimator = animator;
        }else {
            this.mAnimator = mAnimator;
        }
        return this;
    }

    //是否dismiss了
    public boolean isDismiss() {
        return isDismiss.get();
    }

    /**
     * @param alpha 设置背景有多黑
     * @param onAnimator
     * @return
     */
    public ShowWindowUtils setAlpha(float alpha, boolean onAnimator) {
        this.mAlpha = alpha;
        if(onAnimator){
            ValueAnimator animator = ObjectAnimator.ofFloat(1.0f,mAlpha);
            animator.setDuration(duration);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float value = (float) animation.getAnimatedValue();
                    changeWindowAlpha(value);
                }
            });
            mAnimatorWindow = animator;
        }
        return this;
    }


    /**
     * 下面是静态方法，基本无关
     * @param context
     * @return
     * **********************************************************************************************************************
     */

    public static DisplayMetrics getScreen(Context context){
        WindowManager systemService = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if(systemService==null){ throw new RuntimeException("this windowManager is null"); }
        DisplayMetrics metrics = new DisplayMetrics();
        systemService.getDefaultDisplay().getMetrics(metrics);
        return metrics;
    }

    public static DisplayMetrics getScreen(WindowManager systemService){
        DisplayMetrics metrics = new DisplayMetrics();
        systemService.getDefaultDisplay().getMetrics(metrics);
        return metrics;
    }

    /**
     *
     * @param view
     * @param context
     * @param padding --->弹框的外部padding,用来调整位置，可选参数，为了代码写着方便要么不设置要么全设置(4个) --> left,top,right,bottom
     * @return 一个AlertDialog，需要自己调用show方法展现
     */
    public static AlertDialog getFullScreenDialog(View view, Context context, int gravity, int ...padding){
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(context);
        AlertDialog dialog = mBuilder.setView(view).create();
        dialog.setCancelable(true);
//        dialog.setCancelable(true);
        Window window = dialog.getWindow();
        if(window!=null){
            if(padding.length!=0){//设置弹窗外部padding调整显示位置
                if(padding.length!=4){throw new RuntimeException("padding length must be 0 or 4");}
                window.getDecorView().setPadding(padding[0],padding[1],padding[2],padding[3]);
            }
            //DecorView层背景设置全透明
            window.getDecorView().getBackground().setAlpha(0);
//            window.getDecorView().setAlpha(0);
            //弹窗所在window设置沉浸式状态栏flag
            WindowManager.LayoutParams attributes = window.getAttributes();
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //修改布局参数->高度取屏幕高度
            attributes.width = ViewGroup.LayoutParams.MATCH_PARENT;
            window.setGravity(gravity);
//            attributes.height = getScreen(context).heightPixels;
            window.setAttributes(attributes);
        }
        return dialog;
    }

    public static AlertDialog getFullScreenDialogAtBot(View view, Context context, int ...padding){
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(context);
        AlertDialog dialog = mBuilder.setView(view).create();
        dialog.setCancelable(true);
        Window window = dialog.getWindow();
        if(window!=null){
            if(padding.length!=0){//设置弹窗外部padding调整显示位置
                if(padding.length!=4){throw new RuntimeException("padding length must be 0 or 4");}
                window.getDecorView().setPadding(padding[0],padding[1],padding[2],padding[3]);
            }
            //DecorView层背景设置全透明
            window.getDecorView().getBackground().setAlpha(0);
//            window.getDecorView().setAlpha(0);
            //弹窗所在window设置沉浸式状态栏flag
            WindowManager.LayoutParams attributes = window.getAttributes();
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //修改布局参数->高度取屏幕高度
            attributes.width = ViewGroup.LayoutParams.MATCH_PARENT;
            attributes.gravity = Gravity.BOTTOM;
            window.setAttributes(attributes);
        }
        return dialog;
    }


}
