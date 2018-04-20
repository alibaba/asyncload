/**
 * AsyncLoadEnableAdvice.java
 * author: yujiakui
 * 2018年4月19日
 * 下午1:52:23
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
import org.springframework.stereotype.Component;

import com.alibaba.asyncload.impl.util.MethodFilterUtil;
import com.google.common.collect.Maps;

/**
 * @author yujiakui
 *
 *         下午1:52:23
 *
 *         异步加载开启切面
 */
@Component
@Aspect
public class AsyncLoadEnableAdvice {

	/** logger */
	private static final Logger LOGGER = LoggerFactory.getLogger(AsyncLoadEnableAdvice.class);

	@Pointcut("@within(com.alibaba.asyncload.impl.annotation.EnableAsyncClass)")
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

		boolean isAsyncFlag = false;

		// 对方法进行过滤
		if (!MethodFilterUtil.filterMethod(pjp)) {
			LOGGER.info(MessageFormat.format("异步并行框架Enable处理开始pjp={0}", pjp.toShortString()));
			// 根据签名获得方法参数
			EnableAsyncClassMethodInfo[] enableAsyncClassMethodInfos = getEnableInfoFromMethod(pjp);
			if (null != enableAsyncClassMethodInfos && enableAsyncClassMethodInfos.length != 0) {
				Map<String, String[]> asynClassMethodInfoMap = Maps.newHashMap();
				for (EnableAsyncClassMethodInfo enableAsyncClassMethodInfo2 : enableAsyncClassMethodInfos) {
					asynClassMethodInfoMap.put(enableAsyncClassMethodInfo2.classFullName(),
							enableAsyncClassMethodInfo2.methodMatchRegex());
				}
				// 向threadLocal中放入对应的值
				AsyncLoadAnnotationThreadLocal.setAsyncLoadMap(asynClassMethodInfoMap);
				isAsyncFlag = true;
			}
		}
		Object result = null;
		try {
			result = pjp.proceed();
		} finally {
			if (isAsyncFlag) {
				// 清除threadLocal中放入的信息
				AsyncLoadAnnotationThreadLocal.removeAsyncLoadMap();
			}
		}

		return result;
	}

	/**
	 * 获取方法上的对应的enable信息，从方法上和类上合并，如果方法上没有则去类上的
	 *
	 * @param pjp
	 * @return
	 */
	private EnableAsyncClassMethodInfo[] getEnableInfoFromMethod(ProceedingJoinPoint pjp) {

		Signature signature = pjp.getSignature();
		MethodSignature methodSignature = (MethodSignature) signature;
		Method method = methodSignature.getMethod();
		EnableAsyncMethod enableAsyncMethod = method.getAnnotation(EnableAsyncMethod.class);
		if (null == enableAsyncMethod) {
			// 从类上面的注解取
			Class<?> originClassObj = pjp.getTarget().getClass();
			EnableAsyncClass enableAsyncClass = originClassObj
					.getAnnotation(EnableAsyncClass.class);
			return enableAsyncClass.classMethodInfos();
		}
		return enableAsyncMethod.classMethodInfos();
	}
}
