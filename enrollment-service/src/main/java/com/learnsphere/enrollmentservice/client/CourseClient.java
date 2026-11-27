package com.learnsphere.enrollmentservice.client;

import com.learnsphere.enrollmentservice.dto.CourseResponseDTO;
import com.learnsphere.enrollmentservice.exception.CourseNotFoundException;
import com.learnsphere.enrollmentservice.exception.CourseServiceUnavailableException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class CourseClient {

    private static final Logger log = LoggerFactory.getLogger(CourseClient.class);

    private final RestClient restClient;

    public CourseClient(
            @Qualifier("loadBalancedRestClientBuilder")
            RestClient.Builder restClientBuilder) {

        this.restClient = restClientBuilder
                .baseUrl("http://course-service")
                .build();
    }

    @Retry(name = "courseService")
    @CircuitBreaker(
            name = "courseService",
            fallbackMethod = "courseServiceFallback"
    )
    public CourseResponseDTO getCourseById(Long courseId, String correlationId) {

        // Helps observe each actual call/retry to Course Service.
        System.out.println(
                "Attempting Course Service call for courseId: " + courseId
        );

        log.info("Calling Course Service for courseId {}", courseId);

        try {
            return restClient.get()
                    .uri("/courses/{id}", courseId)
                    // Propagate the same correlation ID to Course Service.
                    .header("X-Correlation-ID", correlationId)
                    .retrieve()
                    .body(CourseResponseDTO.class);

        } catch (HttpClientErrorException.NotFound ex) {

            // Course Service is available, but the requested course does not exist.
            // Not retried and not recorded as a Circuit Breaker failure.
            throw new CourseNotFoundException(
                    "Course not found with id: " + courseId
            );

        } catch (IllegalStateException | ResourceAccessException ex) {

            // No running Course Service instance in Eureka, connection failure or read timeout.
            throw new CourseServiceUnavailableException(
                    "Course Service is currently unavailable"
            );

        } catch (RestClientException ex) {

            // Other HTTP failures such as Course Service returning 5xx.
            throw new CourseServiceUnavailableException(
                    "Course Service is currently unavailable"
            );
        }
    }

    // Fallback when the Course Service is unavailable. Does not return or fabricate course data.
    private CourseResponseDTO courseServiceFallback(
            Long courseId,
            String correlationId,
            CourseServiceUnavailableException ex) {

        System.out.println(
                "Fallback executed: Course Service is unavailable."
        );

        throw new CourseServiceUnavailableException(
                "Course service is currently unavailable. Please try again later."
        );
    }
}
