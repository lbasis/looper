package com.looper;

import com.looper.interfaces.IMaterial;

public class Material<M> implements IMaterial<M> {
    private long delay = 0;//任务处理延迟时间
    private int count = 0;//尝试次数
    private int max = 1;//尝试次数
    private boolean execute = false;
    private M m;

    public Material(M m, int max) {
        this.m = m;
        this.max = max;
    }

    @Override
    public boolean available() {
        boolean b = !execute && count < max;
        if (!b) {
            printUnAvaliable(m);
        }
        return b;
    }

    @Override
    public M material() {
        return m;
    }

    @Override
    public long delay() {
        return delay;
    }

    public void setDelay(long delay) {
        this.delay = delay;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getCount() {
        return count;
    }

    public void setExecute(boolean execute) {
        this.execute = execute;
    }

    protected void printUnAvaliable(M material) {
    }

    @Override
    public boolean equals(Object o) {
        return this == o ||
                (o instanceof Material &&
                        null != m && m.equals(((Material) o).m));
    }
}