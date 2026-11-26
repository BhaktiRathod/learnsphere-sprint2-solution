package com.learnsphere.resourceservice.service;

import com.learnsphere.resourceservice.document.Resource;
import com.learnsphere.resourceservice.dto.ResourceDTO;
import com.learnsphere.resourceservice.exception.ResourceNotFoundException;
import com.learnsphere.resourceservice.mapper.ResourceMapper;
import com.learnsphere.resourceservice.repository.ResourceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class ResourceServiceImpl implements ResourceService {

    private static final Logger log = LoggerFactory.getLogger(ResourceServiceImpl.class);

    private final ResourceRepository resourceRepository;

    public ResourceServiceImpl(ResourceRepository resourceRepository) {
        this.resourceRepository = resourceRepository;
    }

    @Override
    public ResourceDTO createResource(ResourceDTO resourceDTO) {
        Resource saved = resourceRepository.save(ResourceMapper.toEntity(resourceDTO));
        return ResourceMapper.toDTO(saved);
    }

    // Cache miss: runs this method and stores the result in the "resources" cache under the id.
    // Cache hit: returns the cached value without running this method (no MongoDB read).
    @Override
    @Cacheable(cacheNames = "resources", key = "#id")
    public ResourceDTO getResourceById(String id) {
        log.info("Fetching resource {} from MongoDB", id);
        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found with id: " + id));
        return ResourceMapper.toDTO(resource);
    }

    // Updates the resource in MongoDB and removes its stale entry from the "resources" cache.
    @Override
    @CacheEvict(cacheNames = "resources", key = "#id")
    public ResourceDTO updateResource(String id, ResourceDTO resourceDTO) {
        log.info("Updating resource {} in MongoDB and evicting its cache entry", id);
        Resource existing = resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found with id: " + id));
        existing.setName(resourceDTO.getName());
        existing.setDescription(resourceDTO.getDescription());
        existing.setUrl(resourceDTO.getUrl());
        Resource saved = resourceRepository.save(existing);
        return ResourceMapper.toDTO(saved);
    }
}
