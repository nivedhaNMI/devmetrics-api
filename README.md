# DevMetrics API

A multi-tenant SaaS REST API for tracking engineering delivery metrics per team. Built with Spring Boot 3 and PostgreSQL, with full tenant isolation and a GitHub Actions CI/CD pipeline.

## What it does

- **Multi-tenant** — each team sends an `X-Tenant-ID` header; data is completely isolated per tenant at the query level
- **Metric tracking** — record and query deploy frequency, incident count, lead time, and change failure rate
- **Date range filtering** — query metrics between any two dates
- **Averages** — get the average value for any metric type per tenant
- **OpenAPI docs** — interactive Swagger UI included
- **Monitoring** — Spring Boot Actuator + Micrometer expose metrics at `/actuator/prometheus`; Prometheus scrapes them and Grafana shows a ready-made dashboard
- **Database migrations** — the schema and indexes are managed by Flyway
- **CI** — GitHub Actions pipeline runs the tests, builds the JAR and builds the Docker image on every push and pull request to `main`

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Framework | Spring Boot 3.2 |
| Database | PostgreSQL 16 |
| Multi-tenancy | Shared schema: `X-Tenant-ID` header, ThreadLocal tenant context, tenant-scoped queries |
| Monitoring | Spring Boot Actuator, Micrometer, Prometheus, Grafana |
| Migrations | Flyway |
| API Docs | SpringDoc OpenAPI |
| Testing | JUnit 5, Mockito |
| CI | GitHub Actions |
| Runtime | Java 21 |
| Containers | Docker, Docker Compose |

## Run locally (one command)

```bash
docker-compose up --build
```

| Service | URL |
|---------|-----|
| API | `http://localhost:8082` |
| Swagger UI | `http://localhost:8082/swagger-ui.html` |
| Prometheus | `http://localhost:9090` |
| Grafana | `http://localhost:3000` (login `admin` / `admin`, dashboard "DevMetrics API") |

## How multi-tenancy works

Every request must include the `X-Tenant-ID` header. This identifies which team the data belongs to. All database queries are automatically scoped to that tenant — no team can ever read another team's data.

```bash
# Team Alpha records a metric
curl -X POST http://localhost:8082/api/metrics \
  -H "Content-Type: application/json" \
  -H "X-Tenant-ID: team-alpha" \
  -d '{
    "teamName": "Backend Team",
    "type": "DEPLOY_FREQUENCY",
    "value": 8,
    "recordedDate": "2025-03-01"
  }'

# Team Alpha retrieves only their own metrics
curl http://localhost:8082/api/metrics \
  -H "X-Tenant-ID: team-alpha"

# Team Beta cannot see Team Alpha's data
curl http://localhost:8082/api/metrics \
  -H "X-Tenant-ID: team-beta"
```

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/metrics` | Record a new metric |
| GET | `/api/metrics` | Get all metrics for tenant |
| GET | `/api/metrics/type/{type}` | Filter by metric type |
| GET | `/api/metrics/range?from=&to=` | Filter by date range |
| GET | `/api/metrics/average/{type}` | Get average for a metric type |

## Metric Types

| Type | Description |
|------|-------------|
| `DEPLOY_FREQUENCY` | How many times the team deployed per period |
| `INCIDENT_COUNT` | Number of incidents reported |
| `LEAD_TIME_DAYS` | Days from commit to production |
| `CHANGE_FAILURE_RATE` | Percentage of deployments causing failures |

## Monitoring

- `/actuator/health` — is the app up?
- `/actuator/prometheus` — all metrics in Prometheus format (HTTP requests, response times, JVM memory, database connections)
- Custom business metric `devmetrics_metrics_recorded_total`, counted per tenant and metric type

The Grafana dashboard (provisioned automatically from `monitoring/grafana/`) shows requests per second, average response time, metrics recorded per tenant and JVM heap usage. Actuator endpoints don't need the `X-Tenant-ID` header.

## Tests

```bash
mvn test
```

Unit tests with JUnit 5 and Mockito: tenant assignment, tenant-scoped queries, the monitoring counter, and the tenant filter (missing or invalid header, ThreadLocal cleared after each request, monitoring endpoints without header).

## CI Pipeline

The GitHub Actions pipeline (`.github/workflows/ci.yml`) runs on every push and pull request to `main`:
1. Starts a PostgreSQL service container
2. Runs all unit tests
3. Builds the JAR
4. Builds the Docker image

## Project Structure

```
src/main/java/com/nivedha/devmetrics/
├── controller/     # REST endpoints
├── model/          # Metric entity
├── repository/     # Tenant-scoped database queries
├── service/        # Business logic
├── dto/            # Request objects (what clients may send)
└── tenant/         # TenantContext + TenantFilter
src/main/resources/db/migration/  # Flyway migrations
monitoring/         # Prometheus config + Grafana datasource and dashboard
.github/workflows/  # GitHub Actions CI
```
