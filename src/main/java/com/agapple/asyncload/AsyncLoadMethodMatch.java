package com.agapple.asyncload;

import java.lang.reflect.Method;

/**
 * 异步加载机制 方法匹配对象定义
 * 
 * @author jianghang 2011-1-21 下午09:49:29
 */
public interface AsyncLoadMethodMatch {

    AsyncLoadMethodMatch TRUE = new AsyncLoadTrueMethodMatcher(); // 默认提供返回always true的实现

    boolean matches(Method method);

}

class AsyncLoadTrueMethodMatcher implements AsyncLoadMethodMatch {

    public boolean matches(Method method) {
        return true;
    }

    public String toString() {
        return "AsyncLoadTrueMethodMatcher.TURE";
    }
}
