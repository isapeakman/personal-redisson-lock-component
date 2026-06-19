package com.example.testlock.controller;

import com.example.testlock.pojo.Test;
import com.example.testlock.service.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {
    @Autowired
    private TestService testService;
    @PostMapping(path="v1")
    public String test(Integer id, Integer number) {
        testService.test(id, number);
        return "testV1";
    }
    @PostMapping(path = "/v2")
    public String testV2(Integer id, Integer number) {
        testService.testV2(id, number);
        return "testV2";
    }
    @PostMapping(path = "/v3")
    public String testV3(Integer id, Integer number) {
        testService.testV3(id, number);
        return "testV3";
    }
    @PostMapping(path = "/v4")
    public String testV4(Integer id, Integer number) {
        testService.testV4(id, number);
        return "testV4";
    }
    @PostMapping(path = "/v5")
    public String testV5(Integer id, Integer number) {
        testService.testV5(id, number);
        return "testV5";
    }
    @PostMapping(path = "/v6")
    public String testV6(Integer id, Integer number) {
        testService.testV6(id, number);
        return "testV6";
    }
    @PostMapping(path = "/v7")
    public String testV7(Integer id, Integer number) {
        testService.testV7(new Test(id,number));
        return "testV7";
    }
}
