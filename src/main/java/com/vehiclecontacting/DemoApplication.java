package com.vehiclecontacting;

import com.google.common.base.Charsets;
import com.google.common.hash.Funnel;
import com.vehiclecontacting.config.BloomFilterConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;


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
