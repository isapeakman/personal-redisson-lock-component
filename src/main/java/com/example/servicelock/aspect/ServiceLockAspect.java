package com.example.servicelock.aspect;


import com.example.servicelock.annotation.ServiceLock;
import com.example.servicelock.enums.LockType;
import com.example.servicelock.factory.LockInfoHandleFactory;
import com.example.servicelock.factory.ServiceLockFactory;
import com.example.servicelock.info.LockInfoHandle;
import com.example.servicelock.lock.ServiceLocker;
import jodd.util.StringUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * 分布式锁 切面
 **/
@Slf4j
@Aspect
@Order(-10)
@AllArgsConstructor
public class ServiceLockAspect {
    
    private final LockInfoHandleFactory lockInfoHandleFactory;
    
    private final ServiceLockFactory serviceLockFactory;
    public static final String SERVICE_LOCK = "service_lock";
    

    @Around("@annotation(servicelock)")
    public Object around(ProceedingJoinPoint joinPoint, ServiceLock servicelock) throws Throwable {
        // 通过锁信息处理工厂获取指定类型的锁信息处理器
        LockInfoHandle lockInfoHandle = lockInfoHandleFactory.getLockInfoHandle(SERVICE_LOCK);
        // 获取锁名称，通过切点、服务锁注解的名称和键来生成
        String lockName = lockInfoHandle.getLockName(joinPoint, servicelock.name(),servicelock.keys());
        // 获取锁类型
        LockType lockType = servicelock.lockType();
        // 获取等待时间
        long waitTime = servicelock.waitTime();
        // 获取时间单位
        TimeUnit timeUnit = servicelock.timeUnit();

        // 通过服务锁工厂获取指定类型的锁实例
        ServiceLocker lock = serviceLockFactory.getLock(lockType);
        // 尝试获取锁，传入锁名称、时间单位和等待时间
        boolean result = lock.tryLock(lockName, timeUnit, waitTime);

        // 如果成功获取到锁
        if (result) {
            try {
                // 执行目标方法
                return joinPoint.proceed();
            }finally{
                // 确保在方法执行完成后释放锁
                lock.unlock(lockName);
            }
        }else {
            // 如果获取锁超时，记录警告日志
            log.warn("Timeout while acquiring serviceLock:{}",lockName);
            // 检查是否有自定义的锁超时处理策略
            String customLockTimeoutStrategy = servicelock.customLockTimeoutStrategy();
            if (StringUtil.isNotEmpty(customLockTimeoutStrategy)) {
                // 如果有自定义策略，则执行自定义处理逻辑
                return handleCustomLockTimeoutStrategy(customLockTimeoutStrategy, joinPoint);
            }else{
                // 否则执行默认的锁超时处理策略
                servicelock.lockTimeoutStrategy().handler(lockName);
            }
            // 继续执行目标方法
            return joinPoint.proceed();
        }
    }

    public Object handleCustomLockTimeoutStrategy(String customLockTimeoutStrategy,JoinPoint joinPoint) {
        // prepare invocation context
        Method currentMethod = ((MethodSignature) joinPoint.getSignature()).getMethod();
        Object target = joinPoint.getTarget();
        Method handleMethod = null;
        try {
            handleMethod = target.getClass().getDeclaredMethod(customLockTimeoutStrategy, currentMethod.getParameterTypes());
            handleMethod.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Illegal annotation param customLockTimeoutStrategy :" + customLockTimeoutStrategy,e);
        }
        Object[] args = joinPoint.getArgs();

        // invoke
        Object result;
        try {
            result = handleMethod.invoke(target, args);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Fail to illegal access custom lock timeout handler: " + customLockTimeoutStrategy ,e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException("Fail to invoke custom lock timeout handler: " + customLockTimeoutStrategy ,e);
        }
        return result;
    }
}
