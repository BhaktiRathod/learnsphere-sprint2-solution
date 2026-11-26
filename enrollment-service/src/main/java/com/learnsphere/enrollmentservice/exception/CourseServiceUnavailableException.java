package com.learnsphere.enrollmentservice.exception;

public class CourseServiceUnavailableException extends RuntimeException {

    public CourseServiceUnavailableException(String message) {
        super(message);
    }
}
