package com.origami.origami.base.callback;

/**
 * @by: origami
 * @date: {2021-08-03}
 * @info:
 **/
public interface RequestPermissionNext {
    /**
     * 请求成功
     */
    void next();

    /**
     * 请求失败
     */
    void failed();
}
