package com.learnsphere.notificationservice.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String QUEUE = "enrollment.notification.queue";

    public static final String EXCHANGE = "enrollment.exchange";

    public static final String ROUTING_KEY = "enrollment.confirmed";

    @Bean
    public Queue enrollmentNotificationQueue() {
        return new Queue(QUEUE);
    }

    @Bean
    public DirectExchange enrollmentExchange() {
        return new DirectExchange(EXCHANGE);
    }

    @Bean
    public Binding enrollmentBinding(
            Queue enrollmentNotificationQueue,
            DirectExchange enrollmentExchange) {

        return BindingBuilder
                .bind(enrollmentNotificationQueue)
                .to(enrollmentExchange)
                .with(ROUTING_KEY);
    }

    // Deserializes the JSON message into an EnrollmentConfirmed event
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
