package com.looper.core;

import android.util.SparseArray;

import com.looper.Logger;
import com.looper.interfaces.IMaterial;
import com.looper.interfaces.IPipe;
import com.looper.interfaces.IProcedure;

import java.util.ArrayList;
import java.util.List;

public abstract class FlowPipe<IM extends IMaterial<M>, M> implements IPipe<IM, M> {
    protected final String TAG = this.getClass().getSimpleName();
    private List<IProcedure> procedures = new ArrayList<>(2);
    /* key:index  value: error total info */
    private final SparseArray<String> completeResult = new SparseArray(8);
    protected int maxProcedure = 1;//最大工序
    private int maxTry = IProcedure.MAX_TRY;
    private int delay = 0;

    /**
     * 流管道：管道中只能存在一个原料在执行。
     *
     * @param procedure 工序数
     */
    public FlowPipe(int procedure) {
        if (procedure < 1) procedure = 1;
        maxProcedure = procedure;
        init();
    }

    private void init() {
        procedures.clear();
        Procedurer looper;
        for (int i = 0; i < maxProcedure; i++) {
            final int index = i;
            looper = new Procedurer<IM, M>("Procedure-" + i) {
                @Override
                public IM onProcess(IM material) {
                    return FlowPipe.this.process(index, material);
                }

                @Override
                public void onComplete(int err, int total) {
                    super.onComplete(err, total);
                    FlowPipe.this.onComplete(index, err, total);
                }
            };
            looper.setMaxTry(maxTry);
            procedures.add(looper);
        }
    }

    public IM process(int index, IM material) {
        //处理原料
        IM result = FlowPipe.this.onProcess(index, material);
        if (result.state()) {
            //流转向下道工序
            IProcedure next = FlowPipe.this.getProcedure(index + 1);
            if (null != result && null != next) {
                int count = next.apply(result);
                // 移出状态集
                if (count > 0) {
                    completeResult.remove(index + 1);
                }
            }
        }
        Logger.e(TAG, "process: index = " + index + "  material = " + material);
        // 轮训next 不需处理；暂停轮训 需手动处理下一个原料分发
        if (!result.loopNext()) {
            //处理出错 或 最后一道工程 后分发next原料
            if (!result.state() || index == maxProcedure - 1) {
                getProcedure(0).loopNext(delay);
            }
        }
        return result;
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
    public void onComplete(int index, int err, int total) {
        String info = "onComplete: index = " + index + " error = " + err + " total = " + total;
        completeResult.append(index, info);
        Logger.e(TAG, "onComplete: index = " + index + " max = " + maxProcedure + " size = " + completeResult.size());
        if (maxProcedure == completeResult.size()) {
            showResult();
            clear();
        }
    }

    public void showResult() {
        StringBuilder builder = new StringBuilder();
        builder.append(" \n");
        int size = completeResult.size();
        for (int i = 0; i < size; i++) {
            int index = completeResult.keyAt(i);
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
