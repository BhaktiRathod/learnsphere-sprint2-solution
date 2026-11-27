package com.learnsphere.enrollmentservice.exception;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CourseNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleCourseNotFound(CourseNotFoundException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        problemDetail.setTitle("Course Not Found");
        problemDetail.setDetail(ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(problemDetail);
    }

    @ExceptionHandler(EnrollmentNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleEnrollmentNotFound(EnrollmentNotFoundException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        problemDetail.setTitle("Enrollment Not Found");
        problemDetail.setDetail(ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(problemDetail);
    }

    @ExceptionHandler(CourseServiceUnavailableException.class)
    public ResponseEntity<ProblemDetail> handleCourseServiceUnavailable(CourseServiceUnavailableException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.SERVICE_UNAVAILABLE);
        problemDetail.setTitle("Course Service Unavailable");
        problemDetail.setDetail(ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(problemDetail);
    }

    // Circuit Breaker is OPEN -> 503 (fails fast without calling Course Service)
    @ExceptionHandler(CallNotPermittedException.class)
    public ResponseEntity<ProblemDetail> handleCircuitBreakerOpen(CallNotPermittedException ex) {

        System.out.println(
                "Circuit Breaker OPEN: Course Service calls are temporarily blocked."
        );

        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.SERVICE_UNAVAILABLE);
        problemDetail.setTitle("Course Service Temporarily Unavailable");
        problemDetail.setDetail(
                "Course service is currently unavailable. Please try again later. "
                        + "The circuit breaker is OPEN and is temporarily blocking requests."
        );

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(problemDetail);
    }
}
