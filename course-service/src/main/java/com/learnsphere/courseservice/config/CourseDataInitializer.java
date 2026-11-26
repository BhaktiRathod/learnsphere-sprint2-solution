package com.learnsphere.courseservice.config;

import com.learnsphere.courseservice.entity.Course;
import com.learnsphere.courseservice.repository.CourseRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class CourseDataInitializer implements CommandLineRunner {

    private final CourseRepository courseRepository;

    public CourseDataInitializer(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    @Override
    public void run(String... args) {
        if (courseRepository.count() == 0) {
            courseRepository.save(new Course("Java Microservices", "Complete Guide to Microservices", "John Doe", 8, 199.99));
            courseRepository.save(new Course("Spring Cloud Fundamentals", "Cloud-Native Architectures", "Jane Smith", 6, 149.99));
            courseRepository.save(new Course("Docker and Kubernetes", "Containerization Mastery", "Alex Brown", 4, 129.99));
        }
    }
}
