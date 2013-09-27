package com.alibaba.asyncload.helper;

import org.junit.Assert;
import org.junit.Test;

import com.alibaba.asyncload.impl.helper.AsyncLoadReflectionHelper;

/**
 * @author jianghang 2011-3-31 下午02:34:00
 */
public class AsyncLoadReflectionHelperTest {

    @Test
    public void testDefaultValue_primitive() {
        // 原型对象数组
        Assert.assertArrayEquals((Object[]) AsyncLoadReflectionHelper.getDefaultValue(Boolean[].class),
                                 new Boolean[] {});
        Assert.assertArrayEquals((Object[]) AsyncLoadReflectionHelper.getDefaultValue(Byte[].class), new Byte[] {});
        Assert.assertArrayEquals((Object[]) AsyncLoadReflectionHelper.getDefaultValue(Character[].class),
                                 new Character[] {});
        Assert.assertArrayEquals((Object[]) AsyncLoadReflectionHelper.getDefaultValue(Short[].class), new Short[] {});
        Assert.assertArrayEquals((Object[]) AsyncLoadReflectionHelper.getDefaultValue(Double[].class), new Double[] {});
        Assert.assertArrayEquals((Object[]) AsyncLoadReflectionHelper.getDefaultValue(Float[].class), new Float[] {});
        Assert.assertArrayEquals((Object[]) AsyncLoadReflectionHelper.getDefaultValue(Integer[].class),
                                 new Integer[] {});
        Assert.assertArrayEquals((Object[]) AsyncLoadReflectionHelper.getDefaultValue(Long[].class), new Long[] {});
        // 原型数组
        Assert.assertEquals(AsyncLoadReflectionHelper.getDefaultValue(boolean[].class).getClass(), boolean[].class);
        Assert.assertEquals(AsyncLoadReflectionHelper.getDefaultValue(byte[].class).getClass(), byte[].class);
        Assert.assertEquals(AsyncLoadReflectionHelper.getDefaultValue(char[].class).getClass(), char[].class);
        Assert.assertEquals(AsyncLoadReflectionHelper.getDefaultValue(short[].class).getClass(), short[].class);
        Assert.assertEquals(AsyncLoadReflectionHelper.getDefaultValue(double[].class).getClass(), double[].class);
        Assert.assertEquals(AsyncLoadReflectionHelper.getDefaultValue(float[].class).getClass(), float[].class);
        Assert.assertEquals(AsyncLoadReflectionHelper.getDefaultValue(int[].class).getClass(), int[].class);
        Assert.assertEquals(AsyncLoadReflectionHelper.getDefaultValue(long[].class).getClass(), long[].class);
        // 原型
        Assert.assertEquals(AsyncLoadReflectionHelper.getDefaultValue(boolean.class), false);
        Assert.assertEquals(AsyncLoadReflectionHelper.getDefaultValue(byte.class), (byte) 0);
        Assert.assertEquals(AsyncLoadReflectionHelper.getDefaultValue(char.class), (char) 0);
        Assert.assertEquals(AsyncLoadReflectionHelper.getDefaultValue(short.class), (short) 0);
        Assert.assertEquals(AsyncLoadReflectionHelper.getDefaultValue(double.class), (double) 0);
        Assert.assertEquals(AsyncLoadReflectionHelper.getDefaultValue(float.class), (float) 0);
        Assert.assertEquals(AsyncLoadReflectionHelper.getDefaultValue(int.class), (int) 0);
        Assert.assertEquals(AsyncLoadReflectionHelper.getDefaultValue(long.class), (long) 0);
        // 原型对应的对象
        Assert.assertEquals(AsyncLoadReflectionHelper.getDefaultValue(Boolean.class), Boolean.FALSE);
        Assert.assertEquals(AsyncLoadReflectionHelper.getDefaultValue(Byte.class), (byte) 0);
        Assert.assertEquals(AsyncLoadReflectionHelper.getDefaultValue(Character.class), (char) 0);
        Assert.assertEquals(AsyncLoadReflectionHelper.getDefaultValue(Short.class), (short) 0);
        Assert.assertEquals(AsyncLoadReflectionHelper.getDefaultValue(Double.class), (double) 0);
        Assert.assertEquals(AsyncLoadReflectionHelper.getDefaultValue(Float.class), (float) 0);
        Assert.assertEquals(AsyncLoadReflectionHelper.getDefaultValue(Integer.class), (int) 0);
        Assert.assertEquals(AsyncLoadReflectionHelper.getDefaultValue(Long.class), (long) 0);
    }

    @Test
    public void testNewInstance() {
        DefaultValueObjectB objectb = (DefaultValueObjectB) AsyncLoadReflectionHelper.newInstance(DefaultValueObjectB.class);
        Assert.assertEquals(objectb.a, 0);
        Assert.assertArrayEquals(objectb.arr, new Integer[] {});

        // 递归对象创建
        DefaultValueObjectA objecta = (DefaultValueObjectA) AsyncLoadReflectionHelper.newInstance(DefaultValueObjectA.class);
        Assert.assertEquals(objecta.a, 0);
        Assert.assertArrayEquals(objecta.arr, new Integer[] {});
        // Assert.assertEquals(objecta.b, null);
        Assert.assertEquals(objecta.b.a, 0);
        Assert.assertArrayEquals(objecta.b.arr, new Integer[] {});

    }
}

class DefaultValueObjectA {

    public int                 a;
    public Integer[]           arr;
    public DefaultValueObjectB b;

    public DefaultValueObjectA(int a, Integer[] arr, DefaultValueObjectB b){
        this.a = a;
        this.arr = arr;
        this.b = b;
    }
}

class DefaultValueObjectB {

    public int       a;
    public Integer[] arr;

    public DefaultValueObjectB(int a, Integer[] arr){
        this.a = a;
        this.arr = arr;
    }
}
