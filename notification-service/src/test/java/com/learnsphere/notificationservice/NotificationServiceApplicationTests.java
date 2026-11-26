package com.learnsphere.notificationservice;

import com.learnsphere.notificationservice.consumer.NotificationConsumer;
import com.learnsphere.notificationservice.controller.NotificationController;
import com.learnsphere.notificationservice.controller.NotificationStatusController;
import com.learnsphere.notificationservice.document.Notification;
import com.learnsphere.notificationservice.event.EnrollmentConfirmed;
import com.learnsphere.notificationservice.exception.GlobalExceptionHandler;
import com.learnsphere.notificationservice.exception.NotificationNotFoundException;
import com.learnsphere.notificationservice.repository.NotificationRepository;
import com.learnsphere.notificationservice.service.NotificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
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
    @DisplayName("A new EnrollmentConfirmed event is stored in MongoDB with the event information")
    void testNewEventIsPersisted() {
        NotificationRepository repository = mock(NotificationRepository.class);
        when(repository.existsByEventId("event-101")).thenReturn(false);

        boolean stored = new NotificationService(repository)
                .processEnrollmentConfirmed(new EnrollmentConfirmed("event-101", 1L, 101L, 201L));

        assertThat(stored).isTrue();
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(repository).save(captor.capture());
        Notification saved = captor.getValue();
        assertThat(saved.getEventId()).isEqualTo("event-101");
        assertThat(saved.getEnrollmentId()).isEqualTo(1L);
        assertThat(saved.getLearnerId()).isEqualTo(101L);
        assertThat(saved.getCourseId()).isEqualTo(201L);
        assertThat(saved.getMessage()).isEqualTo("Enrollment confirmed successfully for Learner 101 for Course 201.");
    }

    @Test
    @DisplayName("An eventId that is already stored is ignored (no duplicate notification record)")
    void testDuplicateEventIsNotPersisted() {
        NotificationRepository repository = mock(NotificationRepository.class);
        when(repository.existsByEventId("event-dup")).thenReturn(true);

        boolean stored = new NotificationService(repository)
                .processEnrollmentConfirmed(new EnrollmentConfirmed("event-dup", 2L, 102L, 202L));

        assertThat(stored).isFalse();
        verify(repository, never()).save(any(Notification.class));
    }

    @Test
    @DisplayName("A duplicate rejected by the unique eventId index is ignored")
    void testUniqueIndexRejectionIsIgnored() {
        NotificationRepository repository = mock(NotificationRepository.class);
        when(repository.existsByEventId("event-race")).thenReturn(false);
        when(repository.save(any(Notification.class))).thenThrow(new DuplicateKeyException("duplicate eventId"));

        boolean stored = new NotificationService(repository)
                .processEnrollmentConfirmed(new EnrollmentConfirmed("event-race", 3L, 103L, 203L));

        assertThat(stored).isFalse();
    }

    @Test
    @DisplayName("GET /notifications returns the stored notifications")
    void testGetAllNotifications() {
        NotificationRepository repository = mock(NotificationRepository.class);
        Notification notification = new Notification("event-1", 1L, 101L, 201L, "msg");
        when(repository.findAll()).thenReturn(List.of(notification));

        ResponseEntity<List<Notification>> response =
                new NotificationController(new NotificationService(repository)).getAllNotifications();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsExactly(notification);
    }

    @Test
    @DisplayName("GET /notifications/{id} returns the requested notification")
    void testGetNotificationById() {
        NotificationRepository repository = mock(NotificationRepository.class);
        Notification notification = new Notification("event-1", 1L, 101L, 201L, "msg");
        notification.setId("abc123");
        when(repository.findById("abc123")).thenReturn(Optional.of(notification));

        ResponseEntity<Notification> response =
                new NotificationController(new NotificationService(repository)).getNotificationById("abc123");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getEventId()).isEqualTo("event-1");
    }

    @Test
    @DisplayName("GET /notifications/{id} for an unknown id returns 404")
    void testGetNotificationByIdNotFound() {
        NotificationRepository repository = mock(NotificationRepository.class);
        when(repository.findById("missing")).thenReturn(Optional.empty());

        NotificationService service = new NotificationService(repository);
        assertThatThrownBy(() -> service.getNotificationById("missing"))
                .isInstanceOf(NotificationNotFoundException.class);

        ResponseEntity<ProblemDetail> response = new GlobalExceptionHandler()
                .handleNotificationNotFound(new NotificationNotFoundException("Notification not found with id: missing"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
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
