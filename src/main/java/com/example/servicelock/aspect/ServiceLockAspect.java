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

import static com.example.servicelock.constant.LockInfoType.SERVICE_LOCK;

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

    /**
     * 处理自定义锁超时策略的方法
     * @param customLockTimeoutStrategy 自定义锁超时处理策略的方法名
     * @param joinPoint 切入点对象，用于获取目标方法、参数等信息
     * @return 调用自定义策略方法后的返回结果
     */
    public Object handleCustomLockTimeoutStrategy(String customLockTimeoutStrategy,JoinPoint joinPoint) {
        // 获取当前调用的方法对象
        Method currentMethod = ((MethodSignature) joinPoint.getSignature()).getMethod();
        // 获取目标对象
        Object target = joinPoint.getTarget();
        // 声明一个用于处理自定义锁超时策略的方法对象，初始值为null
        Method handleMethod = null;
        try {
            // 获取目标类中声明的指定名称的方法，参数类型与当前方法相同
            handleMethod = target.getClass().getDeclaredMethod(customLockTimeoutStrategy, currentMethod.getParameterTypes());
            // 设置该方法为可访问，即使它是私有的
            handleMethod.setAccessible(true);
        } catch (NoSuchMethodException e) {
            // 如果找不到指定的方法，抛出运行时异常，提示非法的注解参数
            throw new RuntimeException("Illegal annotation param customLockTimeoutStrategy :" + customLockTimeoutStrategy,e);
        }
        // 获取方法的参数列表
        Object[] args = joinPoint.getArgs();

        Object result;
        try {
            // 使用目标对象和参数列表调用处理方法
            result = handleMethod.invoke(target, args);
        } catch (IllegalAccessException e) {
            // 如果无法访问方法，抛出运行时异常
            throw new RuntimeException("Fail to illegal access custom lock timeout handler: " + customLockTimeoutStrategy ,e);
        } catch (InvocationTargetException e) {
            // 如果方法调用抛出异常，抛出运行时异常
            throw new RuntimeException("Fail to invoke custom lock timeout handler: " + customLockTimeoutStrategy ,e);
        }
        // 返回方法调用的结果
        return result;
    }
}
