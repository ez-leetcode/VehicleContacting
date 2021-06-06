package com.vehiclecontacting.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RabbitmqWebsocketProductConfig {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    //给fanoutExchange发送消息
    public void sendMessageToFanoutExchange(String message){
        log.info("正在给交换机发送消息，talkMsg：" + message);
        //TalkMsg talkMsg = JSONObject.parseObject(message,TalkMsg.class);
        //中间是设置路由规则，由于是广播模式，这个规则会被抛弃，但是这个字段一定要写上，
        //如果不写上会造成交换机把要转发的内容当做是路由规则直接抛弃，导致消费者监听到的队列中没有数据
        rabbitTemplate.convertAndSend(RabbitmqWebsocketConfig.FANOUT_EXCHANGE_NAME,"hello",message);
    }


}
