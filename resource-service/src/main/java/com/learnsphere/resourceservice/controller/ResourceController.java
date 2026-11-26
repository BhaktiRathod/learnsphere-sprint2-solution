package com.learnsphere.resourceservice.controller;

import com.learnsphere.resourceservice.dto.ResourceDTO;
import com.learnsphere.resourceservice.service.ResourceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/resources")
public class ResourceController {

    private final ResourceService resourceService;

    public ResourceController(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    // Creates a resource so there is data to retrieve and cache.
    @PostMapping
    public ResponseEntity<ResourceDTO> createResource(@RequestBody ResourceDTO resourceDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(resourceService.createResource(resourceDTO));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResourceDTO> getResource(@PathVariable String id) {
        return ResponseEntity.ok(resourceService.getResourceById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResourceDTO> updateResource(@PathVariable String id,
                                                      @RequestBody ResourceDTO resourceDTO) {
        return ResponseEntity.ok(resourceService.updateResource(id, resourceDTO));
    }
}
