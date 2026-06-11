package com.xbanchon.imageservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    public static final String EXCHANGE_NAME = "image.exchange";

    // Main Queue
    public static final String QUEUE_NAME = "image.process.queue";
    public static final String ROUTING_KEY = "image.process.request";

    // Dead Letter Queue
    public static final String DLQ_NAME = "image.process.dlq";
    public static final String DLQ_ROUTING_KEY = "image.process.dlq.routing";

    // Process Completed Queue
    public static final String COMPLETED_QUEUE_NAME = "image.completed.queue";
    public static final String COMPLETED_ROUTING_KEY = "image.process.completed";

    @Bean
    public Queue deadLetterQueue() { return new Queue(DLQ_NAME, true); }

    @Bean
    public Binding dlqBinding(Queue deadLetterQueue, DirectExchange imageExchange) {
        return BindingBuilder.bind(deadLetterQueue).to(imageExchange).with(DLQ_ROUTING_KEY);
    }

    @Bean
    public Queue imageProcessQueue() {
        return QueueBuilder.durable(QUEUE_NAME)
                .withArgument("x-dead-letter-exchange", EXCHANGE_NAME)
                .withArgument("x-dead-letter-routing-key", DLQ_ROUTING_KEY)
                .build();
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
    public Queue completedQueue() {
        return new Queue(COMPLETED_QUEUE_NAME, true);
    }

    @Bean
    public Binding completedBinding(Queue completedQueue, DirectExchange imageExchange) {
        return BindingBuilder.bind(completedQueue).to(imageExchange).with(COMPLETED_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
