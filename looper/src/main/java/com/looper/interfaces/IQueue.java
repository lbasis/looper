package com.looper.interfaces;

import java.util.List;

/**
 * looper队列接口 类似流水线
 * 以下称流水线
 */
public interface IQueue {
    /**
     * 添加原料
     *
     * @param m
     */
    void apply(IMaterial m);

    /**
     * 批量添加原料
     *
     * @param m
     */
    void apply(List<IMaterial> m);

    /**
     * 暂停队列
     *
     * @param pause
     */
    void pause(boolean pause);

    void clear();

    void release();

    /**
     * 根据索引获取looper
     *
     * @param index
     * @return
     */
    ILooper getLooper(int index);

    /**
     * 处理原料接口
     *
     * @param index    looper索引
     * @param material 原料
     * @return next Looper 处理的原料
     */
    IMaterial onProcess(int index, IMaterial material);

    /**
     * 处理完毕回调
     *
     * @param index looper 索引
     * @param count 处理完成时 剩余的原料数
     */
    void onComplete(int index, int count);
}
