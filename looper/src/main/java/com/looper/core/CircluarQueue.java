package com.looper.core;

import com.looper.interfaces.IMaterial;

/**
 * 串行队列
 * 描述：第一道工序（looper）处理，并流向下道工序，直至最后一道工序完成，第一道工序开始处理下一个原料，如此往复。
 * 注意：第一道工序需要等待最后一道工序执行完毕后才能分发下一个原料
 */
public abstract class CircluarQueue extends PipeQueue {

    public CircluarQueue(int looperSize, boolean once) {
        super(looperSize, once);
    }

    @Override
    public boolean handleProcess(int index, IMaterial material) {
        super.handleProcess(index, material);
        if (index == maxLooper - 1) {//开始处理下一个原料
            getLooper(0).loop(0);
        }
        return false;
    }
}
