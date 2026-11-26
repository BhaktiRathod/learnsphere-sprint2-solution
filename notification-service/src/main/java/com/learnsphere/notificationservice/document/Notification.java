package com.learnsphere.notificationservice.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

// Notification record stored in the Notification Service's own MongoDB database.
@Document(collection = "notifications")
public class Notification {

    @Id
    private String id;

    // eventId of the processed EnrollmentConfirmed event.
    private String eventId;

    private Long enrollmentId;
    private Long learnerId;
    private Long courseId;
    private String message;
    private LocalDateTime createdAt;

    public Notification() {
    }

    public Notification(String eventId,
                        Long enrollmentId,
                        Long learnerId,
                        Long courseId,
                        String message) {
        this.eventId = eventId;
        this.enrollmentId = enrollmentId;
        this.learnerId = learnerId;
        this.courseId = courseId;
        this.message = message;
        this.createdAt = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public Long getEnrollmentId() {
        return enrollmentId;
    }

    public void setEnrollmentId(Long enrollmentId) {
        this.enrollmentId = enrollmentId;
    }

    public Long getLearnerId() {
        return learnerId;
    }

    public void setLearnerId(Long learnerId) {
        this.learnerId = learnerId;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
