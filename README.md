# Practice: Implement Synchronous Service Communication

## Problem Statement

Enhance the **LearnSphere** microservices architecture by enabling the **Enrollment Service** to communicate synchronously with the **Course Service**.

When the Enrollment Service needs course information, it must make a REST call to the Course Service using Spring's **RestClient**. The Course Service must be located through **Eureka service discovery** rather than using a fixed hostname or port.

The Enrollment Service must also handle communication failures gracefully. If the Course Service is unavailable or does not respond within the configured timeout, the Enrollment Service should return an appropriate response instead of crashing.

---

## Task-1: Configure RestClient in the Enrollment Service

### Task

Configure the Enrollment Service to use Spring's RestClient for synchronous REST communication.

### Requirements

The Enrollment Service must:

* Include the required Spring Web dependency.
* Create and configure a `RestClient`.
* Use the `RestClient` to make synchronous HTTP requests.
* Keep the REST communication logic in an appropriate service component.
* Not use `WebClient` or `RestTemplate` for this practice.

---

## Task-2: Discover the Course Service Using Eureka

### Task

Configure the Enrollment Service to locate the Course Service through Eureka service discovery.

### Requirements

The Enrollment Service must:

* Be configured as a Eureka client.
* Use the registered Course Service name to identify the downstream service.
* Resolve the Course Service through Eureka.
* Not use a fixed hostname or port for the Course Service.
* Use a load-balanced service URL when creating the request.

The request should follow the service-discovery-based approach:

```text
Enrollment Service
        │
        ▼
     Eureka
        │
        ▼
  Course Service
```

---

## Task-3: Retrieve Course Information from Enrollment Service

### Task

Add functionality in the Enrollment Service to retrieve course information from the Course Service.

### Requirements

The Enrollment Service must:

* Accept a course ID as part of an enrollment-related request.
* Use RestClient to request the corresponding course from the Course Service.
* Retrieve the course information from the downstream response.
* Use the retrieved Course information as part of the Enrollment Service response.

For example:

| HTTP Method | Endpoint                         | Purpose                                                   |
| ----------- | -------------------------------- | --------------------------------------------------------- |
| `GET`       | `/enrollments/course/{courseId}` | Retrieve course information through the Enrollment Service |

A request such as:

```http
GET /enrollments/course/101
```

should cause the Enrollment Service to retrieve Course `101` from the Course Service.

---

## Task-4: Configure a Downstream Timeout

### Task

Configure a suitable timeout for requests from the Enrollment Service to the Course Service.

### Requirements

The Enrollment Service must:

* Configure a response/read timeout.
* Apply the timeout configuration to requests made to the Course Service.
* Avoid allowing a downstream request to wait indefinitely.

The timeout values may be chosen appropriately for the application.

---

## Task-5: Handle Downstream Failures Gracefully

### Task

Handle situations where the Course Service cannot successfully respond to the Enrollment Service.

### Requirements

The Enrollment Service must handle scenarios such as:

* Course Service is unavailable.
* Course Service cannot be reached.
* Course Service does not respond within the configured timeout.
* Course Service returns an unsuccessful HTTP response.

When a downstream failure occurs:

* The exception must be handled by the Enrollment Service.
* The application must not crash.
* The client must receive an appropriate HTTP status.
* The response should contain a meaningful error message.

The exact error response format may vary, but it must clearly indicate that the Course Service could not be reached or did not respond successfully.

---

## Task-6: Verify Synchronous Communication

### Task

Verify successful and failure scenarios using the Enrollment and Course services.

### Requirements

Test the following scenarios:

| Scenario                   | Action                                                | Expected Result                                   |
| -------------------------- | ----------------------------------------------------- | ------------------------------------------------- |
| Course available           | Request an existing course through Enrollment Service | Course information is returned                    |
| Course not found           | Request a non-existing course                         | Appropriate error response is returned            |
| Course Service unavailable | Stop the Course Service and send the request          | Enrollment Service handles the failure gracefully |
| Course Service restored    | Start the Course Service again and retry              | Course information is retrieved successfully      |

Verify that the Enrollment Service remains running in all failure scenarios.

---

## Constraints

* Communication from Enrollment Service to Course Service must use **RestClient**.
* Course Service must be resolved using **Eureka service discovery**.
* A fixed Course Service hostname or port must not be used.
* The Enrollment Service must use synchronous REST communication.
* A response/read timeout must be configured.
* Downstream communication failures must be handled gracefully.
* The Enrollment Service must not crash when the Course Service is unavailable or times out.
* The existing Eureka Server must be used for service discovery.
* The Course Service must remain independently bootable.
* The Enrollment Service must remain independently bootable.

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
│   ├── pom.xml
│   └── src/
│       └── main/
│           ├── java/
│           │   ├── EnrollmentServiceApplication.java
│           │   ├── EnrollmentController.java
│           │   ├── EnrollmentService.java
│           │   └── CourseClient.java
│           └── resources/
│               └── application.properties
│
├── notification-service/
│   └── ...
│
└── README.md
```

---

## Architecture

```text
                         Client
                           │
                           ▼
                  Enrollment Service
                           │
                        RestClient
                           │
                           ▼
                       Eureka
                           │
                           ▼
                    Course Service
```

The Enrollment Service uses **RestClient** to make a synchronous request. Eureka is used to discover the Course Service instead of relying on a fixed host or port.

---

## Evaluation Criteria

1. RestClient is configured and used by the Enrollment Service for synchronous REST communication.
2. Course Service is resolved through Eureka service discovery.
3. No fixed Course Service hostname or port is used for the downstream request.
4. The Enrollment Service exposes an endpoint that triggers the Course Service request.
5. A response/read timeout is configured for the downstream request.
6. Course Service unavailability and downstream failures are handled gracefully with meaningful error responses to the client.
