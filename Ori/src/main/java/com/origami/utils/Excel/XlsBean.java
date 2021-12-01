package com.origami.utils.Excel;

/**
 * @by: origami
 * @date: {2021-09-03}
 * @info:   {@link Excel}
 **/
public abstract class XlsBean {

    int row = Integer.MIN_VALUE;

    /**
     * 在填充完所有的 {@link XlsName} 字段后调用，来填充其他非 excel 内存在的字段
     * @param position 在读取产出的 List 中的位置
     * @param objects 备选功能扩展 {@link Excel#readAll(Object...)} 中的 obj 暂时通过传参加此抽象函数结合的这种方式这么来做一些操作
     *                           比如：obj 传入一个 MAP 在每次的 {@link #init_other_field(int, Object...)} 调用都向内 put 数据
     *                             或者 传入回调类直接执行回调
     */
    public abstract void init_other_field(int position, Object... objects);

}
