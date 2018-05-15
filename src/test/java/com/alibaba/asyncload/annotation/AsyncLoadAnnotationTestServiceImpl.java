/**
 * AsyncLoadAnnotationTestServiceImpl.java
 * author: yujiakui
 * 2018年4月18日
 * 下午3:29:50
 */
package com.alibaba.asyncload.annotation;

import java.util.List;

import org.springframework.stereotype.Component;

import com.alibaba.asyncload.domain.AsyncLoadTestModel;
import com.alibaba.asyncload.domain.AsyncLoadTestServiceImpl;
import com.alibaba.asyncload.impl.annotation.AsyncClassDef;
import com.alibaba.asyncload.impl.annotation.AsyncMethodDef;
import com.alibaba.asyncload.impl.annotation.AsyncThreadPoolConfig;

/**
 * @author yujiakui
 *
 *         下午3:29:50
 *
 */
@Component
@AsyncClassDef(
		classThreadPoolConf = @AsyncThreadPoolConfig(effect = true, poolSize = 2, queueSize = 1))
public class AsyncLoadAnnotationTestServiceImpl extends AsyncLoadTestServiceImpl {

	@Override
	@AsyncMethodDef(timeout = 10)
	public AsyncLoadTestModel getRemoteModel(String name, long sleep) {
		System.out.println(
				"-----getRemoteModel-----threadLocal----" + AsyncThreadLocalInheriteTest.get());
		Thread current = Thread.currentThread();
		System.out.println("线程信息:" + current);
		return super.getRemoteModel(name, sleep);
	}

	@AsyncMethodDef(timeout = 10, inheritThreadLocal = true,
			methodThreadPoolConf = @AsyncThreadPoolConfig(effect = true))
	public AsyncLoadTestModel getRemoteModel1(String name, long sleep) {
		System.out.println(
				"-----getRemoteModel_1-----threadLocal----" + AsyncThreadLocalInheriteTest.get());

		Thread current = Thread.currentThread();
		System.out.println("线程信息:" + current);
		return super.getRemoteModel(name, sleep);
	}

	@Override
	public List<AsyncLoadTestModel> listRemoteModel(String name, long sleep) {
		return super.listRemoteModel(name, sleep);
	}

	@Override
	@AsyncMethodDef
	public Integer countRemoteModel(String name, long sleep) {
		return super.countRemoteModel(name, sleep);
	}

	@Override
	public void updateRemoteModel(String name, long sleep) {
		super.updateRemoteModel(name, sleep);
	}

	@Override
	public String getRemoteName(String name, long sleep) {
		return super.getRemoteName(name, sleep);
	}

	@Override
	public Object getRemoteObject(String name, long sleep) {
		return super.getRemoteObject(name, sleep);
	}
}
