/**
 * EnableAsyncClassMethodInfo.java
 * author: yujiakui
 * 2018年4月19日
 * 上午11:44:26
 */
package com.alibaba.asyncload.impl.annotation;

import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Documented
@Retention(RUNTIME)
@Target(TYPE_USE)
/**
 * @author yujiakui
 *
 *         上午11:44:26
 *
 *         开启异步并行类方法信息
 */
public @interface EnableAsyncClassMethodInfo {

	/**
	 * 对应类的全名，默认是ALL，就是全部（即是标记了异步并行定义的类）
	 *
	 * @return
	 */
	String classFullName() default AsyncLoadAnnotationConstants.ALL_CLASSES;

	/**
	 * 默认全部标记定义了所有异步并行加载的方法
	 *
	 * @return
	 */
	String[] methodMatchRegex() default { AsyncLoadAnnotationConstants.ALL_METHODS };
}
