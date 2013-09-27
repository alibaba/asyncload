package com.alibaba.asyncload.classinfo;

import java.io.Serializable;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author jianghang 2011-4-1 下午11:49:26
 */
@Classi(value = "class")
public class ClassInfoService<O extends Serializable> {

    @Fieldi(value = "field")
    protected O ser;

    @Methodi(value = "method")
    public void test(@Parameteri(value = "param") Object param) {
        System.out.println("hello");
    }

    public O getSer() {
        return ser;
    }

    public void setSer(O ser) {
        this.ser = ser;
    }

}

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
@interface Classi {

    String value();
}

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Inherited
@interface Fieldi {

    String value();
}

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Inherited
@interface Methodi {

    String value();
}

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
@Inherited
@interface Parameteri {

    String value();
}
