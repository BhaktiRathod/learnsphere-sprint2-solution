package com.learnsphere.learnerservice.repository;

import com.learnsphere.learnerservice.entity.Learner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {"eureka.client.enabled=false"})
class LearnerRepositoryTest {

    @Autowired
    private LearnerRepository learnerRepository;

    private Learner learner1;
    private Learner learner2;

    @BeforeEach
    void setUp() {
        learnerRepository.deleteAll();

        learner1 = new Learner("John", "Doe", "john.doe@example.com", "555-0101");
        learner1.addCourseId(101L);
        learner1 = learnerRepository.save(learner1);

        learner2 = new Learner("Jane", "Doe", "jane.doe@example.com", "555-0102");
        learner2 = learnerRepository.save(learner2);
    }

    @Test
    @DisplayName("Test findByEmail - returns matching learner")
    void testFindByEmail() {
        Optional<Learner> found = learnerRepository.findByEmail("john.doe@example.com");
        assertThat(found).isPresent();
        assertThat(found.get().getFirstName()).isEqualTo("John");
    }

    @Test
    @DisplayName("Test existsByEmail - returns true for existing email")
    void testExistsByEmail() {
        boolean exists = learnerRepository.existsByEmail("jane.doe@example.com");
        assertThat(exists).isTrue();

        boolean notExists = learnerRepository.existsByEmail("unknown@example.com");
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("Test findByLastNameIgnoreCase - returns learners with matching surname")
    void testFindByLastNameIgnoreCase() {
        List<Learner> results = learnerRepository.findByLastNameIgnoreCase("doe");
        assertThat(results).hasSize(2);
        assertThat(results).extracting(Learner::getEmail)
                .containsExactlyInAnyOrder("john.doe@example.com", "jane.doe@example.com");
    }

    @Test
    @DisplayName("Test search by first or last name containing keyword")
    void testFindByFirstNameContainingOrLastNameContaining() {
        List<Learner> results = learnerRepository
                .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase("Jane", "Jane");
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getEmail()).isEqualTo("jane.doe@example.com");
    }

    @Test
    @DisplayName("Test JPQL findLearnersByCourseId - returns learners enrolled in course")
    void testFindLearnersByCourseId() {
        List<Learner> learners = learnerRepository.findLearnersByCourseId(101L);
        assertThat(learners).hasSize(1);
        assertThat(learners.get(0).getEmail()).isEqualTo("john.doe@example.com");
    }

    @Test
    @DisplayName("Test JPQL findByIdWithCourses - fetches learner with eagerly loaded courses")
    void testFindByIdWithCourses() {
        Optional<Learner> found = learnerRepository.findByIdWithCourses(learner1.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getEnrolledCourseIds()).contains(101L);
    }
}
