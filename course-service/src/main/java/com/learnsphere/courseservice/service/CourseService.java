package com.learnsphere.courseservice.service;

import com.learnsphere.courseservice.dto.CourseDTO;

import java.util.List;

public interface CourseService {

    CourseDTO createCourse(CourseDTO courseDTO);

    List<CourseDTO> getAllCourses();

    CourseDTO getCourseById(Long id);

    CourseDTO updateCourse(Long id, CourseDTO courseDTO);

    void deleteCourse(Long id);

    List<CourseDTO> searchCoursesByTitle(String title);

    List<CourseDTO> getCoursesByInstructor(String instructor);

    List<CourseDTO> getCoursesByMaxFee(Double maxFee);

    List<CourseDTO> getCoursesByMaxDuration(Integer durationWeeks);
}
