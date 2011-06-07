package com.agapple.asyncload.impl.template;

/**
 * 对应AyncLoad模板的回调函数
 * 
 * @author jianghang 2011-1-24 下午07:38:10
 */
public interface AsyncLoadCallback<R> {

    public R doAsyncLoad();
}
