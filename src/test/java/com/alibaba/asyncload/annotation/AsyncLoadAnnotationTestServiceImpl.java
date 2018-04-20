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

/**
 * @author yujiakui
 *
 *         下午3:29:50
 *
 */
@Component
@AsyncClassDef
public class AsyncLoadAnnotationTestServiceImpl extends AsyncLoadTestServiceImpl {

	@Override
	@AsyncMethodDef(timeout = 10)
	public AsyncLoadTestModel getRemoteModel(String name, long sleep) {
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
