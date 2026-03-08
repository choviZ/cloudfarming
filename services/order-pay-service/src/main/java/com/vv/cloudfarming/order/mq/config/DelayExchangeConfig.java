package com.vv.cloudfarming.order.mq.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DelayExchangeConfig {

    @Bean
    public DirectExchange delayExchange() {
        return ExchangeBuilder
                .directExchange("delay.direct")
                .delayed()
                .durable(true)
                .build();
    }

    @Bean
    public Queue delayedQueue() {
        return new Queue("delay.queue");
    }

    @Bean
    public Binding delayQueueBinding() {
        return BindingBuilder.bind(delayedQueue())
                .to(delayExchange())
                .with("delay");
    }

    @Bean
    public MessageConverter jacksonMessageConverter(ObjectMapper objectMapper) {
        ObjectMapper rabbitObjectMapper = objectMapper.copy()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return new Jackson2JsonMessageConverter(rabbitObjectMapper);
    }
}