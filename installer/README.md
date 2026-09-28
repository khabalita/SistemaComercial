# Instalador de Windows

Este instalador prepara una PC Windows nueva con:

- Java 21 portable.
- MySQL Community Server portable ejecutándose como servicio.
- La aplicación y su configuración.
- Base de datos nueva `sistemacomercial`.
- Usuario de base de datos exclusivo para la aplicación.
- Backup diario a las 23:00.
- Backup de la base y de las imágenes.

## Requisitos de la PC donde se construye

- Windows 10/11 de 64 bits.
- JDK 21 de 64 bits y `JAVA_HOME` definido.
- Inno Setup 6 instalado.
- Internet durante la preparación para descargar MySQL Community Server.

## Construcción

Desde PowerShell, en la raíz del proyecto:

```powershell
./mvnw.cmd clean package
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-21'
./installer/prepare-installer.ps1
```

Después abrir `installer/SistemaComercial.iss` con Inno Setup y presionar **Compile**. El instalador se generará en:

```text
installer/output/SistemaComercial-Setup.exe
```

El script descarga MySQL 8.4.6 desde el sitio oficial. Si cambia la URL, puede indicarse manualmente:

```powershell
./installer/prepare-installer.ps1 -MySqlUrl 'URL_DEL_ZIP_DE_MYSQL'
```

## Instalación en la PC final

1. Ejecutar `SistemaComercial-Setup.exe` como administrador.
2. Elegir la contraseña inicial del usuario `admin`.
3. Esperar a que se inicialice MySQL y la base nueva.
4. Abrir el acceso directo `Sistema Comercial`.
5. Ingresar con usuario `admin` y la contraseña elegida.
6. Crear un usuario operativo y cambiar la contraseña inicial si corresponde.

La instalación no utiliza datos de MySQL anteriores. Hibernate crea el esquema nuevo al iniciar la aplicación.

## Backups

La tarea programada `Sistema Comercial Backup` ejecuta un backup todos los días a las 23:00. Los archivos quedan en:

```text
%LOCALAPPDATA%\SistemaComercial\backups\
```

También se puede ejecutar manualmente desde una consola de PowerShell como administrador:

```powershell
& "$env:LOCALAPPDATA\SistemaComercial\backup.ps1"
```

Conviene copiar periódicamente la carpeta `backups` a otro disco o almacenamiento externo. La desinstalación no debería usarse como método de backup: conservar una copia externa antes de actualizar.

## Primera prueba

Verificar login, creación de usuarios, productos, imágenes, ventas, descuentos, stock, impresión y ejecución de un backup manual antes de entregar la PC.
