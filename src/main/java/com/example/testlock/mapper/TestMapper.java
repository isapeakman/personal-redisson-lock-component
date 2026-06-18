package com.example.testlock.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.testlock.pojo.Test;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TestMapper extends BaseMapper<Test> {

}
