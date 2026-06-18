package com.example.service;

import com.example.mapper.TestMapper;
import com.example.pojo.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TestServiceImpl implements TestService{
    @Autowired
    private TestMapper testMapper;
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void test(Integer id,Integer number) {
        Test test = testMapper.selectById(id);
        Integer originalNumber = test.getNumber();
        originalNumber = originalNumber + number;
        test.setNumber(originalNumber);
        testMapper.updateById(test);
        System.out.println("更新成功"+originalNumber+"->"+test.getNumber());
    }
}
