package com.agapple.asyncload;

import java.util.concurrent.ConcurrentHashMap;

import junit.framework.Assert;

import org.junit.Before;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit38.AbstractJUnit38SpringContextTests;

import com.agapple.asyncload.impl.helper.AsyncLoadProxyRepository;

@ContextConfiguration(locations = { "classpath:asyncload/applicationContext.xml" })
public class BaseAsyncLoadNoRunTest extends AbstractJUnit38SpringContextTests {

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

}
