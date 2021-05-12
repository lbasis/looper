package com.looper.interfaces;

import java.util.List;

/**
 * 工序接口
 *
 * @param <IM>> IMaterial
 * @param <M>   IMaterial<M>的原料类型
 */
public interface IProcedure<IM extends IMaterial<M>, M> {
    int CODE_NEXT = 70001;
    //最小间隔
    int MIN_DELAY = 0;
    int MAX_TRY = 2;

    void setMaxTry(int maxTry);


    void setDelay(int delay);

    /**
     * 添加任务原料
     * 重复添加处理规则：
     * 1. 已处理成功的原料，success列表中存在，不添加
     * 2. 已处理失败的原料，error 列表中存在，移出error列表 后添加
     *
     * @param obj 原料
     * @return 添加原料的记录数
     */
    int apply(Object obj);


    /**
     * 获取最早添加的可用原料 不移出
     */
    IM next();

    /**
     * 手动从原料队列中移出
     *
     * @param material
     */
    boolean remove(IM material);

    /**
     * 清空原料列表
     */
    void clear();

    /**
     * 轮训下一个原料
     *
     * @param delay 延迟时间
     */
    void loopNext(long delay);

    /**
     * 当前原料记录数
     */
    int count();

    /**
     * 暂停 单不会影响正在处理的原料处理
     */
    void pauseLoop();

    /**
     * 恢复
     */
    void resumeLoop();

    /**
     * 释放资源 结束loop线程
     */
    void release();

    /**
     * 处理任务回调
     *
     * @param material
     * @return 是否自动流转的标识 true：流转  false：不流转
     */
    IM onProcess(IM material);

    /**
     * 处理完毕回调
     *
     * @param error 处理失败的记录数
     * @param total 总记录数
     */
    void onComplete(int error, int total);
}
