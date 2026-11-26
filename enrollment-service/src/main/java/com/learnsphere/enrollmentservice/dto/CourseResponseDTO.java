package com.learnsphere.enrollmentservice.dto;

public class CourseResponseDTO {

    private Long id;
    private String title;
    private String description;
    private String instructor;
    private Integer durationWeeks;
    private Double fee;

    public CourseResponseDTO() {
    }

    public CourseResponseDTO(Long id, String title, String description, String instructor, Integer durationWeeks, Double fee) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.instructor = instructor;
        this.durationWeeks = durationWeeks;
        this.fee = fee;
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
}
