package com.learnsphere.learnerservice.service.impl;

import com.learnsphere.learnerservice.dto.LearnerDTO;
import com.learnsphere.learnerservice.entity.Learner;
import com.learnsphere.learnerservice.exception.DuplicateResourceException;
import com.learnsphere.learnerservice.exception.ResourceNotFoundException;
import com.learnsphere.learnerservice.mapper.LearnerMapper;
import com.learnsphere.learnerservice.repository.LearnerRepository;
import com.learnsphere.learnerservice.service.LearnerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class LearnerServiceImpl implements LearnerService {

    private final LearnerRepository learnerRepository;
    private final LearnerMapper learnerMapper;

    public LearnerServiceImpl(LearnerRepository learnerRepository,
                              LearnerMapper learnerMapper) {
        this.learnerRepository = learnerRepository;
        this.learnerMapper = learnerMapper;
    }

    @Override
    public LearnerDTO createLearner(LearnerDTO learnerDTO) {
        if (learnerRepository.existsByEmail(learnerDTO.getEmail())) {
            throw new DuplicateResourceException("Learner with email '" + learnerDTO.getEmail() + "' already exists");
        }
        Learner learner = learnerMapper.toEntity(learnerDTO);
        Learner savedLearner = learnerRepository.save(learner);
        return learnerMapper.toDTO(savedLearner);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LearnerDTO> getAllLearners() {
        return learnerRepository.findAll().stream()
                .map(learnerMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public LearnerDTO getLearnerById(Long id) {
        Learner learner = learnerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Learner not found with id: " + id));
        return learnerMapper.toDTO(learner);
    }

    @Override
    @Transactional(readOnly = true)
    public LearnerDTO getLearnerByEmail(String email) {
        Learner learner = learnerRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Learner not found with email: " + email));
        return learnerMapper.toDTO(learner);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LearnerDTO> getLearnersByLastName(String lastName) {
        return learnerRepository.findByLastNameIgnoreCase(lastName).stream()
                .map(learnerMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LearnerDTO> searchLearnersByName(String query) {
        return learnerRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(query, query).stream()
                .map(learnerMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LearnerDTO> getLearnersByCourseId(Long courseId) {
        return learnerRepository.findLearnersByCourseId(courseId).stream()
                .map(learnerMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public LearnerDTO updateLearner(Long id, LearnerDTO learnerDTO) {
        Learner existingLearner = learnerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Learner not found with id: " + id));

        // If email changed, check uniqueness
        if (!existingLearner.getEmail().equalsIgnoreCase(learnerDTO.getEmail()) &&
                learnerRepository.existsByEmail(learnerDTO.getEmail())) {
            throw new DuplicateResourceException("Learner with email '" + learnerDTO.getEmail() + "' already exists");
        }

        learnerMapper.updateEntityFromDTO(learnerDTO, existingLearner);
        Learner updatedLearner = learnerRepository.save(existingLearner);
        return learnerMapper.toDTO(updatedLearner);
    }

    @Override
    public void deleteLearner(Long id) {
        Learner learner = learnerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Learner not found with id: " + id));
        learnerRepository.delete(learner);
    }

    @Override
    public LearnerDTO enrollInCourse(Long learnerId, Long courseId) {
        Learner learner = learnerRepository.findById(learnerId)
                .orElseThrow(() -> new ResourceNotFoundException("Learner not found with id: " + learnerId));

        learner.addCourseId(courseId);
        Learner savedLearner = learnerRepository.save(learner);
        return learnerMapper.toDTO(savedLearner);
    }

    @Override
    public LearnerDTO unenrollFromCourse(Long learnerId, Long courseId) {
        Learner learner = learnerRepository.findById(learnerId)
                .orElseThrow(() -> new ResourceNotFoundException("Learner not found with id: " + learnerId));

        learner.removeCourseId(courseId);
        Learner savedLearner = learnerRepository.save(learner);
        return learnerMapper.toDTO(savedLearner);
    }
}
