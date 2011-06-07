package com.agapple.asyncload;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.TimeUnit;

import com.agapple.asyncload.impl.pool.AsyncLoadCallable;
import com.agapple.asyncload.impl.pool.AsyncLoadFuture;
import com.agapple.asyncload.impl.pool.AsyncLoadThreadPool;
import com.agapple.asyncload.impl.pool.NamedThreadFactory;

/**
 * 异步加载的具体执行任务者, 支持Runable和Callable两种
 * 
 * @author jianghang 2011-1-21 下午11:32:31
 */
public class AsyncLoadExecutor {

    public static final int        DEFAULT_POOL_SIZE    = 20;
    public static final int        DEFAULT_ACCEPT_COUNT = 100;
    public static final HandleMode DEFAULT_MODE         = HandleMode.REJECT;
    private int                    poolSize;
    private int                    acceptCount;                             // 等待队列长度，避免无限制提交请求
    private HandleMode             mode;                                    // 默认为拒绝服务，用于控制accept队列满了以后的处理方式
    private AsyncLoadThreadPool    pool;
    private volatile boolean       isInit               = false;

    enum HandleMode {
        REJECT, CALLERRUN;
    }

    public AsyncLoadExecutor(){
        this(DEFAULT_POOL_SIZE, DEFAULT_ACCEPT_COUNT, DEFAULT_MODE);
    }

    public AsyncLoadExecutor(int poolSize){
        this(poolSize, DEFAULT_ACCEPT_COUNT, DEFAULT_MODE);
    }

    public AsyncLoadExecutor(int poolSize, int acceptCount){
        this(poolSize, acceptCount, DEFAULT_MODE);
    }

    public AsyncLoadExecutor(int poolSize, int acceptCount, HandleMode mode){
        this.poolSize = poolSize;
        this.acceptCount = acceptCount;
        this.mode = mode;
    }

    public void initital() {
        if (isInit == false) {
            RejectedExecutionHandler handler = getHandler(mode);
            BlockingQueue queue = getBlockingQueue(acceptCount, mode);
            // 构造pool池
            this.pool = new AsyncLoadThreadPool(poolSize, poolSize, 0L, TimeUnit.MILLISECONDS, queue,
                                                new NamedThreadFactory(), handler);

            isInit = true;
        }
    }

    public void destory() {
        if (isInit && pool != null) {
            pool.shutdown();
            pool = null;

            isInit = false;
        }
    }

    public <T> AsyncLoadFuture<T> submit(AsyncLoadCallable<T> task) {
        return pool.submit(task);
    }

    // ==================== help method ===========================

    private BlockingQueue<?> getBlockingQueue(int acceptCount, HandleMode mode) {
        if (acceptCount < 0) {
            return new LinkedBlockingQueue();
        } else if (acceptCount == 0) {
            return new ArrayBlockingQueue(1); // 等于0时等价于队列1
        } else {
            return new ArrayBlockingQueue(acceptCount);
        }
    }

    private RejectedExecutionHandler getHandler(HandleMode mode) {
        return HandleMode.REJECT == mode ? new AsyncLoadThreadPool.AbortPolicy() : new AsyncLoadThreadPool.CallerRunsPolicy();
    }

    // ====================== setter / getter ==========================

    public void setPoolSize(int poolSize) {
        this.poolSize = poolSize;
    }

    public void setAcceptCount(int acceptCount) {
        this.acceptCount = acceptCount;
    }

    public void setMode(HandleMode mode) {
        this.mode = mode;
    }

    public void setMode(String mode) {
        this.mode = HandleMode.valueOf(mode);
    }

    // ======================= help method ==========================

    @Override
    public String toString() {
        return "AsyncLoadExecutor [ poolSize=" + poolSize + ", acceptCount=" + acceptCount + ", mode=" + mode + "]";
    }
}
