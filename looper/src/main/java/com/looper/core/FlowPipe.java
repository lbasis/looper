package com.looper.core;

import android.util.Log;
import android.util.SparseArray;

import com.looper.Logger;
import com.looper.interfaces.IMaterial;
import com.looper.interfaces.IPipe;
import com.looper.interfaces.IProcedure;
import com.looper.interfaces.IProcessStatus;

import java.util.ArrayList;
import java.util.List;

/**
 * 管道流：
 * 1. 管道中的节点（流水线上的工序）互不影响，并行。
 * 2. 管道根据节点顺序维护一个节点集合。
 * 3. 上个节点的处理结果 流转到下个节点充当待处理的原料。
 * 4. 管道中的原料统一封装格式：IMaterial<M> 包含处理的状态。
 * 自动轮训管道（流水线）：
 * 1. 管道中的节点（流水线上的工序）互不影响，并行。
 * 2. 上个节点将处理结果流转到一下节点并存到原料表中，节点自动轮训处理各自的原料表，直至原料列表为空，回调onComplete接口视为一次处理完成。
 * 3. 所有节点都处理完成，视为管道处理完成。
 * 非自动轮训管道（单一通道）：
 * 1. 向首节点分发下一个原料，依次流转到下一节点，直至流转最后一个节点，再次向首节点分发原料。
 * 2. 节点处理原料：成功：流转下个节点，失败：该原理处理结束，向首节点分发下一个原料。
 *
 * @param <IM>
 * @param <M>  原料的类型
 */
public abstract class FlowPipe<IM extends IMaterial<M>, M> implements IPipe<IM, M> {
    protected final String TAG = this.getClass().getSimpleName();
    private List<IProcedure> procedures = new ArrayList<>(2);
    /* key:index  value: error total info */
    private final SparseArray<IProcessStatus<IM, M>> completeResult = new SparseArray(8);
    protected int maxProcedure = 1;//最大工序
    private int maxTry = IProcedure.MAX_TRY;
    private int delay = 0;
    private boolean autoLoopNext;
    // 记录流转节点索引
    private List<Integer> applyIndexs = new ArrayList<>();

    public FlowPipe(int procedure) {
        this(procedure, true);
    }

    /**
     * 流管道：管道中只能存在一个原料在执行。
     *
     * @param procedure    工序数
     * @param autoLoopNext 自动轮训
     */
    public FlowPipe(int procedure, boolean autoLoopNext) {
        if (procedure < 1) procedure = 1;
        maxProcedure = procedure;
        this.autoLoopNext = autoLoopNext;
        init();
    }

    private void init() {
        procedures.clear();
        Procedurer looper;
        for (int i = 0; i < maxProcedure; i++) {
            final int index = i;
            looper = new Procedurer<IM, M>(i, autoLoopNext) {
                @Override
                public IM onProcess(IM material) {
                    return FlowPipe.this.onProcess(index, material);
                }

                @Override
                protected void onAfterProcess(IM material, IM result) {
                    if (result.state()) {
                        //流转向下道工序
                        IProcedure next = FlowPipe.this.getProcedure(index + 1);
                        if (null != result && null != next) {
                            int count = next.apply(result);
                            // 移出状态集
                            if (count > 0) {
                                completeResult.remove(index + 1);
                                if (!applyIndexs.contains(index + 1))
                                    applyIndexs.add(index + 1);
                            }
                        }
                    }
                    // 自动轮训 不需管道分发next原料；
                    // 非自动轮训 在工序流转异常时 需手动向管道分发next原料。
                    if (!autoLoopNext) {
                        //后分发next原料
                        // 1. 处理出错且达最大尝试次数， 出错未达到最大尝试测试，在节点内部出里
                        // 2. 最后一节点 成功 或 出错且达最大尝试次数
                        if ((index == maxProcedure - 1 && (result.state() || material.getCount() >= maxTry))
                                || (!result.state() && material.getCount() >= maxTry)) {
                            IMaterial m = getProcedure(0).next();
                            // fix 问题：首个节点 出里出错，再次looper next 导致execute为空，会再次回调onComplete
                            if (getProcedure(0).count() > 0) {
                                getProcedure(0).loopNext(delay);
                            }
                        }
                    }
                }

                @Override
                public void onComplete() {
                    FlowPipe.this.onComplete(index);

                }
            };
            looper.setMaxTry(maxTry);
            procedures.add(looper);
        }
    }

    @Override
    public void setMaxTry(int maxTry) {
        this.maxTry = maxTry;
        for (int i = 0; i < maxProcedure; i++) {
            IProcedure looper = procedures.get(i);
            looper.setMaxTry(maxTry);
        }
    }

    @Override
    public void setDelay(int delay) {
        this.delay = delay;
        for (int i = 0; i < maxProcedure; i++) {
            IProcedure looper = procedures.get(i);
            looper.setDelay(delay);
        }
    }

    @Override
    public void apply(Object obj) {
        IProcedure first = getProcedure(0);
        if (null != obj && null != first) {
            int count = first.apply(obj);
            if (count > 0) {
                completeResult.remove(0);
                if (!applyIndexs.contains(0))
                    applyIndexs.add(0);
            }
        }
    }

    @Override
    public void pause() {
        for (int i = 0; i < maxProcedure; i++) {
            IProcedure looper = procedures.get(i);
            looper.pauseLoop();
        }
    }

    @Override
    public void resume() {
        for (int i = 0; i < maxProcedure; i++) {
            IProcedure looper = procedures.get(i);
            looper.resumeLoop();
        }
    }

    @Override
    public void clear() {
        for (int i = 0; i < maxProcedure; i++) {
            IProcedure looper = procedures.get(i);
            looper.clear();
        }
    }

    @Override
    public void release() {
        for (int i = 0; i < maxProcedure; i++) {
            IProcedure looper = procedures.get(i);
            looper.release();
        }
        procedures.clear();
    }

    @Override
    public IProcedure getProcedure(int index) {
        if (index > -1 && index < maxProcedure) {
            return procedures.get(index);
        }
        return null;
    }

    @Override
    public void onComplete(int index) {
        completeResult.append(index, getProcedure(index).getProcessStatus());
        Logger.e(TAG, " onComplete ：index " + index + " apply " + applyIndexs.size() + " current = " + completeResult.size());
        // 此处使用流转节点索引数替代maxProcedure作为所有节点都处理完成，
        // 若果原料流转到某个节点时，所有问题都保错，导致不是所有节点都能流转。
        if (applyIndexs.size() == completeResult.size()) {
            showResult();
            clear();
            applyIndexs.clear();
        }
    }

    public void showResult() {
        StringBuilder builder = new StringBuilder();
        builder.append(" \n");
        int size = completeResult.size();
        for (int i = 0; i < size; i++) {
            int index = completeResult.keyAt(i);
            builder.append("onComplete: index = ");
            builder.append(index + " ");
            builder.append(completeResult.get(index));
            builder.append("\n");
        }
        Logger.e(TAG, builder.toString());
    }

    /**
     * 处理原料，并封装成下一道工序的原料
     *
     * @param index    索引
     * @param material 当前工序待处理原料
     * @return 下一个工序的处理的原料
     */
    @Override
    public abstract IM onProcess(int index, IM material);
}
