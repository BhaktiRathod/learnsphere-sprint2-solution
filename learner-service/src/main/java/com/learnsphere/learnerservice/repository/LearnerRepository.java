package com.learnsphere.learnerservice.repository;

import com.learnsphere.learnerservice.entity.Learner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LearnerRepository extends JpaRepository<Learner, Long> {

    Optional<Learner> findByEmail(String email);

    boolean existsByEmail(String email);

    List<Learner> findByLastNameIgnoreCase(String lastName);

    List<Learner> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(String firstName, String lastName);

    @Query("SELECT l FROM Learner l JOIN l.enrolledCourseIds c WHERE c = :courseId")
    List<Learner> findLearnersByCourseId(@Param("courseId") Long courseId);

    @Query("SELECT l FROM Learner l LEFT JOIN FETCH l.enrolledCourseIds WHERE l.id = :id")
    Optional<Learner> findByIdWithCourses(@Param("id") Long id);
}
