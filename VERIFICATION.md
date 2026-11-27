# Sprint 8 Verification: Distributed Logging and Tracing

Send `POST http://localhost:8080/enrollments` through the API Gateway (with and without an
`X-Correlation-ID` header), then replace the *Observed result* block with the real log lines.

| Check | Expected |
| --- | --- |
| Enrollment Service log `Enrollment request received` | `correlationId=<id>` `traceId=<T>` `spanId=<S1>` |
| Enrollment Service log `Calling Course Service` | same `correlationId` and `traceId=<T>` |
| Course Service log `Course request processed` | same `correlationId` and `traceId=<T>`, different `spanId` |
| Request without the header | the API Gateway generates a correlation ID that appears in both services |
| `GET http://localhost:8083/actuator/metrics` | lists `http.server.requests` and `http.client.requests` |

**Observed result:**

```text
(paste Enrollment Service and Course Service log lines and the metrics output here)
```
