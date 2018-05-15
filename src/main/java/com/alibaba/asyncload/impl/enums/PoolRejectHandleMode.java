/**
 * HandleMode.java
 * author: yujiakui
 * 2018年4月17日
 * 下午3:47:33
 */
package com.alibaba.asyncload.impl.enums;

/**
 * @author yujiakui
 *
 *         下午3:47:33
 *
 *         线程池队列满了之后对应的处理模式
 *
 */
public enum PoolRejectHandleMode {
	/** 线程池队列满了之后再来请求直接拒绝 */
	REJECT,
	/** 用于被拒绝任务的处理程序，它直接在 execute 方法的调用线程中运行被拒绝的任务；如果执行程序已关闭，则会丢弃该任务 */
	CALLERRUN;
}
