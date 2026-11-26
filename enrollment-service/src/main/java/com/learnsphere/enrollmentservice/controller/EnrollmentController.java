package com.learnsphere.enrollmentservice.controller;

import com.learnsphere.enrollmentservice.dto.CourseResponseDTO;
import com.learnsphere.enrollmentservice.service.EnrollmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    // Retrieves course information from Course Service through RestClient + Eureka
    @GetMapping("/course/{courseId}")
    public ResponseEntity<CourseResponseDTO> getCourseForEnrollment(@PathVariable Long courseId) {
        return ResponseEntity.ok(enrollmentService.getCourseForEnrollment(courseId));
    }
}
