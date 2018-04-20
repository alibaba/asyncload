/**
 * AsyncLoadHandleFactory.java
 * author: yujiakui
 * 2018年4月17日
 * 下午6:48:35
 */
package com.alibaba.asyncload.impl.annotation;

import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.alibaba.asyncload.AsyncLoadMethodMatch;
import com.alibaba.asyncload.impl.template.AsyncLoadTemplate;
import com.google.common.collect.Maps;

/**
 * @author yujiakui
 *
 *         下午6:48:35
 *
 *         异步处理加载工厂
 */
@Component
public class AsyncLoadHandleFactory {

	/** 日志 */
	private final static Logger LOGGER = LoggerFactory.getLogger(AsyncLoadHandleFactory.class);

	/** 异步注解解析工厂 */
	@Autowired
	private AsyncAnnotationParserFactory asyncAnnotationParserFactory;

	/**
	 * 拦截方法对应的处理
	 *
	 * @param pjp
	 * @return
	 * @throws Throwable
	 */
	public Object handle(ProceedingJoinPoint pjp) throws Throwable {
		// 根据签名获得方法参数
		Signature signature = pjp.getSignature();
		MethodSignature methodSignature = (MethodSignature) signature;
		Method method = methodSignature.getMethod();

		// Class<?> originClassObj =
		// AopProxyUtils.ultimateTargetClass(pjp.getThis());
		Class<?> originClassObj = pjp.getTarget().getClass();

		// 获得对应的拦截类名
		String classFullName = originClassObj.getName();

		// 1. 先从模板中获取
		HandleResult handleResult = handleMethodTemplate(pjp, method, classFullName);
		if (handleResult.isSuccess()) {
			return handleResult.getResult();
		}

		// 2. 在从方法正则表达式中进行匹配
		handleResult = methodMatchAsyncHandle(pjp, method, classFullName);
		if (handleResult.isSuccess()) {
			return handleResult.getResult();
		}

		return pjp.proceed();

	}

	/**
	 * @param pjp
	 * @param method
	 * @param classFullName
	 */
	private HandleResult methodMatchAsyncHandle(ProceedingJoinPoint pjp, Method method,
			String classFullName) {
		List<AsyncLoadTemplate> asyncLoadTemplates = asyncAnnotationParserFactory
				.getMethodConfExecMap().get(classFullName);
		if (!CollectionUtils.isEmpty(asyncLoadTemplates)) {
			for (AsyncLoadTemplate asyncLoadTemplate : asyncLoadTemplates) {
				// 获得对应的matcher
				Map<AsyncLoadMethodMatch, Long> matchMap = asyncLoadTemplate.getConfig()
						.getMatches();
				// 对应的配置中只能有一个match，所以仅仅去一个
				for (Map.Entry<AsyncLoadMethodMatch, Long> entry : matchMap.entrySet()) {
					AsyncLoadMethodMatch match = entry.getKey();
					// 方法匹配标记
					boolean matchFlag = match.matches(method);
					if (matchFlag) {
						updateMethodAsyncTemplate(method, classFullName, asyncLoadTemplate);
						return new HandleResult(asyncLoadTemplate.execute(pjp), true);
					}
				}
			}
		}
		return new HandleResult(null, false);
	}

	/**
	 * @param method
	 * @param classFullName
	 * @param asyncLoadTemplate
	 */
	private void updateMethodAsyncTemplate(Method method, String classFullName,
			AsyncLoadTemplate asyncLoadTemplate) {
		Map<String, Map<Method, AsyncLoadTemplate>> methodAsyncTemplateTable = asyncAnnotationParserFactory
				.getMethodAsyncTemplateTable();
		Map<Method, AsyncLoadTemplate> methodAsyncTemplateMap = methodAsyncTemplateTable
				.get(classFullName);
		if (CollectionUtils.isEmpty(methodAsyncTemplateMap)) {
			methodAsyncTemplateMap = Maps.newConcurrentMap();
			methodAsyncTemplateTable.put(classFullName, methodAsyncTemplateMap);
		}
		methodAsyncTemplateMap.put(method, asyncLoadTemplate);

		LOGGER.info(
				MessageFormat.format("异步并行加载在table中增加对应的方法table={0}", methodAsyncTemplateTable));
	}

	/**
	 * @param pjp
	 * @param method
	 * @param classFullName
	 */
	private HandleResult handleMethodTemplate(ProceedingJoinPoint pjp, Method method,
			String classFullName) {
		Map<String, Map<Method, AsyncLoadTemplate>> methodAsyncTemplateTable = asyncAnnotationParserFactory
				.getMethodAsyncTemplateTable();
		Map<Method, AsyncLoadTemplate> methodTemplateMap = methodAsyncTemplateTable
				.get(classFullName);
		if (!CollectionUtils.isEmpty(methodTemplateMap)) {
			AsyncLoadTemplate asyncLoadTemplate = methodTemplateMap.get(method);
			if (null != asyncLoadTemplate) {
				LOGGER.info(
						MessageFormat.format("执行异步并行方法{0}对应template={1}", pjp, asyncLoadTemplate));
				return new HandleResult(asyncLoadTemplate.execute(pjp), true);
			}
		}
		return new HandleResult(null, false);
	}

	class HandleResult {
		private boolean success;
		private Object result;

		public HandleResult(Object result, boolean success) {
			this.result = result;
			this.success = success;
		}

		/**
		 * @return the success
		 */
		public boolean isSuccess() {
			return success;
		}

		/**
		 * @param success
		 *            the success to set
		 */
		public void setSuccess(boolean success) {
			this.success = success;
		}

		/**
		 * @return the result
		 */
		public Object getResult() {
			return result;
		}

		/**
		 * @param result
		 *            the result to set
		 */
		public void setResult(Object result) {
			this.result = result;
		}

	}
}
