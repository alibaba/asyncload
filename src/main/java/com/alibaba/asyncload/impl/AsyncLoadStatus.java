package com.alibaba.asyncload.impl;

/**
 * 记录一下并行加载执行的一些状态信息：
 * 
 * <pre>
 * 1. status : 代表asyncload的运行状态
 * 2. startTime 开始运行asyncload时间点,记录的是System.currentTimeMillis()时间
 * 3. costTime: 总共消耗时间，单位ms
 *     *. 如果是status==DONE，则返回正常执行的时间, costTime = (完成时间点 - startTime)
 *     *. 如果status==TIMEOUT，则返回实际使用的时间，costTime = (超时时间点 -startTime)
 *     *. 如果status==RUN ,则返回当前使用的时间，costTime = (当前时间 - startTIme)
 * </pre>
 * 
 * @author jianghang 2011-4-4 下午07:13:56
 */
public class AsyncLoadStatus {

    final private long   starTime; // 执行开始时间
    final private long   costTime;
    final private Status status;

    public AsyncLoadStatus(Status status, long startTime, long costTime){
        this.status = status;
        this.starTime = startTime;
        this.costTime = costTime;
    }

    public static enum Status {
        /** 执行中 */
        RUN,
        /** 已超时 */
        TIMEOUT,
        /** 已完成(可能是正常结束/有异常退出) */
        DONE;

        public boolean isRun() {
            return this == RUN;
        }

        public boolean isTimeout() {
            return this == TIMEOUT;
        }

        public boolean isDone() {
            return this == DONE;
        }
    }

    public long getStarTime() {
        return starTime;
    }

    public long getCostTime() {
        return costTime;
    }

    public Status getStatus() {
        return status;
    }

}
