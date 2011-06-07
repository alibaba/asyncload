package com.agapple.asyncload;

import junit.framework.Assert;

import org.junit.Test;

import com.agapple.asyncload.impl.AsyncLoadEnhanceProxy;

/**
 * 测试一下final类+接口方式的代理
 * 
 * @author jianghang 2011-3-31 上午11:50:04
 */
public class AsyncLoadFinalClassTest extends BaseAsyncLoadNoRunTest {

    @Test
    public void testStringFinal() {
        // 初始化config
        AsyncLoadConfig config = new AsyncLoadConfig(3 * 1000l);
        // 初始化executor
        AsyncLoadExecutor executor = new AsyncLoadExecutor(10, 100);
        executor.initital();
        // 初始化proxy
        String targer = "1234567890";
        AsyncLoadEnhanceProxy<CharSequence> proxy = new AsyncLoadEnhanceProxy<CharSequence>();
        proxy.setService(targer);
        proxy.setConfig(config);
        proxy.setExecutor(executor);
        // java.util.String对象的接口类，因为String是final对象，所以只能设置代理对应的接口类
        proxy.setTargetClass(CharSequence.class);

        CharSequence ser = (CharSequence) proxy.getProxy();
        Assert.assertEquals(ser.length(), targer.length());
        System.out.println(ser);

        executor.destory();
    }

    @Test
    public void testServiceFinal() {
        // 初始化config
        AsyncLoadConfig config = new AsyncLoadConfig(3 * 1000l);
        // 初始化executor
        AsyncLoadExecutor executor = new AsyncLoadExecutor(10, 100);
        executor.initital();
        // 初始化proxy
        FinalService service = new FinalServiceImpl();
        AsyncLoadEnhanceProxy<FinalService> proxy = new AsyncLoadEnhanceProxy<FinalService>();
        proxy.setService(service);
        proxy.setConfig(config);
        proxy.setExecutor(executor);
        // FinalServiceImpl是final对象，所以只能设置代理对应的接口类
        proxy.setTargetClass(FinalService.class);

        FinalService target = proxy.getProxy();
        int value = 1;
        FinalModel model = target.count(value);
        Assert.assertEquals(model.getCount(), value);
        executor.destory();
    }

    public interface FinalService {

        FinalModel count(int i);
    }

    class FinalModel {

        private int count;

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

    }

    final class FinalServiceImpl implements FinalService {

        public FinalModel count(int i) {
            FinalModel model = new FinalModel();
            model.setCount(i);
            return model;
        }
    }
}
