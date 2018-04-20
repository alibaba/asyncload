/**
 * AsyncMethod.java
 * author: yujiakui
 * 2018年4月17日
 * 下午3:02:52
 */
package com.alibaba.asyncload.impl.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Documented
@Retention(RUNTIME)
@Target({ METHOD, ElementType.ANNOTATION_TYPE })
/**
 * @author yujiakui
 *
 *         下午3:02:52
 *
 *         异步并行方法对应的注解
 *
 */
public @interface AsyncMethodDef {

	/**
	 * 方法匹配对应的正则PatternMatchUtils
	 *
	 * 注意：将这个注解放在方法上则这个对应的methodMatchRegex将不起作用，因为此时就是对应这个方法
	 *
	 * @return
	 */
	String[] methodMatchRegex() default {};

	/**
	 * 排除方法匹配模式
	 *
	 * 注意：将这个注解放在方法上则这个对应的methodMatchRegex将不起作用，因为此时就是对应这个方法
	 *
	 * @return
	 */
	String[] excludeMethodMatchRegex() default {};

	/**
	 * 默认超时时间
	 *
	 * @return
	 */
	long timeout() default 1000;

	/**
	 * 开启的异步线程池中的对应执行的线程是否继承当前线程的threadLocal，默认不继承
	 *
	 * @return
	 */
	boolean inheritThreadLocal() default false;

	/**
	 * 方法线程池配置
	 *
	 * @return
	 */
	AsyncThreadPoolConfig methodThreadPoolConf() default @AsyncThreadPoolConfig;

}
