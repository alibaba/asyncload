package com.alibaba.asyncload.util;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import junit.framework.Assert;

import org.junit.Test;

import com.alibaba.asyncload.AsyncLoadConfig;
import com.alibaba.asyncload.AsyncLoadExecutor;
import com.alibaba.asyncload.BaseAsyncLoadNoRunTest;
import com.alibaba.asyncload.domain.AsyncLoadTestModel;
import com.alibaba.asyncload.domain.AsyncLoadTestService;
import com.alibaba.asyncload.domain.AsyncLoadTestServiceImpl;
import com.alibaba.asyncload.impl.AsyncLoadEnhanceProxy;
import com.alibaba.asyncload.impl.AsyncLoadStatus;
import com.alibaba.asyncload.impl.exceptions.AsyncLoadException;
import com.alibaba.asyncload.impl.util.AsyncLoadUtils;

/**
 * 获取并行加载的内部数据测试
 * 
 * @author jianghang 2011-4-4 下午06:34:37
 */
public class AsyncLoadUtilsTest extends BaseAsyncLoadNoRunTest {

    @Resource(name = "asyncLoadTestService")
    private AsyncLoadTestService asyncLoadTestService;

    @Test
    public void testIsNull_true() {
        // 初始化config
        AsyncLoadConfig config = new AsyncLoadConfig(3 * 1000l);
        // 初始化executor
        AsyncLoadExecutor executor = new AsyncLoadExecutor(10, 100);
        executor.initital();
        // 初始化proxy
        AsyncLoadEnhanceProxy<AsyncLoadTestService> proxy = new AsyncLoadEnhanceProxy<AsyncLoadTestService>();
        proxy.setService(new AsyncLoadTestServiceNullImpl());
        proxy.setConfig(config);
        proxy.setExecutor(executor);
        AsyncLoadTestService service = proxy.getProxy();

        AsyncLoadTestModel model = service.getRemoteModel("one", 1000); // 方法直接返回了null
        long start = System.currentTimeMillis();
        boolean isNull1 = AsyncLoadUtils.isNull(model);
        long end = System.currentTimeMillis();
        Assert.assertTrue(end - start > 500);// 调用这个方法会阻塞
        Assert.assertTrue(isNull1);
    }

    @Test
    public void testIsNull_false() {
        // 初始化config
        AsyncLoadConfig config = new AsyncLoadConfig(3 * 1000l);
        // 初始化executor
        AsyncLoadExecutor executor = new AsyncLoadExecutor(10, 100);
        executor.initital();
        // 初始化proxy
        AsyncLoadEnhanceProxy<AsyncLoadTestService> proxy = new AsyncLoadEnhanceProxy<AsyncLoadTestService>();
        proxy.setService(new AsyncLoadTestServiceNullImpl());
        proxy.setConfig(config);
        proxy.setExecutor(executor);
        AsyncLoadTestService service = proxy.getProxy();

        List<AsyncLoadTestModel> models = service.listRemoteModel("one", 1000); // 调用list方法，设置返回了为一个空数组
        boolean isNull2 = AsyncLoadUtils.isNull(models);
        Assert.assertFalse(isNull2);
        if (isNull2 == false) { // 如果不为空，可以调用其内部方法
            Assert.assertTrue(models.size() >= 0);
        }
    }

    @Test
    public void testGetOriginalClass() {
        // 初始化config
        AsyncLoadConfig config = new AsyncLoadConfig(3 * 1000l);
        // 初始化executor
        AsyncLoadExecutor executor = new AsyncLoadExecutor(10, 100);
        executor.initital();
        // 初始化proxy
        AsyncLoadEnhanceProxy<AsyncLoadTestService> proxy = new AsyncLoadEnhanceProxy<AsyncLoadTestService>();
        proxy.setService(asyncLoadTestService);
        proxy.setConfig(config);
        proxy.setExecutor(executor);
        AsyncLoadTestService service = proxy.getProxy();

        AsyncLoadTestModel model = service.getRemoteModel("one", 1000); // 方法直接返回了null
        List<AsyncLoadTestModel> models = service.listRemoteModel("one", 1000); // 调用list方法，设置返回了为一个空数组

        long start = System.currentTimeMillis();
        Assert.assertEquals(AsyncLoadUtils.getOriginalClass(model), AsyncLoadTestModel.class);// 是个具体子类
        Assert.assertEquals(AsyncLoadUtils.getOriginalClass(models), List.class);// 是个接口
        long end = System.currentTimeMillis();
        Assert.assertTrue(end - start < 500);// 调用这个方法不会进行阻塞
    }

    @Test
    public void testGetOriginalResult() {
        // 初始化config
        AsyncLoadConfig config = new AsyncLoadConfig(3 * 1000l);
        // 初始化executor
        AsyncLoadExecutor executor = new AsyncLoadExecutor(10, 100);
        executor.initital();
        // 初始化proxy
        AsyncLoadEnhanceProxy<AsyncLoadTestService> proxy = new AsyncLoadEnhanceProxy<AsyncLoadTestService>();
        proxy.setService(asyncLoadTestService);
        proxy.setConfig(config);
        proxy.setExecutor(executor);
        AsyncLoadTestService service = proxy.getProxy();

        AsyncLoadTestModel model = service.getRemoteModel("one", 1000); // 方法直接返回了null
        List<AsyncLoadTestModel> models = service.listRemoteModel("one", 1000); // 调用list方法，设置返回了为一个空数组

        long start = System.currentTimeMillis();
        Assert.assertEquals(AsyncLoadUtils.getOriginalResult(model).getClass(), AsyncLoadTestModel.class);// 是个具体子类
        Assert.assertEquals(AsyncLoadUtils.getOriginalResult(models).getClass(), ArrayList.class);// 真实返回的是个ArrayList
        long end = System.currentTimeMillis();
        Assert.assertTrue(end - start > 500);// 调用这个方法会进行阻塞
    }

    @Test
    public void testGetStatus_Done() {
        // 初始化config
        AsyncLoadConfig config = new AsyncLoadConfig(3 * 1000l);
        // 初始化executor
        AsyncLoadExecutor executor = new AsyncLoadExecutor(10, 100);
        executor.initital();
        // 初始化proxy
        AsyncLoadEnhanceProxy<AsyncLoadTestService> proxy = new AsyncLoadEnhanceProxy<AsyncLoadTestService>();
        proxy.setService(asyncLoadTestService);
        proxy.setConfig(config);
        proxy.setExecutor(executor);
        AsyncLoadTestService service = proxy.getProxy();

        AsyncLoadTestModel model = service.getRemoteModel("one", 1000);
        if (AsyncLoadUtils.isNull(model) == false) {
            AsyncLoadStatus status = AsyncLoadUtils.getStatus(model); // 获取对应的status
            Assert.assertTrue(status.getStatus().isDone());
            Assert.assertTrue(status.getCostTime() > 500 && status.getCostTime() < 1500);
        }

    }

    @Test
    public void testGetStatus_Timeout() {
        // 初始化config
        AsyncLoadConfig config = new AsyncLoadConfig(3 * 1000l);
        // 初始化executor
        AsyncLoadExecutor executor = new AsyncLoadExecutor(10, 100);
        executor.initital();
        // 初始化proxy
        AsyncLoadEnhanceProxy<AsyncLoadTestService> proxy = new AsyncLoadEnhanceProxy<AsyncLoadTestService>();
        proxy.setService(asyncLoadTestService);
        proxy.setConfig(config);
        proxy.setExecutor(executor);
        AsyncLoadTestService service = proxy.getProxy();

        AsyncLoadTestModel model = null;
        try {
            model = service.getRemoteModel("one", 4000);
        } catch (AsyncLoadException e) {
        }
        try {
            AsyncLoadUtils.isNull(model);
            Assert.fail();
        } catch (Exception e) {
            // 因为超时了，所以会出现Timeout异常
        }
        AsyncLoadStatus status = AsyncLoadUtils.getStatus(model); // 获取对应的status
        Assert.assertTrue(status.getStatus().isTimeout());
        Assert.assertTrue(status.getCostTime() > 2500 && status.getCostTime() < 3500);

    }

    @Test
    public void testGetStatus_Run() {
        // 初始化config
        AsyncLoadConfig config = new AsyncLoadConfig(3 * 1000l);
        // 初始化executor
        AsyncLoadExecutor executor = new AsyncLoadExecutor(10, 100);
        executor.initital();
        // 初始化proxy
        AsyncLoadEnhanceProxy<AsyncLoadTestService> proxy = new AsyncLoadEnhanceProxy<AsyncLoadTestService>();
        proxy.setService(asyncLoadTestService);
        proxy.setConfig(config);
        proxy.setExecutor(executor);
        AsyncLoadTestService service = proxy.getProxy();
        final AsyncLoadTestModel model = service.getRemoteModel("one", 2000);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Assert.fail();
        }

        Thread t = new Thread() {

            public void run() {
                long start = System.currentTimeMillis();
                AsyncLoadStatus status = AsyncLoadUtils.getStatus(model); // 获取对应的status
                Assert.assertTrue(status.getStatus().isRun());
                Assert.assertTrue(status.getCostTime() > 500 && status.getCostTime() < 1500); // 当前运行了1000ms左右
                long end = System.currentTimeMillis();
                Assert.assertTrue(end - start < 500);// 调用这个方法不会进行阻塞
            }
        };
        t.start();
    }

    public static class AsyncLoadTestServiceNullImpl extends AsyncLoadTestServiceImpl {

        @Override
        public AsyncLoadTestModel getRemoteModel(String name, long sleep) {
            try {
                Thread.sleep(sleep);
            } catch (InterruptedException e) {
            }
            return null; // 直接mock为空
        }

        @Override
        public List<AsyncLoadTestModel> listRemoteModel(String name, long sleep) {
            try {
                Thread.sleep(sleep);
            } catch (InterruptedException e) {
            }
            return new ArrayList<AsyncLoadTestModel>();
        }
    }

}
