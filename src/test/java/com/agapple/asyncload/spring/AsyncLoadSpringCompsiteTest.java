package com.agapple.asyncload.spring;

import javax.annotation.Resource;

import junit.framework.Assert;

import org.junit.Test;

import com.agapple.asyncload.BaseAsyncLoadNoRunTest;
import com.agapple.asyncload.domain.AsyncLoadTestModel;
import com.agapple.asyncload.domain.AsyncLoadTestService;

/**
 * @author jianghang 2011-4-25 下午03:17:39
 */
public class AsyncLoadSpringCompsiteTest extends BaseAsyncLoadNoRunTest {

    @Resource(name = "asyncLoadTestServiceForCompsitePrototype")
    private AsyncLoadTestService asyncLoadTestServiceForCompsitePrototype;

    @Resource(name = "asyncLoadTestServiceForCompsiteSingleton")
    private AsyncLoadTestService asyncLoadTestServiceForCompsiteSingleton;

    @Resource(name = "asyncLoadTestServiceForCompsiteFactoryBean")
    private AsyncLoadTestService asyncLoadTestServiceForCompsiteFactoryBean;

    @Test
    public void testPrototype() {
        internalTest(asyncLoadTestServiceForCompsitePrototype);
        AsyncLoadTestService service = (AsyncLoadTestService) this.applicationContext.getBean("asyncLoadTestServiceForCompsitePrototype");
        Assert.assertNotSame(service, asyncLoadTestServiceForCompsitePrototype);
    }

    @Test
    public void testSingleton() {
        internalTest(asyncLoadTestServiceForCompsiteSingleton);
        AsyncLoadTestService service = (AsyncLoadTestService) this.applicationContext.getBean("asyncLoadTestServiceForCompsiteSingleton");
        Assert.assertSame(service, asyncLoadTestServiceForCompsiteSingleton);
    }

    @Test
    public void testProxyFactoryBean() {
        internalTest(asyncLoadTestServiceForCompsiteFactoryBean);
        AsyncLoadTestService service = (AsyncLoadTestService) this.applicationContext.getBean("asyncLoadTestServiceForCompsiteFactoryBean");
        Assert.assertNotSame(service, asyncLoadTestServiceForCompsiteFactoryBean);
    }

    private void internalTest(AsyncLoadTestService service) {
        AsyncLoadTestModel model1 = service.getRemoteModel("first", 1000);
        AsyncLoadTestModel model2 = service.getRemoteModel("two", 1000);
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
