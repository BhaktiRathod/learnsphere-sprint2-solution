package com.learnsphere.courseservice.service;

import com.learnsphere.courseservice.dto.CourseDTO;
import com.learnsphere.courseservice.entity.Course;
import com.learnsphere.courseservice.exception.DuplicateResourceException;
import com.learnsphere.courseservice.exception.ResourceNotFoundException;
import com.learnsphere.courseservice.mapper.CourseMapper;
import com.learnsphere.courseservice.repository.CourseRepository;
import com.learnsphere.courseservice.service.impl.CourseServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock
    private CourseRepository courseRepository;

    private CourseMapper courseMapper;
    private CourseServiceImpl courseService;

    private Course course;
    private CourseDTO courseDTO;

    @BeforeEach
    void setUp() {
        courseMapper = new CourseMapper();
        courseService = new CourseServiceImpl(courseRepository, courseMapper);

        course = new Course(1L, "Fullstack Java", "Complete guide", "Dr. Smith", 12, 199.99);
        courseDTO = new CourseDTO(1L, "Fullstack Java", "Complete guide", "Dr. Smith", 12, 199.99);
    }

    @Test
    @DisplayName("createCourse - successfully creates course when title is unique")
    void testCreateCourse_Success() {
        when(courseRepository.existsByTitle("Fullstack Java")).thenReturn(false);
        when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CourseDTO result = courseService.createCourse(courseDTO);

        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Fullstack Java");
        verify(courseRepository).save(any(Course.class));
    }

    @Test
    @DisplayName("createCourse - throws DuplicateResourceException when title already exists")
    void testCreateCourse_DuplicateTitle() {
        when(courseRepository.existsByTitle("Fullstack Java")).thenReturn(true);

        assertThatThrownBy(() -> courseService.createCourse(courseDTO))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("already exists");

        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    @DisplayName("getAllCourses - returns list of course DTOs")
    void testGetAllCourses() {
        when(courseRepository.findAll()).thenReturn(List.of(course));

        List<CourseDTO> results = courseService.getAllCourses();

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getTitle()).isEqualTo("Fullstack Java");
    }

    @Test
    @DisplayName("getCourseById - returns course DTO when found")
    void testGetCourseById_Found() {
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));

        CourseDTO result = courseService.getCourseById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("getCourseById - throws ResourceNotFoundException when not found")
    void testGetCourseById_NotFound() {
        when(courseRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courseService.getCourseById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Course not found with id: 99");
    }

    @Test
    @DisplayName("updateCourse - successfully updates existing course")
    void testUpdateCourse_Success() {
        CourseDTO updateDTO = new CourseDTO(1L, "Fullstack Java Advanced", "Updated desc", "Dr. Smith", 14, 249.99);

        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
        when(courseRepository.existsByTitle("Fullstack Java Advanced")).thenReturn(false);
        when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CourseDTO result = courseService.updateCourse(1L, updateDTO);

        assertThat(result.getTitle()).isEqualTo("Fullstack Java Advanced");
        assertThat(result.getDurationWeeks()).isEqualTo(14);
    }

    @Test
    @DisplayName("deleteCourse - deletes existing course")
    void testDeleteCourse_Success() {
        when(courseRepository.findById(1L)).thenReturn(Optional.of(course));

        courseService.deleteCourse(1L);

        verify(courseRepository).delete(course);
    }
}
