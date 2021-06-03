package com.vehiclecontacting.demo;


import com.alibaba.fastjson.JSONObject;
import com.vehiclecontacting.config.RabbitmqConfig;
import com.vehiclecontacting.config.RabbitmqProductConfig;
import com.vehiclecontacting.mapper.FansMapper;
import com.vehiclecontacting.msg.TalkMsg;
import com.vehiclecontacting.utils.CommentUtils;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.security.RunAs;
import javax.imageio.ImageIO;
import java.awt.*;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DemoApplicationTests {

    @Autowired
    private RabbitmqProductConfig rabbitmqProductConfig;

    @Test
    void ck(){
        String message = "{\n" +
                "\t\"fromId\":123,\t\n" +
                "\t\"toId\":1234,\n" +
                "\t\"info\":\"lxm\"\n" +
                "}";
        System.out.println(message);
        TalkMsg talkMsg = JSONObject.parseObject(message,TalkMsg.class);
        System.out.println(talkMsg.getInfo());
        System.out.println(talkMsg.toString());

    }

}