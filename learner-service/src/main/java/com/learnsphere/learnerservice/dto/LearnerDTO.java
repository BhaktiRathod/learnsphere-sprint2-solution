package com.learnsphere.learnerservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.HashSet;
import java.util.Set;

public class LearnerDTO {

    private Long id;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    private String phone;

    private Set<Long> enrolledCourseIds = new HashSet<>();

    private Set<String> enrolledCourseTitles = new HashSet<>();

    public LearnerDTO() {
    }

    public LearnerDTO(Long id, String firstName, String lastName, String email, String phone) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
    }

    public LearnerDTO(Long id, String firstName, String lastName, String email, String phone,
                      Set<Long> enrolledCourseIds, Set<String> enrolledCourseTitles) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.enrolledCourseIds = enrolledCourseIds != null ? enrolledCourseIds : new HashSet<>();
        this.enrolledCourseTitles = enrolledCourseTitles != null ? enrolledCourseTitles : new HashSet<>();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Set<Long> getEnrolledCourseIds() {
        return enrolledCourseIds;
    }

    public void setEnrolledCourseIds(Set<Long> enrolledCourseIds) {
        this.enrolledCourseIds = enrolledCourseIds;
    }

    public Set<String> getEnrolledCourseTitles() {
        return enrolledCourseTitles;
    }

    public void setEnrolledCourseTitles(Set<String> enrolledCourseTitles) {
        this.enrolledCourseTitles = enrolledCourseTitles;
    }
}
