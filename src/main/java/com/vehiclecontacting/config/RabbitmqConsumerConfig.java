package com.vehiclecontacting.config;

import com.alibaba.fastjson.JSONObject;
import com.vehiclecontacting.msg.MailMsg;
import com.vehiclecontacting.service.MailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
@Slf4j
@RabbitListener(bindings = @QueueBinding(value = @Queue(value = RabbitmqConfig.QUEUE_NAME,durable = "true"),
exchange = @Exchange(value = RabbitmqConfig.EXCHANGE_NAME),key = RabbitmqConfig.ROUTING_KEY))
public class RabbitmqConsumerConfig {

      @Autowired
      private MailService mailService;


      @RabbitHandler
      public void ListenerQueue(Message message,JSONObject jsonObject){
          log.info("已接收到消息");
          log.info(message.toString());
          log.info(jsonObject.toString());
          MailMsg mailMsg = jsonObject.toJavaObject(MailMsg.class);
          log.info(mailMsg.toString());
          mailService.sendEmail(mailMsg.getEmail(),mailMsg.getYzm(),mailMsg.getFunction());
     }

}
