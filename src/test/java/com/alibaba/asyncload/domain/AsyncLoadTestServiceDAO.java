package com.alibaba.asyncload.domain;

import org.springframework.stereotype.Component;

/**
 * @author jianghang 2011-1-21 下午10:46:19
 */
@Component
public class AsyncLoadTestServiceDAO {

	public void doSleep(long sleep) {
		try {
			System.out.println("----------sleep---------");
			Thread.sleep(sleep); // 睡一下
			System.out.println("-----------sleep end---------");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
