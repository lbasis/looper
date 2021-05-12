package com.looper.core;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.text.TextUtils;

import com.looper.Logger;
import com.looper.interfaces.IProcedure;
import com.looper.interfaces.IMaterial;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * 当个流程工序处理队列
 *
 * @param <M> 原料的类型
 */
public abstract class Procedurer<IM extends IMaterial<M>, M> extends HandlerThread implements IProcedure<IM, M> {
    private final static String TAG = "Procedurer";
    protected final List<IM> _materials = new ArrayList<>();
    protected final List<IM> _error = new ArrayList<>();
    protected final List<IM> _success = new ArrayList<>();
    private Handler loopHander;
    // 是否暂停
    private boolean _pause;
    // 正在处理原料：只有当处理完毕回调onComplete()时才会为null，此时 materials为空
    private IM _execute;
    private int _maxTry = MAX_TRY;//最大尝试次数
    private int _delay;

    /* 处理next的handler */
    public static class LpHandler extends Handler {
        private WeakReference<Procedurer> reference;

        public LpHandler(Procedurer procedurer) {
            reference = new WeakReference<>(procedurer);
        }

        @Override
        public void handleMessage(Message msg) {
            Procedurer procedurer = reference.get();
            if (null != procedurer) {
                if (IProcedure.CODE_NEXT == msg.what) {
                    procedurer.pop();
                    procedurer.process();
                }
            }
        }
    }

    /**
     * @param name 名称
     */
    public Procedurer(String name) {
        super(TextUtils.isEmpty(name) ? TAG : name);
        start();
        loopHander = new LpHandler(this);
    }

    @Override
    public void setMaxTry(int maxTry) {
        this._maxTry = maxTry;
    }

    public void setDelay(int delay) {
        this._delay = delay;
    }

    @Override
    public int count() {
        synchronized (_materials) {
            return _materials.size();
        }
    }

    private int checkAdded(IM m) {
        //已经成功 不添加
        if (_success.contains(m)) {
            return 0;
        }
        // 已失败 移出error列表 再添加
        if (_error.contains(m)) {
            _error.remove(m);
        }
        // 添加,若存在先移除
        _materials.remove(m);
        _materials.add(m);
        return 1;
    }

    /**
     * 添加任务原料
     * 重复添加处理规则：
     * 1. 已处理成功的原料，success列表中存在，不添加
     * 2. 已处理失败的原料，error 列表中存在，移出error列表 后添加
     *
     * @param obj 原料
     */
    @Override
    public int apply(Object obj) {
        int count = 0;
        int surplus = _materials.size();
        if (obj instanceof IMaterial) {//单个
            IM m = (IM) obj;
            synchronized (_materials) {
                // 若存在先移除，再添加
                count = checkAdded(m);
            }
        } else if (obj instanceof List) {// 批量
            List<IM> ms = (List<IM>) obj;
            int len = null == ms ? 0 : ms.size();
            if (len > 0) {
                synchronized (_materials) {
                    for (int i = 0; i < len; i++) {
                        IM m = ms.get(i);
                        count += checkAdded(m);
                    }
                }
            }
        }
        int total = _materials.size();
        Logger.e(TAG, getName()
                + " : surplus = " + surplus
                + "  apply = " + count
                + "  current = " + total);
        // 正在处理对象不存在 表明所有原料已处理完毕即回调onComplete
        // 此时需手动触发轮训
        if (total > 0 && null == _execute) {
            loopNext(_delay);
        }
        return count;
    }

    @Override
    public IM next() {
        synchronized (_materials) {
            return _materials.get(0);
        }
    }

    @Override
    public boolean remove(IM material) {
        synchronized (_materials) {
            return _materials.remove(material);
        }
    }

    @Override
    public void clear() {
        synchronized (_materials) {
            _materials.clear();
            _error.clear();
            _success.clear();
        }
    }


    /**
     * 轮训下一个 不影响添加操作
     *
     * @param delay 延迟时间
     */
    @Override
    public void loopNext(long delay) {
        if (delay < MIN_DELAY) delay = MIN_DELAY;
        loopHander.removeMessages(CODE_NEXT);
        if (_pause) {
            return;
        }
        Message msg = Message.obtain();
        msg.what = CODE_NEXT;
        loopHander.sendMessageDelayed(msg, delay);
    }

    public void resumeLoop() {
        _pause = false;
        Logger.e(TAG, getName() + "：resume " + _execute);
        loopNext(_delay);
    }

    @Override
    public void pauseLoop() {
        _pause = true;
        // 移除消息
        loopHander.removeMessages(CODE_NEXT);
        Logger.e(TAG, getName() + "：pause " + _execute);
        //暂停时的处理结果 注意暂停时 原料中可能会有未处理原料 total == success + error + material
        int err = _error.size();
        Logger.e(TAG, getName() + "：error " + err + " total = " + (err + _success.size() + _materials.size()));
    }

    @Override
    public void release() {
        clear();
        loopHander.removeMessages(CODE_NEXT);
        loopHander = null;
        quitSafely();
    }

    /**
     * 当前轮训线程处理弹出一个可以的最早添加原料
     */
    protected void pop() {
        synchronized (_materials) {
            if (_materials.isEmpty()) {
                _execute = null;
            } else {
                _execute = _materials.remove(0);
            }
        }
    }

    protected void add(IM material) {
        synchronized (_materials) {
            _materials.add(material);
        }
    }

    protected void process() {
        if (null != _execute) {
            // try count +1
            if (_execute.getCount() < _maxTry) {
                _execute.setCount(_execute.getCount() + 1);
            }
            IM result = onProcess(_execute);
            if (null == result) {
                Logger.e(TAG, " The Result for onProcess() is Null !");
                return;
            }
            if (result.state()) {// 成功
                // 移出error列表
                _error.remove(_execute);
                // 添加success列表
                _success.add(_execute);
                Logger.e(TAG, getName() + " process 成功:" + _execute.material());
            } else {// 出现异常
                if (_execute.getCount() < _maxTry) {
                    // 还可重试，重新加入原料列表的最后
                    add(_execute);
                    Logger.e(TAG, getName() + " process 异常，待重试:" + _execute.material());
                } else {// 不可尝试 添加error列表
                    _success.remove(_execute);
                    _error.add(_execute);
                    Logger.e(TAG, getName() + " process 错误:" + _execute.material());
                }
            }
            if (result.loopNext()) {
                loopNext(_delay);
            }
        } else {
            int err = _error.size();
            int success = _success.size();
            onComplete(err, err + success);
        }
    }

    @Override
    public void onComplete(int err, int total) {
    }

    @Override
    public abstract IM onProcess(IM material);
}
