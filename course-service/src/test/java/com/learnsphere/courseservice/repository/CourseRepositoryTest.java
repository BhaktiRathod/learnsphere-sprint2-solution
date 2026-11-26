package com.learnsphere.courseservice.repository;

import com.learnsphere.courseservice.entity.Course;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {"eureka.client.enabled=false"})
class CourseRepositoryTest {

    @Autowired
    private CourseRepository courseRepository;

    private Course course1;
    private Course course2;
    private Course course3;

    @BeforeEach
    void setUp() {
        courseRepository.deleteAll();
        course1 = courseRepository.save(new Course("Java Masterclass", "Learn Java from scratch", "John Doe", 8, 199.99));
        course2 = courseRepository.save(new Course("Spring Boot Microservices", "Build scalable cloud services", "Jane Smith", 12, 299.99));
        course3 = courseRepository.save(new Course("Python Fundamentals", "Introduction to Python programming", "John Doe", 6, 149.99));
    }

    @Test
    @DisplayName("Test findByTitle - returns matching course")
    void testFindByTitle() {
        Optional<Course> found = courseRepository.findByTitle("Java Masterclass");
        assertThat(found).isPresent();
        assertThat(found.get().getInstructor()).isEqualTo("John Doe");
    }

    @Test
    @DisplayName("Test existsByTitle - returns true when course exists")
    void testExistsByTitle() {
        boolean exists = courseRepository.existsByTitle("Java Masterclass");
        assertThat(exists).isTrue();

        boolean notExists = courseRepository.existsByTitle("Non Existent Course");
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("Test findByTitleContainingIgnoreCase - returns partial matches")
    void testFindByTitleContainingIgnoreCase() {
        List<Course> results = courseRepository.findByTitleContainingIgnoreCase("java");
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getTitle()).isEqualTo("Java Masterclass");

        List<Course> multiResults = courseRepository.findByTitleContainingIgnoreCase("boot");
        assertThat(multiResults).hasSize(1);
        assertThat(multiResults.get(0).getTitle()).isEqualTo("Spring Boot Microservices");
    }

    @Test
    @DisplayName("Test findByInstructorIgnoreCase - returns courses by instructor")
    void testFindByInstructorIgnoreCase() {
        List<Course> courses = courseRepository.findByInstructorIgnoreCase("john doe");
        assertThat(courses).hasSize(2);
        assertThat(courses).extracting(Course::getTitle)
                .containsExactlyInAnyOrder("Java Masterclass", "Python Fundamentals");
    }

    @Test
    @DisplayName("Test findByDurationWeeksLessThanEqual - returns short courses")
    void testFindByDurationWeeksLessThanEqual() {
        List<Course> courses = courseRepository.findByDurationWeeksLessThanEqual(8);
        assertThat(courses).hasSize(2);
        assertThat(courses).extracting(Course::getTitle)
                .containsExactlyInAnyOrder("Java Masterclass", "Python Fundamentals");
    }

    @Test
    @DisplayName("Test JPQL findCoursesWithFeeLessThanEqual - returns courses under budget")
    void testFindCoursesWithFeeLessThanEqual() {
        List<Course> courses = courseRepository.findCoursesWithFeeLessThanEqual(200.00);
        assertThat(courses).hasSize(2);
        assertThat(courses).extracting(Course::getTitle)
                .containsExactlyInAnyOrder("Java Masterclass", "Python Fundamentals");
    }
}
