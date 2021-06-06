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

    public static final String QUARTZ_EXCHANGE_NAME = "xql_quartz_exchange";

    public static final String QUARTZ_QUEUE_NAME = "xql_quartz_queue";

    public static final String QUARTZ_ROUTING_KEY = "xql_routing_key";

    public static final String BOX_EXCHANGE_NAME = "xql_box_exchange";

    public static final String BOX_QUEUE_NAME = "xql_box_queue";

    public static final String BOX_ROUTING_KEY = "xql_routing_key";

    //1.交换机
    @Bean
    public DirectExchange directExchange(){
        //durable持久化
        log.info("邮件交换机创建成功");
        return new DirectExchange(EXCHANGE_NAME);
    }

    //2.队列
    @Bean
    public Queue msgQueue(){
        //持久化
        log.info("邮件消息队列创建成功");
        return new Queue(QUEUE_NAME,true);
    }


    //3.队列和交互机绑定
    @Bean
    public Binding bindingQueueExchange(){
        log.info("邮件消息队列与交换机绑定成功");
        return BindingBuilder.bind(msgQueue()).to(directExchange()).with(ROUTING_KEY);
    }


    @Bean
    public DirectExchange quartzDirectExchange(){
        log.info("定时任务交换机创建成功");
        return new DirectExchange(QUARTZ_EXCHANGE_NAME);
    }


    @Bean
    public Queue quartzQueue(){
        log.info("定时任务消息队列创建成功");
        return new Queue(QUARTZ_QUEUE_NAME,true);
    }

    @Bean
    public Binding bindingQuartzQueueExchange(){
        log.info("定时任务队列与交换机绑定成功");
        return BindingBuilder.bind(quartzQueue()).to(quartzDirectExchange()).with(QUARTZ_ROUTING_KEY);
    }

    @Bean
    public DirectExchange boxQueueExchange(){
        log.info("消息盒子交换机创建成功");
        return new DirectExchange(BOX_EXCHANGE_NAME);
    }

    @Bean
    public Queue boxQueue(){
        log.info("消息盒子消息队列创建成功");
        return new Queue(BOX_QUEUE_NAME,true);
    }

    @Bean
    public Binding bindingBoxQueueExchange(){
        log.info("消息盒子消息队列和交换机绑定成功");
        return BindingBuilder.bind(boxQueue()).to(boxQueueExchange()).with(BOX_ROUTING_KEY);
    }

}