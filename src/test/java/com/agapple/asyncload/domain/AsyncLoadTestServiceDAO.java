package com.agapple.asyncload.domain;

/**
 * @author jianghang 2011-1-21 下午10:46:19
 */
public class AsyncLoadTestServiceDAO {

    public void doSleep(long sleep) {
        try {
            Thread.sleep(sleep); // 睡一下
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
