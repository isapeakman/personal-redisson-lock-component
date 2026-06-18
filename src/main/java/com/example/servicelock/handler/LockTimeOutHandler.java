package com.example.servicelock.handler;

/**
 * 分布式锁 超时处理接口
 **/
public interface LockTimeOutHandler {
    
    /**
     * 处理
     * @param lockName 锁名
     * */
    void handler(String lockName);
}
