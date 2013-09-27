package com.alibaba.asyncload;

import com.alibaba.asyncload.impl.AsyncLoadEnhanceProxy;

/**
 * 异步加载proxy工厂，创建对应的Proxy Service，当前默认实现为{@linkplain AsyncLoadEnhanceProxy}
 * 
 * <pre>
 * 简单事例代码：
 *  // 初始化config
 *  AsyncLoadConfig config = new AsyncLoadConfig(3 * 1000l);
 *  // 初始化executor
 *  AsyncLoadExecutor executor = new AsyncLoadExecutor(10, 100);
 *  executor.initital();
 *  // 初始化proxy
 *  AsyncLoadEnhanceProxy&lt;AsyncLoadTestService&gt; proxy = new AsyncLoadEnhanceProxy&lt;AsyncLoadTestService&gt;();
 *  proxy.setService(asyncLoadTestService);
 *  proxy.setConfig(config);
 *  proxy.setExecutor(executor);
 *  proxy.setTargetClass(AsyncLoadTestService.class); //指定代理的目标对象
 *  // 执行测试
 *  AsyncLoadTestService service = proxy.getProxy();
 * </pre>
 * 
 * @author jianghang 2011-1-21 下午08:26:32
 */
public interface AsyncLoadProxy<T> {

    public T getProxy();
}
