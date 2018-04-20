package com.alibaba.asyncload.domain;

import java.util.List;

/**
 * 一个asyncLoad的测试对象服务
 *
 * @author jianghang 2011-1-21 下午10:45:19
 */
public interface AsyncLoadTestService {

	public Integer countRemoteModel(String name, long sleep);

	public void updateRemoteModel(String name, long slepp);

	public AsyncLoadTestModel getRemoteModel(String name, long sleep);

	public String getRemoteName(String name, long sleep);

	public Object getRemoteObject(String name, long sleep);

	public List<AsyncLoadTestModel> listRemoteModel(String name, long sleep);

}
