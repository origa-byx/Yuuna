package com.origami.origami.base.act;

import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;

/**
 * @by: origami
 * @date: {2021-10-26}
 * @info:
 **/
public abstract class OriBaseActivity<V extends ViewDataBinding> extends AnnotationActivity {

    protected V mViews;

    @Override
    protected void initContentView(int resId) {
        mViews = DataBindingUtil.setContentView(this, resId);
    }

}
