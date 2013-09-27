package com.alibaba.asyncload;

import java.lang.reflect.Method;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.util.ReflectionUtils;

import com.alibaba.asyncload.impl.AsyncLoadPerl5RegexpMethodMatcher;

/**
 * methodMatch匹配测试
 * 
 * @author jianghang 2011-1-29 下午05:06:25
 */
public class AsyncLoadMethodMatchTest extends BaseAsyncLoadNoRunTest {

    private static final String METHOD4 = "doOtherthing";
    private static final String METHOD3 = "doSomething";
    private static final String METHOD2 = "method2";
    private static final String METHOD1 = "method1";

    @Test
    public void testMatch_include() {
        AsyncLoadPerl5RegexpMethodMatcher matcher = new AsyncLoadPerl5RegexpMethodMatcher();
        matcher.setPatterns(new String[] { METHOD1, METHOD2 });

        Method method1 = ReflectionUtils.findMethod(MethodMatchMock.class, METHOD1);
        Assert.assertTrue(matcher.matches(method1));
        Method method2 = ReflectionUtils.findMethod(MethodMatchMock.class, METHOD2);
        Assert.assertTrue(matcher.matches(method2));
        Method method3 = ReflectionUtils.findMethod(MethodMatchMock.class, METHOD3);
        Assert.assertFalse(matcher.matches(method3));
        Method method4 = ReflectionUtils.findMethod(MethodMatchMock.class, METHOD4);
        Assert.assertFalse(matcher.matches(method4));
    }

    @Test
    public void testMatch_exclude() {
        AsyncLoadPerl5RegexpMethodMatcher matcher = new AsyncLoadPerl5RegexpMethodMatcher();
        matcher.setPatterns(new String[] { METHOD1, METHOD2 });
        matcher.setExcludedPatterns(new String[] { METHOD2, METHOD4 }); // 使用排除必须基于pattern基础上

        Method method1 = ReflectionUtils.findMethod(MethodMatchMock.class, METHOD1);
        Assert.assertTrue(matcher.matches(method1));
        Method method2 = ReflectionUtils.findMethod(MethodMatchMock.class, METHOD2);
        Assert.assertFalse(matcher.matches(method2));
        Method method3 = ReflectionUtils.findMethod(MethodMatchMock.class, METHOD3);
        Assert.assertFalse(matcher.matches(method3));
        Method method4 = ReflectionUtils.findMethod(MethodMatchMock.class, METHOD4);
        Assert.assertFalse(matcher.matches(method4));
    }

    @Test
    public void testMatch_includeOveride() {
        AsyncLoadPerl5RegexpMethodMatcher matcher = new AsyncLoadPerl5RegexpMethodMatcher();
        matcher.setExcludedPatterns(new String[] { METHOD3, METHOD4 });
        matcher.setExcludeOveride(true);

        Method method1 = ReflectionUtils.findMethod(MethodMatchMock.class, METHOD1);
        Assert.assertTrue(matcher.matches(method1));
        Method method2 = ReflectionUtils.findMethod(MethodMatchMock.class, METHOD2);
        Assert.assertTrue(matcher.matches(method2));
        Method method3 = ReflectionUtils.findMethod(MethodMatchMock.class, METHOD3);
        Assert.assertFalse(matcher.matches(method3));
        Method method4 = ReflectionUtils.findMethod(MethodMatchMock.class, METHOD4);
        Assert.assertFalse(matcher.matches(method4));
    }

}

class MethodMatchMock {

    public void method1() {

    }

    public void method2() {

    }

    public void doSomething() {

    }

    public void doOtherthing() {

    }
}
