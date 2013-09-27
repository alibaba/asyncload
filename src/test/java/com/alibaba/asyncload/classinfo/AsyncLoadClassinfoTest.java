package com.alibaba.asyncload.classinfo;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

import org.junit.Assert;
import org.junit.Test;

import com.alibaba.asyncload.AsyncLoadConfig;
import com.alibaba.asyncload.AsyncLoadExecutor;
import com.alibaba.asyncload.BaseAsyncLoadNoRunTest;
import com.alibaba.asyncload.impl.AsyncLoadEnhanceProxy;

/**
 * 测试下代理下的annotation,generic,field属性
 * 
 * @author jianghang 2011-4-1 下午11:58:25
 */
public class AsyncLoadClassinfoTest extends BaseAsyncLoadNoRunTest {

    @Test
    public void testClassAnnotation_lose() {
        ClassInfoService service = getProxy();
        Annotation[] as = service.getClass().getAnnotations();
        Assert.assertEquals(as.length, 1);// 因为设置了Classi为允许继承，所以能获得
    }

    @Test
    public void testField_notFound() {
        ClassInfoService service = getProxy();
        try {
            service.getClass().getDeclaredField("ser");// 属性丢失，无法找到
            Assert.fail();// 不会走到这一步
        } catch (Exception e) {
        }
    }

    @Test
    public void testMethodAnnotation_lose() {

        ClassInfoService service = getProxy();
        try {
            Method method = service.getClass().getMethod("test", new Class[] { Object.class });
            Annotation[] as = method.getAnnotations();
            Assert.assertEquals(as.length, 0);
            Annotation[][] ass = method.getParameterAnnotations();
            Assert.assertEquals(ass.length, 1);// 有1个参数
            Assert.assertEquals(ass[0].length, 0);// 这个参数没有annotation
        } catch (Exception e) {
            Assert.fail();
        }

    }

    @Test
    public void testMethodGeneric_lose() {
        ClassInfoService service = getProxy();
        try {
            Method setMethod = service.getClass().getMethod("setSer", new Class[] { Serializable.class });
            Type[] parameters = setMethod.getGenericParameterTypes();
            Assert.assertFalse(parameters[0] instanceof TypeVariable); // 不是一个泛型对象
            Method getMethod = service.getClass().getMethod("getSer", new Class[] {});
            Type returnType = getMethod.getGenericReturnType();
            Assert.assertFalse(returnType instanceof TypeVariable); // 不是一个泛型对象
        } catch (Exception e) {
            Assert.fail();
        }

    }

    @Test
    public void testSuperClassAnnotation_ok() {
        ClassInfoService service = getProxy();
        Annotation[] as = service.getClass().getSuperclass().getAnnotations();
        Assert.assertEquals(as.length, 1);// 因为设置了Classi为允许继承，所以能获得
    }

    @Test
    public void testSuperFieldAnnotation_ok() {
        ClassInfoService service = getProxy();
        try {
            Field field = service.getClass().getSuperclass().getDeclaredField("ser");// 属性丢失，无法找到
            Annotation[] as = field.getAnnotations();
            Assert.assertEquals(as.length, 1);
        } catch (Exception e) {
            Assert.fail();// 不会走到这一步
        }
    }

    @Test
    public void testSuperMethodAnnotation_ok() {

        ClassInfoService service = getProxy();
        try {
            Method method = service.getClass().getSuperclass().getMethod("test", new Class[] { Object.class });
            Annotation[] as = method.getAnnotations();
            Assert.assertEquals(as.length, 1);
            Annotation[][] ass = method.getParameterAnnotations();
            Assert.assertEquals(ass.length, 1);// 有1个参数
            Assert.assertEquals(ass[0].length, 1);// 有1个annotation
        } catch (Exception e) {
            Assert.fail();
        }

    }

    @Test
    public void testSuperMethodGeneric_ok() {
        ClassInfoService service = getProxy();
        try {
            Method setMethod = service.getClass()
                .getSuperclass()
                .getMethod("setSer", new Class[] { Serializable.class });
            Type[] parameters = setMethod.getGenericParameterTypes();
            Assert.assertTrue(parameters[0] instanceof TypeVariable); // 是一个泛型对象
            Method getMethod = service.getClass().getSuperclass().getMethod("getSer", new Class[] {});
            Type returnType = getMethod.getGenericReturnType();
            Assert.assertTrue(returnType instanceof TypeVariable); // 是一个泛型对象
        } catch (Exception e) {
            Assert.fail();
        }
    }

    private ClassInfoService getProxy() {
        // 初始化config
        AsyncLoadConfig config = new AsyncLoadConfig(3 * 1000l);
        // 初始化executor
        AsyncLoadExecutor executor = new AsyncLoadExecutor(10, 100);
        executor.initital();
        // 初始化proxy
        AsyncLoadEnhanceProxy<ClassInfoService> proxy = new AsyncLoadEnhanceProxy<ClassInfoService>();
        proxy.setService(new ClassInfoService());
        proxy.setConfig(config);
        proxy.setExecutor(executor);
        return proxy.getProxy();
    }
}
