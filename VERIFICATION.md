# Sprint 4 Verification: Asynchronous Communication with RabbitMQ

This file records how the Enrollment → RabbitMQ → Notification flow was verified.
Each step lists the command, the expected result, and the observed result.

> **Before submitting:** run every step on the Docker Compose stack and replace each
> *Observed result* block with the real output (responses, log lines, queue counts).

Base URL through the API Gateway: `http://localhost:8080`
RabbitMQ management UI: `http://localhost:15672` (guest / guest)
Queue: `enrollment.notification.queue`

---

## 0. Build and Start the Stack

```bash
docker compose up --build -d
docker compose ps
```

**Expected:** `rabbitmq` is `running (healthy)`; `discovery-server`, `course-service`, `learner-service`,
`enrollment-service`, `notification-service` and `api-gateway` are `running`.
Eureka dashboard (`http://localhost:8761`) lists COURSE-SERVICE, LEARNER-SERVICE, ENROLLMENT-SERVICE,
NOTIFICATION-SERVICE and API-GATEWAY as UP.

**Observed result:**

```text
(paste docker compose ps output here)
```

Follow the Notification Service logs in a second terminal for the steps below:

```bash
docker compose logs -f notification-service
```

---

## 1. Enrollment Confirmed → Event Published

```bash
curl -i -X POST http://localhost:8080/enrollments \
  -H "Content-Type: application/json" \
  -d '{"learnerId": 101, "courseId": 201}'
```

**Expected:** `HTTP/1.1 201 Created` with the saved enrollment, for example:

```json
{ "id": 1, "learnerId": 101, "courseId": 201, "enrollmentDate": "<today>", "status": "CONFIRMED" }
```

**Observed result:**

```text
(paste response here)
```

---

## 2. Event Delivered and Notification Produced

**Expected Notification Service log lines:**

```text
EnrollmentConfirmed event received
Event ID: <uuid>
Enrollment ID: 1
Enrollment confirmed successfully for Learner 101 for Course 201.
```

**Expected RabbitMQ:** `enrollment.notification.queue` shows **Ready: 0** (message delivered and consumed).

**Observed result:**

```text
(paste log lines and Ready count here)
```

---

## 3. Duplicate Event → No Duplicate Notification

Publishes the same `EnrollmentConfirmed` event (same `eventId`) twice:

```bash
curl -i -X POST http://localhost:8080/enrollments/1/duplicate-notification
```

**Expected response:** `200 OK` — `Duplicate enrollment notification published for enrollment ID: 1`

**Expected Notification Service log lines** (first copy processed, second ignored):

```text
EnrollmentConfirmed event received
Event ID: <uuid-A>
Enrollment ID: 1
Enrollment confirmed successfully for Learner 101 for Course 201.
Duplicate event ignored: <uuid-A>
```

**Observed result:**

```text
(paste response and log lines here)
```

---

## 4. Notification Service Unavailable → Message Waits in RabbitMQ

```bash
docker compose stop notification-service

curl -i -X POST http://localhost:8080/enrollments \
  -H "Content-Type: application/json" \
  -d '{"learnerId": 102, "courseId": 202}'
```

**Expected:**
* The request still returns `201 Created` (Enrollment Service does not depend on Notification Service).
* RabbitMQ → Queues and Streams → `enrollment.notification.queue` shows **Ready: 1**.

**Observed result:**

```text
(paste response and Ready count here)
```

---

## 5. Notification Service Restored → Pending Event Consumed

```bash
docker compose start notification-service
docker compose logs -f notification-service
```

**Expected:**
* Log shows `Enrollment confirmed successfully for Learner 102 for Course 202.`
* `enrollment.notification.queue` goes from **Ready: 1** to **Ready: 0**.

**Observed result:**

```text
(paste log lines and Ready count here)
```

---

## 6. No Direct REST Call from Enrollment to Notification

* `enrollment-service` has no HTTP client for `notification-service`; its only outbound HTTP client is
  `CourseClient` (Sprint 3).
* The only link between the two services is the `enrollment.exchange` → `enrollment.confirmed` →
  `enrollment.notification.queue` binding in RabbitMQ.
* Step 4 shows enrollments succeed while Notification Service is stopped.

---

## Automated Tests

```bash
mvn clean package
```

| Test | Covers |
| ---- | ------ |
| `EnrollmentServiceApplicationTests.testConfirmEnrollmentPublishesEvent` | Enrollment saved; `EnrollmentConfirmed` published with unique `eventId`, enrollment, learner and course IDs |
| `EnrollmentServiceApplicationTests.testSendDuplicateNotification` | Same event published twice for duplicate testing |
| `NotificationServiceApplicationTests.testNewEventIsProcessed` | New event produces a notification |
| `NotificationServiceApplicationTests.testDuplicateEventIsIgnored` | Same `eventId` delivered again is ignored |
| `NotificationServiceApplicationTests.testConsumerDelegatesToService` | `@RabbitListener` consumer passes events to `NotificationService` |
