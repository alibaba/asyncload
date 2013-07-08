package com.agapple.asyncload.classinfo;

import com.agapple.asyncload.BaseAsyncLoadNoRunTest;
import com.agapple.asyncload.impl.helper.AsyncLoadReflectionHelper;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;
import javassist.util.proxy.ProxyObject;
import net.sf.cglib.reflect.FastMethod;
import org.junit.Test;

import java.lang.reflect.Method;

/**
 * @author jianghang 2011-4-2 下午01:54:16
 */
public class JavassistClassinfoTest extends BaseAsyncLoadNoRunTest {

    @Test
    public void test() throws Exception {
        ProxyFactory proxyFactory = new ProxyFactory();
        proxyFactory.setSuperclass(ClassInfoService.class);
        Class<?> proxyClass = proxyFactory.createClass();
        ClassInfoService javassistProxy = (ClassInfoService) proxyClass.newInstance();
        ((ProxyObject) javassistProxy).setHandler(new JavaAssitInterceptor(new ClassInfoService()));

        javassistProxy.test(new Object());

        FastMethod fm = AsyncLoadReflectionHelper.getMethod(javassistProxy.getClass(), "test",
                                                            new Class[] { Object.class });
        System.out.println(fm.getJavaMethod().getAnnotations().length);
    }

    private static class JavaAssitInterceptor implements MethodHandler {

        final Object delegate;

        JavaAssitInterceptor(Object delegate){
            this.delegate = delegate;
        }

        public Object invoke(Object self, Method m, Method proceed, Object[] args) throws Throwable {
            return m.invoke(delegate, args);
        }
    }
}
