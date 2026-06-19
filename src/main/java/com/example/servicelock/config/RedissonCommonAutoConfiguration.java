package com.example.servicelock.config;


import com.example.servicelock.factory.LockInfoHandleFactory;
import com.example.servicelock.handler.RedissonDataHandle;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.spring.starter.RedissonAutoConfiguration;
import org.redisson.spring.starter.RedissonAutoConfigurationV2;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * redisson通用配置
 **/
@AutoConfigureBefore(value = {RedissonAutoConfigurationV2.class, RedissonAutoConfiguration.class})
@EnableConfigurationProperties(RedissonBaseProperties.class)
public class RedissonCommonAutoConfiguration {

    /**
     * 用于记录执行任务线程数量的原子计数器
     * 使用AtomicInteger保证线程安全
     */
    private final AtomicInteger executeTaskThreadCount = new AtomicInteger(1);

    /**
     * 创建并配置RedissonClient Bean
     *
     * @param redisProperties        Redis配置属性
     * @param redissonBaseProperties Redisson基础配置属性
     * @return 配置好的RedissonClient实例
     */
    @Bean
    public RedissonClient redissonClient(RedisProperties redisProperties, RedissonBaseProperties redissonBaseProperties) {
        // 创建Redisson配置对象
        Config config = new Config();
        // 根据SSL配置设置连接前缀
        String prefix = "redis://";
        Method method = ReflectionUtils.findMethod(RedisProperties.class, "isSsl");
        if (method != null && (Boolean) ReflectionUtils.invokeMethod(method, redisProperties)) {
            prefix = "rediss://";
        }
        // 配置单服务器模式
        config.useSingleServer()
                .setAddress(prefix + redisProperties.getHost() + ":" + redisProperties.getPort()) // 设置服务器地址
                .setConnectTimeout(1000) // 设置连接超时时间
                .setDatabase(redisProperties.getDatabase()) // 设置数据库索引
                .setPassword(redisProperties.getPassword()); // 设置密码
        // 设置线程相关配置
        config.setThreads(redissonBaseProperties.getThreads());
        config.setNettyThreads(redissonBaseProperties.getNettyThreads());
        // 如果核心池大小和最大池大小不为空，则配置线程池执行器
        if (Objects.nonNull(redissonBaseProperties.getCorePoolSize()) &&
                Objects.nonNull(redissonBaseProperties.getMaximumPoolSize())) {
            ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
                    redissonBaseProperties.getCorePoolSize(), // 核心线程数
                    redissonBaseProperties.getMaximumPoolSize(), // 最大线程数
                    redissonBaseProperties.getKeepAliveTime(), // 线程空闲时间
                    redissonBaseProperties.getUnit(), // 时间单位
                    new LinkedBlockingQueue<>(redissonBaseProperties.getWorkQueueSize()), // 工作队列
                    r -> new Thread(Thread.currentThread().getThreadGroup(), r,
                            "redisson-thread-" + executeTaskThreadCount.getAndIncrement())); // 线程工厂
            config.setExecutor(threadPoolExecutor);
        }
        // 创建并返回RedissonClient实例
        return Redisson.create(config);
    }

    /**
     * 创建Redisson数据处理Bean
     *
     * @param redissonClient Redisson客户端实例
     * @return RedissonDataHandle实例
     */
    @Bean
    public RedissonDataHandle redissonDataHandle(RedissonClient redissonClient) {
        return new RedissonDataHandle(redissonClient);
    }


    /**
     * 创建锁信息处理器工厂Bean
     *
     * @return LockInfoHandleFactory实例
     */
    @Bean
    public LockInfoHandleFactory lockInfoHandleFactory() {
        return new LockInfoHandleFactory();
    }
}
