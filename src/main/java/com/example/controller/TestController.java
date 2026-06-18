package com.example.controller;

import com.example.service.TestService;
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
}
