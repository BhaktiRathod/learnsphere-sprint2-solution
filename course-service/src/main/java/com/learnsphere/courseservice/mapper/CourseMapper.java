package com.learnsphere.courseservice.mapper;

import com.learnsphere.courseservice.dto.CourseDTO;
import com.learnsphere.courseservice.entity.Course;
import org.springframework.stereotype.Component;

@Component
public class CourseMapper {

    public Course toEntity(CourseDTO dto) {
        if (dto == null) {
            return null;
        }
        Course course = new Course();
        course.setId(dto.getId());
        course.setTitle(dto.getTitle());
        course.setDescription(dto.getDescription());
        course.setInstructor(dto.getInstructor());
        course.setDurationWeeks(dto.getDurationWeeks());
        course.setFee(dto.getFee());
        return course;
    }

    public CourseDTO toDTO(Course course) {
        if (course == null) {
            return null;
        }
        CourseDTO dto = new CourseDTO();
        dto.setId(course.getId());
        dto.setTitle(course.getTitle());
        dto.setDescription(course.getDescription());
        dto.setInstructor(course.getInstructor());
        dto.setDurationWeeks(course.getDurationWeeks());
        dto.setFee(course.getFee());
        dto.setLearnerCount(0);
        return dto;
    }

    public void updateEntityFromDTO(CourseDTO dto, Course course) {
        if (dto == null || course == null) {
            return;
        }
        course.setTitle(dto.getTitle());
        course.setDescription(dto.getDescription());
        course.setInstructor(dto.getInstructor());
        course.setDurationWeeks(dto.getDurationWeeks());
        course.setFee(dto.getFee());
    }
}
