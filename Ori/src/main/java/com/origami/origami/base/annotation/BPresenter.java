package com.origami.origami.base.annotation;

import com.origami.origami.base.act.BasePresenter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @by: origami
 * @date: {2021-10-29}
 * @info:
 **/
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface BPresenter {
    Class<? extends BasePresenter<?>> value();
}
