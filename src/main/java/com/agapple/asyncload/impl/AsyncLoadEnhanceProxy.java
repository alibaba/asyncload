package com.agapple.asyncload.impl;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.CallbackFilter;
import net.sf.cglib.proxy.Dispatcher;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import org.springframework.util.Assert;

import com.agapple.asyncload.AsyncLoadConfig;
import com.agapple.asyncload.AsyncLoadExecutor;
import com.agapple.asyncload.AsyncLoadMethodMatch;
import com.agapple.asyncload.AsyncLoadProxy;
import com.agapple.asyncload.impl.exceptions.AsyncLoadException;
import com.agapple.asyncload.impl.helper.AsyncLoadProxyRepository;
import com.agapple.asyncload.impl.helper.AsyncLoadReflectionHelper;
import com.agapple.asyncload.impl.pool.AsyncLoadCallable;
import com.agapple.asyncload.impl.util.AsyncLoadBarrier;

/**
 * 基于cglib enhance proxy的实现
 * 
 * <pre>
 * 参数说明：
 * 1. targetClass : 用于明确cglib生成的目标对象类型。比如一般的service都有一个接口，但serviceImpl有时已经被进行一次cglib代理，生成了final对象，这里可以指定targetClass为其接口对象
 * </pre>
 * 
 * @author jianghang 2011-1-21 下午10:56:39
 */
public class AsyncLoadEnhanceProxy<T> implements AsyncLoadProxy<T> {

    private T                 service;
    private AsyncLoadConfig   config;
    private AsyncLoadExecutor executor;
    private Class<T>          targetClass;

    public AsyncLoadEnhanceProxy(){
    }

    public AsyncLoadEnhanceProxy(T service, AsyncLoadExecutor executor){
        this(service, new AsyncLoadConfig(), executor);
    }

    public AsyncLoadEnhanceProxy(T service, AsyncLoadConfig config, AsyncLoadExecutor executor){
        this.service = service;
        this.config = config;
        this.executor = executor;
        this.targetClass = (Class<T>) service.getClass();// 默认的代理class对象即为service
    }

    public T getProxy() {
        validate();
        return getProxyInternal();
    }

    /**
     * 相应的检查方法
     */
    private void validate() {
        Assert.notNull(service, "service should not be null");
        Assert.notNull(config, "config should not be null");
        Assert.notNull(executor, "executor should not be null");

        if (Modifier.isFinal(targetClass.getModifiers())) { // 目前暂不支持final类型的处理，以后可以考虑使用jdk proxy
            throw new AsyncLoadException("Enhance proxy not support final class :" + targetClass.getName());
        }

        if (!Modifier.isPublic(targetClass.getModifiers())) {
            // 处理如果是非public属性，则不进行代理，强制访问会出现IllegalAccessException，比如一些内部类或者匿名类不允许直接访问
            throw new AsyncLoadException("Enhance proxy not support private/protected class :" + targetClass.getName());
        }
    }

    class AsyncLoadCallbackFilter implements CallbackFilter {

        public int accept(Method method) {
            // 预先进行匹配，直接计算好需要处理的method，避免动态匹配浪费性能
            if (AsyncLoadObject.class.isAssignableFrom(method.getDeclaringClass())) {// 判断对应的方法是否属于AsyncLoadObject
                return 0; // for AsyncLoadServiceInterceptor
            } else {
                Map<AsyncLoadMethodMatch, Long> matches = config.getMatches();
                Set<AsyncLoadMethodMatch> methodMatchs = matches.keySet();
                if (methodMatchs != null && !methodMatchs.isEmpty()) {
                    for (Iterator<AsyncLoadMethodMatch> methodMatch = methodMatchs.iterator(); methodMatch.hasNext();) {
                        if (methodMatch.next().matches(method)) {
                            return 2; // for AsyncLoadInterceptor
                        }
                    }
                }
                return 1; // for AsyncLoadDirect
            }
        }
    }

    class AsyncLoadServiceInterceptor implements MethodInterceptor {

        public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
            if ("_getOriginalClass".equals(method.getName())) {
                return getOriginalClass();
            }
            throw new AsyncLoadException("method[" + method.getName() + "] is not support!");
        }

        private Object getOriginalClass() {
            return targetClass;
        }
    }

    class AsyncLoadDirect implements Dispatcher {

        public Object loadObject() throws Exception {
            return service;
        }

    }

    class AsyncLoadInterceptor implements MethodInterceptor {

        public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
            Long timeout = getMatchTimeout(method);
            final Object finObj = service;
            final Object[] finArgs = args;
            final Method finMethod = method;

            Class returnClass = method.getReturnType();
            if (Void.TYPE.isAssignableFrom(returnClass)) {// 判断返回值是否为void
                // 不处理void的函数调用
                return finMethod.invoke(finObj, finArgs);
            } else if (!Modifier.isPublic(returnClass.getModifiers())) {
                // 处理如果是非public属性，则不进行代理，强制访问会出现IllegalAccessException，比如一些内部类或者匿名类不允许直接访问
                return finMethod.invoke(finObj, finArgs);
            } else if (Modifier.isFinal(returnClass.getModifiers())) {
                // 处理特殊的final类型，目前暂不支持，后续可采用jdk proxy
                return finMethod.invoke(finObj, finArgs);
            } else if (returnClass.isPrimitive() || returnClass.isArray()) {
                // 不处理特殊类型，因为无法使用cglib代理
                return finMethod.invoke(finObj, finArgs);
            } else if (returnClass == Object.class) {
                // 针对返回对象是Object类型，不做代理。没有具体的method，代理没任何意义
                return finMethod.invoke(finObj, finArgs);
            } else {
                Future future = executor.submit(new AsyncLoadCallable() {

                    public Object call() throws Exception {
                        try {
                            return finMethod.invoke(finObj, finArgs);// 需要直接委托对应的finObj(service)进行处理
                        } catch (Throwable e) {
                            throw new AsyncLoadException("future invoke error!", e);
                        }
                    }

                    public AsyncLoadConfig getConfig() {
                        return config;
                    }
                });
                // 够造一个返回的AsyncLoadResult
                AsyncLoadResult result = new AsyncLoadResult(returnClass, future, timeout);
                // 继续返回一个代理对象
                AsyncLoadObject asyncProxy = (AsyncLoadObject) result.getProxy();
                // 添加到barrier中
                if (config.getNeedBarrierSupport()) {
                    AsyncLoadBarrier.addTask((AsyncLoadObject) asyncProxy);
                }
                // 返回对象
                return asyncProxy;
            }

        }

        /**
         * 返回对应的匹配的timeout时间，一定能找到对应的匹配点
         * 
         * @param method
         * @return
         */
        private Long getMatchTimeout(Method method) {
            Map<AsyncLoadMethodMatch, Long> matches = config.getMatches();
            Set<Map.Entry<AsyncLoadMethodMatch, Long>> entrys = matches.entrySet();
            if (entrys != null && !entrys.isEmpty()) {
                for (Iterator<Map.Entry<AsyncLoadMethodMatch, Long>> iter = entrys.iterator(); iter.hasNext();) {
                    Map.Entry<AsyncLoadMethodMatch, Long> entry = iter.next();
                    if (entry.getKey().matches(method)) {
                        return entry.getValue();
                    }
                }
            }

            return config.getDefaultTimeout();
        }
    }

    // =========================== help mehotd =================================

    /**
     * 优先从Repository进行获取ProxyClass,创建对应的object
     * 
     * @return
     */
    private T getProxyInternal() {
        Class proxyClass = AsyncLoadProxyRepository.getProxy(targetClass.getName());
        if (proxyClass == null) {
            Enhancer enhancer = new Enhancer();
            if (targetClass.isInterface()) { // 判断是否为接口，优先进行接口代理可以解决service为final
                enhancer.setInterfaces(new Class[] { targetClass });
            } else {
                enhancer.setSuperclass(targetClass);
            }
            enhancer.setCallbackTypes(new Class[] { AsyncLoadServiceInterceptor.class, AsyncLoadDirect.class,
                    AsyncLoadInterceptor.class });
            enhancer.setCallbackFilter(new AsyncLoadCallbackFilter());
            proxyClass = enhancer.createClass();
            // 注册proxyClass
            AsyncLoadProxyRepository.registerProxy(targetClass.getName(), proxyClass);
        }

        Enhancer.registerCallbacks(proxyClass, new Callback[] { new AsyncLoadServiceInterceptor(),
                new AsyncLoadDirect(), new AsyncLoadInterceptor() });
        try {
            return (T) AsyncLoadReflectionHelper.newInstance(proxyClass);
        } finally {
            // clear thread callbacks to allow them to be gc'd
            Enhancer.registerStaticCallbacks(proxyClass, null);
        }
    }

    // ====================== setter / getter ===========================

    public void setService(T service) {
        this.service = service;
        if (targetClass == null) {
            this.targetClass = (Class<T>) service.getClass();
        }
    }

    public void setConfig(AsyncLoadConfig config) {
        this.config = config;
    }

    public void setExecutor(AsyncLoadExecutor executor) {
        this.executor = executor;
    }

    public void setTargetClass(Class targetClass) {
        this.targetClass = targetClass;
    }

}
