/*
 * Copyright 1999-2004 Alibaba.com All right reserved. This software is the confidential and proprietary information of
 * Alibaba.com ("Confidential Information"). You shall not disclose such Confidential Information and shall use it only
 * in accordance with the terms of the license agreement you entered into with Alibaba.com.
 */
package com.agapple.asyncload.impl.pool;

import java.util.concurrent.Callable;

import com.agapple.asyncload.AsyncLoadConfig;

/**
 * 扩展callable，支持{@linkplain AsyncLoadConfig}的传递
 * 
 * @author jianghang 2011-4-27 下午03:42:04
 */
public interface AsyncLoadCallable<V> extends Callable<V> {

    AsyncLoadConfig getConfig();
}
