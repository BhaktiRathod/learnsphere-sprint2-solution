package com.learnsphere.learnerservice;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {"eureka.client.enabled=false"})
class LearnerServiceApplicationTests {

    @Test
    @DisplayName("Verify learner-service context loads independently")
    void contextLoads() {
    }
}
