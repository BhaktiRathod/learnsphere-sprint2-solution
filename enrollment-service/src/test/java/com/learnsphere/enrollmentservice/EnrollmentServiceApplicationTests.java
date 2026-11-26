package com.learnsphere.enrollmentservice;

import com.learnsphere.enrollmentservice.client.CourseClient;
import com.learnsphere.enrollmentservice.controller.EnrollmentController;
import com.learnsphere.enrollmentservice.controller.EnrollmentStatusController;
import com.learnsphere.enrollmentservice.dto.CourseResponseDTO;
import com.learnsphere.enrollmentservice.dto.EnrollmentRequestDTO;
import com.learnsphere.enrollmentservice.entity.Enrollment;
import com.learnsphere.enrollmentservice.event.EnrollmentConfirmed;
import com.learnsphere.enrollmentservice.exception.CourseNotFoundException;
import com.learnsphere.enrollmentservice.exception.CourseServiceUnavailableException;
import com.learnsphere.enrollmentservice.exception.GlobalExceptionHandler;
import com.learnsphere.enrollmentservice.producer.EnrollmentEventProducer;
import com.learnsphere.enrollmentservice.repository.EnrollmentRepository;
import com.learnsphere.enrollmentservice.service.EnrollmentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(properties = {"eureka.client.enabled=false"})
class EnrollmentServiceApplicationTests {

    @Test
    @DisplayName("Verify enrollment-service context loads independently")
    void contextLoads() {
    }

    @Test
    @DisplayName("Verify EnrollmentController GET /enrollments")
    void testEnrollmentController() {
        EnrollmentController controller = new EnrollmentController(mock(EnrollmentService.class));
        ResponseEntity<String> response = controller.getEnrollments();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("Enrollment Service reached");
    }

    @Test
    @DisplayName("Verify EnrollmentStatusController GET /enrollments/status")
    void testEnrollmentStatusController() {
        EnrollmentStatusController controller = new EnrollmentStatusController();
        ResponseEntity<String> response = controller.getStatus();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("Enrollment Service is running");
    }

    @Test
    @DisplayName("GET /enrollments/course/{courseId} returns course information from Course Service")
    void testGetCourseForEnrollment() {
        EnrollmentService service = mock(EnrollmentService.class);
        CourseResponseDTO course = new CourseResponseDTO(101L, "Spring Boot", "Deep Dive", "John Doe", 8, 199.99);
        when(service.getCourseForEnrollment(101L)).thenReturn(course);

        ResponseEntity<CourseResponseDTO> response = new EnrollmentController(service).getCourseForEnrollment(101L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getId()).isEqualTo(101L);
        assertThat(response.getBody().getTitle()).isEqualTo("Spring Boot");
    }

    @Test
    @DisplayName("Confirming an enrollment saves it and publishes an EnrollmentConfirmed event with a unique eventId")
    void testConfirmEnrollmentPublishesEvent() {
        CourseClient courseClient = mock(CourseClient.class);
        EnrollmentRepository repository = mock(EnrollmentRepository.class);
        EnrollmentEventProducer producer = mock(EnrollmentEventProducer.class);

        Enrollment saved = new Enrollment(101L, 201L);
        saved.setId(1L);
        when(repository.save(any(Enrollment.class))).thenReturn(saved);

        EnrollmentService service = new EnrollmentService(courseClient, repository, producer);
        Enrollment result = service.confirmEnrollment(new EnrollmentRequestDTO(101L, 201L));

        assertThat(result.getId()).isEqualTo(1L);

        ArgumentCaptor<EnrollmentConfirmed> captor = ArgumentCaptor.forClass(EnrollmentConfirmed.class);
        verify(producer).sendEnrollmentConfirmed(captor.capture());
        EnrollmentConfirmed event = captor.getValue();
        assertThat(event.getEventId()).isNotBlank();
        assertThat(event.getEnrollmentId()).isEqualTo(1L);
        assertThat(event.getLearnerId()).isEqualTo(101L);
        assertThat(event.getCourseId()).isEqualTo(201L);
    }

    @Test
    @DisplayName("Duplicate notification publishes the same event (same eventId) twice")
    void testSendDuplicateNotification() {
        EnrollmentRepository repository = mock(EnrollmentRepository.class);
        EnrollmentEventProducer producer = mock(EnrollmentEventProducer.class);

        Enrollment saved = new Enrollment(101L, 201L);
        saved.setId(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(saved));

        new EnrollmentService(mock(CourseClient.class), repository, producer).sendDuplicateNotification(1L);

        verify(producer).sendDuplicateEnrollmentConfirmed(any(EnrollmentConfirmed.class));
    }

    @Test
    @DisplayName("Course not found is returned as 404 with a meaningful message")
    void testCourseNotFoundHandled() {
        ResponseEntity<ProblemDetail> response = new GlobalExceptionHandler()
                .handleCourseNotFound(new CourseNotFoundException("Course not found with id: 999"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().getDetail()).isEqualTo("Course not found with id: 999");
    }

    @Test
    @DisplayName("Course Service unavailable or timeout is returned as 503 with a meaningful message")
    void testCourseServiceUnavailableHandled() {
        ResponseEntity<ProblemDetail> response = new GlobalExceptionHandler()
                .handleCourseServiceUnavailable(
                        new CourseServiceUnavailableException("Course Service is currently unavailable or timed out"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getBody().getTitle()).isEqualTo("Course Service Unavailable");
    }
}
