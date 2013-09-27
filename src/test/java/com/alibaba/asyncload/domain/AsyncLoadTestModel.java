package com.alibaba.asyncload.domain;

import java.io.Serializable;

/**
 * 一个asyncLoad的测试对象model，返回的对象
 * 
 * @author jianghang 2011-1-21 下午10:45:41
 */
public class AsyncLoadTestModel implements Serializable {

    private static final long serialVersionUID = -5410019316926096126L;

    public AsyncLoadTestModel(int id, String name, String detail){
        this.id = id;
        this.name = name;
        this.detail = detail;
    }

    public int    id;
    public String name;
    public String detail;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    @Override
    public String toString() {
        return "AsyncLoadTestModel [detail=" + detail + ", id=" + id + ", name=" + name + "]";
    }
}
