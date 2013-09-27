package com.alibaba.asyncload.impl;

/**
 * <strong>内部对象，不允许外部直接使用</strong>: 并行加载的Model对象，返回的代理Model对象都会实现该接口
 * 
 * <pre>
 * 提供客户端获取代理对象的一些内部状态,一般不建议直接操作该类：
 * 1. null : 判断真实的model是否为null
 * 2. status : 返回当前model的执行情况，比如运行状态{@linkplain AsyncLoadStatus.Status}, 相关时间
 * 3. originalClass : 返回原先的代理的原始class实例，因为使用代理后会丢失Annotation,Generic,Field数据，所以需要直接操作原始class
 * 4. originalResult : 返回原先的代理的返回原始结果
 * </pre>
 * 
 * @author jianghang 2011-4-4 下午04:06:53
 */
public interface AsyncLoadObject {

    /**
     * @return 判断真实的model是否为null
     */
    boolean _isNull();

    /**
     * @return 并行加载的运行状态
     */
    AsyncLoadStatus _getStatus();

    /**
     * @return 原始的被代理的class对象
     */
    Class<?> _getOriginalClass();

    /**
     * @return 原始的结果对象
     */
    Object _getOriginalResult();
}
