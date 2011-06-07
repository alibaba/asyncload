package com.agapple.asyncload.impl.spring;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.aopalliance.aop.Advice;
import org.springframework.aop.Advisor;
import org.springframework.aop.framework.AopInfrastructureBean;
import org.springframework.aop.framework.ProxyConfig;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.aop.framework.autoproxy.AbstractAutoProxyCreator;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.transaction.interceptor.TransactionProxyFactoryBean;
import org.springframework.util.ClassUtils;
import org.springframework.util.PatternMatchUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

/**
 * 提供一种机制：无侵入的拦截机制，与现有的bean定义进行融合。实现自定义的{@linkplain BeanPostProcessor}进行替换，部分代码copy from
 * {@linkplain AbstractAutoProxyCreator}
 * 
 * <pre>
 * 融合的规则：
 * 1. 原先的bean是{@linkplain ProxyFactoryBean}，则将自己的拦截器定义和proxy bean的定义进行融合,<strong>只是会合并原先的拦截器定义，其他的不做融合</strong>
 *    可通过applyCommonInterceptorsFirst=true/false指定顺序.如果是false则{@linkplain CompositeAutoProxyCreator}定义的拦截器排在后面
 * 2. 如果原先的bean是除{@linkplain ProxyFactoryBean}的bean，则尝试自动创建ProxyFactoryBean，对应的拦截器也仅是所配置的拦截器列表，不会进行自动的扫描和装配
 * 3. 其他的类似：{@linkplain TransactionProxyFactoryBean}并不会进行一个融合的处理
 * 
 * </pre>
 * 
 * @author jianghang 2011-4-25 上午10:43:12
 */
public class CompositeAutoProxyCreator extends ProxyConfig implements BeanPostProcessor, Ordered, BeanClassLoaderAware, BeanFactoryAware, AopInfrastructureBean {

    private static final long  serialVersionUID             = 8458055362270662345L;
    private static final Field interceptorNamesField        = ReflectionUtils.findField(ProxyFactoryBean.class,
                                                                                        "interceptorNames");

    private ClassLoader        proxyClassLoader             = ClassUtils.getDefaultClassLoader();
    private boolean            classLoaderConfigured        = false;
    private BeanFactory        beanFactory;
    private List               beanNames;
    private int                order                        = Integer.MAX_VALUE;
    private String[]           interceptorNames             = new String[0];
    private boolean            applyCommonInterceptorsFirst = false;
    private final Set          nonAdvisedBeans              = Collections.synchronizedSet(new HashSet());

    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        // 不做处理
        return bean;
    }

    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean != null) {
            Object cacheKey = getCacheKey(bean.getClass(), beanName);
            return wrapIfNecessary(bean, beanName, cacheKey);
        }

        return bean;
    }

    protected Object wrapIfNecessary(Object bean, String beanName, Object cacheKey) {
        if (this.nonAdvisedBeans.contains(cacheKey)) {
            return bean;
        }
        if (isInfrastructureClass(bean.getClass())) {
            this.nonAdvisedBeans.add(cacheKey);
            return bean;
        }
        // 不能进行代理cache，singleton的实现有spring core核心机制来保证，如果是singleton不会回调多次
        // Create proxy if we have advice.
        if (this.beanNames != null) {
            for (Iterator it = this.beanNames.iterator(); it.hasNext();) {
                String mappedName = (String) it.next();
                if (isMatch(beanName, mappedName)) {
                    if (ProxyFactoryBean.class.isAssignableFrom(bean.getClass())) {
                        ProxyFactoryBean proxyFactoryBean = (ProxyFactoryBean) bean;
                        String[] orignInterceptorNames = getInterceptorFromProxyFactoryBean(proxyFactoryBean);
                        String[] newInterceptorNames = new String[orignInterceptorNames.length
                                                                  + interceptorNames.length];
                        if (applyCommonInterceptorsFirst) {// 如果是true，则将Auto-proxy的拦截器定义到最前面
                            // 构造新的的拦截器列表
                            System.arraycopy(interceptorNames, 0, newInterceptorNames, 0, interceptorNames.length);
                            System.arraycopy(orignInterceptorNames, 0, newInterceptorNames, interceptorNames.length,
                                             orignInterceptorNames.length);
                        } else {
                            System.arraycopy(orignInterceptorNames, 0, newInterceptorNames, 0,
                                             orignInterceptorNames.length);
                            System.arraycopy(interceptorNames, 0, newInterceptorNames, orignInterceptorNames.length,
                                             interceptorNames.length);
                        }
                        // 重新设置新的inteceptorNames
                        proxyFactoryBean.setInterceptorNames(newInterceptorNames);
                        return proxyFactoryBean;
                    } else {
                        // 如果是单例，对应的代理bean对象为同一个
                        ProxyFactoryBean proxyFactoryBean = new ProxyFactoryBean();
                        proxyFactoryBean.setBeanFactory(beanFactory);
                        proxyFactoryBean.setBeanClassLoader(proxyClassLoader);
                        proxyFactoryBean.setInterceptorNames(interceptorNames);
                        proxyFactoryBean.copyFrom(this); // 拷贝对应的一些Proxy config
                        proxyFactoryBean.setTarget(bean);
                        return proxyFactoryBean.getObject();
                    }
                }
            }
        }

        this.nonAdvisedBeans.add(cacheKey);
        return bean;
    }

    // =========================== helper method ================================

    private String[] getInterceptorFromProxyFactoryBean(ProxyFactoryBean bean) {
        synchronized (interceptorNamesField) {
            try {
                interceptorNamesField.setAccessible(true);
                try {
                    Object obj = interceptorNamesField.get(bean);
                    return obj != null ? (String[]) obj : new String[0];
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } finally {
                interceptorNamesField.setAccessible(false);
            }
        }

    }

    /**
     * 对应的内存cache的key
     */
    protected Object getCacheKey(Class beanClass, String beanName) {
        return beanClass.getName() + "_" + beanName;
    }

    /**
     * 不对基础的框架类做auto-proxy
     * 
     * @param beanClass
     * @return
     */
    protected boolean isInfrastructureClass(Class beanClass) {
        return Advisor.class.isAssignableFrom(beanClass) || Advice.class.isAssignableFrom(beanClass)
               || AopInfrastructureBean.class.isAssignableFrom(beanClass);
    }

    /**
     * 返回是否匹配，支持简单的通配符： "xxx*", "*xxx" "*xxx*"
     */
    protected boolean isMatch(String beanName, String mappedName) {
        return PatternMatchUtils.simpleMatch(mappedName, beanName);
    }

    // ========================= setter / getter ===========================

    public final void setOrder(int order) {
        this.order = order;
    }

    public final int getOrder() {
        return this.order;
    }

    public void setInterceptorNames(String[] interceptorNames) {
        this.interceptorNames = interceptorNames;
    }

    public void setApplyCommonInterceptorsFirst(boolean applyCommonInterceptorsFirst) {
        this.applyCommonInterceptorsFirst = applyCommonInterceptorsFirst;
    }

    public void setBeanClassLoader(ClassLoader classLoader) {
        if (!this.classLoaderConfigured) {
            this.proxyClassLoader = classLoader;
        }
    }

    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    public void setBeanNames(String[] beanNames) {
        this.beanNames = new ArrayList(beanNames.length);
        for (int i = 0; i < beanNames.length; i++) {
            this.beanNames.add(StringUtils.trimWhitespace(beanNames[i]));
        }
    }

}
