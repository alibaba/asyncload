package com.agapple.asyncload.impl.pool;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author jianghang 2011-4-1 下午06:50:26
 */
public class NamedThreadFactory implements ThreadFactory {

    final private static String DEFAULT_NAME = "asyncload-pool";
    final private String        name;
    final private boolean       daemon;
    final private ThreadGroup   group;
    final private AtomicInteger threadNumber = new AtomicInteger(0);

    public NamedThreadFactory(){
        this(DEFAULT_NAME, true);
    }

    public NamedThreadFactory(String name, boolean daemon){
        this.name = name;
        this.daemon = daemon;
        SecurityManager s = System.getSecurityManager();
        group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
    }

    public Thread newThread(Runnable r) {
        Thread t = new Thread(group, r, name + "-" + threadNumber.getAndIncrement(), 0);
        t.setDaemon(daemon);
        if (t.getPriority() != Thread.NORM_PRIORITY) {
            t.setPriority(Thread.NORM_PRIORITY);
        }
        return t;
    }

}
