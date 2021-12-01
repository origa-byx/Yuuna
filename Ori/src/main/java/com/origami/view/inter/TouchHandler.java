package com.origami.view.inter;

import android.view.MotionEvent;

/**
 * @by: origami
 * @date: {2021-08-27}
 * @info:   处理事件分发，拦截
 **/
public interface TouchHandler {

    /**
     * @param ev
     * @return
     *      null: 不做任何返回执行 super
     *      true or false : 返回true or false
     */
    Boolean handlerTouchEvent(MotionEvent ev);

}
