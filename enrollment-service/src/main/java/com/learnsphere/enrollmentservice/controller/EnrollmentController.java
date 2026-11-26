package com.learnsphere.enrollmentservice.controller;

import com.learnsphere.enrollmentservice.dto.CourseResponseDTO;
import com.learnsphere.enrollmentservice.dto.EnrollmentRequestDTO;
import com.learnsphere.enrollmentservice.entity.Enrollment;
import com.learnsphere.enrollmentservice.service.EnrollmentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/enrollments", "/api/enrollments"})
public class EnrollmentController {

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
    public ResponseEntity<CourseResponseDTO> getCourseForEnrollment(@PathVariable Long courseId) {
        return ResponseEntity.ok(enrollmentService.getCourseForEnrollment(courseId));
    }

    // Sprint 4: confirms an enrollment and publishes an EnrollmentConfirmed event to RabbitMQ
    @PostMapping
    public ResponseEntity<Enrollment> confirmEnrollment(@RequestBody EnrollmentRequestDTO request) {
        Enrollment enrollment = enrollmentService.confirmEnrollment(request);
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
