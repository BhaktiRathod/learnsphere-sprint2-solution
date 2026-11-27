package com.learnsphere.enrollmentservice.config;

import java.time.Duration;

import io.micrometer.observation.ObservationRegistry;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    @Primary
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }

    @Bean
    @LoadBalanced
    public RestClient.Builder loadBalancedRestClientBuilder(
            ObservationRegistry observationRegistry) {
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory();
        requestFactory.setReadTimeout(Duration.ofSeconds(3));

        return RestClient.builder()
                .requestFactory(requestFactory)

                // Enable Micrometer tracing for outgoing RestClient calls.
                .observationRegistry(observationRegistry);
    }
}
