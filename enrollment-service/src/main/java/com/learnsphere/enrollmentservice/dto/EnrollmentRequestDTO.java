package com.learnsphere.enrollmentservice.dto;

public class EnrollmentRequestDTO {

    private Long learnerId;
    private Long courseId;

    public EnrollmentRequestDTO() {
    }

    public EnrollmentRequestDTO(Long learnerId, Long courseId) {
        this.learnerId = learnerId;
        this.courseId = courseId;
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
