git # Instalador Linux (Ubuntu y Linux Mint)

El paquete `.deb` está preparado para equipos **amd64** basados en Ubuntu, incluidos Linux Mint. Incluye un runtime Java 21 y configura MySQL, la aplicación como servicio `systemd` y respaldos diarios.

## Instalación

Descargá el artefacto `sistemacomercial_<versión>_amd64.deb` de GitHub Actions y ejecutá:

```bash
sudo apt install ./sistemacomercial_<versión>_amd64.deb
```

Durante la instalación se solicita una contraseña inicial para `admin` (mínimo 8 caracteres). El instalador crea la base `sistemacomercial` y su usuario MySQL dedicado. Al finalizar, abrí `http://localhost:8081/login` o la entrada **Sistema Comercial** del menú de aplicaciones.

El archivo `/etc/sistemacomercial/application.properties` se genera durante la configuración del paquete, con permisos restringidos. Si MySQL no está disponible y `apt` deja el paquete sin configurar, el archivo y las credenciales generadas se conservan para poder reanudar el proceso:

```bash
sudo systemctl status mysql
sudo dpkg --configure sistemacomercial
```

Si la instalación se interrumpió antes de ingresar la contraseña de `admin`, volvé a abrir el diálogo con `sudo dpkg-reconfigure sistemacomercial`.

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

## Tamaño del instalador

El paquete Linux depende del servidor MySQL instalado en el sistema y no lo incluye; sí incluye el runtime Java y la aplicación. El instalador de Windows empaqueta MySQL portable, Java y sus archivos auxiliares, por eso su archivo de instalación es considerablemente más grande.
