package com.learnsphere.learnerservice.mapper;

import com.learnsphere.learnerservice.dto.LearnerDTO;
import com.learnsphere.learnerservice.entity.Learner;
import org.springframework.stereotype.Component;

import java.util.HashSet;

@Component
public class LearnerMapper {

    public Learner toEntity(LearnerDTO dto) {
        if (dto == null) {
            return null;
        }
        Learner learner = new Learner();
        learner.setId(dto.getId());
        learner.setFirstName(dto.getFirstName());
        learner.setLastName(dto.getLastName());
        learner.setEmail(dto.getEmail());
        learner.setPhone(dto.getPhone());
        if (dto.getEnrolledCourseIds() != null) {
            learner.setEnrolledCourseIds(new HashSet<>(dto.getEnrolledCourseIds()));
        }
        return learner;
    }

    public LearnerDTO toDTO(Learner learner) {
        if (learner == null) {
            return null;
        }
        LearnerDTO dto = new LearnerDTO();
        dto.setId(learner.getId());
        dto.setFirstName(learner.getFirstName());
        dto.setLastName(learner.getLastName());
        dto.setEmail(learner.getEmail());
        dto.setPhone(learner.getPhone());
        if (learner.getEnrolledCourseIds() != null) {
            dto.setEnrolledCourseIds(new HashSet<>(learner.getEnrolledCourseIds()));
        }
        return dto;
    }

    public void updateEntityFromDTO(LearnerDTO dto, Learner learner) {
        if (dto == null || learner == null) {
            return;
        }
        learner.setFirstName(dto.getFirstName());
        learner.setLastName(dto.getLastName());
        learner.setEmail(dto.getEmail());
        learner.setPhone(dto.getPhone());
    }
}
