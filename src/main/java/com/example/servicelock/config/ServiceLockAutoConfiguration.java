package com.example.servicelock.config;


import com.example.servicelock.LockInfoType;
import com.example.servicelock.aspect.ServiceLockAspect;
import com.example.servicelock.core.LockManager;
import com.example.servicelock.factory.LockInfoHandleFactory;
import com.example.servicelock.factory.ServiceLockFactory;
import com.example.servicelock.info.LockInfoHandle;
import com.example.servicelock.info.ServiceLockInfoHandle;
import com.example.servicelock.util.ServiceLockTool;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Bean;

/**
 * @program: 极度真实还原大麦网高并发实战项目。 添加 阿星不是程序员 微信，添加时备注 大麦 来获取项目的完整资料
 * @description: 分布式锁 配置
 * @author: 阿星不是程序员
 **/
public class ServiceLockAutoConfiguration {

    /**
     * 创建服务锁信息处理器Bean
     *
     * @return ServiceLockInfoHandle实例
     */
    @Bean(LockInfoType.SERVICE_LOCK)
    public LockInfoHandle serviceLockInfoHandle() {
        return new ServiceLockInfoHandle();
    }

    /**
     * 创建锁管理器Bean
     *
     * @param redissonClient Redisson客户端
     * @return ManageLocker实例
     */
    @Bean
    public LockManager manageLocker(RedissonClient redissonClient) {
        return new LockManager(redissonClient);
    }

    /**
     * 创建服务锁工厂Bean
     *
     * @param manageLocker 管理器锁实例
     * @return ServiceLockFactory实例
     */
    @Bean
    public ServiceLockFactory serviceLockFactory(LockManager manageLocker) {
        return new ServiceLockFactory(manageLocker);
    }

    /**
     * 创建服务锁切面Bean
     *
     * @param lockInfoHandleFactory 锁信息处理器工厂
     * @param serviceLockFactory    服务锁工厂
     * @return ServiceLockAspect实例
     */
    @Bean
    public ServiceLockAspect serviceLockAspect(LockInfoHandleFactory lockInfoHandleFactory, ServiceLockFactory serviceLockFactory) {
        return new ServiceLockAspect(lockInfoHandleFactory, serviceLockFactory);
    }

    /**
     * 创建服务锁工具Bean
     *
     * @param lockInfoHandleFactory 锁信息处理器工厂
     * @param serviceLockFactory    服务锁工厂
     * @return ServiceLockTool实例
     */
    @Bean
    public ServiceLockTool serviceLockUtil(LockInfoHandleFactory lockInfoHandleFactory, ServiceLockFactory serviceLockFactory) {
        return new ServiceLockTool(lockInfoHandleFactory, serviceLockFactory);
    }
}
