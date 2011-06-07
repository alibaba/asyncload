package com.agapple.asyncload.impl.pool;

import java.util.concurrent.FutureTask;

import org.springframework.util.Assert;

import com.agapple.asyncload.AsyncLoadConfig;

/**
 * 继承J.U.C下的FutureTask,主要的变化点：
 * 
 * <pre>
 * 1. 持有提交task的thread引用，用于threadLocal处理.新的pool处理线程可以继承/共享callerThread线程的threadLocal信息
 * </pre>
 * 
 * @author jianghang 2011-3-28 下午10:15:04
 */
public class AsyncLoadFuture<V> extends FutureTask<V> {

    private Thread          callerThread; // 记录提交runnable的thread，在ThreadPool中用于提取ThreadLocal
    private Thread          runnerThread;
    private long            startTime = 0; // 记录下future开始执行的时间
    private long            endTime   = 0; // 记录下future执行结束时间
    private AsyncLoadConfig config;

    public AsyncLoadFuture(AsyncLoadCallable<V> callable){
        super(callable);
        callerThread = Thread.currentThread();
        config = callable.getConfig();

        Assert.notNull(config, "config is null!");
    }

    @Override
    protected void done() {
        endTime = System.currentTimeMillis(); // 记录一下时间点，Future在cancel调用，正常完成，或者运行出异常都会回调该方法
    }

    @Override
    public void run() {
        startTime = System.currentTimeMillis();
        runnerThread = Thread.currentThread(); // 记录的下具体pool中的runnerThread，可能是caller自己
        super.run();
    }

    // =============== setter / getter ===============

    public Thread getCallerThread() {
        return callerThread;
    }

    public Thread getRunnerThread() {
        return runnerThread;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public AsyncLoadConfig getConfig() {
        return config;
    }

}
