# Sprint 5 Verification: Persist Notifications Using MongoDB

This file records the evidence that notifications are persisted in MongoDB and retrievable.
Run each step on the Docker Compose stack and replace each *Observed result* block with the real output.

Base URL through the API Gateway: `http://localhost:8080`

---

## 1. Build and Start the Stack

```bash
docker compose up --build -d
docker compose ps
```

**Expected:** all services `running`; `rabbitmq` and `mongodb` `healthy`.

**Observed result:**

```text
(paste docker compose ps output here)
```

## 2. Confirm an Enrollment

```bash
curl -i -X POST http://localhost:8080/enrollments -H "Content-Type: application/json" -d '{"learnerId": 101, "courseId": 201}'
```

**Expected:** `201 Created` with the saved enrollment.

**Observed result:**

```text
(paste response here)
```

## 3. Notification Stored in MongoDB

```bash
docker compose logs notification-service
docker compose exec mongodb mongosh
# then: use notificationdb
#       db.notifications.find()
```

**Expected:** log line `Enrollment confirmed successfully for Learner 101 for Course 201.`; one document with the same `eventId`, `enrollmentId`, `learnerId`, `courseId`.

**Observed result:**

```text
(paste log lines and mongosh output here)
```

## 4. Retrieve Notifications

```bash
curl http://localhost:8084/notifications
```

**Expected:** JSON list containing the notification; data matches the event.

**Observed result:**

```text
(paste responses here)
```

## 5. Duplicate Event

```bash
curl -X POST http://localhost:8080/enrollments/1/duplicate-notification
curl http://localhost:8084/notifications
docker compose logs notification-service
```

**Expected:** one new record for that `eventId` only; log shows `Duplicate event ignored: <eventId>`.

**Observed result:**

```text
(paste responses and log lines here)
```
