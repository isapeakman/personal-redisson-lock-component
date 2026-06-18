package com.example.testlock.service;

import com.example.testlock.annotation.TestLock;
import com.example.testlock.mapper.TestMapper;
import com.example.testlock.pojo.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class TestServiceImpl implements TestService {
    @Autowired
    private TestMapper testMapper;
    /**
     * 事务()
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void test(Integer id, Integer number) {
        Test test = testMapper.selectById(id);
        Integer originalNumber = test.getNumber();
        test.setNumber(originalNumber + number);
        testMapper.updateById(test);
        System.out.println("更新成功" + originalNumber + "->" + test.getNumber());
    }

    /**
     * 事务(锁)
     * 1.AOP代理拦截方法调用，开启数据库事务。
     * 2.进入目标方法，获取 synchronized 锁。
     * 3.执行数据库查询与更新操作。
     * 4.释放 synchronized 锁。
     * 5.方法返回，AOP代理提交数据库事务。
     * 致命问题：当线程A执行完第4步释放锁时，第5步的事务提交还未完成。
     * 此时线程B可以获取到锁并读取到尚未提交的旧数据，从而产生脏读，
     * 最终导致数据更新丢失，synchronized 形同虚设
     * 最推荐的做法是使用编程式事务（TransactionTemplate）将事务的控制放在同步锁内部，从而保证事务的提交和回滚都在锁的保护范围内完成
     *
     * @param id
     * @param number
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void testV2(Integer id, Integer number) {
        // 直接使用 synchronized (id) 无法可靠地锁住前端传来的同一个 id。
        //在 Java 中，synchronized 锁住的是对象头，也就是判断两个线程是否争抢同一个锁，取决于它们传入的对象引用是否是内存中的同一个对象（即 == 判断，比较的是内存地址）。
        //自动装箱陷阱：前端传来的值在 Spring MVC 绑定到 Integer id 时，会经历自动装箱（Integer.valueOf()）。Java 为了节省内存，对 -128 到 127 之间的 Integer 值使用了缓存池，这个范围内相同的值确实是同一个对象。
        //对象不一致：当 id 的值超出 127 或小于 -128 时，Integer.valueOf() 会在堆内存中新建一个对象。这意味着，即使前端同时传来两个 id=128 的并发请求，方法接收到的两个 Integer id 是两个完全不同的内存对象。
        //锁失效：由于两个线程持有的 id 对象内存地址不同，synchronized (id) 会认为它们是两把不同的锁，两个线程会并行执行，根本达不到互斥的效果。
        synchronized (id) {//不过这里上传的是[1,127]范围的数字，所以不会发生锁失效。
            Test test = testMapper.selectById(id);
            Integer originalNumber = test.getNumber();
            test.setNumber(originalNumber + number);
            testMapper.updateById(test);
            System.out.println("更新成功" + originalNumber + "->" + test.getNumber());
        }
    }

    final Object lock = new Object();
    @Autowired
    TransactionTemplate transactionTemplate;
    /**
     * 锁(编程式事务)
     */
    @Override
    public void testV3(Integer id, Integer number) {
        synchronized (lock) {
            // 编程式事务
            transactionTemplate.execute(status -> {
                Test test = testMapper.selectById(id);
                Integer originalNumber = test.getNumber();
                test.setNumber(originalNumber + number);
                testMapper.updateById(test);
                System.out.println("更新成功" + originalNumber + "->" + test.getNumber());
                return null;
            });
        }
    }
    /**
     * 切面锁(编程式事务)
     */
    @Override
    @TestLock
    public void testV4(Integer id, Integer number) {
        // 编程式事务
        transactionTemplate.execute(status -> {
            Test test = testMapper.selectById(id);
            Integer originalNumber = test.getNumber();
            test.setNumber(originalNumber + number);
            testMapper.updateById(test);
            System.out.println("更新成功" + originalNumber + "->" + test.getNumber());
            return null;
        });
    }
    /**
     * 切面锁(声明式事务)
     */
    @Override
    @TestLock
    @Transactional
    public void testV5(Integer id, Integer number) {
        Test test = testMapper.selectById(id);
        Integer originalNumber = test.getNumber();
        test.setNumber(originalNumber + number);
        testMapper.updateById(test);
        System.out.println("更新成功" + originalNumber + "->" + test.getNumber());
    }
}
