package com.learnsphere.learnerservice.service;

import com.learnsphere.learnerservice.dto.LearnerDTO;

import java.util.List;

public interface LearnerService {

    LearnerDTO createLearner(LearnerDTO learnerDTO);

    List<LearnerDTO> getAllLearners();

    LearnerDTO getLearnerById(Long id);

    LearnerDTO getLearnerByEmail(String email);

    List<LearnerDTO> getLearnersByLastName(String lastName);

    List<LearnerDTO> searchLearnersByName(String query);

    List<LearnerDTO> getLearnersByCourseId(Long courseId);

    LearnerDTO updateLearner(Long id, LearnerDTO learnerDTO);

    void deleteLearner(Long id);

    LearnerDTO enrollInCourse(Long learnerId, Long courseId);

    LearnerDTO unenrollFromCourse(Long learnerId, Long courseId);
}
