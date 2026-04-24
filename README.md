# DevMetrics API

A multi-tenant SaaS REST API for tracking engineering delivery metrics per team. Built with Spring Boot 3 and PostgreSQL, with full tenant isolation and a GitHub Actions CI/CD pipeline.

## What it does

- **Multi-tenant** — each team sends an `X-Tenant-ID` header; data is completely isolated per tenant at the query level
- **Metric tracking** — record and query deploy frequency, incident count, lead time, and change failure rate
- **Date range filtering** — query metrics between any two dates
- **Averages** — get the average value for any metric type per tenant
- **OpenAPI docs** — interactive Swagger UI included
- **CI/CD** — GitHub Actions pipeline runs tests, builds JAR, and builds Docker image on every push

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Framework | Spring Boot 3.2 |
| Database | PostgreSQL 16 |
| Multi-tenancy | Tenant isolation via `X-Tenant-ID` header + query-level filtering |
| API Docs | SpringDoc OpenAPI |
| Testing | JUnit 5, Mockito |
| CI/CD | GitHub Actions |
| Runtime | Java 21 |
| Containers | Docker, Docker Compose |

## Run locally (one command)

```bash
docker-compose up --build
```

App runs on `http://localhost:8082`
Swagger UI: `http://localhost:8082/swagger-ui.html`

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

## CI/CD Pipeline

The GitHub Actions pipeline (`.github/workflows/ci.yml`) runs on every push to `main`:
1. Spins up a PostgreSQL test container
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
└── tenant/         # TenantContext + TenantFilter
.github/workflows/  # GitHub Actions CI/CD
```
