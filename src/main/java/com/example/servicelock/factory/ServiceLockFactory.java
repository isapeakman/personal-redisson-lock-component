package com.example.servicelock.factory;


import com.example.servicelock.core.LockManager;
import com.example.servicelock.enums.LockType;
import com.example.servicelock.lock.ServiceLocker;
import lombok.AllArgsConstructor;

/**
 * 分布式锁工厂:提供四种锁实例
 **/
@AllArgsConstructor
public class ServiceLockFactory {
    
    private final LockManager lockManager;
    

    public ServiceLocker getLock(LockType lockType){
        ServiceLocker lock;
        switch (lockType) {
            case Fair:
                lock = lockManager.getFairLocker();
                break;
            case Write:
                lock = lockManager.getWriteLocker();
                break;
            case Read:
                lock = lockManager.getReadLocker();
                break;
            default:
                lock = lockManager.getReentrantLocker();
                break;
        }
        return lock;
    }
}
