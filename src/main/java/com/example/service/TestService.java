package com.example.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


public interface TestService {

    void test(Integer id, Integer number);
    void  testV2(Integer id,Integer number);

    void  testV3(Integer id, Integer number);
}
