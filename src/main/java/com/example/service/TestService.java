package com.example.service;

import com.example.annotation.MyLock;
import org.springframework.stereotype.Service;
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
}
