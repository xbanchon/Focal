package com.xbanchon.imageservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    public static final String EXCHANGE_NAME = "image.exchange";
    public static final String QUEUE_NAME = "image.process.queue";
    public static final String ROUTING_KEY = "image.process.request";

    @Bean
    public Queue imageProcessQueue() {
        return new Queue(QUEUE_NAME, true);
    }

    @Bean
    public DirectExchange imageExchange() {
        return new DirectExchange(EXCHANGE_NAME);
    }

    @Bean
    public Binding binding(Queue imageProcessQueue, DirectExchange imageExchange) {
        return BindingBuilder.bind(imageProcessQueue).to(imageExchange).with(ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
