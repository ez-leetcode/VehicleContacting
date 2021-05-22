package com.vehiclecontacting.demo;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vehiclecontacting.config.RabbitmqConfig;
import com.vehiclecontacting.mapper.FansMapper;
import com.vehiclecontacting.mapper.RoleMapper;
import com.vehiclecontacting.mapper.UserMapper;
import com.vehiclecontacting.pojo.Fans;
import com.vehiclecontacting.pojo.Role;
import com.vehiclecontacting.pojo.User;
import com.vehiclecontacting.service.SmsService;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class DemoApplicationTests {

    @Autowired
    private RabbitTemplate rabbitTemplate;


    /*
    @Autowired
    private SmsService smsService;

    @Test
    void sendSms(){
        smsService.sendSms("17605024713","52678",1);
    }
     */


   @Test
    void fun(){
       String s = "lqg";
       System.out.println(s.substring(1,s.length() - 1));
   }


}
