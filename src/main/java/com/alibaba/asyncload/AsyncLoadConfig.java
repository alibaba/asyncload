package com.alibaba.asyncload;

import java.util.HashMap;
import java.util.Map;

/**
 * 对应异步加载工具的关注点
 *
 * @author jianghang 2011-1-22 上午12:06:48
 */
public class AsyncLoadConfig {

	public static final Long DEFAULT_TIME_OUT = 0L; // 默认不设置超时,保持系统兼容性
	private volatile Long defaultTimeout = DEFAULT_TIME_OUT; // 单位ms
	private volatile Boolean needBarrierSupport = false; // 默认不开启，如果设置了开启不调用AsyncLoadBarrier.await()会有内存泄漏,必须注意
	private volatile Boolean needThreadLocalSupport = false; // 默认不开启,如果启用可以共享ThreadLocal，需慎用
	private Map<AsyncLoadMethodMatch, Long> matches;

	public AsyncLoadConfig() {
	}

	public AsyncLoadConfig(Long defaultTimeout) {
		this.defaultTimeout = defaultTimeout;
	}

	public AsyncLoadConfig cloneConfig() {
		AsyncLoadConfig config = new AsyncLoadConfig();
		config.setDefaultTimeout(getDefaultTimeout());
		config.setNeedBarrierSupport(getNeedBarrierSupport());
		config.setNeedThreadLocalSupport(getNeedThreadLocalSupport());
		config.setMatches(getMatches()); // map对象直接是个引用复制
		return config;
	}

	// ===================== setter / getter ====================

	public Map<AsyncLoadMethodMatch, Long> getMatches() {
		if (matches == null) {
			matches = new HashMap<AsyncLoadMethodMatch, Long>();
			matches.put(AsyncLoadMethodMatch.TRUE, defaultTimeout);
		}

		return matches;
	}

	public void setMatches(Map<AsyncLoadMethodMatch, Long> matches) {
		this.matches = matches;
	}

	public Long getDefaultTimeout() {
		return defaultTimeout;
	}

	public void setDefaultTimeout(Long defaultTimeout) {
		this.defaultTimeout = defaultTimeout;
	}

	public Boolean getNeedBarrierSupport() {
		return needBarrierSupport;
	}

	public void setNeedBarrierSupport(Boolean needBarrierSupport) {
		this.needBarrierSupport = needBarrierSupport;
	}

	public Boolean getNeedThreadLocalSupport() {
		return needThreadLocalSupport;
	}

	public void setNeedThreadLocalSupport(Boolean needThreadLocalSupport) {
		this.needThreadLocalSupport = needThreadLocalSupport;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "AsyncLoadConfig [defaultTimeout=" + defaultTimeout + ", needBarrierSupport="
				+ needBarrierSupport + ", needThreadLocalSupport=" + needThreadLocalSupport
				+ ", matches=" + matches + "]";
	}

}
