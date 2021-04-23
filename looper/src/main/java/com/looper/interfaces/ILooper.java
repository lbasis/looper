package com.looper.interfaces;

import java.util.List;

/**
 * 原则上有关原料列表数据的增删查 都要维持在队列线程中
 *
 * @param <M>
 */
public interface ILooper<M> {
    /**
     * 添加任务原料
     *
     * @param material 原料
     */
    void apply(IMaterial<M> material);

    /**
     * 批量添加
     *
     * @param materials
     */
    void apply(List<IMaterial<M>> materials);


    void loop(long delay);

    /**
     * 轮循原料
     *
     * @param delete 轮循原料是否从队列中删除
     * @param delay  延迟时间
     */
    void loop(boolean delete, long delay);

    /**
     * 暂停
     *
     * @param pause 标识
     */
    void pause(boolean pause);

    void release();

    /**
     * 获取下一个可用
     *
     * @return
     */
    IMaterial<M> pop();

    /**
     * 手动移除
     *
     * @param material
     * @return
     */
    boolean remove(IMaterial<M> material);

    /**
     * 清空队列
     */
    void clear();

    /**
     * 处理任务回调
     *
     * @param material
     * @return
     */
    boolean onProcess(IMaterial<M> material);

    /**
     * 处理完毕回调
     *
     * @param count 处理失败的记录数
     */
    void onComplete(int count);
}
