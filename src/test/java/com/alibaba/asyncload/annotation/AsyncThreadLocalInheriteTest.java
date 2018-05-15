/**
 * AsyncThreadLocalInheriteTest.java
 * author: yujiakui
 * 2018年4月23日
 * 上午9:20:39
 */
package com.alibaba.asyncload.annotation;

/**
 * @author yujiakui
 *
 *         上午9:20:39
 *
 */
public class AsyncThreadLocalInheriteTest {

	private final static ThreadLocal<String> threadLocal = new ThreadLocal<String>();

	public static void set(String cnt) {
		threadLocal.set(cnt);
	}

	public static String get() {
		return threadLocal.get();
	}

}
