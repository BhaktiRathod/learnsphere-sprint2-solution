# Practice: Implement Distributed Logging and Tracing

## Problem Statement

Enhance the **LearnSphere** microservices architecture by introducing **correlation-ID logging**, **distributed tracing**, and **application metrics**.

A request in a microservices application can travel through multiple services before the operation is completed. The application should provide enough observability information to follow the request across these services and troubleshoot failures.

In this practice, you will propagate a correlation ID across service-to-service requests, configure **Micrometer Tracing** to generate and propagate trace IDs and span IDs, include tracing information in application logs, and expose relevant metrics through **Spring Boot Actuator**.

---

## Task-1: Implement Correlation-ID Logging

### Task

Add correlation-ID support across the LearnSphere microservices.

### Requirements

The application must:

* Accept a correlation ID from an incoming request when one is provided.
* Generate a new correlation ID when the incoming request does not contain one.
* Include the correlation ID in application logs.
* Make the correlation ID available throughout the processing of the request.
* Use the same correlation ID while the request travels across multiple services.

Use a consistent HTTP header for the correlation ID, such as:

```text
X-Correlation-ID
```

The API Gateway is the entry point, so it accepts the correlation ID from the client or generates one when it is missing. The downstream services read the correlation ID from the request header and add it to their logging context.

---

## Task-2: Propagate the Correlation ID Across Services

### Task

Propagate the correlation ID when one LearnSphere service calls another service.

### Requirements

When a service makes an HTTP request to another LearnSphere service:

* Include the correlation ID in the outgoing request.
* Preserve the same correlation ID across the service boundary.
* Ensure the receiving service uses the propagated correlation ID in its logs.
* Do not generate a different correlation ID for the same request flow.

For example:

```text
Client
  │
  │ X-Correlation-ID
  ▼
API Gateway
  │
  │ X-Correlation-ID
  ▼
Enrollment Service
  │
  │ X-Correlation-ID
  ▼
Course Service
```

---

## Task-3: Configure Micrometer Tracing

### Task

Configure distributed tracing using Micrometer Tracing.

### Requirements

The application must:

* Include the required Micrometer Tracing dependencies.
* Configure a tracing implementation appropriate for the application.
* Generate a trace ID for each traced request.
* Generate span IDs for individual operations within the request.
* Propagate tracing information across service-to-service requests.
* Ensure that the same trace ID can be used to follow a request across multiple services.

Configure tracing in the services that make and receive the service-to-service call (Enrollment Service and Course Service). Tracing in the API Gateway is optional. Exporting traces to an external tracing server (for example, Zipkin) is not required.

---

## Task-4: Add Trace and Span Information to Logs

### Task

Enhance application logs with distributed tracing information.

### Requirements

Application logs must include:

* Correlation ID.
* Trace ID.
* Span ID.

Logs should make it possible to identify which service processed each part of a request.

For example:

```text
correlationId=abc123 traceId=7f8a... spanId=91bc... Enrollment request received
correlationId=abc123 traceId=7f8a... spanId=42de... Calling Course Service
correlationId=abc123 traceId=7f8a... spanId=8ac1... Course request processed
```

The exact log format may vary, but the required identifiers must be available in the application logs.

---

## Task-5: Expose Application and Tracing Metrics

### Task

Expose relevant application and tracing metrics using Spring Boot Actuator.

### Requirements

The application must:

* Include Spring Boot Actuator.
* Expose the required Actuator endpoints.
* Expose relevant application metrics.
* Expose available tracing-related metrics.
* Allow the metrics to be viewed through an Actuator endpoint.

Verify the metrics using an appropriate endpoint such as:

```http
GET /actuator/metrics
```

The exact metrics available may depend on the configured tracing and application components.

---

## Task-6: Verify Distributed Observability

### Task

Verify the complete request flow across multiple LearnSphere services.

### Requirements

Send a request that travels through multiple services and verify:

| Verification            | Expected Result                                          |
| ----------------------- | -------------------------------------------------------- |
| Incoming request        | Correlation ID is created or accepted                    |
| API Gateway             | Correlation ID is added to the forwarded request         |
| Downstream request      | Correlation ID and tracing information are propagated    |
| Enrollment Service logs | Same correlation ID and trace ID are visible             |
| Course Service logs     | Same correlation ID and trace ID are visible             |
| Service boundaries      | Different span IDs identify individual operations        |
| Actuator metrics        | Relevant application/tracing metrics are available       |
| Complete request flow   | Request can be followed across the participating services |

Verify the complete flow using application logs and Spring Boot Actuator.

> **Scope:** These checks are for you to observe the behavior while practicing. Submitting captured logs or Actuator output is not required. Evaluation is based on the code and configuration (see Evaluation Criteria).

### Verification Checklist

Start the platform (`docker compose up --build -d`) and send a request through the API Gateway:

```bash
# With your own correlation ID
curl -X POST http://localhost:8080/enrollments \
  -H "Content-Type: application/json" \
  -H "X-Correlation-ID: abc123" \
  -d '{"learnerId": 101, "courseId": 1}'

# Without a correlation ID (the API Gateway generates one)
curl -X POST http://localhost:8080/enrollments \
  -H "Content-Type: application/json" \
  -d '{"learnerId": 101, "courseId": 1}'
```

Check the logs:

```bash
docker compose logs enrollment-service
docker compose logs course-service
```

Expected: the Enrollment Service lines (`Enrollment request received`, `Calling Course Service`) and the Course Service line (`Course request processed`) show the **same** `correlationId` and `traceId`, with **different** `spanId` values.

Check the metrics:

```bash
curl http://localhost:8083/actuator/metrics
curl http://localhost:8083/actuator/metrics/http.server.requests
curl http://localhost:8083/actuator/metrics/http.client.requests
```

---

## Constraints

* Correlation IDs must be propagated using a consistent HTTP header.
* A correlation ID must be generated when an incoming request does not already contain one.
* The same correlation ID must be propagated across service-to-service requests.
* Micrometer Tracing must be used for distributed tracing.
* Trace IDs and span IDs must be generated and propagated across participating services.
* Application logs must include the correlation ID, trace ID, and span ID.
* Spring Boot Actuator must be used to expose application and relevant tracing metrics.
* The implementation must support tracing across multiple LearnSphere services.
* The existing service communication must continue to work after observability features are added.

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
│   └── ...
│
└── README.md
```

The observability configuration should be added to the participating services without changing the existing business-service boundaries.

---

## Architecture

```text
                         Client
                           │
                           ▼
                    API Gateway
                           │
              Correlation ID + Trace
                           │
                           ▼
                  Enrollment Service
                           │
              Correlation ID + Trace
                           │
                           ▼
                    Course Service
```

Each service contributes logs and tracing information to the same request flow.

```text
Correlation ID → Identifies the overall request flow
Trace ID       → Identifies the distributed trace
Span ID        → Identifies an individual operation
```

---

## Evaluation Criteria

1. The API Gateway accepts the `X-Correlation-ID` header from the incoming request, or generates a new correlation ID when it is missing, and forwards it downstream.
2. The Enrollment and Course services add the received correlation ID to the logging context (MDC) so that it appears in application logs.
3. The Enrollment Service includes the `X-Correlation-ID` header in its request to the Course Service.
4. Micrometer Tracing is configured (tracing dependency and sampling probability) to generate trace IDs and span IDs.
5. The `RestClient` used for the service-to-service call is configured for tracing (observation registry) so that trace information is propagated to the Course Service.
6. The logging pattern includes the correlation ID, trace ID and span ID.
