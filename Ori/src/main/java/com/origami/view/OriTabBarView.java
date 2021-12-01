package com.origami.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.origami.origami.R;

import java.util.ArrayList;
import java.util.List;

/**
 * @by: origami
 * @date: {2021-05-19}
 * @info:
 **/
public class OriTabBarView extends OriRelativeLayout {
    private final Context mContext;
    private View rootView;
    private HorizontalScrollView mScrollView;

    private List<View> mListView = new ArrayList<>();

    private View oldView;

    private ViewGroup ori_move_view;
    private LinearLayout ori_tab_view;

    private ValueAnimator moveAnimator;

    private int ori_tab_text_widthTag = 0;
    private int ori_tab_text_size = 0,
            ori_text_color_normal = Color.BLACK,
            ori_text_color_select = Color.rgb(0x33,0xa7,0xfe);


    private OnClickListener tab_view_onClickListener;
    private OriTabListener mOriTabListener;

    public OriTabBarView(Context context) {
        this(context, null);
    }

    public OriTabBarView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public OriTabBarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        init_layout();
    }

    private void init_layout() {
        rootView = LayoutInflater.from(mContext).inflate(R.layout.ori__tab_bar_view, this, true);
        ori_move_view = rootView.findViewById(R.id._ori__move_view);
        ori_tab_view = rootView.findViewById(R.id._ori__tab_view);
        mScrollView = rootView.findViewById(R.id._ori__scroll_view);
        tab_view_onClickListener = new OnClickListener(){
            @Override
            public void onClick(View v) {
                if(v == oldView){ return; }
                moveTo(((TextView) v));
                if(mOriTabListener != null){ mOriTabListener.onClick(v, mListView.indexOf(v)); }
            }
        };
    }

    /**
     * 初始化
     * @param tab_textSize  字体大小 sp
     * @param width         节点宽度
     * @return
     */
    public OriTabBarView initWithValue(int tab_textSize, int width){
        this.ori_tab_text_size = tab_textSize;
        this.ori_tab_text_widthTag = dp2px(width);
        ViewGroup.LayoutParams layoutParams = ori_move_view.getLayoutParams();
        layoutParams.width = ori_tab_text_widthTag;
        ori_move_view.setLayoutParams(layoutParams);
        return this;
    }

    /**
     * 初始化
     * @param tab_textSize  字体大小 sp
     * @param tab_count     节点数量
     *                使用此方法请保证该view已经布局成功或测量过一次了 ->
     *                  使用{@link OriTabBarView#post(Runnable)} 来进行整个操作
     * @return
     */
    public OriTabBarView initWithCount(int tab_textSize, int tab_count){
        this.ori_tab_text_size = tab_textSize;
        this.ori_tab_text_widthTag = -tab_count;
        ViewGroup.LayoutParams layoutParams = ori_move_view.getLayoutParams();
        layoutParams.width = getW() / (-ori_tab_text_widthTag);
        ori_move_view.setLayoutParams(layoutParams);
        return this;
    }

    public OriTabBarView setShowColor(int color_normal, int color_select){
        this.ori_text_color_normal = color_normal;
        this.ori_text_color_select = color_select;
        return this;
    }

    /**
     * 添加一个移动的背景view    不能和 {@link OriTabBarView#addMoveByDrawable(Drawable,int)} 一起用
     * @param moveView
     * @return
     */
    public OriTabBarView addMoveByView(View moveView){
        ori_move_view.addView(moveView);
        return this;
    }

    /**
     * 设置移动的 Drawable   不能和 {@link OriTabBarView#addMoveByView(View)} 一起用
     * @param drawable
     * @return
     */
    public OriTabBarView addMoveByDrawable(Drawable drawable, int padding){
        padding = dp2px(padding);
        ori_move_view.setBackground(drawable);
        MarginLayoutParams layoutParams = ((MarginLayoutParams) ori_move_view.getLayoutParams());
        layoutParams.width -= (padding * 2);
        layoutParams.leftMargin = padding;
        ori_move_view.setLayoutParams(layoutParams);
        return this;
    }

    /**
     * 添加一个tab节点
     * @param txt   显示内容
     * @return
     */
    public OriTabBarView addTextTab(CharSequence txt){
        TextView textView = new TextView(mContext);
        textView.setText(txt);
        textView.setTextSize(ori_tab_text_size);
        textView.setTextColor(ori_text_color_normal);
        textView.setGravity(Gravity.CENTER);
        int width;
        if(ori_tab_text_widthTag > 0){
            width = ori_tab_text_widthTag;
        }else {
            width = getW() / (-ori_tab_text_widthTag);
        }
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, getH());
        params.gravity = Gravity.CENTER;
        textView.setLayoutParams(params);
        textView.setOnClickListener(tab_view_onClickListener);
        ori_tab_view.addView(textView);
        mListView.add(textView);
        return this;
    }

    public void setFocusIndex(int position){
        oldView = mListView.get(position);
        ori_move_view.setTranslationX(oldView.getLeft());
        if(oldView instanceof TextView) {
            ((TextView) oldView).setTextColor(ori_text_color_select);
        }
    }

    /**
     * 设置点击
     * @param mOriTabListener
     * @return
     */
    public OriTabBarView setOriTabListener(OriTabListener mOriTabListener) {
        this.mOriTabListener = mOriTabListener;
        return this;
    }

    /**
     * 移动到第几个
     * @param position
     */
    public void moveToPosition(int position){
        int index = Math.min(mListView.size() - 1, Math.max(0, position));
        moveTo(mListView.get(index));
    }

    /**
     * 移动到特定view
     * @param positionView
     */
    public void moveToView(View positionView){
        if(mListView.contains(positionView)){
            moveTo(positionView);
        }
    }

    private void moveTo(View newView){
        if(newView == oldView){ return; }
        int left = newView.getLeft();
        if(moveAnimator != null && moveAnimator.isRunning()){
            moveAnimator.cancel();
        }
        float move_viewLeft = ori_move_view.getTranslationX();
        if(moveAnimator == null){
            moveAnimator = new ValueAnimator();
            moveAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float value = (float) animation.getAnimatedValue();
                    ori_move_view.setTranslationX(value);
                }
            });
        }
        moveAnimator.setFloatValues(move_viewLeft, left);
        moveAnimator.setDuration(500);
        moveAnimator.start();
        if(oldView instanceof TextView) {
            ((TextView) oldView).setTextColor(ori_text_color_normal);
        }
        if(newView instanceof TextView) {
            ((TextView) newView).setTextColor(ori_text_color_select);
        }
        oldView = newView;
    }

    public interface OriTabListener{
        void onClick(View v, int position);
    }

    private int getW(){
        return getWidth() == 0 ? getMeasuredWidth() : getWidth();
    }

    private int getH(){
        return getHeight() == 0 ? getMeasuredHeight() : getHeight();
    }

    private int dp2px(float dp){
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, Resources.getSystem().getDisplayMetrics());
    }
}
