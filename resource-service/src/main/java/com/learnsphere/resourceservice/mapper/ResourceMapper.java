package com.learnsphere.resourceservice.mapper;

import com.learnsphere.resourceservice.document.Resource;
import com.learnsphere.resourceservice.dto.ResourceDTO;

public class ResourceMapper {

    public static Resource toEntity(ResourceDTO dto) {
        if (dto == null) return null;
        Resource resource = new Resource();
        resource.setName(dto.getName());
        resource.setDescription(dto.getDescription());
        resource.setUrl(dto.getUrl());
        return resource;
    }

    public static ResourceDTO toDTO(Resource resource) {
        if (resource == null) return null;
        return new ResourceDTO(
                resource.getId(),
                resource.getName(),
                resource.getDescription(),
                resource.getUrl()
        );
    }
}
