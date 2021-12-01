package com.origami.window;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.LayoutRes;

import com.origami.origami.R;
import com.origami.utils.Dp2px;
import com.origami.utils.StatusUtils;

import java.lang.ref.WeakReference;

/**
 * @by: origami
 * @date: {2021-05-24}
 * @info:
 * @deprecated 布局多镶嵌了一层，性能，资源较 {@link WindowUtil2} 而言开销较大
 **/
public class WindowUtil {

    private final WeakReference<Activity> mActivity;
    private final RelativeLayout rootLayout;
    private View bindView;

    private final int navH;
    private final WindowManager window;
    private boolean showFlag = false;
    private final WindowManager.LayoutParams paramsWindow = new WindowManager.LayoutParams();
    private RelativeLayout.LayoutParams params;

    private ValueAnimator showAnimator;

    private WindowUtil(Activity activity, int status){
        window = activity.getWindowManager();
        rootLayout = new RelativeLayout(activity){
            @Override
            public boolean dispatchKeyEvent(KeyEvent event) {
                if(event.getKeyCode() == KeyEvent.KEYCODE_BACK && WindowUtil.this.isShowing()){
                    WindowUtil.this.dismiss();
                    return true;
                }
                return super.dispatchKeyEvent(event);
            }
        };
        navH = StatusUtils.getNavigationBarHeight(activity);
        paramsWindow.width = WindowManager.LayoutParams.MATCH_PARENT;
        paramsWindow.height = WindowManager.LayoutParams.MATCH_PARENT;
        paramsWindow.format = PixelFormat.TRANSPARENT;
        paramsWindow.flags = status |
                WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR |
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        paramsWindow.type = WindowManager.LayoutParams.LAST_APPLICATION_WINDOW;
        paramsWindow.gravity = Gravity.CENTER;
        mActivity = new WeakReference<>(activity);
    }

    public static WindowUtil build(Activity activity){
        return new WindowUtil(activity, WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
    }

    public static WindowUtil build(Activity activity, int window_layoutParams_status){
        return new WindowUtil(activity, window_layoutParams_status);
    }

    /**
     * 绑定UI
     * @param res    布局资源
     * @param width  布局宽 例如 {@link android.widget.RelativeLayout.LayoutParams#MATCH_PARENT}
     * @param height 布局高
     * @param rule   布局定位 例如 {@link RelativeLayout#CENTER_IN_PARENT}
     * @return
     */
    public WindowUtil bindView(@LayoutRes int res, int width, int height, int... rule){
        Activity activity = mActivity.get();
        params = new RelativeLayout.LayoutParams(width, height);
        if(rule != null && rule.length > 0) {
            for (int i : rule) { params.addRule(i); }
        }
        if(activity == null){ return this; }
        this.bindView = LayoutInflater.from(activity).inflate(res, rootLayout, false);
        return this;
    }

    /**
     * 看 {@link #bindView(int, int, int, int...)}
     * @return
     */
    public WindowUtil bindView(View view, int width, int height, int... rule){
        params = new RelativeLayout.LayoutParams(width, height);
        if(rule != null && rule.length > 0) {
            for (int i : rule) { params.addRule(i); }
        }
        this.bindView = view;
        return this;
    }

    /**
     * 默认配置绑定布局
     * @param res
     * @return
     */
    public WindowUtil bindDefCenterView(int res){
        Activity activity = mActivity.get();
        if(activity == null){ return this; }
        params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        this.bindView = LayoutInflater.from(activity).inflate(res, rootLayout, false);
        return this;
    }

    /**
     * 默认配置绑定view
     * @param view
     * @return
     */
    public WindowUtil bindDefCenterView(View view){
        params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        this.bindView = view;
        return this;
    }

    /**
     * 为布局内元素绑定事件
     * @param listener
     * @param resId
     * @return
     */
    public WindowUtil setOnclickListener(View.OnClickListener listener, int... resId){
        if(bindView == null){ throw new RuntimeException("you must be bind the view at first"); }
        if(resId == null || resId.length == 0){ return this; }
        for (int id : resId) {
            bindView.findViewById(id).setOnClickListener(listener);
        }
        return this;
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
    public WindowUtil setCanCancel(){
        bindView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //空处理，做捕获事件才能被分发到
            }
        });
        rootLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(showFlag){
                    dismiss();
                }
            }
        });
        return this;
    }

    public void showWithAnimator(){
        if(showFlag){ return; }
        Activity activity = mActivity.get();
        if(activity == null){ return; }
        activity.getWindow().getDecorView().setAlpha(0.6f);
        int translationY = Dp2px.dp2px(100);
        bindView.setTranslationY(translationY);
        bindView.setAlpha(0);
        params.bottomMargin = navH;
        rootLayout.addView(bindView,params);
        window.addView(rootLayout,paramsWindow);
        showFlag = true;
        if(showAnimator == null){
            showAnimator = ObjectAnimator.ofFloat(0,1f);
            showAnimator.setDuration(400);
            showAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float value = (float) animation.getAnimatedValue();
                    bindView.setAlpha(value);
                    bindView.setTranslationY(translationY * (1 - value));
                }
            });
        }
        showAnimator.start();
    }

    public void show(){
        if(showFlag){ return; }
        Activity activity = mActivity.get();
        if(activity == null){ return; }
        activity.getWindow().getDecorView().setAlpha(0.6f);
        params.bottomMargin = navH;
        rootLayout.addView(bindView,params);
        window.addView(rootLayout,paramsWindow);
        showFlag = true;
    }

    public void dismiss(){
        if(!showFlag){ return; }
        if(showAnimator != null && showAnimator.isRunning()){ showAnimator.cancel(); }
        Activity activity = mActivity.get();
        if(activity == null){ return; }
        activity.getWindow().getDecorView().setAlpha(1f);
        rootLayout.removeAllViews();
        window.removeView(rootLayout);
        showFlag = false;
    }

    public View getRootView(){
        return rootLayout;
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
     * @param activity
     * @param texts   可选列表
     * @param selectListener  可选列表的点击事件
     * @param showCancelView  是否含有取消选项
     */
    public static void showSelect(Activity activity, String[] texts, OnSelectListener selectListener, boolean showCancelView){
        if(activity == null || selectListener == null){ return; }
        View selectView = LayoutInflater.from(activity)
                .inflate(
                        R.layout._base_show_select,
                        activity.getWindow().getDecorView().findViewById(android.R.id.content),
                        false);
        LinearLayout showLayout = selectView.findViewById(R.id.select_list);
        WindowUtil windowUtil = WindowUtil.build(activity).bindView(selectView,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                RelativeLayout.ALIGN_PARENT_BOTTOM,
                RelativeLayout.CENTER_HORIZONTAL).setCanCancel();
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(v instanceof  TextView){
                    int index =(int) v.getTag();
                    windowUtil.dismiss();
                    selectListener.onSelect(texts[index], index);
                }
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
            cancel_view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    windowUtil.dismiss();
                }
            });
        }else {
            cancel_view.setVisibility(View.GONE);
        }
        windowUtil.showWithAnimator();
    }
}
