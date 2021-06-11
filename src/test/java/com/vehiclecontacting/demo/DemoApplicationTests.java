package com.vehiclecontacting.demo;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.vehiclecontacting.config.*;
import com.vehiclecontacting.mapper.FansMapper;
import com.vehiclecontacting.mapper.UserMapper;
import com.vehiclecontacting.msg.TalkMsg;
import com.vehiclecontacting.msg.WebsocketMsg;
import com.vehiclecontacting.pojo.BoxMessage;
import com.vehiclecontacting.pojo.User;
import com.vehiclecontacting.service.WebsocketService;
import com.vehiclecontacting.utils.CommentUtils;
import com.vehiclecontacting.utils.RedisUtils;
import com.vehiclecontacting.utils.TalkResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import springfox.documentation.spring.web.json.Json;

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
@Slf4j
class DemoApplicationTests {

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private BloomFilterConfig bloomFilterConfig;

    @Test
    void fun(){
        System.out.println(redisUtils.includeByBloomFilter(bloomFilterConfig,"xql","1395933122936774658"));
        redisUtils.addByBloomFilter(bloomFilterConfig,"xql","1395933122936774658");
        System.out.println(redisUtils.includeByBloomFilter(bloomFilterConfig,"xql","1395933122936774658"));
    }


}