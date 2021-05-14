package com.vehiclecontacting;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;


@MapperScan("com.vehiclecontacting.mapper")
//redis缓存中间键
@EnableCaching
//配置定时任务
@EnableScheduling
//启动配置文件配置
@EnableConfigurationProperties
@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

}
