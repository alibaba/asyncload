/**
 * AsyncLoadAnnotationThreadLocal.java
 * author: yujiakui
 * 2018年4月20日
 * 下午2:56:51
 */
package com.alibaba.asyncload.impl.annotation;

import java.util.Map;

/**
 * @author yujiakui
 *
 *         下午2:56:51
 *
 */
public class AsyncLoadAnnotationThreadLocal {

	/** 异步并行加载map，其中key对应的classFullName，String[]对应的是方法签名匹配表达式 */
	private static final ThreadLocal<Map<String, String[]>> asyncLoadMapThreadLocal = new ThreadLocal<>();

	public static void setAsyncLoadMap(Map<String, String[]> asyncLoadMap) {
		asyncLoadMapThreadLocal.set(asyncLoadMap);
	}

	public static Map<String, String[]> getAsyncLoadMap() {
		return asyncLoadMapThreadLocal.get();
	}

	public static void removeAsyncLoadMap() {
		asyncLoadMapThreadLocal.remove();
	}
}
