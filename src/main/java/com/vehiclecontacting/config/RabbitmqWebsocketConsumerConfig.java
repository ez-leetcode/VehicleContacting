package com.vehiclecontacting.config;

import com.alibaba.fastjson.JSONObject;
import com.vehiclecontacting.msg.TalkMsg;
import com.vehiclecontacting.service.WebsocketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RabbitListener(bindings = @QueueBinding(value = @Queue(value = RabbitmqWebsocketConfig.BROADCAST_QUEUE_NAME,durable = "true"),
        exchange = @Exchange(value = RabbitmqWebsocketConfig.FANOUT_EXCHANGE_NAME,type = "fanout")))
public class RabbitmqWebsocketConsumerConfig {

    //注入websocket
    @Autowired
    private WebsocketService websocketService;

    //监听发过来的消息
    @RabbitHandler
    public void ListenerQueue(Message message1, String message){
        log.info("已接收到消息");
        log.info(message1.toString());
        log.info(message);
        TalkMsg talkMsg = JSONObject.parseObject(message,TalkMsg.class);
        websocketService.sendMsg(talkMsg);
        log.info("消息消费成功");
    }


}
