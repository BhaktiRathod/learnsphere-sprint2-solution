package com.learnsphere.courseservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class CourseDTO {

    private Long id;

    @NotBlank(message = "Course title is required")
    private String title;

    private String description;

    @NotBlank(message = "Instructor is required")
    private String instructor;

    @NotNull(message = "Duration in weeks is required")
    @Min(value = 1, message = "Duration must be at least 1 week")
    private Integer durationWeeks;

    @NotNull(message = "Fee is required")
    @Positive(message = "Fee must be a positive number")
    private Double fee;

    private Integer learnerCount;

    public CourseDTO() {
    }

    public CourseDTO(Long id, String title, String description, String instructor, Integer durationWeeks, Double fee) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.instructor = instructor;
        this.durationWeeks = durationWeeks;
        this.fee = fee;
    }

    public CourseDTO(Long id, String title, String description, String instructor, Integer durationWeeks, Double fee, Integer learnerCount) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.instructor = instructor;
        this.durationWeeks = durationWeeks;
        this.fee = fee;
        this.learnerCount = learnerCount;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getInstructor() {
        return instructor;
    }

    public void setInstructor(String instructor) {
        this.instructor = instructor;
    }

    public Integer getDurationWeeks() {
        return durationWeeks;
    }

    public void setDurationWeeks(Integer durationWeeks) {
        this.durationWeeks = durationWeeks;
    }

    public Double getFee() {
        return fee;
    }

    public void setFee(Double fee) {
        this.fee = fee;
    }

    public Integer getLearnerCount() {
        return learnerCount;
    }

    public void setLearnerCount(Integer learnerCount) {
        this.learnerCount = learnerCount;
    }
}
