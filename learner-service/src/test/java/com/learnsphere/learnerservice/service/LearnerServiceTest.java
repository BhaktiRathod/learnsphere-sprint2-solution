package com.learnsphere.learnerservice.service;

import com.learnsphere.learnerservice.dto.LearnerDTO;
import com.learnsphere.learnerservice.entity.Learner;
import com.learnsphere.learnerservice.exception.DuplicateResourceException;
import com.learnsphere.learnerservice.exception.ResourceNotFoundException;
import com.learnsphere.learnerservice.mapper.LearnerMapper;
import com.learnsphere.learnerservice.repository.LearnerRepository;
import com.learnsphere.learnerservice.service.impl.LearnerServiceImpl;
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
class LearnerServiceTest {

    @Mock
    private LearnerRepository learnerRepository;

    private LearnerMapper learnerMapper;
    private LearnerServiceImpl learnerService;

    private Learner learner;
    private LearnerDTO learnerDTO;

    @BeforeEach
    void setUp() {
        learnerMapper = new LearnerMapper();
        learnerService = new LearnerServiceImpl(learnerRepository, learnerMapper);

        learner = new Learner(1L, "John", "Doe", "john@example.com", "555-1234");
        learnerDTO = new LearnerDTO(1L, "John", "Doe", "john@example.com", "555-1234");
    }

    @Test
    @DisplayName("createLearner - successfully creates learner when email is unique")
    void testCreateLearner_Success() {
        when(learnerRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(learnerRepository.save(any(Learner.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LearnerDTO result = learnerService.createLearner(learnerDTO);

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("john@example.com");
        verify(learnerRepository).save(any(Learner.class));
    }

    @Test
    @DisplayName("createLearner - throws DuplicateResourceException when email already exists")
    void testCreateLearner_DuplicateEmail() {
        when(learnerRepository.existsByEmail("john@example.com")).thenReturn(true);

        assertThatThrownBy(() -> learnerService.createLearner(learnerDTO))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("already exists");

        verify(learnerRepository, never()).save(any(Learner.class));
    }

    @Test
    @DisplayName("getAllLearners - returns list of all learner DTOs")
    void testGetAllLearners() {
        when(learnerRepository.findAll()).thenReturn(List.of(learner));

        List<LearnerDTO> results = learnerService.getAllLearners();

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getFirstName()).isEqualTo("John");
    }

    @Test
    @DisplayName("getLearnerById - returns learner DTO when found")
    void testGetLearnerById_Found() {
        when(learnerRepository.findById(1L)).thenReturn(Optional.of(learner));

        LearnerDTO result = learnerService.getLearnerById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("getLearnerById - throws ResourceNotFoundException when not found")
    void testGetLearnerById_NotFound() {
        when(learnerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> learnerService.getLearnerById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Learner not found with id: 99");
    }

    @Test
    @DisplayName("enrollInCourse - adds courseId to learner")
    void testEnrollInCourse() {
        when(learnerRepository.findById(1L)).thenReturn(Optional.of(learner));
        when(learnerRepository.save(any(Learner.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LearnerDTO result = learnerService.enrollInCourse(1L, 101L);

        assertThat(result.getEnrolledCourseIds()).contains(101L);
    }

    @Test
    @DisplayName("unenrollFromCourse - removes courseId from learner")
    void testUnenrollFromCourse() {
        learner.addCourseId(101L);
        when(learnerRepository.findById(1L)).thenReturn(Optional.of(learner));
        when(learnerRepository.save(any(Learner.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LearnerDTO result = learnerService.unenrollFromCourse(1L, 101L);

        assertThat(result.getEnrolledCourseIds()).doesNotContain(101L);
    }
}
