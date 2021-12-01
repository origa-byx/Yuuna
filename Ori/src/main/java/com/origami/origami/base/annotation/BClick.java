package com.origami.origami.base.annotation;

import android.view.View;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author xiao gan
 * @date 2020/12/2
 * @description: {@link View#setOnClickListener(View.OnClickListener)}
 **/
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface BClick {
    int[] value();
}
