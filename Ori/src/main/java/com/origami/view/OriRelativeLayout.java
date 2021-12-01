package com.origami.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

import com.origami.view.inter.OriViewGroup;
import com.origami.view.inter.TouchHandler;

/**
 * @by: origami
 * @date: {2021-08-27}
 * @info:
 **/
public class OriRelativeLayout extends RelativeLayout implements OriViewGroup {

    TouchHandler dispatchTouchHandler,onInterceptTouchHandler,onTouchHandler;

    public OriRelativeLayout(Context context) {
        super(context);
    }

    public OriRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public OriRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if(dispatchTouchHandler != null){
            Boolean aBoolean = dispatchTouchHandler.handlerTouchEvent(ev);
            if(aBoolean != null){ return aBoolean; }
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if(onInterceptTouchHandler != null){
            Boolean aBoolean = onInterceptTouchHandler.handlerTouchEvent(ev);
            if(aBoolean != null){ return aBoolean; }
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(onTouchHandler != null){
            Boolean aBoolean = onTouchHandler.handlerTouchEvent(event);
            if(aBoolean != null){ return aBoolean; }
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void setDispatchTouchHandler(TouchHandler dispatchTouchHandler) {
        this.dispatchTouchHandler = dispatchTouchHandler;
    }

    @Override
    public void setOnInterceptTouchHandler(TouchHandler onInterceptTouchHandler) {
        this.onInterceptTouchHandler = onInterceptTouchHandler;
    }

    @Override
    public void setOnTouchHandler(TouchHandler onTouchHandler) {
        this.onTouchHandler = onTouchHandler;
    }

}
