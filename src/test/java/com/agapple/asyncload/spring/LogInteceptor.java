package com.agapple.asyncload.spring;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

/**
 * @author jianghang 2011-4-25 下午03:14:42
 */
public class LogInteceptor implements MethodInterceptor {

    public Object invoke(MethodInvocation invocation) throws Throwable {
        try {
            System.out.println("start invoke:" + invocation.getMethod().getName());
            return invocation.proceed();
        } finally {
            System.out.println("end invoke:" + invocation.getMethod().getName());
        }
    }

}
