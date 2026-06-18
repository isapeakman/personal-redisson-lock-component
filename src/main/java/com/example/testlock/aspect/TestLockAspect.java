package com.example.testlock.aspect;

import com.example.testlock.annotation.TestLock;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.concurrent.locks.ReentrantLock;

/**
 * 锁切面
 */
@Aspect
@Component
@Order(1) // 保证锁的切面在事务切面外面，保证锁的释放在事务的提交之后
public class TestLockAspect {
    //解析出加锁的键
    ReentrantLock lock = new ReentrantLock();
    // 基于注解
    @Around("@annotation(testLock)")
    public Object around(ProceedingJoinPoint joinPoint, TestLock testLock) throws Throwable {
        //进行加锁
        lock.lock();
        //如果加锁成功
        try {
            //执行业务逻辑
            return joinPoint.proceed();
        } finally {
            //解锁
            lock.unlock();
        }
    }
}
