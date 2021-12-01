package com.origami.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

/**
 * @by: origami
 * @date: {2021-09-02}
 * @info:   侧边 abcdefg# 索引View
 **/
public class ABCView extends ViewGroup {

    private int h;
    private int oldIndex = -1;

    public ABCView(Context context) {
        this(context,null);
    }

    public ABCView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public ABCView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int childCount = getChildCount();
        if(childCount>0){
            h = (getHeight()-getPaddingBottom()-getPaddingTop())/childCount;
            for(int i=0;i<childCount;i++){
                getChildAt(i).layout(getPaddingLeft(),getPaddingTop()+h*i,+getWidth()-getPaddingRight(),getPaddingTop()+h*(i+1));
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        switch (action){
            case MotionEvent.ACTION_UP:
                if(mClickIndexListener!=null){
                    mClickIndexListener.clickIndex(((int) event.getY() - getPaddingTop()) / h);
                }
                break;
        }
        return true;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void switchABC(int index){
        TextView childAtI = (TextView) getChildAt(index);
        childAtI.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        childAtI.setTextColor(Color.parseColor("#0d47a1"));
        if(oldIndex != -1 && index != oldIndex){
            TextView childAtOld = (TextView) getChildAt(oldIndex);
            childAtOld.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
            childAtOld.setTextColor(Color.parseColor("#29b6f6"));
        }
        oldIndex = index;
    }

    public void addAbcView(CharSequence text){
        TextView v = new TextView(getContext());
        v.setText(text);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        v.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        v.setLayoutParams(layoutParams);

        v.setTextColor(Color.parseColor("#29b6f6"));
        addView(v);
    }

    private ClickIndexListener mClickIndexListener;

    public interface ClickIndexListener{
        void clickIndex(int index);
    }

    public void setClickIndexListener(ClickIndexListener mClickIndexListener) {
        this.mClickIndexListener = mClickIndexListener;
    }
}
