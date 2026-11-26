package com.learnsphere.courseservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.Objects;

@Entity
@Table(name = "courses")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String title;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private String instructor;

    @Column(nullable = false)
    private Integer durationWeeks;

    @Column(nullable = false)
    private Double fee;

    public Course() {
    }

    public Course(Long id, String title, String description, String instructor, Integer durationWeeks, Double fee) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.instructor = instructor;
        this.durationWeeks = durationWeeks;
        this.fee = fee;
    }

    public Course(String title, String description, String instructor, Integer durationWeeks, Double fee) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Course course = (Course) o;
        return Objects.equals(id, course.id) || Objects.equals(title, course.title);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title);
    }

    @Override
    public String toString() {
        return "Course{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", instructor='" + instructor + '\'' +
                ", durationWeeks=" + durationWeeks +
                ", fee=" + fee +
                '}';
    }
}
