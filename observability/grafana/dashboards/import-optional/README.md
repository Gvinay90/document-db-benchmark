# Optional Grafana imports

`mongodb-pmm-percona.json` is a **Percona Monitoring and Management** MongoDB dashboard (Grafana.com id [7353](https://grafana.com/grafana/dashboards/7353)). It targets PMM Prometheus exporters, not this app’s `/actuator/prometheus` metrics.

To try it: Grafana → **Dashboards → Import** → upload the JSON, then point panels at a datasource that scrapes **MongoDB host** metrics (e.g. `mongodb_exporter`). Expect to adjust datasource UIDs and queries.
