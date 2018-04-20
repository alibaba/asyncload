/**
 * AsyncEnableAnnotationParserFactory.java
 * author: yujiakui
 * 2018年4月19日
 * 下午1:56:02
 */
package com.alibaba.asyncload.impl.annotation;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * @author yujiakui
 *
 *         下午1:56:02
 *
 *         异步开启注解解析器工厂
 */
public class AsyncEnableAnnotationParserFactory
		implements ApplicationContextAware, InitializingBean {

	/* (non-Javadoc)
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
	 */
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		// TODO Auto-generated method stub

	}

}
