package com.learnsphere.enrollmentservice.service;

import com.learnsphere.enrollmentservice.client.CourseClient;
import com.learnsphere.enrollmentservice.dto.CourseResponseDTO;
import com.learnsphere.enrollmentservice.dto.EnrollmentRequestDTO;
import com.learnsphere.enrollmentservice.entity.Enrollment;
import com.learnsphere.enrollmentservice.event.EnrollmentConfirmed;
import com.learnsphere.enrollmentservice.exception.EnrollmentNotFoundException;
import com.learnsphere.enrollmentservice.producer.EnrollmentEventProducer;
import com.learnsphere.enrollmentservice.repository.EnrollmentRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class EnrollmentService {

    private final CourseClient courseClient;
    private final EnrollmentRepository enrollmentRepository;
    private final EnrollmentEventProducer enrollmentEventProducer;

    public EnrollmentService(CourseClient courseClient,
                             EnrollmentRepository enrollmentRepository,
                             EnrollmentEventProducer enrollmentEventProducer) {
        this.courseClient = courseClient;
        this.enrollmentRepository = enrollmentRepository;
        this.enrollmentEventProducer = enrollmentEventProducer;
    }

    // Sprint 3: synchronous call to Course Service
    public CourseResponseDTO getCourseForEnrollment(Long courseId) {
        return courseClient.getCourseById(courseId);
    }

    // Sprint 4: confirm an enrollment and publish an EnrollmentConfirmed event
    public Enrollment confirmEnrollment(EnrollmentRequestDTO request) {

        // Sprint 7: validate the course first (Retry + Circuit Breaker in CourseClient).
        // If the course does not exist (404) or Course Service is unavailable (503),
        // an exception is thrown here and no enrollment is saved.
        courseClient.getCourseById(request.getCourseId());

        Enrollment savedEnrollment = enrollmentRepository.save(
                new Enrollment(request.getLearnerId(), request.getCourseId()));

        EnrollmentConfirmed event = new EnrollmentConfirmed(
                UUID.randomUUID().toString(),
                savedEnrollment.getId(),
                savedEnrollment.getLearnerId(),
                savedEnrollment.getCourseId()
        );

        enrollmentEventProducer.sendEnrollmentConfirmed(event);

        return savedEnrollment;
    }

    // Publishes the same event (same eventId) twice to test duplicate handling
    public void sendDuplicateNotification(Long enrollmentId) {

        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new EnrollmentNotFoundException(
                        "Enrollment not found with id: " + enrollmentId));

        EnrollmentConfirmed event = new EnrollmentConfirmed(
                UUID.randomUUID().toString(),
                enrollment.getId(),
                enrollment.getLearnerId(),
                enrollment.getCourseId()
        );

        enrollmentEventProducer.sendDuplicateEnrollmentConfirmed(event);
    }
}
