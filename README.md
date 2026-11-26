# Practice: Implement Application-Level Caching

## Problem Statement

Enhance the **LearnSphere** microservices architecture by introducing **application-level caching** in a new **Resource Service**.

Create the Resource Service as an independent Spring Boot module under the existing LearnSphere parent project. The service will manage resource information and use **Spring Cache** with **Caffeine** to reduce repeated database access.

The Resource Service must cache resource data when it is retrieved and remove stale cached data when a resource is updated.

---

## Task-1: Create the Resource Service

### Task

Create a standalone Resource Service as a new Spring Boot module under the LearnSphere parent project.

### Requirements

The Resource Service must:

* Be added as a module in the LearnSphere parent Maven project.
* Have its own `pom.xml`.
* Be a Spring Boot application.
* Have its own application entry point.
* Be independently bootable.
* Have its own database configuration.
* Follow the same service structure used by the existing LearnSphere services.

The Resource Service must not be implemented as part of an existing service.

---

## Task-2: Create the Resource Document and DTO

### Task

Create the data model required by the Resource Service.

### Requirements

Create a `Resource` document containing appropriate resource information, such as:

* `id`
* `name`
* `description`
* `url`

The `Resource` class must:

* Be configured as a MongoDB document.
* Have a unique identifier.
* Store resource information in the Resource Service's database.

Create a separate `ResourceDTO` to represent resource data exposed through the REST API.

The API must use the DTO rather than directly exposing the persistence document.

---

## Task-3: Implement Resource Retrieval with Caching

### Task

Implement an API to retrieve a resource by ID and cache the retrieved resource.

### Requirements

Create the following API:

| HTTP Method | Endpoint              | Purpose                   |
| ----------- | --------------------- | ------------------------- |
| `GET`       | `/api/resources/{id}` | Retrieve a resource by ID |

The service method handling the retrieval must:

* Use `@Cacheable`.
* Use an appropriate cache name.
* Use the resource ID as the cache key.
* Retrieve the resource from the database when it is not present in the cache.
* Return the cached value for subsequent requests for the same resource.

The first request for a resource should retrieve the data from the database and place it in the cache.

Subsequent requests for the same resource should be served from the cache while the cached entry is valid.

---

## Task-4: Implement Resource Update with Cache Eviction

### Task

Implement an API to update a resource and remove its stale cached data.

### Requirements

Create the following API:

| HTTP Method | Endpoint              | Purpose           |
| ----------- | --------------------- | ----------------- |
| `PUT`       | `/api/resources/{id}` | Update a resource |

The service method handling the update must:

* Update the resource in the database.
* Use `@CacheEvict`.
* Evict the cache entry corresponding to the updated resource.
* Use the resource ID as the cache key.

After a successful update, the next GET request for the resource must retrieve the updated data from the database and place the new value in the cache.

---

## Task-5: Configure Caffeine Caching

### Task

Configure application-level caching using Spring Cache and Caffeine.

### Requirements

The Resource Service must:

* Include the required Spring Cache dependency.
* Include the Caffeine caching dependency.
* Enable Spring's caching support.
* Configure Caffeine as the cache provider.
* Define an appropriate cache name for resources.
* Configure a suitable TTL (time-to-live) for cached resource data.
* Ensure that cached entries expire after the configured TTL.

The TTL value may be chosen appropriately for the application.

---

## Task-6: Verify Cache Behavior

### Task

Verify that caching and cache eviction work correctly.

### Requirements

Test the following scenarios:

| Scenario           | Action                                                         | Expected Result                                               |
| ------------------ | -------------------------------------------------------------- | ------------------------------------------------------------- |
| First GET          | Request a resource for the first time                          | Resource is retrieved from the database and cached            |
| Repeated GET       | Request the same resource again                                | Cached resource is returned                                   |
| Different resource | Request another resource                                       | The corresponding resource is retrieved and cached separately |
| Update             | Update the resource using PUT                                  | Resource is updated and its cache entry is evicted            |
| GET after update   | Request the updated resource again                             | Updated resource is retrieved and cached                      |
| TTL expiry         | Wait for the configured TTL to expire and request the resource | Resource is retrieved again and cached                        |

Verify the cache behavior using suitable application logs or debugging.

> **Scope:** Verifying the scenarios with the application logs is sufficient. Automated integration tests (for example, with Testcontainers or a short test TTL) are optional.

### Verification Checklist

Start MongoDB and the Resource Service, then run the requests below and watch the Resource Service logs. The service logs a line such as `Fetching resource <id> from MongoDB` only when it reads from the database (cache miss).

**1. Create two resources (to have data to retrieve)**

```bash
curl -X POST http://localhost:8085/api/resources \
  -H "Content-Type: application/json" \
  -d '{"name": "Spring Boot Guide", "description": "Getting started", "url": "https://learnsphere.io/r/1"}'

curl -X POST http://localhost:8085/api/resources \
  -H "Content-Type: application/json" \
  -d '{"name": "Docker Basics", "description": "Containers", "url": "https://learnsphere.io/r/2"}'
```

Note the `id` returned for each resource.

**2. First GET (cache miss)**

```bash
curl http://localhost:8085/api/resources/<id1>
```

Expected: the resource is returned, and the log shows `Fetching resource <id1> from MongoDB`.

**3. Repeated GET (cache hit)**

```bash
curl http://localhost:8085/api/resources/<id1>
```

Expected: the same resource is returned, and the MongoDB-read log does **not** appear again.

**4. Different resource (cached separately)**

```bash
curl http://localhost:8085/api/resources/<id2>
curl http://localhost:8085/api/resources/<id2>
```

Expected: the first call logs `Fetching resource <id2> from MongoDB`; the second call does not.

**5. Update (cache eviction)**

```bash
curl -X PUT http://localhost:8085/api/resources/<id1> \
  -H "Content-Type: application/json" \
  -d '{"name": "Spring Boot Guide v2", "description": "Updated", "url": "https://learnsphere.io/r/1-v2"}'
```

Expected: `200 OK` with the updated resource; the log shows the update and cache eviction.

**6. GET after update**

```bash
curl http://localhost:8085/api/resources/<id1>
```

Expected: the updated data is returned, and the log shows `Fetching resource <id1> from MongoDB` again.

**7. TTL expiry**

Wait longer than the configured TTL, then request the resource again:

```bash
curl http://localhost:8085/api/resources/<id1>
```

Expected: the log shows `Fetching resource <id1> from MongoDB`, confirming the cached entry expired.

---

## Constraints

* The Resource Service must be created as a new Spring Boot module under the LearnSphere parent Maven project.
* The Resource Service must be independently bootable and manageable.
* Resource persistence must use a MongoDB document.
* A separate `ResourceDTO` must be used for API data.
* `GET /api/resources/{id}` must use `@Cacheable`.
* `PUT /api/resources/{id}` must use `@CacheEvict`.
* The cache must use Spring Cache with Caffeine.
* A suitable TTL must be configured for the resource cache.
* The resource ID must be used as the cache key.
* The Resource Service must not be added as functionality inside an existing LearnSphere service.
* Cached data must be evicted when the corresponding resource is successfully updated.
* Cache behavior may be verified using application logs; automated integration tests are optional.

---

## Project Structure

```text
learnsphere/
├── pom.xml
│
├── discovery-server/
│   └── ...
│
├── api-gateway/
│   └── ...
│
├── course-service/
│   └── ...
│
├── learner-service/
│   └── ...
│
├── enrollment-service/
│   └── ...
│
├── notification-service/
│   └── ...
│
├── resource-service/
│   ├── pom.xml
│   └── src/
│       └── main/
│           ├── java/
│           │   ├── ResourceServiceApplication.java
│           │   ├── Resource.java
│           │   ├── ResourceDTO.java
│           │   ├── ResourceRepository.java
│           │   ├── ResourceService.java
│           │   └── ResourceController.java
│           └── resources/
│               └── application.properties
│
└── README.md
```

---

## Architecture

```text
                         LearnSphere
                              │
                              ▼
                      Resource Service
                              │
                 ┌────────────┴────────────┐
                 │                         │
                 ▼                         ▼
             Caffeine                  MongoDB
              Cache                   Resource Data
                 │
                 │
        ┌────────┴────────┐
        │                 │
   GET /{id}         PUT /{id}
   @Cacheable        @CacheEvict
        │                 │
        ▼                 ▼
   Return cached     Update database
      data           Evict cache
```

The Resource Service owns its resource data and manages its application-level cache independently using Spring Cache and Caffeine.

---

## Evaluation Criteria

1. A standalone Resource Service module is added to the LearnSphere parent Maven project, with its own `pom.xml`, Spring Boot application class and `application.properties`.
2. A MongoDB `Resource` document and a separate `ResourceDTO` are implemented, and the REST API uses the `ResourceDTO`.
3. The service method for `GET /api/resources/{id}` is annotated with `@Cacheable` using the resource ID as the cache key.
4. The service method for `PUT /api/resources/{id}` updates the resource and is annotated with `@CacheEvict` using the resource ID as the cache key.
5. Spring caching is enabled and Caffeine is configured as the cache provider with a TTL for the resource cache.
