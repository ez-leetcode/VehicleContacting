package com.vehiclecontacting.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


//为了给分布式开发做铺垫，用户来回发消息使用广播模式
@Slf4j
@Configuration
public class RabbitmqWebsocketConfig {

    public static final String BROADCAST_QUEUE_NAME = "xql_broadcast_queue";

    public static final String FANOUT_EXCHANGE_NAME = "xql_fanout_exchange";


    //创建广播队列
    @Bean
    public Queue broadcastQueue(){
        log.info("创建了广播队列");
        //持久化
        return new Queue(BROADCAST_QUEUE_NAME,true);
    }

    //创建广播交换机
    @Bean
    public FanoutExchange fanoutExchange(){
        log.info("创建了交换机");
        return new FanoutExchange(FANOUT_EXCHANGE_NAME);
    }

    //将广播队列绑定交换机
    @Bean
    public Binding binding(){
        log.info("正在将队列绑定交换机");
        return BindingBuilder.bind(broadcastQueue()).to(fanoutExchange());
    }


}
