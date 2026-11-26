package com.learnsphere.resourceservice.service;

import com.learnsphere.resourceservice.dto.ResourceDTO;

public interface ResourceService {

    ResourceDTO createResource(ResourceDTO resourceDTO);

    ResourceDTO getResourceById(String id);

    ResourceDTO updateResource(String id, ResourceDTO resourceDTO);
}
