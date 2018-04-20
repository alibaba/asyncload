/**
 * EnableAsyncMethod.java
 * author: yujiakui
 * 2018年4月20日
 * 下午3:54:02
 */
package com.alibaba.asyncload.impl.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Documented
@Retention(RUNTIME)
@Target(METHOD)
/**
 * @author yujiakui
 *
 *         下午3:54:02
 *
 */
public @interface EnableAsyncMethod {

	/**
	 * 异步并行类方法信息列表
	 *
	 * @return
	 */
	EnableAsyncClassMethodInfo[] classMethodInfos() default { @EnableAsyncClassMethodInfo };

}
