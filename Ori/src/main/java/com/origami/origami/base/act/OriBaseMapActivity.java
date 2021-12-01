package com.origami.origami.base.act;

import android.os.Bundle;

import androidx.databinding.ViewDataBinding;

/**
 * @by: origami
 * @date: {2021-10-29}
 * @info:
 **/
public abstract class OriBaseMapActivity<V extends ViewDataBinding, P extends BasePresenter<? extends AnnotationActivity>>
    extends OriBaseActivity<V> {

    protected P mPresenter;

    public abstract P newPresenter();

    @Override
    protected void onCreateBefore(Bundle savedInstanceState) {
        mPresenter = newPresenter();
    }

}
