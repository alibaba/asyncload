package com.alibaba.asyncload.impl.pool;

import java.lang.reflect.Field;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.springframework.util.ReflectionUtils;

/**
 * 扩展了J.U.C的ThreadPoolExecutor，主要扩展点说明：
 * 
 * <pre>
 * 1. 覆写newTaskFor函数，返回自定义的{@linkplain AsyncLoadFuture}
 * 2. 增强了Pool池中的Worker线程，会自动复制caller Thread的threadLocal信息，几点考虑：
 *   a. Worker线程为pool的内部管理对象，在操作ThreadLocal信息时安全性上不存在问题，持有的引用在task完成后也可以正常释放。ThreadLocal引用在Worker线程中的生命周期<=Caller Thread线程
 *   b. 做为并行异步加载，一个主要的设计思想就是对业务尽可能的透明，尽可能的减少使用陷井，所以这里通过非正常手段实现了ThreadLocal的支持，实属无奈
 * </pre>
 * 
 * @author jianghang 2011-3-28 下午09:56:32
 */
public class AsyncLoadThreadPool extends ThreadPoolExecutor {

    private static final Field threadLocalField            = ReflectionUtils.findField(Thread.class, "threadLocals");
    private static final Field inheritableThreadLocalField = ReflectionUtils.findField(Thread.class,
                                                               "inheritableThreadLocals");
    static {
        // 强制的声明accessible
        ReflectionUtils.makeAccessible(threadLocalField);
        ReflectionUtils.makeAccessible(inheritableThreadLocalField);
    }

    // 继承自ThreadPoolExecutor的构造函数
    public AsyncLoadThreadPool(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
                               BlockingQueue<Runnable> workQueue){
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
    }

    public AsyncLoadThreadPool(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
                               BlockingQueue<Runnable> workQueue, RejectedExecutionHandler handler){
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler);
    }

    public AsyncLoadThreadPool(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
                               BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory,
                               RejectedExecutionHandler handler){
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
    }

    public AsyncLoadThreadPool(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
                               BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory){
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
    }

    public <T> AsyncLoadFuture<T> submit(AsyncLoadCallable<T> task) {
        if (task == null) throw new NullPointerException();
        AsyncLoadFuture ftask = new AsyncLoadFuture<T>(task); // 使用自定义的Future
        execute(ftask);
        return ftask;
    }

    // ====================== 扩展点 ==========================

    @Override
    public void execute(Runnable command) {
        if (command instanceof AsyncLoadFuture) {
            AsyncLoadFuture afuture = (AsyncLoadFuture) command;
            boolean flag = afuture.getConfig().getNeedThreadLocalSupport();
            if (flag) {
                Thread thread = Thread.currentThread();
                if (ReflectionUtils.getField(threadLocalField, thread) == null) {
                    // 创建一个空的ThreadLocal,立马写回去
                    new ThreadLocal<Boolean>(); // 这时会在runner线程产生一空记录的ThreadLocalMap记录
                }
                if (ReflectionUtils.getField(inheritableThreadLocalField, thread) == null) {
                    // 创建一个空的ThreadLocal,立马写回去
                    new InheritableThreadLocal<Boolean>(); // 可继承的ThreadLocal
                }
            }
        }

        super.execute(command);// 调用父类进行提交
    }

    @Override
    protected void beforeExecute(Thread t, Runnable command) {
        // 在执行之前处理下ThreadPool的属性继承
        if (command instanceof AsyncLoadFuture) {
            AsyncLoadFuture afuture = (AsyncLoadFuture) command;
            boolean flag = afuture.getConfig().getNeedThreadLocalSupport();
            if (flag) {
                initThreadLocal(threadLocalField, afuture.getCallerThread(), t);
                initThreadLocal(inheritableThreadLocalField, afuture.getCallerThread(), t);
            }
        }

        super.beforeExecute(t, command);
    }

    @Override
    protected void afterExecute(Runnable command, Throwable t) {
        // 在执行结束后清理下ThreadPool的属性，GC处理
        if (command instanceof AsyncLoadFuture) {
            AsyncLoadFuture afuture = (AsyncLoadFuture) command;
            boolean flag = afuture.getConfig().getNeedThreadLocalSupport();
            if (flag) {
                recoverThreadLocal(threadLocalField, afuture.getCallerThread(), afuture.getRunnerThread());
                recoverThreadLocal(inheritableThreadLocalField, afuture.getCallerThread(), afuture.getRunnerThread());
            }
        }

        super.afterExecute(command, t);
    }

    private void initThreadLocal(Field field, Thread caller, Thread runner) {
        if (caller == null || runner == null) {
            return;
        }
        // 主要考虑这样的情况：
        // 1.
        // 如果caller线程没有使用ThreadLocal对象，而异步加载的runner线程执行中使用了ThreadLocal对象，则需要复制对象到caller线程上
        // 2.
        // 后续caller,多个runner线程有使用ThreadLocal对象，使用的是同一个引用,直接set都是针对同一个ThreadLocal,所以以后就不需要进行合并

        // 因为在提交Runnable时已经同步创建了一个ThreadLocalMap对象，所以runner线程只需要复制caller对应的引用即可，不需要进行合并，简化处理
        // threadlocal属性复制,注意是引用复制
        Object callerThreadLocalMap = ReflectionUtils.getField(field, caller);
        if (callerThreadLocalMap != null) {
            ReflectionUtils.setField(field, runner, callerThreadLocalMap);// 复制caller的信息到runner线程上
        } else {
            // 这个分支不会出现,因为在execute提交的时候已经添加
        }
    }

    private void recoverThreadLocal(Field field, Thread caller, Thread runner) {
        if (runner == null) {
            return;
        }
        // 清理runner线程的ThreadLocal，为下一个task服务
        ReflectionUtils.setField(field, runner, null);
    }

}
