package com.learnsphere.courseservice.controller;

import com.learnsphere.courseservice.dto.CourseDTO;
import com.learnsphere.courseservice.service.CourseService;
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
@RequestMapping({"/courses", "/api/courses"})
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @PostMapping
    public ResponseEntity<CourseDTO> createCourse(@Valid @RequestBody CourseDTO courseDTO) {
        CourseDTO createdCourse = courseService.createCourse(courseDTO);
        return new ResponseEntity<>(createdCourse, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<CourseDTO>> getAllCourses(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String instructor,
            @RequestParam(required = false) Double maxFee,
            @RequestParam(required = false) Integer maxDuration) {

        if (title != null && !title.trim().isEmpty()) {
            return ResponseEntity.ok(courseService.searchCoursesByTitle(title.trim()));
        }
        if (instructor != null && !instructor.trim().isEmpty()) {
            return ResponseEntity.ok(courseService.getCoursesByInstructor(instructor.trim()));
        }
        if (maxFee != null) {
            return ResponseEntity.ok(courseService.getCoursesByMaxFee(maxFee));
        }
        if (maxDuration != null) {
            return ResponseEntity.ok(courseService.getCoursesByMaxDuration(maxDuration));
        }

        return ResponseEntity.ok(courseService.getAllCourses());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CourseDTO> getCourseById(@PathVariable Long id) {
        return ResponseEntity.ok(courseService.getCourseById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CourseDTO> updateCourse(
            @PathVariable Long id,
            @Valid @RequestBody CourseDTO courseDTO) {
        return ResponseEntity.ok(courseService.updateCourse(id, courseDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id) {
        courseService.deleteCourse(id);
        return ResponseEntity.noContent().build();
    }
}
