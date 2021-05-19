package com.vehiclecontacting.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RabbitmqListenerConfig {

    @RabbitListener(queues = "xql_queue")
    public void ListenerQueue(Message message){
        log.info(message.toString());
    }

}
