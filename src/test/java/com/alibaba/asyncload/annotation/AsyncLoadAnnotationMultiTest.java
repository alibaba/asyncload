/**
 * AsyncLoadAnnotationMultiTest.java
 * author: yujiakui
 * 2018年4月20日
 * 下午3:32:12
 */
package com.alibaba.asyncload.annotation;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.alibaba.asyncload.domain.AsyncLoadTestModel;

/**
 * @author yujiakui
 *
 *         下午3:32:12
 *
 */
public class AsyncLoadAnnotationMultiTest {

	public static void main(String[] args) {
		AnnotationConfigApplicationContext annotationConfigApplicationContext = new AnnotationConfigApplicationContext(
				"com.alibaba.asyncload.impl.annotation", "com.alibaba.asyncload.annotation",
				"com.alibaba.asyncload.domain");
		System.out.println(annotationConfigApplicationContext.getBeanDefinitionNames());

		/*AsyncLoadHandlerAop aop = annotationConfigApplicationContext
				.getBean(AsyncLoadHandlerAop.class);*/

		// 执行测试
		AsyncLoadAnnotationMultiMethodTest service = annotationConfigApplicationContext
				.getBean(AsyncLoadAnnotationMultiMethodTest.class);
		Thread current = Thread.currentThread();

		AsyncThreadLocalInheriteTest.set("yjk----test");
		System.out.println("线程信息:" + current);
		AsyncLoadTestModel model = service.multiHandler("xxx", 1000);
		System.out.println(model);
		long start = 0, end = 0;
		start = System.currentTimeMillis();
		System.out.println(model.getDetail());
		end = System.currentTimeMillis();
		System.out.println("costTime:" + (end - start));
	}
}
