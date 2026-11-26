package com.learnsphere.enrollmentservice.exception;

public class EnrollmentNotFoundException extends RuntimeException {

    public EnrollmentNotFoundException(String message) {
        super(message);
    }
}
