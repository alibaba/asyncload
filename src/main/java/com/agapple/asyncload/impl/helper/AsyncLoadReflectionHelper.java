package com.agapple.asyncload.impl.helper;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.cglib.core.ReflectUtils;
import net.sf.cglib.reflect.FastClass;
import net.sf.cglib.reflect.FastMethod;

/**
 * AyncLoad中常用的一些反射方法
 * 
 * @author jianghang 2011-3-29 下午09:55:12
 */
public class AsyncLoadReflectionHelper {

    private static final Map               primitiveValueMap = new HashMap(16);
    private static Map<String, FastClass>  fastClassCache    = new ConcurrentHashMap<String, FastClass>();
    private static Map<String, FastMethod> fastMethodCache   = new ConcurrentHashMap<String, FastMethod>();

    static {
        primitiveValueMap.put(Boolean.class, Boolean.FALSE);
        primitiveValueMap.put(Byte.class, Byte.valueOf((byte) 0));
        primitiveValueMap.put(Character.class, Character.valueOf((char) 0));
        primitiveValueMap.put(Short.class, Short.valueOf((short) 0));
        primitiveValueMap.put(Double.class, Double.valueOf(0));
        primitiveValueMap.put(Float.class, Float.valueOf(0));
        primitiveValueMap.put(Integer.class, Integer.valueOf(0));
        primitiveValueMap.put(Long.class, Long.valueOf(0));
        primitiveValueMap.put(boolean.class, Boolean.FALSE);
        primitiveValueMap.put(byte.class, Byte.valueOf((byte) 0));
        primitiveValueMap.put(char.class, Character.valueOf((char) 0));
        primitiveValueMap.put(short.class, Short.valueOf((short) 0));
        primitiveValueMap.put(double.class, Double.valueOf(0));
        primitiveValueMap.put(float.class, Float.valueOf(0));
        primitiveValueMap.put(int.class, Integer.valueOf(0));
        primitiveValueMap.put(long.class, Long.valueOf(0));

    }

    /**
     * 特殊处理，允许通过带参数的constructor创建对象
     * 
     * @param type
     * @return
     */
    public static Object newInstance(Class type) {
        Constructor _constructor = null;
        Object[] _constructorArgs = new Object[0];
        try {
            _constructor = type.getConstructor(new Class[] {});// 先尝试默认的空构造函数
        } catch (NoSuchMethodException e) {
            // ignore
        }

        if (_constructor == null) {// 没有默认的构造函数，尝试别的带参数的函数
            Constructor[] constructors = type.getConstructors();
            if (constructors.length == 0) {
                throw new UnsupportedOperationException("Class[" + type.getName() + "] has no public constructors");
            }
            _constructor = constructors[0];// 默认取第一个参数
            Class[] params = _constructor.getParameterTypes();
            _constructorArgs = new Object[params.length];
            for (int i = 0; i < params.length; i++) {
                _constructorArgs[i] = getDefaultValue(params[i]);
            }
        }

        return ReflectUtils.newInstance(_constructor, _constructorArgs);
    }

    public static FastMethod getMethod(Class<?> clazz, String methodName) {
        return getMethod(clazz, methodName, new Class[] {});
    }

    /**
     * 根据信息查询FastMethod，已经有cache实现。
     * 
     * @param clazz
     * @param methodName
     * @param parameterTypes
     * @return
     */
    public static FastMethod getMethod(Class<?> clazz, String methodName, Class... parameterTypes) {
        String clazzName = clazz.getName();
        String methodKey = clazzName + "#" + methodName;

        FastMethod method = fastMethodCache.get(methodKey);
        if (null == method) {
            FastClass fc = fastClassCache.get(clazzName);
            if (null == fc) {
                fc = FastClass.create(clazz);
                fastClassCache.put(clazzName, fc);
            }
            method = fc.getMethod(methodName, parameterTypes);
            if (null == method) {
                fastMethodCache.put(methodKey, method);
            }
        }

        return method;
    }

    /**
     * 根据class类型返回默认值值
     * 
     * @param cl
     * @return
     */
    public static Object getDefaultValue(Class cl) {
        if (cl.isArray()) {// 处理数组
            return Array.newInstance(cl.getComponentType(), 0);
        } else if (cl.isPrimitive() || primitiveValueMap.containsKey(cl)) { // 处理原型
            return primitiveValueMap.get(cl);
        } else {
            return AsyncLoadReflectionHelper.newInstance(cl);
            // return null;
        }
    }
}
