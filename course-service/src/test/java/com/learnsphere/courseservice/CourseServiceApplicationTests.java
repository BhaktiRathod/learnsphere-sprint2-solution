package com.learnsphere.courseservice;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {"eureka.client.enabled=false"})
class CourseServiceApplicationTests {

    @Test
    @DisplayName("Verify course-service context loads independently")
    void contextLoads() {
    }
}
