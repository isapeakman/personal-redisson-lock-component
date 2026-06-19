package com.example.servicelock.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.concurrent.TimeUnit;

/**
 * redisson属性配置
 **/
@Data
@ConfigurationProperties(prefix = "spring.redis.redisson")
public class RedissonBaseProperties {

    /**
     * 默认线程数配置
     * 这些参数用于控制线程池的运行行为
     */
    private Integer threads = 16;    // 默认工作线程数
    
    private Integer nettyThreads = 32;  // 默认Netty线程数
    
    private Integer corePoolSize = null; // 核心线程数，初始为null，表示使用默认值
   
    private Integer maximumPoolSize = null; // 最大线程数，初始为null，表示使用默认值
    
    private long keepAliveTime = 30; // 线程空闲存活时间
    
    private TimeUnit unit = TimeUnit.SECONDS; // 时间单位，秒
  
    private Integer workQueueSize = 256;   // 工作队列大小，用于存储待处理的任务
}
