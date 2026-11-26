package com.learnsphere.notificationservice;

import com.learnsphere.notificationservice.consumer.NotificationConsumer;
import com.learnsphere.notificationservice.controller.NotificationStatusController;
import com.learnsphere.notificationservice.event.EnrollmentConfirmed;
import com.learnsphere.notificationservice.service.NotificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@SpringBootTest(properties = {
        "eureka.client.enabled=false",
        "spring.rabbitmq.listener.simple.auto-startup=false"
})
class NotificationServiceApplicationTests {

    @Test
    @DisplayName("Verify notification-service context loads independently")
    void contextLoads() {
    }

    @Test
    @DisplayName("Verify NotificationStatusController GET /api/notifications/status")
    void testNotificationStatusController() {
        NotificationStatusController controller = new NotificationStatusController();
        ResponseEntity<String> response = controller.getStatus();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("Notification Service is running");
    }

    @Test
    @DisplayName("A new EnrollmentConfirmed event produces a notification")
    void testNewEventIsProcessed() {
        NotificationService service = new NotificationService();

        boolean processed = service.processEnrollmentConfirmed(
                new EnrollmentConfirmed("event-101", 1L, 101L, 201L));

        assertThat(processed).isTrue();
    }

    @Test
    @DisplayName("The same eventId delivered again is ignored (no duplicate notification)")
    void testDuplicateEventIsIgnored() {
        NotificationService service = new NotificationService();

        boolean first = service.processEnrollmentConfirmed(
                new EnrollmentConfirmed("event-dup-1", 2L, 102L, 202L));
        boolean second = service.processEnrollmentConfirmed(
                new EnrollmentConfirmed("event-dup-1", 2L, 102L, 202L));

        assertThat(first).isTrue();
        assertThat(second).isFalse();
    }

    @Test
    @DisplayName("NotificationConsumer delegates received events to NotificationService")
    void testConsumerDelegatesToService() {
        NotificationService service = mock(NotificationService.class);
        EnrollmentConfirmed event = new EnrollmentConfirmed("event-3", 3L, 103L, 203L);

        new NotificationConsumer(service).receiveEnrollmentConfirmed(event);

        verify(service).processEnrollmentConfirmed(event);
    }
}
