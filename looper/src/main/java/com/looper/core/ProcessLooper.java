package com.looper.core;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.text.TextUtils;

import com.looper.Logger;
import com.looper.interfaces.ILooper;
import com.looper.interfaces.IMaterial;

import java.util.ArrayList;
import java.util.List;

/**
 * 当个流程工序处理队列
 *
 * @param <M>
 */
public abstract class ProcessLooper<M> extends HandlerThread implements ILooper<M> {
    private final static String TAG = "ProcessLooper";
    private final static int CODE_APPLY = 70001;
    private final static int CODE_NEXT = 70002;
    private final static int CODE_NEXT_AND_DELETE = 70003;
    private final List<IMaterial<M>> _materials = new ArrayList<>();
    private Handler loopHander;
    private boolean _delete;//执行next时是否删除
    private boolean _pause;//是否暂停
    //自定义字段
    public Object obj;

    /**
     * @param name   名称
     * @param delete process时是否删除
     */
    public ProcessLooper(String name, boolean delete) {
        super(TextUtils.isEmpty(name) ? "ProcessLooper" : name);
        this._delete = delete;
        start();
        loopHander = new Handler(getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                int what = msg.what;
                if (CODE_APPLY == what) {
                    Object o = msg.obj;
                    int count = 0, surplus = _materials.size();
                    if (o instanceof IMaterial) {
                        count = _apply((IMaterial) o);
                    } else if (o instanceof List) {
                        count = _apply((List<IMaterial>) o);
                    }
                    Logger.e(TAG, getName() + " 剩余：" + surplus + " apply ：" + count + " total = " + _materials.size());
                    loop(_delete, 0);
                } else if (CODE_NEXT == what || CODE_NEXT_AND_DELETE == what) {
                    IMaterial m = _pop(CODE_NEXT_AND_DELETE == what);
                    if (null != m) {
                        boolean next = onProcess(m);
                        if (next) {
                            loop(_delete, m.delay());
                        }
                    } else {
                        onComplete(_materials.size());
                    }
                }
            }
        };
    }

    @Override
    public void apply(IMaterial<M> material) {
        Message msg = Message.obtain();
        msg.what = CODE_APPLY;
        msg.obj = material;
        loopHander.sendMessage(msg);
    }

    @Override
    public void apply(List<IMaterial<M>> os) {
        Message msg = Message.obtain();
        msg.what = CODE_APPLY;
        msg.obj = os;
        loopHander.sendMessage(msg);
    }

    @Override
    public void loop(long delay) {
        loop(_delete, delay);
    }

    public void loop(boolean delete, long delay) {
        if (delay < 0) delay = 0;
        loopHander.removeMessages(CODE_NEXT);
        loopHander.removeMessages(CODE_NEXT_AND_DELETE);
        if (_pause) {
            return;
        }
        Message msg = Message.obtain();
        msg.what = delete ? CODE_NEXT_AND_DELETE : CODE_NEXT;
        loopHander.sendMessageDelayed(msg, delay);
    }

    @Override
    public void pause(boolean pause) {
        this._pause = pause;
        if (!_pause) {//恢复
            loop(0);
        }
    }

    @Override
    public void release() {
        clear();
        loopHander.removeMessages(CODE_APPLY);
        loopHander.removeMessages(CODE_NEXT);
        loopHander.removeMessages(CODE_NEXT_AND_DELETE);
        loopHander = null;
        quitSafely();
    }

    @Override
    public IMaterial<M> pop() {
        return _pop(false);
    }

    @Override
    public boolean remove(IMaterial<M> material) {
        synchronized (_materials) {
            return _materials.remove(material);
        }
    }

    @Override
    public void clear() {
        synchronized (_materials) {
            _materials.clear();
        }
    }

    /**
     * 当前线程执行
     *
     * @param m
     * @return
     */
    private int _apply(IMaterial m) {
        if (null == m) return -1;
        synchronized (_materials) {
            if (!_materials.contains(m)) {
                _materials.add(m);
            }
        }
        return 1;
    }

    /**
     * 当前线程运行
     *
     * @param ms
     * @return 添加记录数
     */
    private int _apply(List<IMaterial> ms) {
        int len = null == ms ? 0 : ms.size();
        if (len < 1) return -1;
        int count = 0;
        synchronized (_materials) {
            for (int i = 0; i < len; i++) {
                IMaterial m = ms.get(i);
                if (!_materials.contains(m)) {
                    _materials.add(m);
                    count++;
                }
            }
        }
        return count;
    }

    private IMaterial _pop(boolean delete) {
        IMaterial material = null;
        synchronized (_materials) {
            int len = _materials.size();
            for (int i = 0; i < len; i++) {
                IMaterial m = _materials.get(i);
                if (m.available()) {
                    material = m;
                    break;
                }
            }
            if (delete && null != material) {
                _materials.remove(material);
            }
        }
        return material;
    }


    @Override
    public void onComplete(int count) {
    }

    @Override
    public abstract boolean onProcess(IMaterial<M> material);

}
