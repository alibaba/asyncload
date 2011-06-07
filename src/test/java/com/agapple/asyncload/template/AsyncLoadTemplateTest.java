package com.agapple.asyncload.template;

import javax.annotation.Resource;

import org.junit.Assert;
import org.junit.Test;

import com.agapple.asyncload.BaseAsyncLoadNoRunTest;
import com.agapple.asyncload.domain.AsyncLoadTestModel;
import com.agapple.asyncload.domain.AsyncLoadTestService;
import com.agapple.asyncload.impl.template.AsyncLoadCallback;
import com.agapple.asyncload.impl.template.AsyncLoadTemplate;

/**
 * @author jianghang 2011-1-29 下午07:13:12
 */
public class AsyncLoadTemplateTest extends BaseAsyncLoadNoRunTest {

    @Resource(name = "asyncLoadTemplate")
    private AsyncLoadTemplate    asyncLoadTemplate;

    @Resource(name = "asyncLoadTestService")
    private AsyncLoadTestService asyncLoadTestService;

    @Test
    public void testTemplate() {

        long start = 0, end = 0;
        start = System.currentTimeMillis();

        AsyncLoadTestModel model1 = asyncLoadTestService.getRemoteModel("ljhtest", 1000);
        System.out.println(model1.getDetail());
        end = System.currentTimeMillis();
        System.out.println(end - start);
        Assert.assertTrue((end - start) > 500l); // 第一次会阻塞, 响应时间会在1000ms左右
        Assert.assertTrue((end - start) < 1500l);

        start = System.currentTimeMillis();
        AsyncLoadTestModel model2 = asyncLoadTemplate.execute(new AsyncLoadCallback<AsyncLoadTestModel>() {

            public AsyncLoadTestModel doAsyncLoad() {
                // 总共sleep 2000ms
                return asyncLoadTestService.getRemoteModel("ljhtest", 1000);
            }
        });
        asyncLoadTestService.getRemoteModel("ljhtest", 1000);

        System.out.println(model2.getDetail());
        end = System.currentTimeMillis();
        System.out.println(end - start);
        Assert.assertTrue((end - start) > 500l); // 只会阻塞一次 1000ms
        Assert.assertTrue((end - start) < 1500l);
    }

    @Test
    public void testTemplate_returnClass() {

        long start = 0, end = 0;
        start = System.currentTimeMillis();
        AsyncLoadTestModel model2 = (AsyncLoadTestModel) asyncLoadTemplate.execute(new AsyncLoadCallback() {

            public Object doAsyncLoad() {
                // 总共sleep 1000ms
                return asyncLoadTestService.getRemoteModel("ljhtest", 1000);
            }
        }, AsyncLoadTestModel.class); // 这里指定了返回目标class
        asyncLoadTestService.getRemoteModel("ljhtest", 1000);

        System.out.println(model2.getDetail());
        end = System.currentTimeMillis();
        System.out.println(end - start);
        Assert.assertTrue((end - start) > 500l); // 只会阻塞一次 1000ms
        Assert.assertTrue((end - start) < 1500l);
    }

    @Test
    public void testTemplate_noGeneric() {
        // 没有指定返回对象，会抛异常
        try {
            asyncLoadTemplate.execute(new AsyncLoadCallback() {

                public Object doAsyncLoad() {
                    // 总共sleep 2000ms
                    return asyncLoadTestService.getRemoteModel("ljhtest", 1000);
                }
            });

            Assert.fail();// 不会执行到这一步
        } catch (Exception e) {

        }
    }
}
