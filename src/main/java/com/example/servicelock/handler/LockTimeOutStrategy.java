package com.example.servicelock.handler;


/**
 *  锁超时处理策略
 **/
public enum LockTimeOutStrategy implements LockTimeOutHandler{

    /**
     * 快速失败
     * FAIL 是 LockTimeOutStrategy 这个枚举类的实例，以匿名内部类实现hanlder。
     * */
    FAIL(){
        @Override
        public void handler(String lockName) {
            String msg = String.format("%s请求频繁",lockName);
            throw new RuntimeException(msg);
        }
    }
}
