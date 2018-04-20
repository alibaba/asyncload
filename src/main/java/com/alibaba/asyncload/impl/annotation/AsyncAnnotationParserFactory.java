/**
 * AsyncAnnotationParserFactory.java
 * author: yujiakui
 * 2018年4月17日
 * 下午4:26:50
 */
package com.alibaba.asyncload.impl.annotation;

import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.alibaba.asyncload.AsyncLoadConfig;
import com.alibaba.asyncload.AsyncLoadExecutor;
import com.alibaba.asyncload.AsyncLoadMethodMatch;
import com.alibaba.asyncload.impl.AsyncLoadPerl5RegexpMethodMatcher;
import com.alibaba.asyncload.impl.exceptions.AsyncLoadException;
import com.alibaba.asyncload.impl.template.AsyncLoadTemplate;
import com.alibaba.asyncload.impl.util.MethodFilterUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * @author yujiakui
 *
 *         下午4:26:50
 *
 *         异步注解解析
 */
@Component
public class AsyncAnnotationParserFactory implements ApplicationContextAware, InitializingBean {

	/** 日志 */
	private final static Logger LOGGER = LoggerFactory
			.getLogger(AsyncAnnotationParserFactory.class);

	/** spring 应用程序上下文 */
	private ApplicationContext applicationContext;

	/** 类中方法对应的异步模板表：String对应的类全名 */
	private Map<String, Map<Method, AsyncLoadTemplate>> methodAsyncTemplateTable = Maps
			.newConcurrentMap();

	/** 类中的方法配置执行器，其中String对应的是类全名 */
	private Map<String, List<AsyncLoadTemplate>> methodConfExecMap = Maps.newConcurrentMap();

	/** 从配置文件中读取，如果没有读取到默认是20 */
	@Value("${asyncLoad.poolSize:20}")
	private int poolSize;

	/** 线程池大小，默认是100 */
	@Value("${aysncLoad.queueSize:100}")
	private int queueSize;

	/** 线程池拒绝策略，默认是在主线程中执行 */
	@Value("${aysncLoad.rejectPolicy:CALLERRUN}")
	private String rejectPolicy;

	/* (non-Javadoc)
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		Map<String, Object> asyncLoadBeanMap = applicationContext
				.getBeansWithAnnotation(AsyncClassDef.class);

		// 遍历对应的对象填充对应的map
		for (Object beanObj : asyncLoadBeanMap.values()) {
			// 因为被拦截器拦住，使用cglib进行代理，所有需要获得对应的原始类
			Class<?> originBeanClass = AopProxyUtils.ultimateTargetClass(beanObj);

			// 解析类上面的注解
			AsyncLoadExecutor asyncLoadExecutor = parseClassAnnotation(originBeanClass);
			// 解析方法上面的注解
			parseMethodAnnotation(originBeanClass, asyncLoadExecutor);
		}

		LOGGER.info(MessageFormat.format(
				"异步并行加载解析对应的结果methodAsyncTemplateTable={0},methodConfExecMap={1}",
				methodAsyncTemplateTable, methodConfExecMap));
	}

	/**
	 * 解析方法注解
	 *
	 * @param originBeanClasss
	 */
	private void parseMethodAnnotation(Class<?> originBeanClasss,
			AsyncLoadExecutor parentExecutor) {

		Map<Method, AsyncLoadTemplate> asyncLoadTemplateMap = methodAsyncTemplateTable
				.get(originBeanClasss.getName());
		if (null != asyncLoadTemplateMap) {
			throw new AsyncLoadException(
					MessageFormat.format("类名className={0}对应的配置在table中已经存在,table={1}",
							originBeanClasss.getName(), methodAsyncTemplateTable));
		} else {
			asyncLoadTemplateMap = Maps.newConcurrentMap();
		}
		Method[] methods = originBeanClasss.getMethods();
		for (Method method : methods) {
			// 排除一些方法，排除Object对应的方法，仅仅需要public方法
			if (MethodFilterUtil.filterMethod(method)) {
				continue;
			}

			// 方法上的注解
			AsyncMethodDef asyncMethod = method.getDeclaredAnnotation(AsyncMethodDef.class);
			if (null != asyncMethod) {

				AsyncLoadTemplate asyncLoadTemplate = assembleAsyncLoadTemplate(asyncMethod,
						parentExecutor, new String[] { method.getName() });
				asyncLoadTemplateMap.put(method, asyncLoadTemplate);
			}
		}

		if (!CollectionUtils.isEmpty(asyncLoadTemplateMap)) {
			methodAsyncTemplateTable.put(originBeanClasss.getName(), asyncLoadTemplateMap);
		}
	}

	/**
	 * 组装异步加载模板
	 *
	 * @param asyncMethod
	 * @param parentExecutor
	 * @return
	 */
	private AsyncLoadTemplate assembleAsyncLoadTemplate(AsyncMethodDef asyncMethod,
			AsyncLoadExecutor parentExecutor, String[] overrideMethodMatchRegexs) {

		AsyncLoadConfig asyncLoadConfig = new AsyncLoadConfig();
		asyncLoadConfig.setDefaultTimeout(asyncMethod.timeout());
		asyncLoadConfig.setNeedThreadLocalSupport(asyncMethod.inheritThreadLocal());
		AsyncLoadPerl5RegexpMethodMatcher asyncLoadPerl5RegexpMethodMatcher = new AsyncLoadPerl5RegexpMethodMatcher();
		if (null == overrideMethodMatchRegexs || overrideMethodMatchRegexs.length == 0) {
			asyncLoadPerl5RegexpMethodMatcher.setPatterns(asyncMethod.methodMatchRegex());
			asyncLoadPerl5RegexpMethodMatcher
					.setExcludedPatterns(asyncMethod.excludeMethodMatchRegex());
		} else {
			asyncLoadPerl5RegexpMethodMatcher.setPatterns(overrideMethodMatchRegexs);
		}
		Map<AsyncLoadMethodMatch, Long> methodMatchMap = Maps.newHashMap();
		methodMatchMap.put(asyncLoadPerl5RegexpMethodMatcher, asyncMethod.timeout());
		asyncLoadConfig.setMatches(methodMatchMap);

		AsyncLoadExecutor methodAsyncExecutor = parentExecutor;
		AsyncThreadPoolConfig methodAsyncThreadPoolConfig = asyncMethod.methodThreadPoolConf();
		if (methodAsyncThreadPoolConfig.effect()) {
			methodAsyncExecutor = new AsyncLoadExecutor();
			methodAsyncExecutor.setAcceptCount(methodAsyncThreadPoolConfig.queueSize());
			methodAsyncExecutor.setMode(methodAsyncThreadPoolConfig.rejectPolicy());
			methodAsyncExecutor.setPoolSize(methodAsyncThreadPoolConfig.poolSize());
			// TODO 调用初始化生成线程池
			// methodAsyncExecutor.initital();
		}

		AsyncLoadTemplate asyncLoadTemplate = new AsyncLoadTemplate();
		asyncLoadTemplate.setConfig(asyncLoadConfig);
		asyncLoadTemplate.setExecutor(methodAsyncExecutor);

		return asyncLoadTemplate;
	}

	/**
	 * 类上面的注解解析
	 *
	 * @param originBeanClass
	 */
	private AsyncLoadExecutor parseClassAnnotation(Class<?> originBeanClass) {
		// 获取AsyncClass注解
		AsyncClassDef asyncClass = originBeanClass.getAnnotation(AsyncClassDef.class);
		String classFullName = originBeanClass.getName();
		// 根据类名获得对应的映射结果
		List<AsyncLoadTemplate> asyncLoadTemplates = methodConfExecMap.get(classFullName);
		if (null != asyncLoadTemplates) {
			throw new AsyncLoadException(MessageFormat.format(
					"类名className={0}对应的配置在table中已经存在,table={1}", classFullName, methodConfExecMap));
		} else {
			asyncLoadTemplates = Lists.newArrayList();
		}
		AsyncThreadPoolConfig asyncThreadPoolConfig = asyncClass.classThreadPoolConf();
		AsyncLoadExecutor asyncLoadExecutor = getClassThreadPoolConfig(asyncThreadPoolConfig);
		AsyncMethodDef[] asyncMethods = asyncClass.asyncMethods();

		if (null != asyncMethods) {
			for (AsyncMethodDef asyncMethod : asyncMethods) {
				// 获得模板
				AsyncLoadTemplate asyncLoadTemplate = assembleAsyncLoadTemplate(asyncMethod,
						asyncLoadExecutor, null);
				asyncLoadTemplates.add(asyncLoadTemplate);
			}
		}

		if (!CollectionUtils.isEmpty(asyncLoadTemplates)) {
			methodConfExecMap.put(classFullName, asyncLoadTemplates);
		}
		return asyncLoadExecutor;
	}

	/**
	 * 获取类级别的线程池配置
	 *
	 * 如果类上面配置的失效，则使用默认全局的
	 *
	 * @param asyncThreadPoolConfig
	 * @return
	 */
	private AsyncLoadExecutor getClassThreadPoolConfig(
			AsyncThreadPoolConfig asyncThreadPoolConfig) {
		AsyncLoadExecutor asyncLoadExecutor = new AsyncLoadExecutor();
		if (asyncThreadPoolConfig.effect()) {
			asyncLoadExecutor.setAcceptCount(asyncThreadPoolConfig.queueSize());
			asyncLoadExecutor.setMode(asyncThreadPoolConfig.rejectPolicy());
			asyncLoadExecutor.setPoolSize(asyncThreadPoolConfig.poolSize());
		} else {
			asyncLoadExecutor.setAcceptCount(queueSize);
			asyncLoadExecutor.setMode(rejectPolicy);
			asyncLoadExecutor.setPoolSize(poolSize);
		}
		return asyncLoadExecutor;
	}

	/* (non-Javadoc)
	 * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
	 */
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	/**
	 * @return the methodAsyncTemplateTable
	 */
	public Map<String, Map<Method, AsyncLoadTemplate>> getMethodAsyncTemplateTable() {
		return methodAsyncTemplateTable;
	}

	/**
	 * @return the methodConfExecMap
	 */
	public Map<String, List<AsyncLoadTemplate>> getMethodConfExecMap() {
		return methodConfExecMap;
	}

	/**
	 * @param methodConfExecMap
	 *            the methodConfExecMap to set
	 */
	public void setMethodConfExecMap(Map<String, List<AsyncLoadTemplate>> methodConfExecMap) {
		this.methodConfExecMap = methodConfExecMap;
	}

}
