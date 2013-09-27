package com.alibaba.asyncload.impl.util;

import com.alibaba.asyncload.impl.AsyncLoadObject;
import com.alibaba.asyncload.impl.exceptions.AsyncLoadException;

import java.util.ArrayList;
import java.util.List;

/**
 * 提供一栅栏机制,利用该栅栏可以要求Thread主线程提交的异步所有并行加载单元返回结果 <br>
 * 说明：如果出现嵌套的并行加载调用，需要自己设置多个点的栅栏。
 * 
 * <pre>
 * 比如：主线程调用A方法,A方法中调用B方法和C方法。简化一点说：A -> B ,A -> C
 * 需要在A方法的最后，执行一次barrier.await()操作
 * 同样需要在主线程的最后，再执行一次barrier.await()操作
 * </pre>
 * 
 * <pre>
 * barrier使用例子：
 * try {
 * ModelA a = xxService.getModelA(); //提交一个加载单元
 * ModelB b = xxService.getModelB(); //提交一个加载单元
 * } finally { //务必要执行,不然会内有内存泄漏,barrier中会持有临时的加载单元
 *  try {
 *      AsyncLoadBarrier.await();
 *  } catch (InterruptedException ex) {
 *      return;
 *  } catch (AsyncLoadException ex) {
 *      return;
 *  }
 * }
 *  // 通过栅栏之后, ModelA和ModelB数据已正式加载完成
 * </pre>
 * 
 * @author jianghang 2011-4-27 下午02:18:30
 */
public class AsyncLoadBarrier {

    private static ThreadLocal<List<AsyncLoadObject>> tasks = new ThreadLocal<List<AsyncLoadObject>>() {

                                                                protected List<AsyncLoadObject> initialValue() {
                                                                    return new ArrayList<AsyncLoadObject>();
                                                                }

                                                            };

    public static void await() throws AsyncLoadException {
        List<AsyncLoadObject> objects = tasks.get();
        try {
            for (AsyncLoadObject object : objects) {
                object._getOriginalResult();// 调用一个方法，进行阻塞等待结果，内部会返回timeout , interrupt异常等
            }
        } finally {
            objects = null;
            tasks.set(new ArrayList<AsyncLoadObject>()); // 清空掉barrier记录,避免内存泄漏
        }
    }

    // =================== helper method =================

    public static void addTask(AsyncLoadObject object) {
        // 内部方法,用于提交task
        tasks.get().add(object);
    }
}
