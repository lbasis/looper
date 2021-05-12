package com.looper.interfaces;

/**
 * 原料封装接口
 *
 * @param <M>
 */
public interface IMaterial<M> {
    /**
     * 原料
     */
    M material();

    /**
     * 获取处理状态
     *
     * @return true：处理成功，false：处理失败
     */
    boolean state();

    /**
     * 设置当前尝试次数
     *
     * @param count
     */
    void setCount(int count);

    /**
     * 获取尝试次数
     */
    int getCount();
}
