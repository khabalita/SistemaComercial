#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
JAR="$ROOT_DIR/target/sistemacomercial-0.0.1-SNAPSHOT.jar"
PACKAGE_FILES="$ROOT_DIR/installer/linux/package"
BUILD_DIR="$ROOT_DIR/target/linux-deb"
STAGE_DIR="$BUILD_DIR/sistemacomercial"
DIST_DIR="$ROOT_DIR/target/distributions"

if [[ ! -f "$JAR" ]]; then
    echo "No se encontró el JAR. Ejecutá ./mvnw clean package primero." >&2
    exit 1
fi

if [[ -z "${JAVA_HOME:-}" || ! -x "$JAVA_HOME/bin/jlink" ]]; then
    echo "JAVA_HOME debe apuntar a un JDK 21 para generar el runtime Linux." >&2
    exit 1
fi

APP_VERSION="$(python3 - "$ROOT_DIR/pom.xml" <<'PY'
import sys
import xml.etree.ElementTree as ET

namespace = "{http://maven.apache.org/POM/4.0.0}"
root = ET.parse(sys.argv[1]).getroot()
print(root.findtext(f"{namespace}version"))
PY
)"
DEB_VERSION="${APP_VERSION/-SNAPSHOT/~snapshot}"

rm -rf "$STAGE_DIR"
mkdir -p \
    "$STAGE_DIR/DEBIAN" \
    "$STAGE_DIR/opt/sistemacomercial/app" \
    "$STAGE_DIR/usr/lib/systemd/system" \
    "$STAGE_DIR/usr/bin" \
    "$STAGE_DIR/usr/local/sbin" \
    "$STAGE_DIR/usr/share/applications" \
    "$DIST_DIR"

sed "s/@VERSION@/$DEB_VERSION/g" "$PACKAGE_FILES/DEBIAN/control.in" \
    > "$STAGE_DIR/DEBIAN/control"
cp "$PACKAGE_FILES/DEBIAN/config" "$STAGE_DIR/DEBIAN/config"
cp "$PACKAGE_FILES/DEBIAN/templates" "$STAGE_DIR/DEBIAN/templates"
cp "$PACKAGE_FILES/DEBIAN/postinst" "$STAGE_DIR/DEBIAN/postinst"
cp "$PACKAGE_FILES/DEBIAN/prerm" "$STAGE_DIR/DEBIAN/prerm"
cp "$PACKAGE_FILES/DEBIAN/postrm" "$STAGE_DIR/DEBIAN/postrm"
chmod 0755 "$STAGE_DIR/DEBIAN/config" "$STAGE_DIR/DEBIAN/postinst" \
    "$STAGE_DIR/DEBIAN/prerm" "$STAGE_DIR/DEBIAN/postrm"

install -m 0644 "$JAR" "$STAGE_DIR/opt/sistemacomercial/app/sistemacomercial.jar"
"$JAVA_HOME/bin/jlink" \
    --module-path "$JAVA_HOME/jmods" \
    --add-modules java.base,java.compiler,java.datatransfer,java.desktop,java.instrument,java.management,java.naming,java.net.http,java.prefs,java.rmi,java.scripting,java.security.jgss,java.sql,java.transaction.xa,java.xml,jdk.crypto.ec,jdk.unsupported,jdk.zipfs \
    --strip-debug --no-man-pages --no-header-files --compress=2 \
    --output "$STAGE_DIR/opt/sistemacomercial/java"

install -m 0644 "$PACKAGE_FILES/usr/lib/systemd/system/sistemacomercial.service" \
    "$STAGE_DIR/usr/lib/systemd/system/sistemacomercial.service"
install -m 0644 "$PACKAGE_FILES/usr/lib/systemd/system/sistemacomercial-backup.service" \
    "$STAGE_DIR/usr/lib/systemd/system/sistemacomercial-backup.service"
install -m 0644 "$PACKAGE_FILES/usr/lib/systemd/system/sistemacomercial-backup.timer" \
    "$STAGE_DIR/usr/lib/systemd/system/sistemacomercial-backup.timer"
install -m 0755 "$PACKAGE_FILES/usr/local/sbin/sistemacomercial-backup" \
    "$STAGE_DIR/usr/local/sbin/sistemacomercial-backup"
install -m 0755 "$PACKAGE_FILES/usr/bin/sistemacomercial" \
    "$STAGE_DIR/usr/bin/sistemacomercial"
install -m 0644 "$PACKAGE_FILES/usr/share/applications/sistemacomercial.desktop" \
    "$STAGE_DIR/usr/share/applications/sistemacomercial.desktop"

dpkg-deb --build --root-owner-group "$STAGE_DIR" \
    "$DIST_DIR/sistemacomercial_${DEB_VERSION}_amd64.deb"
echo "Paquete generado: $DIST_DIR/sistemacomercial_${DEB_VERSION}_amd64.deb"
