package com.origami.view.inter;

/**
 * @by: origami
 * @date: {2021-08-31}
 * @info:
 **/
public interface OriViewGroup {

    void setDispatchTouchHandler(TouchHandler dispatchTouchHandler);

    void setOnInterceptTouchHandler(TouchHandler onInterceptTouchHandler);

    void setOnTouchHandler(TouchHandler onTouchHandler);

}
