package com.learnsphere.enrollmentservice.service;

import com.learnsphere.enrollmentservice.client.CourseClient;
import com.learnsphere.enrollmentservice.dto.CourseResponseDTO;
import org.springframework.stereotype.Service;

@Service
public class EnrollmentService {

    private final CourseClient courseClient;

    public EnrollmentService(CourseClient courseClient) {
        this.courseClient = courseClient;
    }

    public CourseResponseDTO getCourseForEnrollment(Long courseId) {
        return courseClient.getCourseById(courseId);
    }
}
