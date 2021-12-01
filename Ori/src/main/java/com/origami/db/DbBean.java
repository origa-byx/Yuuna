package com.origami.db;

/**
 * @by: origami
 * @date: {2021-09-28}
 * @info:   DB
 *
 * @see DbName
 * @see Key
 * @see Item
 **/
public interface DbBean {

    /**
     * 当 select 时，其他数据以及装载完毕后 初始化其他非 数据库数据用
     */
    void readAndThen();
}
