package com.vehiclecontacting.demo;

import com.vehiclecontacting.config.RabbitmqConfig;
import com.vehiclecontacting.mapper.RoleMapper;
import com.vehiclecontacting.pojo.Role;
import com.vehiclecontacting.service.SmsService;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

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
    void testSend(){
        rabbitTemplate.convertAndSend(RabbitmqConfig.EXCHANGE_NAME,"boot.gx","hello gaoxu");
    }


}
