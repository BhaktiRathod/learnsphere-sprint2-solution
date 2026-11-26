# Practice: Implement Asynchronous Service Communication

## Problem Statement

Enhance the **LearnSphere** microservices architecture by introducing **asynchronous, event-driven communication** between the **Enrollment Service** and the **Notification Service**.

When an enrollment is confirmed, the Enrollment Service must publish an `EnrollmentConfirmed` event to **RabbitMQ**. The Notification Service must consume this event and process it by sending a simple enrollment confirmation notification.

The Enrollment Service must not directly call the Notification Service. RabbitMQ must act as the communication channel between the two services.

The Notification Service must also handle duplicate events safely so that processing the same event more than once does not result in duplicate notifications.

The entire LearnSphere application along with RabbitMQ must be dockerized and run using **Docker**.

---

## Task-1: Dockerize the LearnSphere Application and Configure RabbitMQ

### Task

Dockerize the complete LearnSphere microservices platform and configure RabbitMQ as the messaging broker using Docker Compose.

### Requirements

* Create a single root `Dockerfile` to build all Maven modules and provide dedicated target stages for each service. Use Java 25 images that match the project toolchain (Java 25, Spring Boot 4.1.1, Spring Cloud 2025.1.x), for example `maven:3.9.16-eclipse-temurin-25` for the build stage and `eclipse-temurin:25-jre` for the service stages:
  * `discovery-server`
  * `api-gateway`
  * `course-service`
  * `learner-service`
  * `enrollment-service`
  * `notification-service`
* Create a `docker-compose.yml` to orchestrate all LearnSphere microservices and RabbitMQ.
* Configure RabbitMQ as a Docker service:
  * Use the official `rabbitmq:4-management` image with the management interface.
  * Expose the RabbitMQ messaging port (`5672`) and management dashboard port (`15672`).
  * Configure default credentials (`guest`/`guest`) and container healthcheck.
* Configure environment variables and service dependencies:
  * Set `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://discovery-server:8761/eureka/` for all services.
  * Configure Enrollment and Notification services to connect to RabbitMQ using host `rabbitmq` and port `5672`.
  * Configure `depends_on` rules so dependent services wait for `discovery-server` and `rabbitmq`.
* Verify that all services and RabbitMQ build and start successfully using Docker Compose.

---

## Task-2: Create and Publish the EnrollmentConfirmed Event

### Task

Create an event that represents a successfully confirmed enrollment and publish it from the Enrollment Service.

### Requirements

Create an `EnrollmentConfirmed` event containing appropriate information, such as:

* `eventId`
* `enrollmentId`
* `learnerId`
* `courseId`

The Enrollment Service must:

* Create the event when an enrollment is confirmed.
* Publish the event to RabbitMQ.
* Use an appropriate exchange and routing key.
* Serialize the event into a message format suitable for transmission.
* Not directly call the Notification Service.

The communication must follow:

```text
Enrollment Service
        │
        │ Publish EnrollmentConfirmed
        ▼
     RabbitMQ
```

---

## Task-3: Consume the Event in Notification Service

### Task

Configure the Notification Service to consume `EnrollmentConfirmed` events from RabbitMQ.

### Requirements

The Notification Service must:

* Connect to RabbitMQ.
* Configure the required exchange, queue, and binding.
* Listen for `EnrollmentConfirmed` events.
* Deserialize the received event.
* Process the event after receiving it.

The communication must follow:

```text
     RabbitMQ
        │
        │ EnrollmentConfirmed
        ▼
Notification Service
```

The Notification Service must not require a direct REST call from the Enrollment Service.

---

## Task-4: Process the Enrollment Confirmation

### Task

Process the `EnrollmentConfirmed` event and send a simple enrollment confirmation notification.

### Requirements

When an `EnrollmentConfirmed` event is received, the Notification Service must:

* Extract the relevant enrollment information from the event.
* Create a simple confirmation notification.
* Process or record the notification.
* Ensure that the notification is produced as a result of consuming the event.

For example:

```text
Enrollment confirmed successfully for Learner 101
for Course 201.
```

The notification may be represented as a log message or stored using a simple implementation appropriate for the exercise.

---

## Task-5: Make the Notification Consumer Idempotent

### Task

Ensure that processing the same `EnrollmentConfirmed` event multiple times does not create duplicate notifications.

### Requirements

The `EnrollmentConfirmed` event must contain a unique `eventId`.

The Notification Service must:

* Use the `eventId` to identify an already processed event.
* Check whether the event has already been processed before creating a notification.
* Process a new event only once.
* Ignore a duplicate event without creating another notification.

For example:

```text
Event received
     │
     ▼
Is eventId already processed?
     │
   ┌─┴─┐
  No  Yes
  │    │
  ▼    ▼
Process Ignore
  │
  ▼
Record eventId
```

* Check and record the `eventId` in one step, for example by using the boolean result of `Set.add(eventId)`: it returns `false` when the `eventId` was already processed.
* An in-memory collection of processed `eventId`s is sufficient for this practice.

> **Scope:** Persisting processed event IDs across service restarts (for example, in a database) and dead-letter queues are **out of scope** for this practice.

---

## Task-6: Verify Asynchronous Communication

### Task

Verify the complete asynchronous communication flow between the Enrollment and Notification services.

### Requirements

Start the following components:

```text
RabbitMQ
Enrollment Service
Notification Service
```

Verify the following scenarios:

| Scenario                         | Action                                         | Expected Result                                             |
| -------------------------------- | ---------------------------------------------- | ----------------------------------------------------------- |
| Enrollment confirmed             | Confirm an enrollment                          | `EnrollmentConfirmed` event is published                    |
| Event available                  | Check RabbitMQ                                 | Message is delivered to the Notification Service            |
| Notification processing          | Notification Service consumes the event        | Enrollment confirmation notification is produced            |
| Duplicate event                  | Deliver the same `eventId` again               | No duplicate notification is produced                       |
| Notification Service unavailable | Publish an event while consumer is unavailable | Message remains available in RabbitMQ for later consumption |
| Notification Service restored    | Start the Notification Service again           | Pending event is consumed and processed                     |

Verify that the Enrollment Service does not make a direct REST call to the Notification Service.

### Verification Checklist

Run the following from the project root and confirm each expected result.

**1. Build and start the platform**

```bash
docker compose build
docker compose up -d
docker compose ps
```

Expected: all services are `Up` and `rabbitmq` is `healthy`. Open `http://localhost:8761` to confirm the services are registered with Eureka.

**2. Confirm an enrollment (publish)**

```bash
curl -X POST http://localhost:8080/enrollments \
  -H "Content-Type: application/json" \
  -d '{"learnerId": 101, "courseId": 201}'
```

Expected: `201 Created` with the saved enrollment (note its `id`).

**3. Check consumption**

```bash
docker compose logs notification-service
```

Expected log line:

```text
Enrollment confirmed successfully for Learner 101 for Course 201.
```

In the RabbitMQ Management UI (`http://localhost:15672`, `guest`/`guest`) → **Queues and Streams** → the notification queue shows `Ready: 0`.

**4. Duplicate event**

Publish the same event (same `eventId`) twice, for example using a duplicate-delivery endpoint:

```bash
curl -X POST http://localhost:8080/enrollments/1/duplicate-notification
docker compose logs notification-service
```

Expected: one confirmation message **and** one `Duplicate event ignored: <eventId>` message for the same `eventId`.

**5. Notification Service unavailable (message stays in RabbitMQ)**

```bash
docker compose stop notification-service
curl -X POST http://localhost:8080/enrollments \
  -H "Content-Type: application/json" \
  -d '{"learnerId": 102, "courseId": 202}'
```

Expected: the enrollment is still created, and the notification queue shows `Ready: 1`.

**6. Notification Service restored (pending event consumed)**

```bash
docker compose start notification-service
docker compose logs -f notification-service
```

Expected: the pending event is processed (`Enrollment confirmed successfully for Learner 102 for Course 202.`) and the queue returns to `Ready: 0`.

**7. Stop the platform**

```bash
docker compose down
```

---

## Constraints

* RabbitMQ must be used as the message broker.
* RabbitMQ must be configured and run using Docker, with a root multi-stage `Dockerfile` and `docker-compose.yml` configured to dockerize the complete application.
* The Enrollment Service must publish an `EnrollmentConfirmed` event.
* The Notification Service must consume the `EnrollmentConfirmed` event.
* The Enrollment Service must not directly call the Notification Service.
* Communication between Enrollment and Notification must occur through RabbitMQ.
* The event must contain a unique `eventId`.
* The Notification consumer must be idempotent.
* Duplicate events must not result in duplicate notifications.
* Processed event IDs may be tracked in memory; durable storage of processed event IDs across restarts is out of scope.
* The asynchronous communication must be independently verifiable.
* The Enrollment and Notification services must remain independently bootable.

---

## Project Structure

```text
learnsphere/
├── pom.xml
│
├── Dockerfile
├── docker-compose.yml
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
│           │   └── EnrollmentConfirmed.java
│           └── resources/
│               └── application.properties
│
├── notification-service/
│   ├── pom.xml
│   └── src/
│       └── main/
│           ├── java/
│           │   ├── NotificationServiceApplication.java
│           │   ├── NotificationConsumer.java
│           │   ├── NotificationService.java
│           │   └── EnrollmentConfirmed.java
│           └── resources/
│               └── application.properties
│
└── README.md
```

---

## Architecture

```text
                    Enrollment Service
                           │
                           │ Publish Event
                           ▼
                    ┌─────────────┐
                    │  RabbitMQ   │
                    └──────┬──────┘
                           │
                           │ Consume Event
                           ▼
                  Notification Service
                           │
                           ▼
                  Confirmation Notification
```

The Enrollment Service publishes an `EnrollmentConfirmed` event without knowing about the Notification Service. RabbitMQ delivers the event to the Notification Service, which consumes and processes it independently.

---

## Evaluation Criteria

1. A root multi-stage `Dockerfile` with a target stage for each service and a `docker-compose.yml` defining all LearnSphere services and RabbitMQ (`rabbitmq:4-management` with healthcheck, Eureka and RabbitMQ environment variables, and `depends_on` rules) are provided.
2. An `EnrollmentConfirmed` event is created with a unique `eventId` and relevant enrollment information.
3. Enrollment Service publishes the `EnrollmentConfirmed` event to RabbitMQ when an enrollment is confirmed.
4. Notification Service is configured to consume `EnrollmentConfirmed` events from RabbitMQ.
5. Enrollment Service does not directly call Notification Service; RabbitMQ is used as the communication channel between the two services.
6. Notification Service uses the event ID to prevent duplicate processing of the same event (in-memory tracking of processed event IDs is sufficient; durability across restarts is out of scope).
