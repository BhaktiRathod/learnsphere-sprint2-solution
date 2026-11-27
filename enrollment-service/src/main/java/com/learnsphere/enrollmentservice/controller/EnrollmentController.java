package com.learnsphere.enrollmentservice.controller;

import com.learnsphere.enrollmentservice.dto.CourseResponseDTO;
import com.learnsphere.enrollmentservice.dto.EnrollmentRequestDTO;
import com.learnsphere.enrollmentservice.entity.Enrollment;
import com.learnsphere.enrollmentservice.service.EnrollmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/enrollments", "/api/enrollments"})
public class EnrollmentController {

    private static final Logger log = LoggerFactory.getLogger(EnrollmentController.class);

    private final EnrollmentService enrollmentService;

    public EnrollmentController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    @GetMapping
    public ResponseEntity<String> getEnrollments() {
        return ResponseEntity.ok("Enrollment Service reached");
    }

    // Sprint 3: retrieves course information from Course Service through RestClient + Eureka
    @GetMapping("/course/{courseId}")
    public ResponseEntity<CourseResponseDTO> getCourseForEnrollment(
            @PathVariable Long courseId,
            @RequestHeader(value = "X-Correlation-ID", required = false) String correlationId) {
        log.info("Course lookup request received for courseId {}", courseId);
        return ResponseEntity.ok(enrollmentService.getCourseForEnrollment(courseId, correlationId));
    }

    // Sprint 4: confirms an enrollment and publishes an EnrollmentConfirmed event to RabbitMQ
    @PostMapping
    public ResponseEntity<Enrollment> confirmEnrollment(
            @RequestBody EnrollmentRequestDTO request,
            @RequestHeader(value = "X-Correlation-ID", required = false) String correlationId) {
        log.info("Enrollment request received for learnerId {} and courseId {}",
                request.getLearnerId(), request.getCourseId());
        Enrollment enrollment = enrollmentService.confirmEnrollment(request, correlationId);
        return new ResponseEntity<>(enrollment, HttpStatus.CREATED);
    }

    // Sprint 4: publishes the same event twice to test duplicate handling in Notification Service
    @PostMapping("/{id}/duplicate-notification")
    public ResponseEntity<String> sendDuplicateNotification(@PathVariable Long id) {
        enrollmentService.sendDuplicateNotification(id);
        return ResponseEntity.ok(
                "Duplicate enrollment notification published for enrollment ID: " + id);
    }
}
