package com.learnsphere.courseservice.service.impl;

import com.learnsphere.courseservice.dto.CourseDTO;
import com.learnsphere.courseservice.entity.Course;
import com.learnsphere.courseservice.exception.DuplicateResourceException;
import com.learnsphere.courseservice.exception.ResourceNotFoundException;
import com.learnsphere.courseservice.mapper.CourseMapper;
import com.learnsphere.courseservice.repository.CourseRepository;
import com.learnsphere.courseservice.service.CourseService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final CourseMapper courseMapper;

    public CourseServiceImpl(CourseRepository courseRepository, CourseMapper courseMapper) {
        this.courseRepository = courseRepository;
        this.courseMapper = courseMapper;
    }

    @Override
    public CourseDTO createCourse(CourseDTO courseDTO) {
        if (courseRepository.existsByTitle(courseDTO.getTitle())) {
            throw new DuplicateResourceException("Course with title '" + courseDTO.getTitle() + "' already exists");
        }
        Course course = courseMapper.toEntity(courseDTO);
        Course savedCourse = courseRepository.save(course);
        return courseMapper.toDTO(savedCourse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseDTO> getAllCourses() {
        return courseRepository.findAll().stream()
                .map(courseMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CourseDTO getCourseById(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));
        return courseMapper.toDTO(course);
    }

    @Override
    public CourseDTO updateCourse(Long id, CourseDTO courseDTO) {
        Course existingCourse = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));

        // If title changed, check uniqueness
        if (!existingCourse.getTitle().equalsIgnoreCase(courseDTO.getTitle()) &&
                courseRepository.existsByTitle(courseDTO.getTitle())) {
            throw new DuplicateResourceException("Course with title '" + courseDTO.getTitle() + "' already exists");
        }

        courseMapper.updateEntityFromDTO(courseDTO, existingCourse);
        Course updatedCourse = courseRepository.save(existingCourse);
        return courseMapper.toDTO(updatedCourse);
    }

    @Override
    public void deleteCourse(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + id));
        courseRepository.delete(course);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseDTO> searchCoursesByTitle(String title) {
        return courseRepository.findByTitleContainingIgnoreCase(title).stream()
                .map(courseMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseDTO> getCoursesByInstructor(String instructor) {
        return courseRepository.findByInstructorIgnoreCase(instructor).stream()
                .map(courseMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseDTO> getCoursesByMaxFee(Double maxFee) {
        return courseRepository.findCoursesWithFeeLessThanEqual(maxFee).stream()
                .map(courseMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseDTO> getCoursesByMaxDuration(Integer durationWeeks) {
        return courseRepository.findByDurationWeeksLessThanEqual(durationWeeks).stream()
                .map(courseMapper::toDTO)
                .collect(Collectors.toList());
    }
}
