package com.origami.origami.base.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author xiao gan
 * @date 2020/12/2
 * @description: {@link android.app.Activity#setContentView(int)}
 *
 **/
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface BContentView {
    int value();
}
