package com.looper.interfaces;

import java.util.List;

/**
 * 处理状态信息
 */
public interface IProcessStatus<IM extends IMaterial<M>, M> {

    int error();

    int total();


    List<IM> errorMaterials();


    void release();
}
