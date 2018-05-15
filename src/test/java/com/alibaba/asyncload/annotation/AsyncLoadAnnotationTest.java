/**
 * AsyncLoadAnnotationTest.java
 * author: yujiakui
 * 2018年4月18日
 * 下午3:25:33
 */
package com.alibaba.asyncload.annotation;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.alibaba.asyncload.domain.AsyncLoadTestModel;

import junit.framework.Assert;

/**
 * @author yujiakui
 *
 *         下午3:25:33
 *
 */
public class AsyncLoadAnnotationTest {

	public static void main(String[] args) {
		AnnotationConfigApplicationContext annotationConfigApplicationContext = new AnnotationConfigApplicationContext(
				"com.alibaba.asyncload.impl.annotation", "com.alibaba.asyncload.annotation",
				"com.alibaba.asyncload.domain");
		System.out.println(annotationConfigApplicationContext.getBeanDefinitionNames());

		/*AsyncLoadHandlerAop aop = annotationConfigApplicationContext
				.getBean(AsyncLoadHandlerAop.class);*/

		// 执行测试
		AsyncLoadAnnotationTestServiceImpl service = annotationConfigApplicationContext
				.getBean(AsyncLoadAnnotationTestServiceImpl.class);
		AsyncLoadTestModel model1 = service.getRemoteModel("first", 1000); // 每个请求sleep
																			// 1000ms
		AsyncLoadTestModel model2 = service.getRemoteModel("two", 1000); // 每个请求sleep
																			// 1000ms
		AsyncLoadTestModel model3 = service.getRemoteModel("three", 1000); // 每个请求sleep
																			// 1000ms

		long start = 0, end = 0;
		start = System.currentTimeMillis();
		System.out.println(model1.getDetail());
		end = System.currentTimeMillis();
		System.out.println("costTime:" + (end - start));
		Assert.assertTrue((end - start) > 500l); // 第一次会阻塞, 响应时间会在1000ms左右

		start = System.currentTimeMillis();
		System.out.println(model2.getDetail());
		end = System.currentTimeMillis();
		System.out.println("costTime:" + (end - start));
		Assert.assertTrue((end - start) < 500l); // 第二次不会阻塞，因为第一个已经阻塞了1000ms

		start = System.currentTimeMillis();
		System.out.println(model3.getDetail());
		end = System.currentTimeMillis();
		System.out.println("costTime:" + (end - start));
		Assert.assertTrue((end - start) < 500l); // 第三次不会阻塞，因为第一个已经阻塞了1000ms

	}
}
