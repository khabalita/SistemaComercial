# Instalador Linux (Ubuntu y Linux Mint)

El paquete `.deb` está preparado para equipos **amd64** basados en Ubuntu, incluidos Linux Mint. Incluye un runtime Java 21 y configura MySQL, la aplicación como servicio `systemd` y respaldos diarios.

## Instalación

Descargá el artefacto `sistemacomercial_<versión>_amd64.deb` de GitHub Actions y ejecutá:

```bash
sudo apt install ./sistemacomercial_<versión>_amd64.deb
```

Durante la instalación se solicita una contraseña inicial para `admin` (mínimo 8 caracteres). Si es más corta, el instalador vuelve a pedirla y no continúa con una respuesta vacía. El instalador crea la base `sistemacomercial` y su usuario MySQL dedicado. Al finalizar, abrí `http://localhost:8081/login` o la entrada **Sistema Comercial** del menú de aplicaciones.

El archivo `/etc/sistemacomercial/application.properties` se genera durante la configuración del paquete, con permisos restringidos. Si MySQL no está disponible y `apt` deja el paquete sin configurar, el archivo y las credenciales generadas se conservan para poder reanudar el proceso:

```bash
sudo systemctl status mysql
sudo dpkg --configure sistemacomercial
```

Si una instalación anterior guardó una respuesta incorrecta, volvé a abrir el diálogo desde una terminal con `sudo dpkg-reconfigure -f readline sistemacomercial`. La nueva respuesta se guarda en la configuración de la aplicación, incluso si esta ya se había generado antes de que fallara la instalación. No hace falta borrar ni reinstalar MySQL. Los cambios de contraseña inicial solo crean el usuario `admin` si todavía no existe; para cambiar la contraseña de un usuario ya creado, hacelo desde la administración de usuarios de la aplicación.

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
