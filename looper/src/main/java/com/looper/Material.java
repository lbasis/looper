package com.looper;

import com.looper.interfaces.IMaterial;

public class Material<M> implements IMaterial<M> {
    private M m;
    private int count;
    private boolean status = false;
    private boolean loopNext = true;

    public Material(M m) {
        this.m = m;
    }

    public Material(M m, boolean loopNext, boolean status) {
        this.m = m;
        this.loopNext = loopNext;
        this.status = status;
    }

    @Override
    public boolean loopNext() {
        return loopNext;
    }

    @Override
    public M material() {
        return m;
    }

    @Override
    public boolean state() {
        return status;
    }

    @Override
    public void setCount(int count) {
        this.count = count;
    }

    @Override
    public int getCount() {
        return count;
    }

    @Override
    public boolean equals(Object o) {
        return this == o ||
                (o instanceof Material &&
                        null != m && m.equals(((Material) o).m));
    }

    @Override
    public String toString() {
        return "{" +
                "m:" + m +
                ", count:" + count +
                ", status:" + status +
                ", loopNext:" + loopNext +
                '}';
    }
}