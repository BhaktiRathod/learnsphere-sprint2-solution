# Sprint 7 Verification: Resilience4j Retry and Circuit Breaker

Run each step on the Docker Compose stack and replace each *Observed result* block with the real output
(Enrollment Service log lines from `docker compose logs -f enrollment-service` and responses).

| Step | Action | Expected |
| --- | --- | --- |
| 1. Normal operation | `POST /enrollments` with an existing course | `Attempting Course Service call ...` once; `201 Created` |
| 2. Course not found | `POST /enrollments` with `courseId: 999999` | one attempt (no retry); `404 Course Not Found` |
| 3. Course unavailable | `docker compose stop course-service`, `POST /enrollments` | 3 attempts, `Fallback executed ...`; `503`; no enrollment created |
| 4. Circuit opens | more requests | `State transition from CLOSED to OPEN`; `/actuator/health` shows `OPEN` |
| 5. Circuit open | another request | no `Attempting` line; immediate `503` |
| 6. Half-open and recovery | `docker compose start course-service`, wait 10 s, send requests | `OPEN to HALF_OPEN`, then `HALF_OPEN to CLOSED`; `201 Created`; health shows `CLOSED` |

**Observed result:**

```text
(paste log lines, responses and /actuator/health output here)
```
