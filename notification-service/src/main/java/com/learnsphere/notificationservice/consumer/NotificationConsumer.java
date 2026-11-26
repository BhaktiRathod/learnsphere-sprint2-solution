package com.learnsphere.notificationservice.consumer;

import com.learnsphere.notificationservice.config.RabbitMQConfig;
import com.learnsphere.notificationservice.event.EnrollmentConfirmed;
import com.learnsphere.notificationservice.service.NotificationService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationConsumer {

    private final NotificationService notificationService;

    public NotificationConsumer(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE)
    public void receiveEnrollmentConfirmed(EnrollmentConfirmed event) {

        System.out.println("Received EnrollmentConfirmed event: " + event.getEventId());

        // Pass the received event to the service layer.
        if (notificationService.saveNotification(event)) {
            System.out.println("Notification saved to MongoDB");
        }
    }
}
