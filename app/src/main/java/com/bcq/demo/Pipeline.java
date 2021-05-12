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
        super(3,true);
        setDelay(200);
        setMaxTry(2);
    }

    @Override
    public Material<String> onProcess(int index, Material<String> material) {
        // TODO: 2/24/21 模拟耗时操作
        SystemClock.sleep(index == 0 ? 200 : index == 2 ? 400 : 800);
        String result = material.material() + "_P" + index;
        //随机模拟处理失败
        Random random = new Random();
        int count = random.nextInt();
        boolean success = count > 0;
        return new Material(result, success);
    }
}
