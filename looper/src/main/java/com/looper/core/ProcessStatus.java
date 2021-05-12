package com.looper.core;

import com.looper.interfaces.IMaterial;
import com.looper.interfaces.IProcessStatus;

import java.util.ArrayList;
import java.util.List;

/**
 * 工序执行状态
 *
 * @param <M>
 */
public class ProcessStatus<IM extends IMaterial<M>, M> implements IProcessStatus<IM, M> {
    private int error;
    private int total;
    private List<IM> errorMaterials;

    protected ProcessStatus(List<IM> errorMaterials, int total) {
        this.error = errorMaterials.size();
        this.total = total;
        this.errorMaterials = new ArrayList<>(error);
        this.errorMaterials.addAll(errorMaterials);
    }

    @Override
    public int error() {
        return error;
    }

    @Override
    public int total() {
        return total;
    }

    @Override
    public List<IM> errorMaterials() {
        return errorMaterials;
    }

    @Override
    public void release() {
        errorMaterials.clear();
        errorMaterials = null;
    }

    @Override
    public String toString() {
        return "{" +
                "error:" + error +
                ", total:" + total +
                ", errorMaterials:" + errorMaterials +
                '}';
    }
}