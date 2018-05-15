package com.alibaba.asyncload.impl.template;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.concurrent.Future;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.MethodSignature;

import com.alibaba.asyncload.AsyncLoadConfig;
import com.alibaba.asyncload.AsyncLoadExecutor;
import com.alibaba.asyncload.impl.AsyncLoadObject;
import com.alibaba.asyncload.impl.AsyncLoadResult;
import com.alibaba.asyncload.impl.exceptions.AsyncLoadException;
import com.alibaba.asyncload.impl.pool.AsyncLoadCallable;
import com.alibaba.asyncload.impl.util.AsyncLoadBarrier;
import com.alibaba.asyncload.impl.util.AsyncLoadUtils;

/**
 * 基于template模式提供的一套AsyncLoad机制，编程式
 *
 * @author jianghang 2011-1-24 下午07:01:07
 */
public class AsyncLoadTemplate {

	private AsyncLoadExecutor executor;
	private AsyncLoadConfig config = new AsyncLoadConfig();

	/**
	 * 异步执行callback模板,设置默认的超时时间，同时返回对应的proxy model,执行AsyncLoad
	 *
	 * @param <R>
	 * @param callback
	 * @return
	 */
	public <R> R execute(AsyncLoadCallback<R> callback) {
		return execute(callback, config);
	}

	/**
	 * 异步执行callback模板,设置默认的超时时间，同时返回对应的proxy model,执行AsyncLoad
	 *
	 * @param <R>
	 * @param callback
	 * @return
	 */
	public Object execute(final ProceedingJoinPoint proceedingJoinPoint) {

		AsyncLoadCallback<Object> asyncLoadCallback = new AsyncLoadCallback<Object>() {
			@Override
			public Object doAsyncLoad() {
				try {
					return proceedingJoinPoint.proceed();
				} catch (Throwable throwable) {
					throw new AsyncLoadException(throwable);
				}
			}
		};

		Signature signature = proceedingJoinPoint.getSignature();
		MethodSignature methodSignature = (MethodSignature) signature;
		Class<?> returnClass = methodSignature.getReturnType();

		// 初始化执行器
		executor.initital();

		return execute(asyncLoadCallback, returnClass, config);
	}

	/**
	 * 异步执行callback模板,同时返回对应的proxy model,执行AsyncLoad
	 *
	 * @param <R>
	 * @param callback
	 * @param timeout
	 * @return
	 */
	public <R> R execute(final AsyncLoadCallback<R> callback, long timeout) {
		AsyncLoadUtils.notNull(callback, "callback is null!");

		Type type = callback.getClass().getGenericInterfaces()[0];
		if (!(type instanceof ParameterizedType)) {
			// 用户不指定AsyncLoadCallBack的泛型信息
			throw new AsyncLoadException(
					"you should specify AsyncLoadCallBack<R> for R type, ie: AsyncLoadCallBack<OfferModel>");
		}
		Class returnClass = getGenericClass((ParameterizedType) type, 0);

		AsyncLoadConfig copy = config.cloneConfig();
		copy.setDefaultTimeout(timeout);
		return execute(callback, returnClass, copy);
	}

	/**
	 * 异步执行callback模板,设置默认的超时时间，同时返回对应的proxy model,执行AsyncLoad
	 *
	 * @param <R>
	 * @param callback
	 * @param returnClass
	 *            期望的返回对象class
	 * @return
	 */
	public <R> R execute(AsyncLoadCallback<R> callback, Class<?> returnClass) {
		return execute(callback, returnClass, config);
	}

	/**
	 * 异步执行callback模板,同时返回对应的proxy model,执行AsyncLoad
	 *
	 * @param <R>
	 * @param callback
	 * @param returnClass
	 *            期望的返回对象class
	 * @param timeout
	 * @return
	 */
	public <R> R execute(final AsyncLoadCallback<R> callback, Class<?> returnClass, long timeout) {
		AsyncLoadConfig copy = config.cloneConfig();
		copy.setDefaultTimeout(timeout);
		return execute(callback, returnClass, copy);
	}

	/**
	 * 异步执行callback模板,传递config对象
	 *
	 * @param <R>
	 * @param callback
	 * @param config
	 * @return
	 */
	public <R> R execute(final AsyncLoadCallback<R> callback, AsyncLoadConfig config) {
		AsyncLoadUtils.notNull(callback, "callback is null!");

		Type type = callback.getClass().getGenericInterfaces()[0];
		if (!(type instanceof ParameterizedType)) {
			// 用户不指定AsyncLoadCallBack的泛型信息
			throw new AsyncLoadException(
					"you should specify AsyncLoadCallBack<R> for R type, ie: AsyncLoadCallBack<OfferModel>");
		}
		Class returnClass = getGenericClass((ParameterizedType) type, 0);
		return execute(callback, returnClass, config);
	}

	/**
	 * 异步执行callback模板,传递config对象
	 *
	 * @param <R>
	 * @param callback
	 * @param returnClass
	 * @param config
	 * @return
	 */
	public <R> R execute(final AsyncLoadCallback<R> callback, Class<?> returnClass,
			AsyncLoadConfig config) {
		AsyncLoadUtils.notNull(callback, "callback is null!");
		AsyncLoadUtils.notNull(returnClass, "returnClass is null!");
		AsyncLoadUtils.notNull(config, "config is null!");

		if (Void.TYPE.isAssignableFrom(returnClass)) {// 判断返回值是否为void
			// 不处理void的函数调用
			return callback.doAsyncLoad();
		} else if (!Modifier.isPublic(returnClass.getModifiers())) {
			// 处理如果是非public属性，则不进行代理，强制访问会出现IllegalAccessException，比如一些内部类或者匿名类不允许直接访问
			return callback.doAsyncLoad();
		} else if (Modifier.isFinal(returnClass.getModifiers())) {
			// 处理特殊的final类型，目前暂不支持，后续可采用jdk proxy
			return callback.doAsyncLoad();
		} else if (returnClass.isPrimitive() || returnClass.isArray()) {
			// 不处理特殊类型，因为无法使用cglib代理
			return callback.doAsyncLoad();
		} else if (returnClass == Object.class) {
			// 针对返回对象是Object类型，不做代理。没有具体的method，代理没任何意义
			return callback.doAsyncLoad();
		} else {
			final AsyncLoadConfig copy = config;
			System.out
					.println("------executor addr=" + executor.hashCode() + ";content=" + executor);
			Future<R> future = executor.submit(new AsyncLoadCallable() {

				@Override
				public R call() throws Exception {
					System.out.println("------doAsyncLoad-----");
					return callback.doAsyncLoad();
				}

				@Override
				public AsyncLoadConfig getConfig() {
					return copy;
				}
			});
			// 够造一个返回的AsyncLoadResult
			AsyncLoadResult result = new AsyncLoadResult(returnClass, future,
					config.getDefaultTimeout());
			// 继续返回一个代理对象
			R asyncProxy = (R) result.getProxy();
			// 添加到barrier中
			if (config.getNeedBarrierSupport()) {
				AsyncLoadBarrier.addTask((AsyncLoadObject) asyncProxy);
			}
			// 返回对象
			return asyncProxy;
		}
	}

	/**
	 * 取得范性信息
	 *
	 * @param cls
	 * @param i
	 * @return
	 */
	private Class<?> getGenericClass(ParameterizedType parameterizedType, int i) {
		Object genericClass = parameterizedType.getActualTypeArguments()[i];
		if (genericClass instanceof ParameterizedType) { // 处理多级泛型
			return (Class<?>) ((ParameterizedType) genericClass).getRawType();
		} else if (genericClass instanceof GenericArrayType) { // 处理数组泛型
			return (Class<?>) ((GenericArrayType) genericClass).getGenericComponentType();
		} else {
			return (Class<?>) genericClass;
		}
	}

	// ===================== setter / getter =============================

	public void setExecutor(AsyncLoadExecutor executor) {
		this.executor = executor;
	}

	public void setConfig(AsyncLoadConfig config) {
		this.config = config;
	}

	/**
	 * @return the executor
	 */
	public AsyncLoadExecutor getExecutor() {
		return executor;
	}

	/**
	 * @return the config
	 */
	public AsyncLoadConfig getConfig() {
		return config;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "AsyncLoadTemplate [executor=" + executor + ", config=" + config + "]";
	}

}
