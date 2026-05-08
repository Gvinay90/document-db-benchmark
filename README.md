# document-db-benchmark

Spring Boot application for experimenting with document database performance (MongoDB), bulk JSON ingest, and Prometheus metrics via Spring Boot Actuator.

Academic write-up: **`document-db-benchmark.pdf`** (performance evaluation of document databases).

## Prerequisites

- **Java**: JDK 8 or newer (the project targets Java 8; it also runs on newer LTS versions such as 17).
- **MongoDB**: Running locally on **host** `localhost` and **port** `27017`, database name `DemoData` (see `src/main/resources/application.properties`).
- **Maven**: Not required if you use the included wrapper (`mvnw`).

## Start MongoDB

If you do not already have MongoDB listening on `localhost:27017`, you can run it with Docker:

```bash
docker run -d --name mongodb-perftest -p 27017:27017 mongo:6
```

Stop/remove when finished:

```bash
docker stop mongodb-perftest
docker rm mongodb-perftest
```

## Run the application

From the repository root:

```bash
bash mvnw spring-boot:run
```

If the wrapper script is executable on your machine, `./mvnw spring-boot:run` works the same.

The app listens on **port 8080** by default.

### Quick checks

- Health (includes MongoDB status): `curl -s http://localhost:8080/actuator/health`
- List stored documents: `curl -s http://localhost:8080/`

With MongoDB up, health should report `"status":"UP"` and `"mongo":{"status":"UP",...}`.

## Build and tests

```bash
bash mvnw compile
bash mvnw test
```

Tests load the full Spring context; you may see MongoDB connection errors in logs if nothing is listening on `27017`, but the included smoke test can still pass depending on your environment.

## Configuration

Main settings are in `src/main/resources/application.properties`:

- `spring.data.mongodb.host`, `spring.data.mongodb.port`, `spring.data.mongodb.database`
- Actuator endpoints are exposed broadly for metrics (`management.endpoints.web.exposure.include=*`)
- Prometheus scrape path: **`/actuator/prometheus`** (port 8080)

## HTTP API (summary)

| Method | Path | Purpose |
|--------|------|---------|
| `GET` | `/` | List all `Demo` documents |
| `GET` | `/{id}` | Get document by id |
| `POST` | `/insert` | Insert JSON body as a `Demo` entity |
| `POST` | `/insertData` | Upload JSON file (multipart) for bulk insert |
| `POST` | `/insertDataParallel` | Same as above using parallel insert path |

## Optional: Prometheus and Grafana

Under `ConfFiles/` there is a sample `prometheus.yml` (scrapes `actuator/prometheus`) and a `compose.yml` intended for Prometheus/Grafana. Those files may reference paths on the author’s machine; adjust volume mounts and scrape targets (for example `host.docker.internal:8080` on macOS) before using them.
