/**
 * AsyncLoadHandlerAop.java
 * author: yujiakui
 * 2018年4月17日
 * 下午6:44:08
 */
package com.alibaba.asyncload.impl.annotation;

import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.Map;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.alibaba.asyncload.impl.AsyncLoadPerl5RegexpMethodMatcher;
import com.alibaba.asyncload.impl.util.MethodFilterUtil;

/**
 * @author yujiakui
 *
 *         下午6:44:08
 *
 *         异步加载处理aop
 *
 */
// 使用cglib代理生成目标类
@EnableAspectJAutoProxy(proxyTargetClass = true)
@Component
@Aspect
public class AsyncLoadHandlerAdvice {

	/** logger */
	private static final Logger LOGGER = LoggerFactory.getLogger(AsyncLoadHandlerAdvice.class);

	/** 异步加载处理工厂类 */
	@Autowired
	private AsyncLoadHandleFactory asyncLoadHandleFactory;

	@Pointcut("@within(com.alibaba.asyncload.impl.annotation.AsyncClassDef)")
	public void aspectjMethod() {
	}

	/**
	 * Around 手动控制调用核心业务逻辑，以及调用前和调用后的处理,
	 *
	 * @param pjp
	 * @return
	 * @throws Throwable
	 */
	@Around(value = "aspectjMethod()")
	public Object aroundAdvice(ProceedingJoinPoint pjp) throws Throwable {
		// 对方法进行过滤
		if (MethodFilterUtil.filterMethod(pjp) || !isNeedAsyncLoad(pjp)) {
			return pjp.proceed();
		} else {
			LOGGER.info(MessageFormat.format("异步并行框架处理开始pjp={0}", pjp.toShortString()));
			return asyncLoadHandleFactory.handle(pjp);
		}
	}

	/**
	 * 是否需要异步处理
	 *
	 * @param pjp
	 * @return
	 */
	private boolean isNeedAsyncLoad(ProceedingJoinPoint pjp) {

		Map<String, String[]> classMethodMatchMap = AsyncLoadAnnotationThreadLocal
				.getAsyncLoadMap();
		if (!CollectionUtils.isEmpty(classMethodMatchMap)) {
			Class<?> originClassObj = pjp.getTarget().getClass();
			String[] methodRegexMatchs = classMethodMatchMap.get(originClassObj.getName());
			// 获取通用类
			if (null == methodRegexMatchs) {
				methodRegexMatchs = classMethodMatchMap
						.get(AsyncLoadAnnotationConstants.ALL_CLASSES);
			}
			if (null != methodRegexMatchs && methodRegexMatchs.length != 0) {
				// 根据签名获得方法参数
				Signature signature = pjp.getSignature();
				MethodSignature methodSignature = (MethodSignature) signature;
				Method method = methodSignature.getMethod();

				AsyncLoadPerl5RegexpMethodMatcher regexMethodMatcher = new AsyncLoadPerl5RegexpMethodMatcher();
				regexMethodMatcher.setPatterns(methodRegexMatchs);
				return regexMethodMatcher.matches(method);
			}

		}
		return false;
	}
}
