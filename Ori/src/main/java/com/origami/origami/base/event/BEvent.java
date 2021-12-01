package com.origami.origami.base.event;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @by: origami
 * @date: {2021-10-29}
 * @info:
 **/
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface BEvent {
    String value();
    int runThread() default OriEventBus.ui_thread;
}
