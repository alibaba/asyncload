package com.alibaba.asyncload.impl;

/**
 * 并行加载的service类,默认生成的代理proxy会实现该接口.
 * <p>
 * {@linkplain AsyncLoadService}和{@linkplain AsyncLoadObject}关系：对应的是service和model之间的概念，调用proxy service的某个方法，会创建一个proxy
 * model
 * 
 * <pre>
 * 提供客户端获取代理serivce的一些内部状态,一般不建议直接操作该类：
 * 1. originalClass : 返回原先的代理的原始class实例，因为使用代理后会丢失Annotation,Generic,Field数据，所以需要直接操作原始class
 * 
 * TODO : 后续可添加一些profile的统计信息，比如当前service并行加载的model有多少，每个代理方法的平均响应时间信息等。暂时没这需求，先不实现
 * </pre>
 * 
 * @author jianghang 2011-4-4 下午04:06:53
 */
public interface AsyncLoadService {

    /**
     * @return 原始的被代理的class对象
     */
    Class<?> _getOriginalClass();
}
