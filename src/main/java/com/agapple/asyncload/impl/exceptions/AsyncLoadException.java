package com.agapple.asyncload.impl.exceptions;

/**
 * 并行加载自定义异常
 * 
 * @author jianghang 2011-4-1 下午05:06:37
 */
public class AsyncLoadException extends RuntimeException {

    private static final long serialVersionUID = -2128834565845654572L;

    public AsyncLoadException(){
        super();
    }

    public AsyncLoadException(String message, Throwable cause){
        super(message, cause);
    }

    public AsyncLoadException(String message){
        super(message);
    }

    public AsyncLoadException(Throwable cause){
        super(cause);
    }

}
