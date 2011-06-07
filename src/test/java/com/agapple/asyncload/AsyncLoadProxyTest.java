package com.agapple.asyncload;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

import javax.annotation.Resource;

import junit.framework.Assert;

import org.junit.Test;

import com.agapple.asyncload.domain.AsyncLoadTestModel;
import com.agapple.asyncload.domain.AsyncLoadTestService;
import com.agapple.asyncload.impl.AsyncLoadEnhanceProxy;

public class AsyncLoadProxyTest extends BaseAsyncLoadNoRunTest {

    @Resource(name = "asyncLoadTestService")
    private AsyncLoadTestService asyncLoadTestService;

    @Test
    public void testProxy() {
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
        // 执行测试
        AsyncLoadTestService service = proxy.getProxy();
        AsyncLoadTestModel model1 = service.getRemoteModel("first", 1000); // 每个请求sleep 1000ms
        AsyncLoadTestModel model2 = service.getRemoteModel("two", 1000); // 每个请求sleep 1000ms
        AsyncLoadTestModel model3 = service.getRemoteModel("three", 1000); // 每个请求sleep 1000ms

        long start = 0, end = 0;
        start = System.currentTimeMillis();
        System.out.println(model1.getDetail());
        end = System.currentTimeMillis();
        Assert.assertTrue((end - start) > 500l); // 第一次会阻塞, 响应时间会在1000ms左右

        start = System.currentTimeMillis();
        System.out.println(model2.getDetail());
        end = System.currentTimeMillis();
        Assert.assertTrue((end - start) < 500l); // 第二次不会阻塞，因为第一个已经阻塞了1000ms

        start = System.currentTimeMillis();
        System.out.println(model3.getDetail());
        end = System.currentTimeMillis();
        Assert.assertTrue((end - start) < 500l); // 第三次不会阻塞，因为第一个已经阻塞了1000ms

        // 销毁executor
        executor.destory();
    }

    @Test
    public void testProxy_timeout() {
        // 初始化config
        AsyncLoadConfig config = new AsyncLoadConfig(3 * 100l); // 设置超时时间为300ms
        // 初始化executor
        AsyncLoadExecutor executor = new AsyncLoadExecutor(10, 100);
        executor.initital();
        // 初始化proxy
        AsyncLoadEnhanceProxy<AsyncLoadTestService> proxy = new AsyncLoadEnhanceProxy<AsyncLoadTestService>();
        proxy.setService(asyncLoadTestService);
        proxy.setConfig(config);
        proxy.setExecutor(executor);

        AsyncLoadTestService service = proxy.getProxy();
        AsyncLoadTestModel model1 = service.getRemoteModel("first", 1000); // 每个请求sleep 1000ms
        AsyncLoadTestModel model2 = service.getRemoteModel("two", 200); // 每个请求sleep 1000ms

        long start = 0, end = 0;
        start = System.currentTimeMillis();
        try {
            System.out.println(model1.getDetail());
            Assert.fail(); // 不会走到这一步
        } catch (Exception e) { // TimeoutException异常
            System.out.println(e);
        }
        end = System.currentTimeMillis();
        Assert.assertTrue((end - start) < 500l); // 会超时

        start = System.currentTimeMillis();
        try {
            System.out.println(model2.getDetail());
        } catch (Exception e) {
            Assert.fail(); // 不会走到这一步
        }
        end = System.currentTimeMillis();
        Assert.assertTrue((end - start) < 500l); // 不会超时
    }

    @Test
    public void testProxy_block_reject() {
        // 初始化config
        AsyncLoadConfig config = new AsyncLoadConfig(3 * 1000l); // 设置超时时间为300ms
        // 初始化executor
        AsyncLoadExecutor executor = new AsyncLoadExecutor(8, 2, AsyncLoadExecutor.HandleMode.REJECT); // 设置为拒绝,8个工作线程,2个等待队列
        executor.initital();
        // 初始化proxy
        AsyncLoadEnhanceProxy<AsyncLoadTestService> proxy = new AsyncLoadEnhanceProxy<AsyncLoadTestService>();
        proxy.setService(asyncLoadTestService);
        proxy.setConfig(config);
        proxy.setExecutor(executor);

        AsyncLoadTestService service = proxy.getProxy();
        ExecutorService executeService = Executors.newFixedThreadPool(10);
        long start = 0, end = 0;
        start = System.currentTimeMillis();
        try {
            for (int i = 0; i < 10; i++) { // 创建10个任务
                final AsyncLoadTestModel model = service.getRemoteModel("first:" + i, 1000); // 每个请求sleep 1000ms
                executeService.submit(new Runnable() {

                    public void run() {
                        System.out.println(model.getDetail());
                    }
                });
            }
        } catch (RejectedExecutionException e) { // 不会出现reject
            Assert.fail();
        }

        try {
            final AsyncLoadTestModel model = service.getRemoteModel("first:" + 11, 1000); // 创建第11个任务，会出现reject异常
            executeService.submit(new Runnable() {

                public void run() {
                    System.out.println(model.getDetail());
                }
            });

            Assert.fail();// 不会走到这一步
        } catch (RejectedExecutionException e) {
            System.out.println(e);// 会出现reject
        }

        try {
            Thread.sleep(2000l);
        } catch (InterruptedException e) {
            Assert.fail();
        }
        executeService.shutdown();
        end = System.currentTimeMillis();
        System.out.println(end - start);
    }

    @Test
    public void testProxy_block_reject_noQueue() {
        // 初始化config
        AsyncLoadConfig config = new AsyncLoadConfig(3 * 1000l); // 设置超时时间为3000ms
        // 初始化executor
        AsyncLoadExecutor executor = new AsyncLoadExecutor(2, 0, AsyncLoadExecutor.HandleMode.REJECT); // 设置为拒绝
        executor.initital();
        // 初始化proxy
        AsyncLoadEnhanceProxy<AsyncLoadTestService> proxy = new AsyncLoadEnhanceProxy<AsyncLoadTestService>();
        proxy.setService(asyncLoadTestService);
        proxy.setConfig(config);
        proxy.setExecutor(executor);

        AsyncLoadTestService service = proxy.getProxy();
        ExecutorService executeService = Executors.newFixedThreadPool(10);
        long start = 0, end = 0;
        start = System.currentTimeMillis();
        try {
            for (int i = 0; i < 5; i++) { // 创建5个任务
                final AsyncLoadTestModel model = service.getRemoteModel("first:" + i, 1000); // 每个请求sleep 1000ms
                executeService.submit(new Runnable() {

                    public void run() {
                        System.out.println(model.getDetail());
                    }
                });
            }

            Assert.fail(); // 不会走到这一步
        } catch (RejectedExecutionException e) { // 会出现reject
            System.out.println(e);// 会出现reject
        }

        try {
            Thread.sleep(2000l);
        } catch (InterruptedException e) {
            Assert.fail();
        }
        executeService.shutdown();
        end = System.currentTimeMillis();
        System.out.println(end - start);
    }

    @Test
    public void testProxy_block_callerRun() {
        // 初始化config
        AsyncLoadConfig config = new AsyncLoadConfig(3 * 1000l); // 设置超时时间为3000ms
        // 初始化executor
        AsyncLoadExecutor executor = new AsyncLoadExecutor(1, 0, AsyncLoadExecutor.HandleMode.CALLERRUN); // 设置为caller线程运行模式,10个工作线程,0个等待队列
        executor.initital();
        // 初始化proxy
        AsyncLoadEnhanceProxy<AsyncLoadTestService> proxy = new AsyncLoadEnhanceProxy<AsyncLoadTestService>();
        proxy.setService(asyncLoadTestService);
        proxy.setConfig(config);
        proxy.setExecutor(executor);

        AsyncLoadTestService service = proxy.getProxy();
        ExecutorService executeService = Executors.newFixedThreadPool(2);
        long start = 0, end = 0;
        start = System.currentTimeMillis();
        try {
            for (int i = 0; i < 3; i++) { // 创建10个任务
                final AsyncLoadTestModel model = service.getRemoteModel("first:" + i, 1000); // 每个请求sleep 1000ms
                executeService.submit(new Runnable() {

                    public void run() {
                        System.out.println(model.getDetail());
                    }
                });
            }

            Thread.sleep(4000l);
        } catch (RejectedExecutionException e) { // 不会出现reject
            Assert.fail();
        } catch (InterruptedException e) {
            Assert.fail();
        }

        executeService.shutdown();
        end = System.currentTimeMillis();
        System.out.println(end - start);
    }
}
