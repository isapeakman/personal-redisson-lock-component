package com.example.servicelock.core;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;



/**
 * spring工具
 **/

public class SpringUtil implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    
    private static ConfigurableApplicationContext configurableApplicationContext;
    public static final String PREFIX_DISTINCTION_NAME = "prefix.distinction.name";
    public static final String DEFAULT_PREFIX_DISTINCTION_NAME = "damai";
    
    public static String getPrefixDistinctionName(){
        return configurableApplicationContext.getEnvironment().getProperty(PREFIX_DISTINCTION_NAME,
                DEFAULT_PREFIX_DISTINCTION_NAME);
    }
    
    @Override
    public void initialize(final ConfigurableApplicationContext applicationContext) {
        configurableApplicationContext = applicationContext;
    }
    
    public static <T> T getBean(Class<T> requiredType){
        return configurableApplicationContext.getBean(requiredType);
    }
    public static <T> T getBean(String name, Class<T> requiredType){
        return configurableApplicationContext.getBean(name,requiredType);
    }
}
