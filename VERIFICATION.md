# Sprint 6 Verification: Application-Level Caching

Run each step against the Resource Service (`http://localhost:8085`) and replace each
*Observed result* block with the real output (responses and log lines).

| Step | Request | Expected log |
| --- | --- | --- |
| 1. Create resources | `POST /api/resources` (x2) | none |
| 2. First GET | `GET /api/resources/<id1>` | `Fetching resource <id1> from MongoDB` |
| 3. Repeated GET | `GET /api/resources/<id1>` | no MongoDB-read log (cache hit) |
| 4. Different resource | `GET /api/resources/<id2>` (x2) | MongoDB-read log on the first call only |
| 5. Update | `PUT /api/resources/<id1>` | `Updating resource <id1> in MongoDB and evicting its cache entry` |
| 6. GET after update | `GET /api/resources/<id1>` | `Fetching resource <id1> from MongoDB` (updated data) |
| 7. TTL expiry (60 s) | wait > 60 s, `GET /api/resources/<id1>` | `Fetching resource <id1> from MongoDB` |

**Observed result:**

```text
(paste responses and Resource Service log lines here)
```
