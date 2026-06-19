package com.example.testlock.service;


import com.example.servicelock.annotation.ServiceLock;
import com.example.servicelock.enums.LockType;
import com.example.testlock.pojo.Test;
import org.springframework.transaction.annotation.Transactional;

public interface TestService {
    /**
     * 事务()
     */
    void test(Integer id, Integer number);

    /**
     * 事务(锁)
     */
    void  testV2(Integer id,Integer number);

    /**
     * 锁(编程式事务)
     */
    void  testV3(Integer id, Integer number);

    /**
     * 切面锁(编程式事务)
     */
    void  testV4(Integer id, Integer number);

    /**
     * 切面锁(声明式事务)
     */
    void testV5(Integer id, Integer number);

    @ServiceLock(lockType = LockType.Reentrant,name = "test", keys = {"id","number"})
    @Transactional
    void testV6(Integer id, Integer number);

    @ServiceLock(lockType = LockType.Reentrant,name = "test", keys = {"#testDto.id","#testDto.number"})
    @Transactional
    void testV7(Test testDto);
}
