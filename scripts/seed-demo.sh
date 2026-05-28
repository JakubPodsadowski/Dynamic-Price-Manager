#!/usr/bin/env bash
# Salon simulation: 10 services [sym], 5 employees, 10 client accounts, dense booking calendar.
# Spring profile: demo-seed. Requires a running database (see application.properties).
#
# From project root:
#   ./scripts/seed-demo.sh
#
# Does not open port 8080 — safe while the main app is already running.
# Stop (Ctrl+C) after log: Salon simulation seed complete
# Re-runs do not duplicate data (employee marker __SALON_SIM__).
#
# Demo clients: simulation.client01@local.test … client10, password: demo123

set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

echo "=== Salon simulation (demo-seed profile) ==="
echo "Main app may stay on http://localhost:8080 — seed uses no web port."
echo "Stop (Ctrl+C) after log: Salon simulation seed complete"
echo "Demo clients: simulation.client01@local.test … client10 — password: demo123"
echo ""

exec mvn -q spring-boot:run -Dspring-boot.run.profiles=demo-seed
