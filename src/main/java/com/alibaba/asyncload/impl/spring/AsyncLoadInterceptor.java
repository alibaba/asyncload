package com.alibaba.asyncload.impl.spring;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import com.alibaba.asyncload.impl.exceptions.AsyncLoadException;
import com.alibaba.asyncload.impl.template.AsyncLoadCallback;
import com.alibaba.asyncload.impl.template.AsyncLoadTemplate;

/**
 * 开发基于拦截器实现的并行加载，注意依赖{@linkplain AsyncLoadTemplate}
 * 
 * <pre>
 * 适用于以下情况：
 * 1. 原先的service对象已经被进行cglib代理，并行加载可以做为其中的一个代理进行织入
 * 2. 希望通过BeanNameAutoProxyCreator进行自动代理配置,不想影响原先的bean配置
 * 
 * 设计注意：
 * 1. 这里依赖{@linkplain AsyncLoadTemplate}进行代码块的并行加载控制，而不能对原先的service进行代理(会产生死循环)
 * 2. 并不直接提供method match的机制，希望是可以直接利用spring提供的PointCut进行控制
 * </pre>
 * 
 * 使用示例配置：
 * 
 * <pre>
 *     <bean class="org.springframework.aop.framework.autoproxy.BeanNameAutoProxyCreator">
 *         <property name="proxyTargetClass" value="true" />
 *         <property name="beanNames">
 *             <list>
 *                 <value>*DataFeeder</value>
 *             </list>
 *         </property>
 *         <property name="interceptorNames">
 *             <list>
 *                 <value>asyncLoadInterceptor</value>
 *             </list>
 *         </property>
 *     </bean>
 * </pre>
 * 
 * @author jianghang 2011-4-1 下午04:52:51
 */
public class AsyncLoadInterceptor implements MethodInterceptor {

    private AsyncLoadTemplate asyncLoadTemplate;

    public Object invoke(MethodInvocation invocation) throws Throwable {
        final MethodInvocation temp = invocation;
        return asyncLoadTemplate.execute(new AsyncLoadCallback() {

            public Object doAsyncLoad() {
                try {
                    return temp.proceed();
                } catch (Throwable e) {
                    throw new AsyncLoadException("AsyncLoadInterceptor invoke error!", e);
                }
            }
        }, invocation.getMethod().getReturnType()); // 这里指定了返回目标class

    }

    // =============== setter / getter =================

    public void setAsyncLoadTemplate(AsyncLoadTemplate asyncLoadTemplate) {
        this.asyncLoadTemplate = asyncLoadTemplate;
    }

}
