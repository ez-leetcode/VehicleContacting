package com.vehiclecontacting.config;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;


@Slf4j
@Component
public class RabbitmqProductConfig {

    @Autowired
    private RabbitTemplate rabbitTemplate;


    //发送邮件
    public void sendMsg(JSONObject jsonObject){
        log.info("正在发送邮件消息");
        log.info(jsonObject.toString());
        CorrelationData correlationData = new CorrelationData(UUID.randomUUID().toString());
        //把消息放入对应的路由中去
        rabbitTemplate.convertAndSend(RabbitmqConfig.EXCHANGE_NAME,RabbitmqConfig.ROUTING_KEY,jsonObject,correlationData);
    }

    //发送消息盒子的消息
    public void sendBoxMessage(JSONObject jsonObject){
        log.info("正在发送消息盒子消息");
        log.info(jsonObject.toString());
        CorrelationData correlationData = new CorrelationData(UUID.randomUUID().toString());
        //把消息放到对应的路由中去
        rabbitTemplate.convertAndSend(RabbitmqConfig.BOX_EXCHANGE_NAME,RabbitmqConfig.BOX_ROUTING_KEY,jsonObject,correlationData);
    }

    public void sendQuartzMessage(JSONObject jsonObject){
        log.info("正在发送定时任务消息");
        log.info(jsonObject.toString());
        CorrelationData correlationData = new CorrelationData(UUID.randomUUID().toString());
        rabbitTemplate.convertAndSend(RabbitmqConfig.QUARTZ_EXCHANGE_NAME,RabbitmqConfig.QUARTZ_ROUTING_KEY,jsonObject,correlationData);
    }



}