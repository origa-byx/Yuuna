package com.origami.origami.base.frgment;

import android.view.View;

import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;

import com.origami.origami.base.act.AnnotationActivity;
import com.origami.origami.base.frgment.AnnotationFragment;

/**
 * @by: origami
 * @date: {2021-10-26}
 * @info:
 **/
public abstract class OriBaseFragment<T extends AnnotationActivity, V extends ViewDataBinding> extends AnnotationFragment<T> {

    protected V bViews;

    @Override
    protected void initBindView(View view) {
        bViews = DataBindingUtil.bind(view);
    }

}
