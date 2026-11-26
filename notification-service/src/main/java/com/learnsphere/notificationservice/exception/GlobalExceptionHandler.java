package com.learnsphere.notificationservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotificationPersistenceException.class)
    public ProblemDetail handleNotificationPersistenceException(
            NotificationPersistenceException ex) {

        // Handles this exception when it occurs during an HTTP request.
        // RabbitMQ listener failures are handled separately by Spring AMQP.
        ProblemDetail problemDetail =
                ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);

        problemDetail.setTitle("Notification Persistence Error");
        problemDetail.setDetail(ex.getMessage());

        return problemDetail;
    }
}
