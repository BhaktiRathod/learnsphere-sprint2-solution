package com.learnsphere.resourceservice.repository;

import com.learnsphere.resourceservice.document.Resource;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ResourceRepository extends MongoRepository<Resource, String> {
}
