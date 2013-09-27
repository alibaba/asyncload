package com.alibaba.asyncload;

import com.alibaba.asyncload.impl.helper.AsyncLoadProxyRepository;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.concurrent.ConcurrentHashMap;

//@ContextConfiguration(locations = { "classpath:asyncload/applicationContext.xml" })
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath*:/asyncload/application*.xml"})
@Ignore
public abstract class BaseAsyncLoadNoRunTest implements ApplicationContextAware {
    public ApplicationContext applicationContext;
    @Before
    public void setUp() {
        // System.setProperty(DebuggingClassWriter.DEBUG_LOCATION_PROPERTY, "/tmp/cglib/");
        // 清空repository内的cache记录
        try {
            TestUtils.setField(new AsyncLoadProxyRepository(), "reponsitory", new ConcurrentHashMap<String, Class>());
        } catch (Exception e) {
            Assert.fail();
        }
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException{
        this.applicationContext = applicationContext;
    }


}
