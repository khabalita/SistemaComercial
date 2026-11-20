#!/usr/bin/env bash
set -euo pipefail

DEB="${1:?Uso: smoke-test.sh target/distributions/sistemacomercial_*.deb}"
PASSWORD="SmokeTest-Admin-123"

echo "sistemacomercial sistemacomercial/admin-password password ${PASSWORD}" \
  | sudo debconf-set-selections
sudo DEBIAN_FRONTEND=noninteractive apt-get install -y "$DEB"

sudo systemctl is-active --quiet mysql
sudo systemctl is-active --quiet sistemacomercial
curl --fail --silent --show-error http://127.0.0.1:8081/login >/dev/null
test -f /etc/sistemacomercial/application.properties
test -f /usr/share/icons/hicolor/scalable/apps/sistemacomercial.svg

echo "Smoke test Linux OK."
