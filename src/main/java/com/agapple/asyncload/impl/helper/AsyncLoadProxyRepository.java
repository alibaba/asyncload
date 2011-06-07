package com.agapple.asyncload.impl.helper;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 提供对应的proxy仓库,避免重复创建对应的class
 * 
 * @author jianghang 2011-1-24 下午03:36:17
 */
public class AsyncLoadProxyRepository {

    private static Map<String, Class> reponsitory = new ConcurrentHashMap<String, Class>(); // 在方法调用级别进行sync控制,这里不需要使用cocurrent包

    /**
     * 如果存在对应的key的ProxyClass就返回，没有则返回null
     * 
     * @param key
     * @return
     */
    public static Class getProxy(String key) {
        return reponsitory.get(key);
    }

    /**
     * 注册对应的proxyClass到仓库中
     * 
     * @param key
     * @param proxyClass
     */
    public static void registerProxy(String key, Class proxyClass) {
        if (!reponsitory.containsKey(key)) { // 避免重复提交
            reponsitory.put(key, proxyClass);
        }
    }
}
