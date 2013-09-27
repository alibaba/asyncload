package com.alibaba.asyncload.impl;

import java.lang.reflect.Method;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.CallbackFilter;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.LazyLoader;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import com.alibaba.asyncload.impl.exceptions.AsyncLoadException;
import com.alibaba.asyncload.impl.helper.AsyncLoadProxyRepository;
import com.alibaba.asyncload.impl.helper.AsyncLoadReflectionHelper;
import com.alibaba.asyncload.impl.pool.AsyncLoadFuture;

/**
 * 异步加载返回的proxy result
 * 
 * @author jianghang 2011-1-21 下午09:45:14
 */
public class AsyncLoadResult {

    private Class  returnClass;
    private Future future;
    private Long   timeout;

    public AsyncLoadResult(Class returnClass, Future future, Long timeout){
        this.returnClass = returnClass;
        this.future = future;
        this.timeout = timeout;
    }

    public Object getProxy() {
        Class proxyClass = AsyncLoadProxyRepository.getProxy(returnClass.getName());
        if (proxyClass == null) { // 进行cache处理
            Enhancer enhancer = new Enhancer();
            if (returnClass.isInterface()) {// 判断returnClass是否为接口
                enhancer.setInterfaces(new Class[] { AsyncLoadObject.class, returnClass }); // 设置默认的接口
            } else {
                enhancer.setInterfaces(new Class[] { AsyncLoadObject.class });// 设置默认的接口
                enhancer.setSuperclass(returnClass);
            }
            enhancer.setCallbackFilter(new AsyncLoadCallbackFilter());
            enhancer.setCallbackTypes(new Class[] { AsyncLoadResultInterceptor.class, AsyncLoadObjectInterceptor.class });
            proxyClass = enhancer.createClass();

            AsyncLoadProxyRepository.registerProxy(returnClass.getName(), proxyClass);
        }

        Enhancer.registerCallbacks(proxyClass, new Callback[] { new AsyncLoadResultInterceptor(),
                new AsyncLoadObjectInterceptor() });
        try {
            // 返回对象
            return AsyncLoadReflectionHelper.newInstance(proxyClass);
        } finally {
            // clear thread callbacks to allow them to be gc'd
            Enhancer.registerStaticCallbacks(proxyClass, null);
        }

    }

    /**
     * future.get()的返回对象
     * 
     * @return
     * @throws InterruptedException
     * @throws ExecutionException
     */
    private Object loadFuture() throws AsyncLoadException {
        try {
            // 使用cglib lazyLoader，避免每次调用future
            if (timeout <= 0) {// <=0处理，不进行超时控制
                return future.get();
            } else {
                return future.get(timeout, TimeUnit.MILLISECONDS);
            }
        } catch (TimeoutException e) {
            future.cancel(true);
            throw new AsyncLoadException(e);
        } catch (InterruptedException e) {
            throw new AsyncLoadException(e);
        } catch (Exception e) {
            throw new AsyncLoadException(e);
        }
    }

    class AsyncLoadCallbackFilter implements CallbackFilter {

        public int accept(Method method) {
            // 预先进行匹配，直接计算好需要处理的method，避免动态匹配浪费性能
            if (AsyncLoadObject.class.isAssignableFrom(method.getDeclaringClass())) {// 判断对应的方法是否属于AsyncLoadObject
                return 1;
            } else {
                // 其他全部返回0
                return 0;
            }

        }
    }

    /**
     * 针对AsyncLoadObject方法的实现
     * 
     * @author jianghang 2011-4-4 下午04:22:09
     */
    class AsyncLoadObjectInterceptor implements MethodInterceptor {

        public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
            if ("_isNull".equals(method.getName())) {
                return isNull();
            } else if ("_getStatus".equals(method.getName())) {
                return getStatus();
            } else if ("_getOriginalClass".equals(method.getName())) {
                return getOriginalClass();
            } else if ("_getOriginalResult".equals(method.getName())) {
                return getOriginalResut();
            }

            throw new AsyncLoadException("method[" + method.getName() + "] is not support!");
        }

        private Object isNull() throws Throwable {
            try {
                return loadFuture() == null; // 判断原始对象是否为null
            } catch (Exception e) {
                // 如果出现异常，直接返回为true，这里不再抛出异常，没意义，因为我这里想要的是isNull判断
                // 在最后get()属性时会返回对应future执行的异常信息
                // return true;
                throw e;
            }
        }

        private Object getStatus() {
            long startTime = 0;
            long endTime = 0;
            if (future instanceof AsyncLoadFuture) {
                startTime = ((AsyncLoadFuture) future).getStartTime();
                endTime = ((AsyncLoadFuture) future).getEndTime();
            }
            AsyncLoadStatus.Status status = null;
            if (future.isCancelled()) { // 如果已经完成
                // 在timeout时会标记future为cancel，所有可由cancel状态判断是否为timeout
                status = AsyncLoadStatus.Status.TIMEOUT;
            } else if (future.isDone()) {
                status = AsyncLoadStatus.Status.DONE;
            } else {
                // 这里并不严格区分是否正在运行或者在Executor进行排队中，比如Executor直接拒绝Reject
                status = AsyncLoadStatus.Status.RUN;
                if (endTime == 0) {
                    endTime = System.currentTimeMillis();// 设置为当前时间
                }
            }

            return new AsyncLoadStatus(status, startTime, (endTime - startTime));
        }

        private Object getOriginalClass() {
            return returnClass;
        }

        private Object getOriginalResut() throws Throwable {
            return loadFuture();
        }

    }

    /**
     * 针对model对象的所有方法进行代理实现
     * 
     * @author jianghang 2011-4-4 下午04:24:40
     */
    class AsyncLoadResultInterceptor implements LazyLoader {

        public Object loadObject() throws Exception {
            return loadFuture();
        }

    }

}
