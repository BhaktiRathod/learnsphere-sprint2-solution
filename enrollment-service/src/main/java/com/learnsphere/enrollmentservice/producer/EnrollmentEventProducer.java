package com.learnsphere.enrollmentservice.producer;

import com.learnsphere.enrollmentservice.config.RabbitMQConfig;
import com.learnsphere.enrollmentservice.event.EnrollmentConfirmed;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class EnrollmentEventProducer {

    private final RabbitTemplate rabbitTemplate;

    public EnrollmentEventProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    // Publishes an EnrollmentConfirmed event to RabbitMQ.
    public void sendEnrollmentConfirmed(EnrollmentConfirmed event) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE,
                RabbitMQConfig.ROUTING_KEY,
                event
        );
    }

    // Test method: publishes the same event twice to simulate duplicate delivery.
    public void sendDuplicateEnrollmentConfirmed(EnrollmentConfirmed event) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE,
                RabbitMQConfig.ROUTING_KEY,
                event
        );

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE,
                RabbitMQConfig.ROUTING_KEY,
                event
        );
    }
}
