package com.example.servicelock.core;


import com.example.servicelock.enums.LockType;
import com.example.servicelock.lock.ServiceLocker;
import com.example.servicelock.lock.impl.RedissonFairLocker;
import com.example.servicelock.lock.impl.RedissonReadLocker;
import com.example.servicelock.lock.impl.RedissonReentrantLocker;
import com.example.servicelock.lock.impl.RedissonWriteLocker;
import org.redisson.api.RedissonClient;

import java.util.HashMap;
import java.util.Map;

import static com.example.servicelock.enums.LockType.*;

/**
 * 分布式锁管理器：锁缓存
 **/
public class LockManager {

    private final Map<LockType, ServiceLocker> cacheLocker = new HashMap<>();
    
    public LockManager(RedissonClient redissonClient){
        cacheLocker.put(Reentrant,new RedissonReentrantLocker(redissonClient));
        cacheLocker.put(Fair,new RedissonFairLocker(redissonClient));
        cacheLocker.put(Write,new RedissonWriteLocker(redissonClient));
        cacheLocker.put(Read,new RedissonReadLocker(redissonClient));
    }
    
    public ServiceLocker getReentrantLocker(){
        return cacheLocker.get(Reentrant);
    }
    
    public ServiceLocker getFairLocker(){
        return cacheLocker.get(Fair);
    }
    
    public ServiceLocker getWriteLocker(){
        return cacheLocker.get(Write);
    }
    
    public ServiceLocker getReadLocker(){
        return cacheLocker.get(Read);
    }
}
