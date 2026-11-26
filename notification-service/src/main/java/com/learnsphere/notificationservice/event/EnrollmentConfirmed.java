package com.learnsphere.notificationservice.event;

public class EnrollmentConfirmed {

    // Unique ID used to identify duplicate RabbitMQ messages.
    private String eventId;

    private Long enrollmentId;
    private Long learnerId;
    private Long courseId;

    public EnrollmentConfirmed() {
    }

    public EnrollmentConfirmed(String eventId, Long enrollmentId, Long learnerId, Long courseId) {
        this.eventId = eventId;
        this.enrollmentId = enrollmentId;
        this.learnerId = learnerId;
        this.courseId = courseId;
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
}
