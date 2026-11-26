# Practice: Persist Notifications Using MongoDB

## Problem Statement

Enhance the **Notification Service** by giving it its own database for storing notification records.

The Notification Service already consumes `EnrollmentConfirmed` events from RabbitMQ. In this practice, you will introduce **MongoDB** as the Notification Service's database and persist a notification record whenever an enrollment confirmation event is processed.

The Notification Service must independently manage its notification data using **Spring Data MongoDB**, following the **Database-per-Service** approach established in Sprint 1.

MongoDB, RabbitMQ, and the entire LearnSphere application suite must be dockerized and run using **Docker** and Docker Compose.

---

## Task-1: Configure MongoDB and Dockerize the LearnSphere Platform

### Task

Add MongoDB as a Docker service for the Notification Service and configure the complete application platform using Docker Compose.

### Requirements

* Add MongoDB as a Docker service in `docker-compose.yml`:
  * Use the official `mongo:8.0` Docker image.
  * Expose the MongoDB port (`27017`).
  * Configure the database name (`notificationdb`) and healthcheck.
* Configure the MongoDB connection for the Notification Service:
  * Add the required `spring-boot-starter-data-mongodb` dependency.
  * Configure the Notification Service to connect to its MongoDB database (`mongodb://mongodb:27017/notificationdb` inside Docker and `mongodb://localhost:27017/notificationdb` locally).
* Orchestrate all services in `docker-compose.yml` using the single multi-stage root `Dockerfile` alongside RabbitMQ and MongoDB.
* Ensure the MongoDB instance is independent of the databases used by other LearnSphere services.

---

## Task-2: Create the Notification Document

### Task

Design a MongoDB document to represent a notification record.

### Requirements

Create a notification document containing appropriate information, such as:

* `id`
* `eventId`
* `enrollmentId`
* `learnerId`
* `courseId`
* `message`

The document must:

* Be configured as a MongoDB document.
* Have a unique identifier.
* Store the `eventId` used to identify the processed `EnrollmentConfirmed` event.
* Store enough information to represent the enrollment confirmation notification.

The exact fields may vary, but the document must contain the information required to represent and retrieve a notification record.

---

## Task-3: Create the Notification Repository

### Task

Create a repository for persisting and retrieving notification documents.

### Requirements

* Create a `NotificationRepository` interface.
* Configure it as a Spring Data MongoDB repository.
* Use the notification document as the repository entity.
* Provide the standard operations required to save and retrieve notification records.

---

## Task-4: Store Notifications When Events Are Processed

### Task

Enhance the existing `EnrollmentConfirmed` event consumer to persist notification records.

### Requirements

When the Notification Service receives an `EnrollmentConfirmed` event:

* Consume the event from RabbitMQ.
* Create a notification document from the event information.
* Store the notification document in MongoDB.
* Ensure that the notification is persisted only after the event has been successfully processed.
* Use the `eventId` to maintain the idempotent behavior established in the previous sprint.

If the same event is received again, it must not create another notification record.

The duplicate check may keep the approach from the previous sprint, or check the stored notification records, for example with a repository method such as `existsByEventId(eventId)`.

The flow should be:

```text
Enrollment Service
        │
        │ EnrollmentConfirmed
        ▼
     RabbitMQ
        │
        ▼
Notification Service
        │
        ▼
 Create Notification
        │
        ▼
    MongoDB
```

---

## Task-5: Retrieve Notification Records

### Task

Provide a REST API to retrieve the notification records stored in MongoDB.

### Requirements

Create a REST controller that provides an API similar to:

| HTTP Method | Endpoint              | Purpose                           |
| ----------- | --------------------- | --------------------------------- |
| `GET`       | `/notifications`      | Retrieve all stored notifications |
| `GET`       | `/notifications/{id}` | Retrieve a notification by ID     |

The API must:

* Retrieve notification records from MongoDB.
* Return the notification information as JSON.
* Use the repository layer to access MongoDB.
* Not use an in-memory collection to store notification records.

---

## Task-6: Verify MongoDB Persistence

### Task

Verify that notifications generated from `EnrollmentConfirmed` events are persisted and can be retrieved.

### Requirements

Verify the following scenarios:

| Scenario                  | Action                                  | Expected Result                             |
| ------------------------- | --------------------------------------- | ------------------------------------------- |
| New enrollment            | Confirm an enrollment                   | `EnrollmentConfirmed` event is published    |
| Event processing          | Notification Service consumes the event | Notification record is stored in MongoDB    |
| Retrieve notifications    | `GET /notifications`                    | Stored notification records are returned    |
| Retrieve one notification | `GET /notifications/{id}`               | Requested notification is returned          |
| Duplicate event           | Process the same `eventId` again        | No duplicate notification record is created |

Verify that the notification data returned by the REST API matches the information from the processed enrollment event.

### Verification Checklist

Run the following from the project root and confirm each expected result.

**1. Build and start the platform**

```bash
docker compose up --build -d
docker compose ps
```

Expected: all services are `Up`, and `rabbitmq` and `mongodb` are `healthy`.

**2. Confirm an enrollment (publish)**

```bash
curl -X POST http://localhost:8080/enrollments \
  -H "Content-Type: application/json" \
  -d '{"learnerId": 101, "courseId": 201}'
```

Expected: `201 Created` with the saved enrollment (note its `id`).

**3. Check the notification is stored in MongoDB**

```bash
docker compose logs notification-service
docker compose exec mongodb mongosh notificationdb --eval "db.notifications.find()"
```

Expected: the log shows `Enrollment confirmed successfully for Learner 101 for Course 201.`, and one document exists in the `notifications` collection with the same `eventId`, `enrollmentId`, `learnerId` and `courseId`.

**4. Retrieve notifications through the REST API**

```bash
curl http://localhost:8080/notifications
curl http://localhost:8080/notifications/<id>
```

Expected: the list contains the stored notification, and the second call returns the notification with that `id`. The returned data matches the enrollment event.

**5. Duplicate event**

Publish the same event (same `eventId`) again, for example using the duplicate-delivery endpoint from the previous sprint:

```bash
curl -X POST http://localhost:8080/enrollments/1/duplicate-notification
curl http://localhost:8080/notifications
```

Expected: only **one** new notification record is stored for that `eventId`, and the log shows `Duplicate event ignored: <eventId>`.

**6. Stop the platform**

```bash
docker compose down
```

---

## Constraints

* The Notification Service must use **MongoDB** as its database.
* MongoDB (`mongo:8.0`), RabbitMQ, and the complete application suite must be configured and orchestrated using **Docker** and Docker Compose.
* MongoDB must be independently managed by the Notification Service.
* The Notification Service must use **Spring Data MongoDB** for persistence.
* Notification data must not be stored in an in-memory Java collection.
* A notification must be persisted when an `EnrollmentConfirmed` event is successfully processed.
* The existing RabbitMQ-based event communication must be retained.
* The Notification Service must maintain the idempotent behavior established in the previous sprint.
* Duplicate `EnrollmentConfirmed` events must not create duplicate notification records.
* Notification records must be retrievable through a REST API.
* Other LearnSphere services must not directly access the Notification Service's MongoDB database.

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
│   └── ...
│
├── notification-service/
│   ├── pom.xml
│   └── src/
│       └── main/
│           ├── java/
│           │   ├── NotificationServiceApplication.java
│           │   ├── Notification.java
│           │   ├── NotificationRepository.java
│           │   ├── NotificationService.java
│           │   ├── NotificationController.java
│           │   ├── NotificationConsumer.java
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
                           │ EnrollmentConfirmed
                           ▼
                      RabbitMQ
                           │
                           ▼
                  Notification Service
                           │
                           ▼
                       MongoDB
                           │
                           │
                           ▼
                    Notification API
```

The **Notification Service owns its notification data** and is the only service responsible for managing its MongoDB database.

---

## Running with Docker Compose

To build and launch the entire microservice ecosystem along with RabbitMQ and MongoDB (`mongo:8.0`):

1. **Build and start all services:**
   ```bash
   docker compose up --build -d
   ```

2. **Verify running containers:**
   ```bash
   docker compose ps
   ```

3. **Verify notifications via API Gateway:**
   ```bash
   curl http://localhost:8080/notifications
   ```

4. **Stop all services:**
   ```bash
   docker compose down
   ```

---

## Evaluation Criteria

1. MongoDB (`mongo:8.0`) and RabbitMQ are defined as Docker services with healthchecks in `docker-compose.yml`, and the complete LearnSphere platform is orchestrated with Docker Compose using the root multi-stage `Dockerfile`.
2. The Notification Service is configured to use Spring Data MongoDB for persistence (`spring-boot-starter-data-mongodb` dependency and MongoDB connection URI).
3. A Notification document is implemented with appropriate notification and event information, including the `eventId` of the processed `EnrollmentConfirmed` event.
4. A Spring Data MongoDB repository is created for notification persistence and retrieval.
5. The Notification Service persists `EnrollmentConfirmed` event notifications in MongoDB without using an in-memory collection for notification records.
6. REST APIs are provided to retrieve all notifications and an individual notification, returning notification records as JSON.
