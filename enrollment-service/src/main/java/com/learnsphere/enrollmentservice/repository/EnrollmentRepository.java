package com.learnsphere.enrollmentservice.repository;

import com.learnsphere.enrollmentservice.entity.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
}
