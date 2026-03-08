package com.vv.cloudfarming.order.mq.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MqConfig {

    @Bean
    public DirectExchange orderEventExchange() {
        return new DirectExchange("order-event-exange", true, false);
    }

    @Bean
    public Queue orderSeckillQueue(){
        return new Queue("order.seckill.queue",true,false,false);
    }

    @Bean
    public Binding orderSeckillQueueBinding(){
        return new Binding("order.seckill.queue",
                Binding.DestinationType.QUEUE,
                "order-event-exange",
                "order.seckill.order",
                null);
    }
}
