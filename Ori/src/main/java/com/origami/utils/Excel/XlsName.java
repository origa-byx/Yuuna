package com.origami.utils.Excel;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @by: origami
 * @date: {2021-09-02}
 * @info:
 **/
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface XlsName {
    /**
     * 读取时无所谓，生成用
     * @return 此列的title名称
     */
    String name();

    /**
     * ####重要####
     * @return 在 excel 中的列序号，从 0 开始
     */
    int index();

    /**
     * 读取时无所谓，生成用
     * @return 此列上单元格的宽度
     */
    int width() default 35;
}
