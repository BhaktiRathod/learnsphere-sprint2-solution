package com.learnsphere.enrollmentservice.config;

import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "enrollment.exchange";
    public static final String ROUTING_KEY = "enrollment.confirmed";

    @Bean
    public DirectExchange enrollmentExchange() {
        return new DirectExchange(EXCHANGE);
    }

    // Serializes the EnrollmentConfirmed event as JSON
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
