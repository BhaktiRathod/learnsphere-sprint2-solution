package com.learnsphere.enrollmentservice.client;

import com.learnsphere.enrollmentservice.dto.CourseResponseDTO;
import com.learnsphere.enrollmentservice.exception.CourseNotFoundException;
import com.learnsphere.enrollmentservice.exception.CourseServiceUnavailableException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class CourseClient {

    private final RestClient restClient;

    public CourseClient(
            @Qualifier("loadBalancedRestClientBuilder")
            RestClient.Builder restClientBuilder) {

        this.restClient = restClientBuilder
                .baseUrl("http://course-service")
                .build();
    }

    public CourseResponseDTO getCourseById(Long courseId) {
        try {
            return restClient.get()
                    .uri("/courses/{id}", courseId)
                    .retrieve()
                    .body(CourseResponseDTO.class);

        } catch (HttpClientErrorException.NotFound ex) {
            throw new CourseNotFoundException(
                    "Course not found with id: " + courseId
            );

        } catch (ResourceAccessException ex) {
            throw new CourseServiceUnavailableException(
                    "Course Service is currently unavailable or timed out"
            );

        } catch (RestClientException ex) {
            throw new CourseServiceUnavailableException(
                    "Course Service could not be reached or returned an error"
            );
        }
    }
}
