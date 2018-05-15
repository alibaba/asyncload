/**
 * AsyncThreadPoolConfig.java
 * author: yujiakui
 * 2018年4月17日
 * 下午3:43:29
 */
package com.alibaba.asyncload.impl.annotation;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.alibaba.asyncload.impl.enums.PoolRejectHandleMode;

@Documented
@Retention(RUNTIME)
@Target({ ElementType.ANNOTATION_TYPE })
/**
 * @author yujiakui
 *
 *         下午3:43:29
 *
 *         异步线程池配置
 */
public @interface AsyncThreadPoolConfig {

	/**
	 * 是否生效，默认为不生效（如果方法上的线程池不生效，则找类上面的，如果类上面的也不生效，则只有默认全局的）
	 *
	 * @return
	 */
	boolean effect() default false;

	/**
	 * 线程池核心线程数和最大线程数的大小，默认是20
	 *
	 * @return
	 */
	int poolSize() default 20;

	/**
	 * 队列大小，默认是100
	 *
	 * @return
	 */
	int queueSize() default 100;

	/**
	 * 线程池拒绝处理策略
	 *
	 * @return
	 */
	PoolRejectHandleMode rejectPolicy() default PoolRejectHandleMode.CALLERRUN;
}
