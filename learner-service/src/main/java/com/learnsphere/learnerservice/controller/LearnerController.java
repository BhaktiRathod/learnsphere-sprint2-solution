package com.learnsphere.learnerservice.controller;

import com.learnsphere.learnerservice.dto.LearnerDTO;
import com.learnsphere.learnerservice.service.LearnerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping({"/learners", "/api/learners"})
public class LearnerController {

    private final LearnerService learnerService;

    public LearnerController(LearnerService learnerService) {
        this.learnerService = learnerService;
    }

    @PostMapping
    public ResponseEntity<LearnerDTO> createLearner(@Valid @RequestBody LearnerDTO learnerDTO) {
        LearnerDTO createdLearner = learnerService.createLearner(learnerDTO);
        return new ResponseEntity<>(createdLearner, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<LearnerDTO>> getAllLearners(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String lastName) {

        if (search != null && !search.trim().isEmpty()) {
            return ResponseEntity.ok(learnerService.searchLearnersByName(search.trim()));
        }
        if (lastName != null && !lastName.trim().isEmpty()) {
            return ResponseEntity.ok(learnerService.getLearnersByLastName(lastName.trim()));
        }

        return ResponseEntity.ok(learnerService.getAllLearners());
    }

    @GetMapping("/{id}")
    public ResponseEntity<LearnerDTO> getLearnerById(@PathVariable Long id) {
        return ResponseEntity.ok(learnerService.getLearnerById(id));
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<LearnerDTO> getLearnerByEmail(@PathVariable String email) {
        return ResponseEntity.ok(learnerService.getLearnerByEmail(email));
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<LearnerDTO>> getLearnersByCourseId(@PathVariable Long courseId) {
        return ResponseEntity.ok(learnerService.getLearnersByCourseId(courseId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<LearnerDTO> updateLearner(
            @PathVariable Long id,
            @Valid @RequestBody LearnerDTO learnerDTO) {
        return ResponseEntity.ok(learnerService.updateLearner(id, learnerDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLearner(@PathVariable Long id) {
        learnerService.deleteLearner(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{learnerId}/courses/{courseId}")
    public ResponseEntity<LearnerDTO> enrollCourse(
            @PathVariable Long learnerId,
            @PathVariable Long courseId) {
        LearnerDTO enrolledLearner = learnerService.enrollInCourse(learnerId, courseId);
        return ResponseEntity.ok(enrolledLearner);
    }

    @DeleteMapping("/{learnerId}/courses/{courseId}")
    public ResponseEntity<LearnerDTO> unenrollCourse(
            @PathVariable Long learnerId,
            @PathVariable Long courseId) {
        LearnerDTO updatedLearner = learnerService.unenrollFromCourse(learnerId, courseId);
        return ResponseEntity.ok(updatedLearner);
    }
}
