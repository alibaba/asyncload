/*
 * Copyright 1999-2004 Alibaba.com All right reserved. This software is the confidential and proprietary information of
 * Alibaba.com ("Confidential Information"). You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered into with Alibaba.com.
 */
package com.agapple.asyncload.util;

import javax.annotation.Resource;

import junit.framework.Assert;

import org.junit.Test;

import com.agapple.asyncload.AsyncLoadConfig;
import com.agapple.asyncload.AsyncLoadExecutor;
import com.agapple.asyncload.BaseAsyncLoadNoRunTest;
import com.agapple.asyncload.domain.AsyncLoadTestModel;
import com.agapple.asyncload.domain.AsyncLoadTestService;
import com.agapple.asyncload.impl.AsyncLoadEnhanceProxy;
import com.agapple.asyncload.impl.template.AsyncLoadCallback;
import com.agapple.asyncload.impl.template.AsyncLoadTemplate;
import com.agapple.asyncload.impl.util.AsyncLoadBarrier;

/**
 * @author jianghang 2011-4-27 下午04:22:42
 */
public class AsyncLoadBarrierTest extends BaseAsyncLoadNoRunTest {

    @Resource(name = "asyncLoadTestService")
    private AsyncLoadTestService asyncLoadTestService;

    @Resource(name = "asyncLoadTemplate")
    private AsyncLoadTemplate    asyncLoadTemplate;

    @Test
    public void testTemplateBarrier() {
        AsyncLoadConfig config = new AsyncLoadConfig(3 * 1000l);
        config.setNeedBarrierSupport(true); // 

        long start = 0, end = 0;
        start = System.currentTimeMillis();
        AsyncLoadTestModel model1 = null;
        AsyncLoadTestModel model2 = null;
        AsyncLoadTestModel model3 = null;
        try {
            model1 = asyncLoadTemplate.execute(new AsyncLoadCallback<AsyncLoadTestModel>() {

                public AsyncLoadTestModel doAsyncLoad() {
                    return asyncLoadTestService.getRemoteModel("first", 1000);
                }
            }, config);

            model2 = asyncLoadTemplate.execute(new AsyncLoadCallback<AsyncLoadTestModel>() {

                public AsyncLoadTestModel doAsyncLoad() {
                    return asyncLoadTestService.getRemoteModel("two", 1000);
                }
            }, config);

            model3 = asyncLoadTemplate.execute(new AsyncLoadCallback<AsyncLoadTestModel>() {

                public AsyncLoadTestModel doAsyncLoad() {
                    return asyncLoadTestService.getRemoteModel("three", 1000);
                }
            }, config);
        } finally {
            AsyncLoadBarrier.await();
            end = System.currentTimeMillis();
            Assert.assertTrue((end - start) > 500l); // barrier会阻塞等待所有的线程返回, 响应时间会在1000ms左右
            start = System.currentTimeMillis();
            model1.getDetail();
            model2.getDetail();
            model3.getDetail();
            end = System.currentTimeMillis();
            Assert.assertTrue((end - start) < 500l); // barrier后的都不会进行阻塞

        }
    }

    @Test
    public void testTemplateNoBarrier() {
        AsyncLoadConfig config = new AsyncLoadConfig(3 * 1000l);
        config.setNeedBarrierSupport(false); // 

        long start = 0, end = 0;
        start = System.currentTimeMillis();
        AsyncLoadTestModel model1 = null;
        AsyncLoadTestModel model2 = null;
        AsyncLoadTestModel model3 = null;
        try {
            model1 = asyncLoadTemplate.execute(new AsyncLoadCallback<AsyncLoadTestModel>() {

                public AsyncLoadTestModel doAsyncLoad() {
                    return asyncLoadTestService.getRemoteModel("first", 1000);
                }
            }, config);

            model2 = asyncLoadTemplate.execute(new AsyncLoadCallback<AsyncLoadTestModel>() {

                public AsyncLoadTestModel doAsyncLoad() {
                    return asyncLoadTestService.getRemoteModel("two", 1000);
                }
            }, config);

            model3 = asyncLoadTemplate.execute(new AsyncLoadCallback<AsyncLoadTestModel>() {

                public AsyncLoadTestModel doAsyncLoad() {
                    return asyncLoadTestService.getRemoteModel("three", 1000);
                }
            }, config);
        } finally {
            AsyncLoadBarrier.await();
            end = System.currentTimeMillis();
            Assert.assertTrue((end - start) < 500l); // 没有barrier，就不会阻塞
            start = System.currentTimeMillis();
            model1.getDetail();
            model2.getDetail();
            model3.getDetail();
            end = System.currentTimeMillis();
            Assert.assertTrue((end - start) > 500l); // 进行阻塞

        }
    }

    @Test
    public void testBarrier() {
        // 初始化config
        AsyncLoadConfig config = new AsyncLoadConfig(3 * 1000l);
        config.setNeedBarrierSupport(true); // 
        // 初始化executor
        AsyncLoadExecutor executor = new AsyncLoadExecutor(10, 100);
        executor.initital();
        // 初始化proxy
        AsyncLoadEnhanceProxy<AsyncLoadTestService> proxy = new AsyncLoadEnhanceProxy<AsyncLoadTestService>();
        proxy.setService(asyncLoadTestService);
        proxy.setConfig(config);
        proxy.setExecutor(executor);
        // 执行测试
        long start, end;
        start = System.currentTimeMillis();
        AsyncLoadTestModel model1 = null;
        AsyncLoadTestModel model2 = null;
        AsyncLoadTestModel model3 = null;

        try {
            AsyncLoadTestService service = proxy.getProxy();
            model1 = service.getRemoteModel("first", 1000); // 每个请求sleep 1000ms
            model2 = service.getRemoteModel("two", 1000); // 每个请求sleep 1000ms
            model3 = service.getRemoteModel("three", 1000); // 每个请求sleep 1000ms

        } finally {
            AsyncLoadBarrier.await();
            end = System.currentTimeMillis();
            Assert.assertTrue((end - start) > 500l); // barrier会阻塞等待所有的线程返回, 响应时间会在1000ms左右
            start = System.currentTimeMillis();
            model1.getDetail();
            model2.getDetail();
            model3.getDetail();
            end = System.currentTimeMillis();
            Assert.assertTrue((end - start) < 500l); // barrier后的都不会进行阻塞

            // 销毁executor
            executor.destory();
        }

    }

    @Test
    public void testNoBarrier() {
        // 初始化config
        AsyncLoadConfig config = new AsyncLoadConfig(3 * 1000l);
        config.setNeedBarrierSupport(true); // 
        // 初始化executor
        AsyncLoadExecutor executor = new AsyncLoadExecutor(10, 100);
        executor.initital();
        // 初始化proxy
        AsyncLoadEnhanceProxy<AsyncLoadTestService> proxy = new AsyncLoadEnhanceProxy<AsyncLoadTestService>();
        proxy.setService(asyncLoadTestService);
        proxy.setConfig(config);
        proxy.setExecutor(executor);
        // 执行测试
        long start, end;
        start = System.currentTimeMillis();
        AsyncLoadTestModel model1 = null;
        AsyncLoadTestModel model2 = null;
        AsyncLoadTestModel model3 = null;

        try {
            AsyncLoadTestService service = proxy.getProxy();
            model1 = service.getRemoteModel("first", 1000); // 每个请求sleep 1000ms
            model2 = service.getRemoteModel("two", 1000); // 每个请求sleep 1000ms
            model3 = service.getRemoteModel("three", 1000); // 每个请求sleep 1000ms

        } finally {
            // AsyncLoadBarrier.await();
            end = System.currentTimeMillis();
            Assert.assertTrue((end - start) < 500l); // 没有barrier，就不会阻塞
            start = System.currentTimeMillis();
            model1.getDetail();
            model2.getDetail();
            model3.getDetail();
            end = System.currentTimeMillis();
            Assert.assertTrue((end - start) > 500l); // 阻塞调用

            // 销毁executor
            executor.destory();
        }

    }

}
