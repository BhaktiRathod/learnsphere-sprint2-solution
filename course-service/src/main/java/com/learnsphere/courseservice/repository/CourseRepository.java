package com.learnsphere.courseservice.repository;

import com.learnsphere.courseservice.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    Optional<Course> findByTitle(String title);

    boolean existsByTitle(String title);

    List<Course> findByTitleContainingIgnoreCase(String title);

    List<Course> findByInstructorIgnoreCase(String instructor);

    List<Course> findByDurationWeeksLessThanEqual(Integer durationWeeks);

    @Query("SELECT c FROM Course c WHERE c.fee <= :maxFee")
    List<Course> findCoursesWithFeeLessThanEqual(@Param("maxFee") Double maxFee);
}
