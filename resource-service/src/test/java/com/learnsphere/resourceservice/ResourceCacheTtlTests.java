package com.learnsphere.resourceservice;

import com.learnsphere.resourceservice.document.Resource;
import com.learnsphere.resourceservice.repository.ResourceRepository;
import com.learnsphere.resourceservice.service.ResourceService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Uses a short test TTL so cache expiry can be verified quickly.
@SpringBootTest(properties = {
        "eureka.client.enabled=false",
        "spring.cache.caffeine.spec=maximumSize=500,expireAfterWrite=1s"
})
class ResourceCacheTtlTests {

    @MockitoBean
    private ResourceRepository resourceRepository;

    @Autowired
    private ResourceService resourceService;

    @Test
    @DisplayName("After the TTL expires, the resource is read from MongoDB again and re-cached")
    void cachedEntryExpiresAfterTtl() throws InterruptedException {
        when(resourceRepository.findById("ttl-1"))
                .thenReturn(Optional.of(new Resource("ttl-1", "TTL Guide", "d", "u")));

        // First GET: cache miss -> MongoDB read
        resourceService.getResourceById("ttl-1");
        // Within the TTL: cache hit -> no MongoDB read
        resourceService.getResourceById("ttl-1");
        verify(resourceRepository, times(1)).findById("ttl-1");

        // Wait longer than the 1-second test TTL
        Thread.sleep(1500);

        // After the TTL: cache miss -> MongoDB read again
        assertThat(resourceService.getResourceById("ttl-1").getName()).isEqualTo("TTL Guide");
        verify(resourceRepository, times(2)).findById("ttl-1");

        // Re-cached: next GET is a hit again
        resourceService.getResourceById("ttl-1");
        verify(resourceRepository, times(2)).findById("ttl-1");
    }
}
