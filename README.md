# Practice: Handle Service Failures with Resilience4j

## Problem Statement

Enhance the **Enrollment Service** by making its communication with the **Course Service** resilient to temporary downstream failures.

The Enrollment Service already uses synchronous communication to retrieve Course information before creating an enrollment. In this practice, you will use **Resilience4j Retry** and **Circuit Breaker** to handle situations where the Course Service is temporarily unavailable.

The Enrollment Service must retry only appropriate downstream availability failures, prevent enrollment creation when Course validation cannot be completed, and return a controlled **503 Service Unavailable** response when the Course Service remains unavailable.

The Circuit Breaker must also expose its state through **Spring Boot Actuator** and log state transitions so that the complete `CLOSED → OPEN → HALF_OPEN → CLOSED` lifecycle can be observed.

---

## Task-1: Configure Resilience4j

### Task

Add and configure Resilience4j in the Enrollment Service.

### Requirements

The Enrollment Service must:

* Include the required Resilience4j dependencies.
* Configure a Retry instance for Course Service communication.
* Configure a Circuit Breaker for Course Service communication.
* Apply the resilience mechanisms to the existing Course Service call.
* Use appropriate configuration values for retry attempts, wait duration, and Circuit Breaker thresholds.

---

## Task-2: Configure Retry for Course Service Availability Failures

### Task

Configure Retry so that temporary Course Service availability failures are retried.

### Requirements

The Retry configuration must:

* Retry only failures that indicate the Course Service is unavailable.
* Retry communication failures such as connection failures or downstream timeouts.
* Not retry a `404 Not Found` response from the Course Service.
* Not retry other failures that represent a valid downstream response.
* Use a finite maximum number of retry attempts.
* Use an appropriate wait duration between retry attempts.

For example:

```text
Course Service unavailable
        │
        ▼
     Retry
        │
   ┌────┴────┐
   │         │
Success    Still failing
   │         │
   ▼         ▼
Continue   Circuit Breaker
```

A missing Course is a valid business response and must not be treated as a temporary availability failure.

---

## Task-3: Configure the Circuit Breaker

### Task

Configure a Circuit Breaker to track the relevant Course Service availability failures.

### Requirements

The Circuit Breaker must:

* Track only the failures that represent Course Service availability problems.
* Not treat a missing Course (`404`) as a Circuit Breaker failure.
* Open the circuit after the configured failure threshold is reached.
* Prevent unnecessary downstream calls while the circuit is open.
* Transition to `HALF_OPEN` after the configured wait duration.
* Allow trial requests in `HALF_OPEN`.
* Return to `CLOSED` when the Course Service successfully recovers.

The expected state lifecycle is:

```text
CLOSED
   │
   │ Repeated Course Service failures
   ▼
 OPEN
   │
   │ Wait duration
   ▼
HALF_OPEN
   │
   │ Successful trial request
   ▼
 CLOSED
```

---

## Task-4: Prevent Enrollment Creation During Course Service Failure

### Task

Ensure that an enrollment is created only after the Course Service successfully validates the requested Course.

### Requirements

The Enrollment Service must:

* Request the Course information before creating the enrollment.
* Create the enrollment only when Course validation succeeds.
* Not create an enrollment when the Course Service is unavailable.
* Not create an enrollment when the downstream request times out.
* Not create an enrollment when the Circuit Breaker is open.
* Not fabricate or substitute Course information when the Course Service cannot be reached.

The enrollment must be persisted only after successful Course validation.

---

## Task-5: Configure a Controlled Fallback

### Task

Provide a fallback for Course Service availability failures.

### Requirements

The fallback must:

* Handle failures after Retry and Circuit Breaker processing.
* Return a controlled `503 Service Unavailable` response.
* Provide a meaningful error message.
* Not create an enrollment.
* Not create or fabricate Course data.
* Not hide a genuine `404 Course Not Found` response as a `503`.

For example:

```json
{
  "status": 503,
  "message": "Course service is currently unavailable. Please try again later."
}
```

The exact response structure may vary, but the response must clearly indicate that the downstream Course Service is unavailable.

---

## Task-6: Log Circuit Breaker State Transitions

### Task

Configure logging so that Circuit Breaker state changes can be observed.

### Requirements

The Enrollment Service must log Circuit Breaker state transitions, including transitions such as:

```text
CLOSED → OPEN
OPEN → HALF_OPEN
HALF_OPEN → CLOSED
```

The logs must make it possible to identify:

* The previous state.
* The new state.

For example:

```text
Circuit Breaker State Changed: State transition from CLOSED to OPEN
```

Printing the transition (for example, with `System.out.println`, as shown in class) or using a logger are both acceptable.

---

## Task-7: Expose Circuit Breaker Health Through Actuator

### Task

Expose the Circuit Breaker state through Spring Boot Actuator.

### Requirements

The Enrollment Service must:

* Include Spring Boot Actuator.
* Expose the required Actuator endpoint.
* Expose Circuit Breaker health information.
* Allow the current Circuit Breaker state to be observed.

Verify the Circuit Breaker state using an appropriate Actuator endpoint.

For example:

```http
GET /actuator/health
```

The response should provide Circuit Breaker health information when the Circuit Breaker is configured for health monitoring.

---

## Task-8: Verify Circuit Breaker State Transitions

### Task

Stop and restart the Course Service to observe the Circuit Breaker lifecycle.

### Requirements

Verify the following scenarios:

| Scenario           | Action                                                         | Expected Result                                         |
| ------------------ | -------------------------------------------------------------- | ------------------------------------------------------- |
| Normal operation   | Course Service is running and an enrollment is requested       | Course is validated and enrollment is created           |
| Course unavailable | Stop Course Service and request an enrollment                  | Retry attempts occur and enrollment is not created      |
| Circuit opens      | Continue sending requests while Course Service is unavailable  | Circuit transitions from `CLOSED` to `OPEN`             |
| Circuit open       | Send another enrollment request                                | Request fails fast without creating an enrollment       |
| Half-open          | Wait for the configured open-state duration                    | Circuit transitions to `HALF_OPEN`                      |
| Recovery           | Restart Course Service and send a trial request                | Course validation succeeds                              |
| Circuit closes     | Continue sending successful requests                           | Circuit transitions from `HALF_OPEN` to `CLOSED`        |
| Course not found   | Request an enrollment for a non-existing Course                | `404` is returned without Retry or Circuit Breaker failure |

Verify the Circuit Breaker state using both application logs and Spring Boot Actuator.

> **Scope:** These scenarios are for you to observe the behavior while practicing. Submitting captured logs, Actuator output or enrollment counts is not required, and automated integration tests are optional. Evaluation is based on the code and configuration (see Evaluation Criteria).

### Verification Checklist

Start the platform (`docker compose up --build -d`), then send enrollment requests and watch the Enrollment Service logs (`docker compose logs -f enrollment-service`).

```bash
# Enrollment request (replace 1 with an existing course id)
curl -X POST http://localhost:8083/enrollments \
  -H "Content-Type: application/json" \
  -d '{"learnerId": 101, "courseId": 1}'

# Circuit Breaker state
curl http://localhost:8083/actuator/health
```

| Step | Action | Expected log / response |
| --- | --- | --- |
| 1 | Course Service running, send a request | `Attempting Course Service call ...` once; `201 Created` |
| 2 | Request a non-existing course (for example `courseId: 999999`) | `Attempting ...` once (no retry); `404 Course Not Found` |
| 3 | `docker compose stop course-service`, send a request | `Attempting ...` 3 times, `Fallback executed ...`; `503`; no enrollment created |
| 4 | Send more requests | `State transition from CLOSED to OPEN`; health shows `OPEN` |
| 5 | Send a request while OPEN | No `Attempting` line; immediate `503` |
| 6 | `docker compose start course-service`, wait for the open-state duration, send requests | `State transition from OPEN to HALF_OPEN`, then `State transition from HALF_OPEN to CLOSED`; `201 Created`; health shows `CLOSED` |

---

## Constraints

* Resilience4j must be used for Retry and Circuit Breaker functionality.
* Retry must apply only to Course Service availability failures.
* A `404 Not Found` from Course Service must not trigger Retry.
* A `404 Not Found` from Course Service must not count as a Circuit Breaker failure.
* The Circuit Breaker must track the relevant downstream availability failures only.
* Enrollment creation must occur only after successful Course validation.
* An enrollment must not be created when Course validation cannot be completed.
* The fallback must return `503 Service Unavailable` for Course Service availability failures.
* The fallback must not fabricate Course data.
* Circuit Breaker state transitions must be logged.
* Circuit Breaker health must be exposed through Spring Boot Actuator.
* The Circuit Breaker configuration must support the `CLOSED → OPEN → HALF_OPEN → CLOSED` lifecycle (failure threshold, open-state wait duration and half-open trial calls).

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
│           │   ├── CourseClient.java
│           │   └── ...
│           └── resources/
│               └── application.properties
│
├── notification-service/
│   └── ...
│
├── resource-service/
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
                           ▼
                    Resilience4j
                  ┌────────┴────────┐
                  │                 │
                Retry        Circuit Breaker
                  │                 │
                  └────────┬────────┘
                           │
                       RestClient
                           │
                           ▼
                      Eureka
                           │
                           ▼
                    Course Service
```

When the Course Service is unavailable, the Enrollment Service retries the appropriate failures. If failures continue, the Circuit Breaker opens and subsequent requests fail without repeatedly calling the unavailable service.

Once the Course Service recovers, the Circuit Breaker moves through `HALF_OPEN` and, after a successful trial request, returns to `CLOSED`.

---

## Evaluation Criteria

1. Resilience4j `@Retry` and `@CircuitBreaker` are applied to the Course Service call and configured in `application.properties`.
2. A Course Service `404 Not Found` is handled separately and is not retried or counted as a Circuit Breaker failure.
3. The enrollment is saved only after the Course Service call succeeds.
4. When the Course Service is unavailable or the circuit is open, a `503 Service Unavailable` response with a meaningful message is returned without fabricating Course data.
5. Circuit Breaker state transitions are logged and Circuit Breaker health is exposed through Spring Boot Actuator.
