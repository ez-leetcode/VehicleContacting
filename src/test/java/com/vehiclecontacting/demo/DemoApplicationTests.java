package com.vehiclecontacting.demo;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vehiclecontacting.config.RabbitmqConfig;
import com.vehiclecontacting.mapper.DiscussMapper;
import com.vehiclecontacting.mapper.FansMapper;
import com.vehiclecontacting.mapper.RoleMapper;
import com.vehiclecontacting.mapper.UserMapper;
import com.vehiclecontacting.pojo.Discuss;
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


    @Autowired
    private DiscussMapper discussMapper;

    /*
    @Autowired
    private SmsService smsService;

    @Test
    void sendSms(){
        smsService.sendSms("17605024713","52678",1);
    }
     */
    @Autowired
    private FansMapper fansMapper;


   @Test
    void fun(){
       Page<Discuss> page = new Page<>(0,2);
       List<Discuss> discussList = discussMapper.getFollowDiscuss(1393953426531430402L,page);
       System.out.println(discussList.toString());
       System.out.println(page.getTotal());
       System.out.println(page.getPages());
   }


}
