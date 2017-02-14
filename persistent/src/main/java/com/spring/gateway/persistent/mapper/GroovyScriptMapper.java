package com.spring.gateway.persistent.mapper;


import com.spring.gateway.persistent.entity.GroovyScript;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.context.annotation.Bean;

/**
 * Created by Steven on 2017/2/14.
 */
@Mapper
public interface GroovyScriptMapper {
    @Select("select * from groovy_script")
    GroovyScript[] getAll();
}
