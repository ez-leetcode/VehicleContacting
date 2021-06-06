package com.vehiclecontacting.config;

import com.alibaba.fastjson.JSONObject;
import com.vehiclecontacting.msg.MailMsg;
import com.vehiclecontacting.pojo.BoxMessage;
import com.vehiclecontacting.service.MailService;
import com.vehiclecontacting.service.WebsocketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
@Slf4j
public class RabbitmqConsumerConfig {

      @Autowired
      private MailService mailService;

      @Autowired
      private WebsocketService websocketService;


      @RabbitListener(bindings = @QueueBinding(value = @Queue(value = RabbitmqConfig.QUEUE_NAME,durable = "true"),
            exchange = @Exchange(value = RabbitmqConfig.EXCHANGE_NAME),key = RabbitmqConfig.ROUTING_KEY))
      @RabbitHandler
      public void ListenerQueue(Message message,JSONObject jsonObject){
          log.info("已接收到邮件消息");
          log.info(message.toString());
          log.info(jsonObject.toString());
          MailMsg mailMsg = jsonObject.toJavaObject(MailMsg.class);
          log.info(mailMsg.toString());
          mailService.sendEmail(mailMsg.getEmail(),mailMsg.getYzm(),mailMsg.getFunction());
     }


      @RabbitListener(bindings = @QueueBinding(value = @Queue(value = RabbitmqConfig.QUARTZ_QUEUE_NAME,durable = "true"),
            exchange = @Exchange(value = RabbitmqConfig.QUARTZ_EXCHANGE_NAME),key = RabbitmqConfig.QUARTZ_ROUTING_KEY))
      @RabbitHandler
      public void ListenerQuartzQueue(Message message,JSONObject jsonObject){
          log.info("已接收到定时任务消息盒子消息");
          log.info(message.toString());
          log.info(jsonObject.toString());
          //待完成
          BoxMessage boxMessage = jsonObject.toJavaObject(BoxMessage.class);
          log.info(boxMessage.toString());
          websocketService.sendBoxMsg(boxMessage);
     }

     @RabbitListener(bindings = @QueueBinding(value = @Queue(value = RabbitmqConfig.BOX_QUEUE_NAME,durable = "true"),
             exchange = @Exchange(value = RabbitmqConfig.BOX_EXCHANGE_NAME),key = RabbitmqConfig.BOX_ROUTING_KEY))
     @RabbitHandler
     public void ListenerBoxQueue(Message message,JSONObject jsonObject){
          log.info("已接收到消息盒子消息");
          log.info(message.toString());
          log.info(jsonObject.toString());
          //待完成
          BoxMessage boxMessage = jsonObject.toJavaObject(BoxMessage.class);
          log.info(boxMessage.toString());
          websocketService.sendBoxMsg(boxMessage);
     }

}
