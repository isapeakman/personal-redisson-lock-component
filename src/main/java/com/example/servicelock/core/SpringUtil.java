package com.example.servicelock.core;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;


/**
 * spring工具
 **/

public class SpringUtil implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    /**
     * Spring上下文配置类，用于管理Spring应用程序上下文并提供一些通用功能
     */
    private static ConfigurableApplicationContext configurableApplicationContext; // Spring可配置应用程序上下文实例
    /**
     * 前缀区分名称的配置键名常量
     */
    public static final String PREFIX_DISTINCTION_NAME = "prefix.distinction.name";
    /**
     * 前缀区分名称的默认值常量
     */
    public static final String DEFAULT_PREFIX_DISTINCTION_NAME = "lock";

    /**
     * 获取前缀区分名称的方法
     *
     * @return 从配置中获取的前缀区分名称，如果未配置则返回默认值"lock"
     */
    public static String getPrefixDistinctionName() {
        return configurableApplicationContext.getEnvironment().getProperty(PREFIX_DISTINCTION_NAME,
                DEFAULT_PREFIX_DISTINCTION_NAME);
    }

    /**
     * 初始化方法，用于设置Spring应用程序上下文
     * 这里的方法在Spring应用程序启动时执行，将Spring应用程序上下文设置为静态成员变量configurableApplicationContext
     * applicationContext 并非依赖注入，而是启动时自动传入的参数
     * @param applicationContext Spring可配置应用程序上下文
     */
    @Override
    public void initialize(final ConfigurableApplicationContext applicationContext) {
        configurableApplicationContext = applicationContext;
    }

    /**
     * 根据类型获取Spring容器中的Bean
     *
     * @param requiredType Bean的类型
     * @return 指定类型的Bean实例
     */
    public static <T> T getBean(Class<T> requiredType) {
        return configurableApplicationContext.getBean(requiredType);
    }

    /**
     * 根据名称和类型获取Spring容器中的Bean
     *
     * @param name         Bean的名称
     * @param requiredType Bean的类型
     * @return 指定名称和类型的Bean实例
     */
    public static <T> T getBean(String name, Class<T> requiredType) {
        return configurableApplicationContext.getBean(name, requiredType);
    }
}
