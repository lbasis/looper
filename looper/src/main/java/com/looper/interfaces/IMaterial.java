package com.looper.interfaces;

public interface IMaterial<M> {
    /**
     * 原料
     */
    M material();

    /**
     * 处理延迟时间
     */
    long delay();

    /**
     * 原料是否可用
     */
    boolean available();
}
