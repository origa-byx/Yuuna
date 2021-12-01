package com.origami.origami.base.act;


import com.origami.origami.base.act.AnnotationActivity;

import java.lang.ref.WeakReference;

/**
 * @by: origami
 * @date: {2021-05-21}
 * @info:
 **/
public abstract class BasePresenter<T extends AnnotationActivity> {

    protected WeakReference<T> weak_act;

    public BasePresenter(T activity){
        this.weak_act = new WeakReference<>(activity);
    }

    /**
     * @return may be null
     */
    private T getAct(){
        return this.weak_act.get();
    }

}
