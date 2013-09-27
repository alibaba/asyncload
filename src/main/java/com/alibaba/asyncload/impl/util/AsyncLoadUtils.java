package com.alibaba.asyncload.impl.util;

import net.sf.cglib.proxy.Enhancer;

import com.alibaba.asyncload.impl.AsyncLoadObject;
import com.alibaba.asyncload.impl.AsyncLoadService;
import com.alibaba.asyncload.impl.AsyncLoadStatus;
import com.alibaba.asyncload.impl.exceptions.AsyncLoadException;

/**
 * 提供给外部的一些AsyncLoad的便利的操作方法
 * 
 * @author jianghang 2011-4-4 下午04:44:26
 */
public class AsyncLoadUtils {

    /**
     * Assert that an object is not <code>null</code> .
     */
    public static void notNull(Object object, String message) {
        if (object == null) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Assert that an object is not <code>null</code> .
     * 
     * <pre class="code">
     * Assert.notNull(clazz);
     * </pre>
     */
    public static void notNull(Object object) {
        notNull(object, "[Assertion failed] - this argument is required; it must not be null");
    }

    /**
     * Assert that an array has elements; that is, it must not be
     * <code>null</code> and must have at least one element.
     */
    public static void notEmpty(Object[] array, String message) {
        if (array == null || array.length == 0) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Assert that an array has elements; that is, it must not be
     * <code>null</code> and must have at least one element.
     */
    public static void notEmpty(Object[] array) {
        notEmpty(array, "[Assertion failed] - this array must not be empty: it must contain at least 1 element");
    }

    /**
     * 根据model实例,判断一个Model当前是否使用并行加载
     * 
     * @param model
     * @return
     */
    public static boolean isAsyncLoad(Object model) throws AsyncLoadException {
        if (model == null) {
            return false;
        }

        return isAsyncLoad(model.getClass());
    }

    /**
     * 根据model class,判断当前是否使用并行加载
     * 
     * @param model
     * @return
     */
    public static boolean isAsyncLoad(Class clazz) throws AsyncLoadException {
        if (clazz == null) {
            return false;
        }
        // Enhancer.isEnhanced(clazz)判断会进行一个method查找,在整个asyncload工具自身占了比较多时间
        return AsyncLoadObject.class.isAssignableFrom(clazz);
    }

    /**
     * 并行加载会返回一个proxy model对象(永远不会为null),所以为满足以前的if(model ==
     * null)判断，提供了一个util方法进行处理
     * 
     * <pre>
     * 说明: 
     * 1. 如果当前model没有采用并行加载,则直接返回model == null判断，兼容处理
     * 2. 加载model过程中出现异常，该方法直接返回true。对应的异常：并行加载超时异常，service抛出业务异常等
     * 3. 调用该方法会进行阻塞并行加载，直到结果返回
     * </pre>
     * 
     * @param model
     * @return
     */
    public static boolean isNull(Object model) throws AsyncLoadException {
        if (!isAsyncLoad(model)) {// 如果不是并行加载model
            // throw new
            // IllegalArgumentException("model is not run asyncload mode!");
            return model == null;
        } else {
            return ((AsyncLoadObject) model)._isNull(); // 进行强制转型处理
        }
    }

    /**
     * 执行并行加载后，原先的使用时间统计方式已不在有效，这里提供一个util方法获取底层的并行加载数据状态
     * 
     * <pre>
     * 说明: 
     * 1. 如果当前model没有采用并行加载,则直接返回null
     * 2. 调用该方法不会阻塞并行加载
     * </pre>
     * 
     * @param model
     * @return
     */
    public static AsyncLoadStatus getStatus(Object model) throws AsyncLoadException {
        if (!isAsyncLoad(model)) {// 如果不是并行加载model
            // throw new
            // IllegalArgumentException("model is not run asyncload mode!");
            return null;
        } else {
            return ((AsyncLoadObject) model)._getStatus(); // 进行强制转型处理
        }
    }

    /**
     * 执行并行加载后，原先的Model对象已经被代理，如Annotation,Generic,Field属性都会丢失。
     * 这里提供一个util方法获取原始的model class
     * 
     * <pre>
     * 说明: 
     * 1. 如果当前model没有采用并行加载,则直接返回model的class对象
     * 2. 调用该方法不会阻塞并行加载
     * </pre>
     * 
     * @param model
     * @return
     */
    public static Class<?> getOriginalClass(Object model) throws AsyncLoadException {
        if (!isAsyncLoad(model)) {// 如果不是并行加载model
            // throw new
            // IllegalArgumentException("model is not run asyncload mode!");
            return model.getClass();
        } else {
            return ((AsyncLoadObject) model)._getOriginalClass(); // 进行强制转型处理
        }
    }

    /**
     * 执行并行加载后，原先的Model对象已经被代理，这里提供一个util方法获取原始的方法调用的返回对象
     * 
     * <pre>
     * 说明: 
     * 1. 如果当前model没有采用并行加载,则直接返回model本身
     * 2. 调用该方法会进行阻塞并行加载
     * </pre>
     * 
     * @param model
     * @return
     */
    public static Object getOriginalResult(Object model) throws AsyncLoadException {
        if (!isAsyncLoad(model)) {// 如果不是并行加载model
            // throw new
            // IllegalArgumentException("model is not run asyncload mode!");
            return model;
        } else {
            return ((AsyncLoadObject) model)._getOriginalResult(); // 进行强制转型处理
        }
    }

    /**
     * 实施并行加载后，原先的service对象已经被代理，如Annotation,Generic,Field属性都会丢失。
     * 这里提供一个util方法获取原始的service class
     * 
     * <pre>
     * 说明: 
     * 1. 如果当前model没有采用并行加载,则直接返回service的class对象
     * 2. 调用该方法不会阻塞并行加载
     * </pre>
     * 
     * @param service代理之前的class，可能是个接口或者具体类
     * @return
     */
    public static Class<?> getServiceOriginalClass(Object service) throws AsyncLoadException {
        Class clazz = service.getClass();
        if (Enhancer.isEnhanced(clazz) && AsyncLoadService.class.isAssignableFrom(clazz)) {// 如果不是并行加载model
            // throw new
            // IllegalArgumentException("service is not run asyncload mode!");
            return service.getClass();
        } else {
            return ((AsyncLoadService) service)._getOriginalClass(); // 进行强制转型处理
        }
    }
}
