package com.alibaba.asyncload.spring;

import javax.annotation.Resource;

import junit.framework.Assert;

import org.junit.Test;

import com.alibaba.asyncload.BaseAsyncLoadNoRunTest;
import com.alibaba.asyncload.domain.AsyncLoadTestModel;
import com.alibaba.asyncload.domain.AsyncLoadTestService;

/**
 * 测试一下基于spring拦截器配置的并行加载
 * 
 * @author jianghang 2011-4-1 下午05:16:15
 */
public class AsyncLoadSpringInteceptorTest extends BaseAsyncLoadNoRunTest {

    @Resource(name = "asyncLoadTestServiceForInteceptor")
    private AsyncLoadTestService asyncLoadTestServiceForInteceptor;

    @Test
    public void testSpringInteceptor() {
        AsyncLoadTestModel model1 = asyncLoadTestServiceForInteceptor.getRemoteModel("first", 1000);
        AsyncLoadTestModel model2 = asyncLoadTestServiceForInteceptor.getRemoteModel("two", 1000);
        long start = 0, end = 0;
        start = System.currentTimeMillis();
        System.out.println(model1.getDetail());
        end = System.currentTimeMillis();
        Assert.assertTrue((end - start) > 500l); // 第一次会阻塞, 响应时间会在1000ms左右

        start = System.currentTimeMillis();
        System.out.println(model2.getDetail());
        end = System.currentTimeMillis();
        Assert.assertTrue((end - start) < 500l); // 第二次不会阻塞，第一个已经阻塞了1000ms
    }
}
