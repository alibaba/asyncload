package com.alibaba.asyncload;

import javax.annotation.Resource;

import junit.framework.Assert;

import org.junit.Test;

import com.alibaba.asyncload.domain.AsyncLoadTestModel;
import com.alibaba.asyncload.domain.AsyncLoadTestService;
import com.alibaba.asyncload.impl.template.AsyncLoadCallback;
import com.alibaba.asyncload.impl.template.AsyncLoadTemplate;

/**
 * 测试下ThreadLocal继承
 * 
 * <pre>
 * 在异步加载中对ThreadLocal为只读,尽量不对其set操作，有set操作潜在的分析：
 * 1. 两个并行加载代码块，B依赖A的ThreadLocal设置
 * 2. 并行加载代码快和caller线程存在ThreadLocal依赖，caller线程依赖其ThreadLocal设置
 * 3. 两个并行加载代码块，各自设置了自己的ThreadLocal信息，需要在caller线程进行合并
 * </pre>
 * 
 * @author jianghang 2011-3-28 下午11:00:35
 */
public class AsyncLoadThreadLocalTest extends BaseAsyncLoadNoRunTest {

    @Resource(name = "asyncLoadTemplate")
    private AsyncLoadTemplate         asyncLoadTemplate;

    @Resource(name = "asyncLoadTestService")
    private AsyncLoadTestService      asyncLoadTestService;

    private final ThreadLocal<String> threadLocal                  = new ThreadLocal<String>();
    private final ThreadLocal<String> callerThreadLocal            = new ThreadLocal<String>();
    private final ThreadLocal<String> inheritableThreadLocal       = new InheritableThreadLocal<String>();
    private final ThreadLocal<String> inheritableCallerThreadLocal = new InheritableThreadLocal<String>();

    @Test
    public void testThreadLocalGet() {
        final String name = "testThreadLocal";
        threadLocal.set(name);
        AsyncLoadTestModel model = asyncLoadTemplate.execute(new AsyncLoadCallback<AsyncLoadTestModel>() {

            public AsyncLoadTestModel doAsyncLoad() {
                Assert.assertEquals(threadLocal.get(), name);// 验证threadLocal属性是否和caller Thread设置的一样
                return asyncLoadTestService.getRemoteModel("ljhtest", 1000);
            }
        }, 2000);

        model.getName();// 阻塞至结果返回
    }

    @Test
    public void testThreadLocalRunnerSet() {
        final String name = "testThreadLocal";
        AsyncLoadTestModel model = asyncLoadTemplate.execute(new AsyncLoadCallback<AsyncLoadTestModel>() {

            public AsyncLoadTestModel doAsyncLoad() {
                threadLocal.set(name);// 内部设置了threadLocal,外部能直接获取
                return asyncLoadTestService.getRemoteModel("ljhtest", 1000);
            }
        }, 2000);
        model.getName();// 阻塞至结果返回
        Assert.assertEquals(threadLocal.get(), name);// 验证threadLocal属性是否和runner Thread设置的一样
    }

    @Test
    public void testThreadLocalCallerSet() {
        final String name = "testThreadLocal";
        callerThreadLocal.set(name + 2);
        AsyncLoadTestModel model = asyncLoadTemplate.execute(new AsyncLoadCallback<AsyncLoadTestModel>() {

            public AsyncLoadTestModel doAsyncLoad() {
                Assert.assertEquals(callerThreadLocal.get(), name + 2);// 验证threadLocal属性是否和caller Thread设置的一样
                threadLocal.set(name + 1);// 内部设置了threadLocal,外部能直接获取
                return asyncLoadTestService.getRemoteModel("ljhtest", 1000);
            }
        }, 2000);
        model.getName();// 阻塞至结果返回
        Assert.assertEquals(threadLocal.get(), name + 1);// 验证threadLocal属性是否和runner Thread设置的一样
        System.out.println(callerThreadLocal.get());
        System.out.println(threadLocal.get());
    }

    @Test
    public void testInheritableThreadLocalGet() {
        final String name = "testInheritableThreadLocal";
        inheritableThreadLocal.set(name);
        AsyncLoadTestModel model = asyncLoadTemplate.execute(new AsyncLoadCallback<AsyncLoadTestModel>() {

            public AsyncLoadTestModel doAsyncLoad() {
                Assert.assertEquals(inheritableThreadLocal.get(), name);// 验证threadLocal属性是否和caller Thread设置的一样
                return asyncLoadTestService.getRemoteModel("ljhtest", 1000);
            }
        }, 2000);

        model.getName();// 阻塞至结果返回
    }

    @Test
    public void testInheritableThreadLocalRunnerSet() {
        final String name = "testInheritableThreadLocal";
        AsyncLoadTestModel model = asyncLoadTemplate.execute(new AsyncLoadCallback<AsyncLoadTestModel>() {

            public AsyncLoadTestModel doAsyncLoad() {
                inheritableThreadLocal.set(name);// 内部设置了threadLocal,外部能直接获取
                return asyncLoadTestService.getRemoteModel("ljhtest", 1000);
            }
        }, 2000);
        model.getName();// 阻塞至结果返回
        Assert.assertEquals(inheritableThreadLocal.get(), name);// 验证threadLocal属性是否和runner Thread设置的一样
    }

    @Test
    public void testInheritableThreadLocalCallerSet() {
        final String name = "testInheritableThreadLocal";
        inheritableCallerThreadLocal.set(name + 2);
        AsyncLoadTestModel model = asyncLoadTemplate.execute(new AsyncLoadCallback<AsyncLoadTestModel>() {

            public AsyncLoadTestModel doAsyncLoad() {
                Assert.assertEquals(inheritableCallerThreadLocal.get(), name + 2);// 验证threadLocal属性是否和caller
                // Thread设置的一样
                inheritableThreadLocal.set(name + 1);// 内部设置了threadLocal,外部能直接获取
                return asyncLoadTestService.getRemoteModel("ljhtest", 1000);
            }
        }, 2000);
        model.getName();// 阻塞至结果返回
        Assert.assertEquals(inheritableThreadLocal.get(), name + 1);// 验证threadLocal属性是否和runner Thread设置的一样
        System.out.println(inheritableCallerThreadLocal.get());
        System.out.println(inheritableThreadLocal.get());
    }

    @Test
    public void testThreadLocalMisc() {
        // 处理ThreadLocal和inheritableThreadLocal混合使用，检查是否正确处理数据
        final String name = "testThreadLocalMisc";
        inheritableCallerThreadLocal.set(name + 2);
        AsyncLoadTestModel model = asyncLoadTemplate.execute(new AsyncLoadCallback<AsyncLoadTestModel>() {

            public AsyncLoadTestModel doAsyncLoad() {
                Assert.assertEquals(inheritableCallerThreadLocal.get(), name + 2);// 验证threadLocal属性是否和caller
                // Thread设置的一样
                threadLocal.set(name + 1);// 内部设置了threadLocal,外部能直接获取
                return asyncLoadTestService.getRemoteModel("ljhtest", 1000);
            }
        }, 2000);
        model.getName();// 阻塞至结果返回
        Assert.assertEquals(threadLocal.get(), name + 1);// 验证threadLocal属性是否和runner Thread设置的一样
        System.out.println(inheritableCallerThreadLocal.get());
        System.out.println(threadLocal.get());
    }
}
