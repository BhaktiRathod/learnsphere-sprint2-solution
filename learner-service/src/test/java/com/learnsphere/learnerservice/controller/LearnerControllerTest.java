package com.learnsphere.learnerservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learnsphere.learnerservice.dto.LearnerDTO;
import com.learnsphere.learnerservice.exception.DuplicateResourceException;
import com.learnsphere.learnerservice.exception.GlobalExceptionHandler;
import com.learnsphere.learnerservice.exception.ResourceNotFoundException;
import com.learnsphere.learnerservice.service.LearnerService;
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
import java.util.Set;

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
class LearnerControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private LearnerService learnerService;

    @InjectMocks
    private LearnerController learnerController;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(learnerController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /learners - returns 201 when learner payload is valid")
    void testCreateLearner_ValidPayload_ReturnsCreated() throws Exception {
        LearnerDTO inputDTO = new LearnerDTO(null, "Alice", "Smith", "alice@example.com", "555-0100");
        LearnerDTO createdDTO = new LearnerDTO(1L, "Alice", "Smith", "alice@example.com", "555-0100");

        when(learnerService.createLearner(any(LearnerDTO.class))).thenReturn(createdDTO);

        mockMvc.perform(post("/learners")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.firstName", is("Alice")))
                .andExpect(jsonPath("$.email", is("alice@example.com")));
    }

    @Test
    @DisplayName("GET /learners - returns list of all learners")
    void testGetAllLearners() throws Exception {
        LearnerDTO l1 = new LearnerDTO(1L, "Alice", "Smith", "alice@example.com", "555-0100");
        LearnerDTO l2 = new LearnerDTO(2L, "Bob", "Jones", "bob@example.com", "555-0200");

        when(learnerService.getAllLearners()).thenReturn(List.of(l1, l2));

        mockMvc.perform(get("/learners"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].firstName", is("Alice")))
                .andExpect(jsonPath("$[1].firstName", is("Bob")));
    }

    @Test
    @DisplayName("GET /learners/{id} - returns learner when found")
    void testGetLearnerById_Found() throws Exception {
        LearnerDTO l1 = new LearnerDTO(1L, "Alice", "Smith", "alice@example.com", "555-0100");
        when(learnerService.getLearnerById(1L)).thenReturn(l1);

        mockMvc.perform(get("/learners/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.email", is("alice@example.com")));
    }

    @Test
    @DisplayName("GET /learners/{id} - returns 404 when not found")
    void testGetLearnerById_NotFound() throws Exception {
        when(learnerService.getLearnerById(99L))
                .thenThrow(new ResourceNotFoundException("Learner not found with id: 99"));

        mockMvc.perform(get("/learners/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("not found with id: 99")));
    }

    @Test
    @DisplayName("PUT /learners/{id} - updates learner")
    void testUpdateLearner() throws Exception {
        LearnerDTO updateDTO = new LearnerDTO(1L, "Alice", "Johnson", "alice@example.com", "555-0100");
        when(learnerService.updateLearner(eq(1L), any(LearnerDTO.class))).thenReturn(updateDTO);

        mockMvc.perform(put("/learners/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lastName", is("Johnson")));
    }

    @Test
    @DisplayName("DELETE /learners/{id} - returns 204 No Content")
    void testDeleteLearner() throws Exception {
        doNothing().when(learnerService).deleteLearner(1L);

        mockMvc.perform(delete("/learners/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("POST /learners/{learnerId}/courses/{courseId} - enrolls learner in course")
    void testEnrollCourse() throws Exception {
        LearnerDTO enrolledDTO = new LearnerDTO(1L, "Alice", "Smith", "alice@example.com", "555-0100",
                Set.of(10L), Set.of());

        when(learnerService.enrollInCourse(1L, 10L)).thenReturn(enrolledDTO);

        mockMvc.perform(post("/learners/1/courses/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enrolledCourseIds", hasSize(1)));
    }

    @Test
    @DisplayName("DELETE /learners/{learnerId}/courses/{courseId} - unenrolls learner from course")
    void testUnenrollCourse() throws Exception {
        LearnerDTO unenrolledDTO = new LearnerDTO(1L, "Alice", "Smith", "alice@example.com", "555-0100");

        when(learnerService.unenrollFromCourse(1L, 10L)).thenReturn(unenrolledDTO);

        mockMvc.perform(delete("/learners/1/courses/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enrolledCourseIds", hasSize(0)));
    }
}
