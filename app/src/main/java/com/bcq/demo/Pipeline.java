package com.bcq.demo;

import android.os.SystemClock;

import com.looper.Material;
import com.looper.core.FlowPipe;
import com.looper.interfaces.IPipe;

import java.util.Random;

public class Pipeline extends FlowPipe<Material<String>, String> {
    private final static Pipeline _pipe = new Pipeline();

    public static IPipe getPipe() {
        return _pipe;
    }

    private Pipeline() {
        super(8,false);
        setDelay(0);
        setMaxTry(1);
    }

    @Override
    public Material<String> onProcess(int index, Material<String> material) {
        // TODO: 2/24/21 模拟耗时操作
//        SystemClock.sleep(index == 0 ? 20 : index == 2 ? 40 : 80);
        String result = material.material() + "_P" + index;
        //随机模拟处理失败
        Random random = new Random();
        int count = random.nextInt();
        boolean success = count > 0;
        return new Material(result, success);
    }
}
