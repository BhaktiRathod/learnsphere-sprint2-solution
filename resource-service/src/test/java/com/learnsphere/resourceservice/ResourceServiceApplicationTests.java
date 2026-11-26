package com.learnsphere.resourceservice;

import com.learnsphere.resourceservice.document.Resource;
import com.learnsphere.resourceservice.dto.ResourceDTO;
import com.learnsphere.resourceservice.exception.ResourceNotFoundException;
import com.learnsphere.resourceservice.repository.ResourceRepository;
import com.learnsphere.resourceservice.service.ResourceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(properties = {"eureka.client.enabled=false"})
class ResourceServiceApplicationTests {

    @MockitoBean
    private ResourceRepository resourceRepository;

    @Autowired
    private ResourceService resourceService;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void clearCache() {
        cacheManager.getCache("resources").clear();
    }

    @Test
    @DisplayName("Caffeine is the cache provider and the 'resources' cache exists")
    void caffeineCacheIsConfigured() {
        assertThat(cacheManager).isInstanceOf(CaffeineCacheManager.class);
        assertThat(cacheManager.getCache("resources")).isNotNull();
    }

    @Test
    @DisplayName("First GET reads MongoDB; repeated GET for the same id is served from the cache")
    void repeatedGetIsServedFromCache() {
        when(resourceRepository.findById("r1"))
                .thenReturn(Optional.of(new Resource("r1", "Spring Guide", "Intro", "https://learnsphere.io/r1")));

        ResourceDTO first = resourceService.getResourceById("r1");
        ResourceDTO second = resourceService.getResourceById("r1");

        assertThat(first.getName()).isEqualTo("Spring Guide");
        assertThat(second.getName()).isEqualTo("Spring Guide");
        verify(resourceRepository, times(1)).findById("r1");
        assertThat(cacheManager.getCache("resources").get("r1")).isNotNull();
    }

    @Test
    @DisplayName("A different resource id is retrieved and cached separately")
    void differentIdIsCachedSeparately() {
        when(resourceRepository.findById("r1"))
                .thenReturn(Optional.of(new Resource("r1", "One", "d", "u1")));
        when(resourceRepository.findById("r2"))
                .thenReturn(Optional.of(new Resource("r2", "Two", "d", "u2")));

        resourceService.getResourceById("r1");
        resourceService.getResourceById("r2");

        verify(resourceRepository, times(1)).findById("r1");
        verify(resourceRepository, times(1)).findById("r2");
        assertThat(cacheManager.getCache("resources").get("r1")).isNotNull();
        assertThat(cacheManager.getCache("resources").get("r2")).isNotNull();
    }

    @Test
    @DisplayName("PUT evicts the cache entry; the next GET reads the updated resource from MongoDB")
    void updateEvictsCacheEntry() {
        Resource original = new Resource("r3", "Old Name", "Old", "https://learnsphere.io/r3");
        Resource updated = new Resource("r3", "New Name", "New", "https://learnsphere.io/r3-v2");
        when(resourceRepository.findById("r3")).thenReturn(Optional.of(original));
        when(resourceRepository.save(any(Resource.class))).thenReturn(updated);

        resourceService.getResourceById("r3");
        assertThat(cacheManager.getCache("resources").get("r3")).isNotNull();

        resourceService.updateResource("r3",
                new ResourceDTO(null, "New Name", "New", "https://learnsphere.io/r3-v2"));
        assertThat(cacheManager.getCache("resources").get("r3")).isNull();

        when(resourceRepository.findById("r3")).thenReturn(Optional.of(updated));
        ResourceDTO afterUpdate = resourceService.getResourceById("r3");

        assertThat(afterUpdate.getName()).isEqualTo("New Name");
        assertThat(cacheManager.getCache("resources").get("r3")).isNotNull();
    }

    @Test
    @DisplayName("GET for an unknown id throws ResourceNotFoundException and nothing is cached")
    void unknownIdIsNotCached() {
        when(resourceRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> resourceService.getResourceById("missing"))
                .isInstanceOf(ResourceNotFoundException.class);
        assertThat(cacheManager.getCache("resources").get("missing")).isNull();
    }
}
