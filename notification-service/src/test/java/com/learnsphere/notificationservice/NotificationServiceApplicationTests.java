package com.learnsphere.notificationservice;

import com.learnsphere.notificationservice.controller.NotificationStatusController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
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
}
