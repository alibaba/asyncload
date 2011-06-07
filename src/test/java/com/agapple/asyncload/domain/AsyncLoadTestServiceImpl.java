package com.agapple.asyncload.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * 一个测试AsyncLoad的默认实现
 * 
 * @author jianghang 2011-1-21 下午10:46:19
 */
public class AsyncLoadTestServiceImpl implements AsyncLoadTestService {

    private AsyncLoadTestServiceDAO asyncLoadTestServiceDAO;

    public AsyncLoadTestModel getRemoteModel(String name, long sleep) {
        if (sleep > 0) {
            asyncLoadTestServiceDAO.doSleep(sleep);
        }
        AsyncLoadTestModel model = new AsyncLoadTestModel(1, name, name);
        return model;
    }

    public List<AsyncLoadTestModel> listRemoteModel(String name, long sleep) {
        List<AsyncLoadTestModel> models = new ArrayList<AsyncLoadTestModel>();
        for (int i = 0; i < 2; i++) {
            if (sleep > 0) {
                asyncLoadTestServiceDAO.doSleep(sleep);
            }
            AsyncLoadTestModel model = new AsyncLoadTestModel(1, name, name);
            models.add(model);
        }
        return models;
    }

    public int countRemoteModel(String name, long sleep) {
        if (sleep > 0) {
            asyncLoadTestServiceDAO.doSleep(sleep);
        }
        return 0;
    }

    public void updateRemoteModel(String name, long sleep) {
        if (sleep > 0) {
            asyncLoadTestServiceDAO.doSleep(sleep);
        }
    }

    public String getRemoteName(String name, long sleep) {
        if (sleep > 0) {
            asyncLoadTestServiceDAO.doSleep(sleep);
        }
        return name;
    }

    public Object getRemoteObject(String name, long sleep) {
        if (sleep > 0) {
            asyncLoadTestServiceDAO.doSleep(sleep);
        }
        return name;
    }

    public void setAsyncLoadTestServiceDAO(AsyncLoadTestServiceDAO asyncLoadTestServiceDAO) {
        this.asyncLoadTestServiceDAO = asyncLoadTestServiceDAO;
    }

}
