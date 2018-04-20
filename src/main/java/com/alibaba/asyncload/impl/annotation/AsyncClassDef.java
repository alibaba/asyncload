/**
 * AsyncClass.java
 * author: yujiakui
 * 2018年4月17日
 * 下午3:06:31
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
 *         下午3:06:31
 *
 */
public @interface AsyncClassDef {

	/**
	 * 异步方法列表
	 *
	 * @return
	 */
	AsyncMethodDef[] asyncMethods() default {};

	/**
	 * 类级别线程池配置
	 *
	 * @return
	 */
	AsyncThreadPoolConfig classThreadPoolConf() default @AsyncThreadPoolConfig;

}
