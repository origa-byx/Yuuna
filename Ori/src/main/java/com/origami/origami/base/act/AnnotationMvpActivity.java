package com.origami.origami.base.act;

import android.os.Bundle;

/**
 * @by: origami
 * @date: {2021-05-28}
 * @info:
 **/
public abstract class AnnotationMvpActivity<T extends BasePresenter<? extends AnnotationActivity>> extends AnnotationActivity {

    protected T mPresenter;

    public abstract T newPresenter();

    @Override
    protected void onCreateBefore(Bundle savedInstanceState) {
        mPresenter = newPresenter();
    }

}
