package com.vehiclecontacting.config;


import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitmqConfig {

    public static final String EXCHANGE_NAME = "xql_topic_exchange";

    public static final String QUEUE_NAME = "xql_queue";

    //1.交换机
    @Bean("bootExchange")
    public Exchange springbootExchange(){
        //durable持久化
        return ExchangeBuilder.topicExchange(EXCHANGE_NAME).durable(true).build();
    }

    //2.队列
    @Bean("bootQueue")
    public Queue springbootQueue(){
        return QueueBuilder.durable(QUEUE_NAME).build();
    }

    //3.队列和交互机绑定
    @Bean
    public Binding bindingQueueExchange(@Qualifier("bootQueue") Queue queue,@Qualifier("bootExchange") Exchange exchange){
        return BindingBuilder.bind(queue).to(exchange).with("boot.#").noargs();
    }


}
