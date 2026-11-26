package com.learnsphere.enrollmentservice;

import com.learnsphere.enrollmentservice.controller.EnrollmentController;
import com.learnsphere.enrollmentservice.controller.EnrollmentStatusController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class EnrollmentServiceApplicationTests {

    @Test
    @DisplayName("Verify enrollment-service context loads independently")
    void contextLoads() {
    }

    @Test
    @DisplayName("Verify EnrollmentController GET /enrollments")
    void testEnrollmentController() {
        EnrollmentController controller = new EnrollmentController();
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
}
