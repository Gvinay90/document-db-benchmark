# document-db-benchmark

Spring Boot application for experimenting with document database performance (MongoDB), bulk JSON ingest, and Prometheus metrics via Spring Boot Actuator.

Academic write-up: **`document-db-benchmark.pdf`** (performance evaluation of document databases).

## Prerequisites

- **Java** & **Maven**: Only needed for local development without Docker (see below).
- **Docker** (optional): Recommended one-command setup via `docker compose` (builds the app image and starts MongoDB).

Local (non-Docker) runs expect **MongoDB** on **localhost:27017**, database `DemoData` (see `src/main/resources/application.properties`).

## Run with Docker (recommended)

From the repository root:

```bash
docker compose up --build
```

- **API & Actuator**: [http://localhost:8080](http://localhost:8080) — e.g. `curl -s http://localhost:8080/actuator/health`
- **MongoDB** runs only on the Compose network (not published to the host by default) so it does not conflict with an existing local MongoDB on port 27017. To expose Mongo on the host, add under the `mongodb` service in `docker-compose.yml`: `ports: ["27017:27017"]`.

Stop and remove containers:

```bash
docker compose down
```

To remove the named volume (Mongo data) as well:

```bash
docker compose down -v
```

Build the application image only:

```bash
docker build -t document-db-benchmark:local .
```

## Start MongoDB (local dev without Compose)

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

### Troubleshooting (local `spring-boot:run`)

**`Port 8080 was already in use`**  
Something else is bound to 8080—often the same app running under **`docker compose`**. Stop it: `docker compose down`. Or see what is using the port: `lsof -nP -iTCP:8080 -sTCP:LISTEN`. To run locally on another port: `SERVER_PORT=8081 bash mvnw spring-boot:run`.

**`Connection refused` to `localhost:27017` (MongoDB)**  
The JVM is trying to reach Mongo on your **host**, but nothing is listening. Start Mongo (see [Start MongoDB](#start-mongodb-local-dev-without-compose) above). If you only started **`docker compose`**, Mongo is **not** published to the host unless you added `ports: ["27017:27017"]` under `mongodb`, so `spring-boot:run` on the host still cannot see it—either map that port or run the app **inside** Compose instead of locally.

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
