package com.learnsphere.notificationservice;

import com.learnsphere.notificationservice.consumer.NotificationConsumer;
import com.learnsphere.notificationservice.controller.NotificationController;
import com.learnsphere.notificationservice.controller.NotificationStatusController;
import com.learnsphere.notificationservice.document.Notification;
import com.learnsphere.notificationservice.event.EnrollmentConfirmed;
import com.learnsphere.notificationservice.exception.NotificationPersistenceException;
import com.learnsphere.notificationservice.repository.NotificationRepository;
import com.learnsphere.notificationservice.service.NotificationService;
import com.learnsphere.notificationservice.service.NotificationServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(properties = {
        "eureka.client.enabled=false",
        "spring.rabbitmq.listener.simple.auto-startup=false",
        "spring.data.mongodb.auto-index-creation=false"
})
class NotificationServiceApplicationTests {

    @Test
    @DisplayName("Verify notification-service context loads independently")
    void contextLoads() {
    }

    @Test
    @DisplayName("Verify NotificationStatusController GET /api/notifications/status")
    void testNotificationStatusController() {
        ResponseEntity<String> response = new NotificationStatusController().getStatus();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("Notification Service is running");
    }

    @Test
    @DisplayName("A received EnrollmentConfirmed event is saved to MongoDB with the event information")
    void testEventIsSavedToMongoDB() {
        NotificationRepository repository = mock(NotificationRepository.class);

        boolean saved = new NotificationServiceImpl(repository)
                .saveNotification(new EnrollmentConfirmed("event-101", 1L, 101L, 201L));

        assertThat(saved).isTrue();
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(repository).save(captor.capture());
        Notification notification = captor.getValue();
        assertThat(notification.getEventId()).isEqualTo("event-101");
        assertThat(notification.getEnrollmentId()).isEqualTo(1L);
        assertThat(notification.getLearnerId()).isEqualTo(101L);
        assertThat(notification.getCourseId()).isEqualTo(201L);
        assertThat(notification.getMessage())
                .isEqualTo("Enrollment confirmed successfully for Learner 101 for Course 201.");
    }

    @Test
    @DisplayName("An eventId already stored in MongoDB is ignored, even after a restart (no duplicate record)")
    void testDuplicateEventIsNotSavedAgain() {
        NotificationRepository repository = mock(NotificationRepository.class);
        Notification existing = new Notification("event-dup", 2L, 102L, 202L, "msg");
        when(repository.findByEventId("event-dup")).thenReturn(Optional.of(existing));

        // A new service instance simulates a restarted Notification Service.
        boolean saved = new NotificationServiceImpl(repository)
                .saveNotification(new EnrollmentConfirmed("event-dup", 2L, 102L, 202L));

        assertThat(saved).isFalse();
        verify(repository, never()).save(any(Notification.class));
    }

    @Test
    @DisplayName("A duplicate rejected by the unique eventId index is ignored safely")
    void testDuplicateKeyRaceIsIgnored() {
        NotificationRepository repository = mock(NotificationRepository.class);
        when(repository.findByEventId("event-race")).thenReturn(Optional.empty());
        when(repository.save(any(Notification.class)))
                .thenThrow(new DuplicateKeyException("E11000 duplicate key error"));

        boolean saved = new NotificationServiceImpl(repository)
                .saveNotification(new EnrollmentConfirmed("event-race", 4L, 104L, 204L));

        assertThat(saved).isFalse();
    }

    @Test
    @DisplayName("A MongoDB save failure is converted to NotificationPersistenceException")
    void testSaveFailureIsWrapped() {
        NotificationRepository repository = mock(NotificationRepository.class);
        when(repository.save(any(Notification.class))).thenThrow(new RuntimeException("MongoDB down"));

        assertThatThrownBy(() -> new NotificationServiceImpl(repository)
                .saveNotification(new EnrollmentConfirmed("event-fail", 3L, 103L, 203L)))
                .isInstanceOf(NotificationPersistenceException.class);
    }

    @Test
    @DisplayName("GET /notifications returns the notifications stored in MongoDB")
    void testGetAllNotifications() {
        NotificationRepository repository = mock(NotificationRepository.class);
        Notification notification = new Notification("event-1", 1L, 101L, 201L, "msg");
        when(repository.findAll()).thenReturn(List.of(notification));

        List<Notification> result =
                new NotificationController(new NotificationServiceImpl(repository)).getAllNotifications();

        assertThat(result).containsExactly(notification);
    }

    @Test
    @DisplayName("NotificationConsumer passes received events to NotificationService")
    void testConsumerDelegatesToService() {
        NotificationService service = mock(NotificationService.class);
        EnrollmentConfirmed event = new EnrollmentConfirmed("event-3", 3L, 103L, 203L);

        new NotificationConsumer(service).receiveEnrollmentConfirmed(event);

        verify(service).saveNotification(event);
    }
}
