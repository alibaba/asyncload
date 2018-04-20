/**
 * EnableAsyncClass.java
 * author: yujiakui
 * 2018年4月19日
 * 上午11:35:46
 */
package com.alibaba.asyncload.impl.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Documented
@Retention(RUNTIME)
@Target({ TYPE })
@Inherited
/**
 * @author yujiakui
 *
 *         上午11:35:46
 *
 */
public @interface EnableAsyncClass {

	/**
	 * 异步并行类方法信息列表
	 *
	 * @return
	 */
	EnableAsyncClassMethodInfo[] classMethodInfos() default { @EnableAsyncClassMethodInfo };
}
