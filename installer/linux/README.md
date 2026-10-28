# Instalador Linux (Ubuntu y Linux Mint)

El paquete `.deb` está preparado para equipos **amd64** basados en Ubuntu, incluidos Linux Mint. Incluye un runtime Java 21 y configura MySQL, la aplicación como servicio `systemd` y respaldos diarios.

## Instalación

Descargá el artefacto `sistemacomercial_<versión>_amd64.deb` de GitHub Actions y ejecutá:

```bash
sudo apt install ./sistemacomercial_<versión>_amd64.deb
```

Durante la instalación se solicita una contraseña inicial para `admin` (mínimo 8 caracteres). El instalador crea la base `sistemacomercial` y su usuario MySQL dedicado. Al finalizar, abrí `http://localhost:8081/login` o la entrada **Sistema Comercial** del menú de aplicaciones.

## Servicio y registros

La aplicación inicia automáticamente con el sistema:

```bash
systemctl status sistemacomercial
journalctl -u sistemacomercial -f
```

Configuración: `/etc/sistemacomercial/application.properties`  
Datos e imágenes: `/var/lib/sistemacomercial/`  
Respaldos diarios de base e imágenes: `/var/backups/sistemacomercial/`

Para detener o iniciar el servicio manualmente:

```bash
sudo systemctl stop sistemacomercial
sudo systemctl start sistemacomercial
```

## Construcción local

En Ubuntu/Linux Mint con JDK 21, `dpkg-deb` y el wrapper del proyecto:

```bash
./mvnw clean test package
./installer/linux/build-deb.sh
```

El paquete queda en `target/distributions/`.
