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

import java.util.*;

@SpringBootTest
class DemoApplicationTests {


    @Autowired
    private FansMapper fansMapper;

    @Test
    void  ss(){
        Map<Long,Integer> map = new HashMap<>();
        map.put(12L,15);
        map.put(11L,13);
        map.put(18L,11);
        map.put(22L,55);
        List<Map.Entry<Long,Integer>> list = new ArrayList<>(map.entrySet());
        list.sort((Comparator.comparingInt(Map.Entry::getValue)));
        Long hotDiscuss1 = list.get(map.size() - 1).getKey();
        Long hotDiscuss2 = list.get(map.size() - 2).getKey();
        Long hotDiscuss3 = list.get(map.size() - 3).getKey();
        System.out.println(hotDiscuss1);
        System.out.println(hotDiscuss2);
        System.out.println(hotDiscuss3);
    }


}
