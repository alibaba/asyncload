/**
 * AsyncLoadAnnotationMultiMethodTest.java
 * author: yujiakui
 * 2018年4月20日
 * 下午3:22:25
 */
package com.alibaba.asyncload.annotation;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.asyncload.domain.AsyncLoadTestModel;
import com.alibaba.asyncload.impl.annotation.EnableAsyncClass;
import com.google.common.collect.Lists;

/**
 * @author yujiakui
 *
 *         下午3:22:25
 *
 */
@Component
@EnableAsyncClass
public class AsyncLoadAnnotationMultiMethodTest {

	@Autowired
	private AsyncLoadAnnotationTestServiceImpl asyncLoadAnnotationTestServiceImpl;

	public AsyncLoadTestModel multiHandler(String name, long sleep) {

		List<AsyncLoadTestModel> results = Lists.newArrayList();
		for (int i = 0; i < 5; i++) {
			AsyncLoadTestModel model = asyncLoadAnnotationTestServiceImpl.getRemoteModel(name,
					sleep);

			results.add(model);
		}
		// AsyncLoadTestModel model =
		// asyncLoadAnnotationTestServiceImpl.getRemoteModel(name, sleep);
		// AsyncLoadTestModel model2 =
		// asyncLoadAnnotationTestServiceImpl.getRemoteModel(name, sleep);

		// Integer num =
		// asyncLoadAnnotationTestServiceImpl.countRemoteModel(name, sleep);
		System.out.println("-------------here---------");
		System.out.println(results.get(0).getDetail());
		// System.out.println(num);
		System.out.println("====================");
		return results.get(0);
	}
}
