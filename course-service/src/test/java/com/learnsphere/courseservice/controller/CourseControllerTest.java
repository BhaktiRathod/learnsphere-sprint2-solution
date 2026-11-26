package com.learnsphere.courseservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learnsphere.courseservice.dto.CourseDTO;
import com.learnsphere.courseservice.exception.DuplicateResourceException;
import com.learnsphere.courseservice.exception.GlobalExceptionHandler;
import com.learnsphere.courseservice.exception.ResourceNotFoundException;
import com.learnsphere.courseservice.service.CourseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CourseControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private CourseService courseService;

    @InjectMocks
    private CourseController courseController;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(courseController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /courses - returns 201 when course payload is valid")
    void testCreateCourse_ValidPayload_ReturnsCreated() throws Exception {
        CourseDTO inputDTO = new CourseDTO(null, "Spring Boot", "Deep Dive", "John Doe", 8, 199.99);
        CourseDTO createdDTO = new CourseDTO(1L, "Spring Boot", "Deep Dive", "John Doe", 8, 199.99);

        when(courseService.createCourse(any(CourseDTO.class))).thenReturn(createdDTO);

        mockMvc.perform(post("/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.title", is("Spring Boot")))
                .andExpect(jsonPath("$.fee", is(199.99)));
    }

    @Test
    @DisplayName("POST /courses - returns 409 when duplicate course title")
    void testCreateCourse_DuplicateTitle_ReturnsConflict() throws Exception {
        CourseDTO inputDTO = new CourseDTO(null, "Existing Course", "Desc", "Instructor", 4, 100.0);

        when(courseService.createCourse(any(CourseDTO.class)))
                .thenThrow(new DuplicateResourceException("Course already exists"));

        mockMvc.perform(post("/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDTO)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString("already exists")));
    }

    @Test
    @DisplayName("GET /courses - returns list of all courses")
    void testGetAllCourses() throws Exception {
        CourseDTO course1 = new CourseDTO(1L, "Course 1", "Desc 1", "Inst 1", 4, 99.0);
        CourseDTO course2 = new CourseDTO(2L, "Course 2", "Desc 2", "Inst 2", 6, 149.0);

        when(courseService.getAllCourses()).thenReturn(List.of(course1, course2));

        mockMvc.perform(get("/courses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].title", is("Course 1")))
                .andExpect(jsonPath("$[1].title", is("Course 2")));
    }

    @Test
    @DisplayName("GET /courses/{id} - returns course when found")
    void testGetCourseById_Found() throws Exception {
        CourseDTO course = new CourseDTO(1L, "Course 1", "Desc 1", "Inst 1", 4, 99.0);
        when(courseService.getCourseById(1L)).thenReturn(course);

        mockMvc.perform(get("/courses/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.title", is("Course 1")));
    }

    @Test
    @DisplayName("GET /courses/{id} - returns 404 when not found")
    void testGetCourseById_NotFound() throws Exception {
        when(courseService.getCourseById(99L))
                .thenThrow(new ResourceNotFoundException("Course not found with id: 99"));

        mockMvc.perform(get("/courses/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("not found with id: 99")));
    }

    @Test
    @DisplayName("PUT /courses/{id} - returns updated course")
    void testUpdateCourse() throws Exception {
        CourseDTO updateDTO = new CourseDTO(1L, "Updated Title", "Updated Desc", "Inst 1", 5, 120.0);
        when(courseService.updateCourse(eq(1L), any(CourseDTO.class))).thenReturn(updateDTO);

        mockMvc.perform(put("/courses/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Updated Title")));
    }

    @Test
    @DisplayName("DELETE /courses/{id} - returns 204 No Content")
    void testDeleteCourse() throws Exception {
        doNothing().when(courseService).deleteCourse(1L);

        mockMvc.perform(delete("/courses/1"))
                .andExpect(status().isNoContent());
    }
}
