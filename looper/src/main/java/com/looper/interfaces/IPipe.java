package com.looper.interfaces;

import java.util.List;

/**
 * 管道接口
 *
 * @param <IM>> IMaterial
 * @param <M>   IMaterial<M>的原料类型
 */
public interface IPipe<IM extends IMaterial<M>, M> {

    void setMaxTry(int maxTry);


    void setDelay(int delay);

    /**
     * 添加原料，并向首到工序流转
     *
     * @param m
     */
    void apply(Object m);

    /**
     * 暂停管道的所有工序，不会影响管道中正在处理的原料
     */
    void pause();

    /**
     * 恢复管道的所有工序
     */
    void resume();

    /**
     * 清空管道的所有工序的待处理原料列表
     */
    void clear();

    /**
     * 释放资源
     */
    void release();

    /**
     * 获取指定位置的工序实例
     *
     * @param index
     * @return
     */
    IProcedure getProcedure(int index);

    /**
     * 处理原料，并封装成下一道工序的原料
     *
     * @param index  索引
     * @param result 当前工序待处理的结果 待流转下道工序的原料
     * @return 下一个工序的处理的原料
     */
    IM onProcess(int index, IM result);

    /**
     * 处理完毕回调
     *
     * @param index looper 索引
     * @param err   处理完成时 剩余的原料数
     * @param err   处理原料总数
     */
    void onComplete(int index, int err, int total);
}
