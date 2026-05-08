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
- **Prometheus**: [http://localhost:9090](http://localhost:9090) — targets and raw metrics (job `spring-boot` → `app:8080`)
- **Grafana**: [http://localhost:3000](http://localhost:3000) — login **`admin` / `admin`** (change after first login in dev only); dashboard **document-db-benchmark** is auto-loaded
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

## Generate sample data

`scripts/generate_demo_data.py` writes a JSON **array** of `Demo` objects (`id`, `name`, `value`) for bulk upload to `/insertData` or `/insertDataParallel`.

From the repository root (requires Python 3):

```bash
python3 scripts/generate_demo_data.py -n 50000 -o data/benchmark/demo-50k.json
```

Useful options:

- `--id-start 1` — first `id` (default `1`)
- `--seed 42` — reproducible random `value` fields
- `--name-prefix item` — names become `item-<id>`
- `--pretty` — indented JSON (larger files)

Example upload (app on localhost:8080):

```bash
curl -s -F "file=@data/benchmark/demo-50k.json" http://localhost:8080/insertDataParallel
```

Generated files under `data/benchmark/` are gitignored by default.

## Observability (graphs)

The default **`docker compose`** stack includes **Prometheus** and **Grafana** alongside the app and MongoDB.

| Service     | URL                     | Notes |
|------------|-------------------------|--------|
| Grafana    | http://localhost:3000   | User **`admin`**, password **`admin`**. Open **Dashboards → document-db-benchmark** for HTTP, JVM heap, CPU, and bulk-insert Micrometer panels. |
| Prometheus | http://localhost:9090 | **Status → Targets** should show `spring-boot` as **UP** once the app is healthy. **Graph** tab for ad-hoc PromQL. |
| Metrics    | http://localhost:8080/actuator/prometheus | Raw scrape endpoint (used by Prometheus). |

Configs live under **`observability/`**:

- `observability/prometheus/prometheus.yml` — scrapes `app:8080` inside Compose
- `observability/grafana/provisioning/` — Prometheus datasource + file-based dashboards
- `observability/grafana/dashboards/document-db-benchmark.json` — starter graphs (request rate by URI, heap, CPU, insert timer / failed counter)

After bringing the stack up, generate load (for example upload data with `curl` to `/insertDataParallel`) and use the time picker in Grafana (**Last 15 minutes**) to see changes.

### Prometheus only, app on the host

If you run **`bash mvnw spring-boot:run`** on the machine and want Prometheus in Docker with graphs against **localhost:8080**, use the alternate config (macOS/Windows Docker Desktop: `host.docker.internal`):

```bash
docker run --rm -p 9090:9090 \
  -v "$(pwd)/observability/prometheus/prometheus-host.yml:/etc/prometheus/prometheus.yml:ro" \
  prom/prometheus:v2.52.0
```

Then open Grafana separately or add Prometheus as a datasource in an existing Grafana instance pointing at `http://localhost:9090`.

### Optional Mongo host dashboard

A Percona-style MongoDB Grafana export is kept for manual import only: **`observability/grafana/dashboards/import-optional/`** (see the README there). It is separate from the bundled **document-db-benchmark** dashboard, which shows JVM and application metrics.
