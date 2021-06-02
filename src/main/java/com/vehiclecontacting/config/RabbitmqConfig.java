package com.vehiclecontacting.config;


import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class RabbitmqConfig {

    public static final String EXCHANGE_NAME = "xql_exchange";

    public static final String QUEUE_NAME = "xql_queue";

    public static final String ROUTING_KEY = "xql_routing_key";

    //1.交换机
    @Bean
    public DirectExchange directExchange(){
        //durable持久化
        log.info("交换机创建成功");
        return new DirectExchange(EXCHANGE_NAME);
    }

    //2.队列
    @Bean
    public Queue msgQueue(){
        //持久化
        log.info("消息队列创建成功");
        return new Queue(QUEUE_NAME,true);
    }


    //3.队列和交互机绑定
    @Bean
    public Binding bindingQueueExchange(){
        log.info("消息队列与交换机绑定成功");
        return BindingBuilder.bind(msgQueue()).to(directExchange()).with(ROUTING_KEY);
    }


}