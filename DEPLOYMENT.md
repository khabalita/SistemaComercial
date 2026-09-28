# Instalacion en la PC final

## Requisitos

- Java 21.
- MySQL 8.
- Navegador actualizado.
- Impresora instalada en el sistema operativo.

La configuracion actual escucha solo en `127.0.0.1`, porque el uso previsto es local en la PC del usuario. Si otros equipos deben acceder al sistema, quitar `server.address=127.0.0.1` y configurar el firewall de forma controlada.

Maven no es necesario para ejecutar el `.jar` final.

## Variables de entorno

Definir antes de iniciar la aplicacion:

```text
DB_URL=jdbc:mysql://localhost:3306/sistemacomercial?serverTimezone=America/Argentina/Buenos_Aires&useSSL=false&allowPublicKeyRetrieval=true
DB_USERNAME=usuario_sistema
DB_PASSWORD=contrasena_segura
ADMIN_INITIAL_USERNAME=admin
ADMIN_INITIAL_PASSWORD=contrasena_inicial_segura
PRODUCT_IMAGES_DIR=./data/product-images
```

`PRODUCT_IMAGES_DIR` puede ser una ruta absoluta si la aplicacion se ejecuta como servicio.

## Base de datos

Crear la base y un usuario dedicado antes de iniciar la aplicacion. No utilizar el usuario root para el uso diario.

Realizar un backup de la base y de `data/product-images` antes de la primera ejecucion.

La aplicacion usa `spring.jpa.hibernate.ddl-auto=update` para crear o actualizar las tablas durante la instalacion inicial. Una vez verificado el esquema, se recomienda cambiarlo a `validate`.

Si la base fue creada con una version anterior que tenia estados de venta, ejecutar despues del backup:

```sql
ALTER TABLE sale DROP COLUMN status;
```

La columna solo debe eliminarse si todavia existe.

## Compilacion

Desde el proyecto:

```bash
./mvnw clean test
./mvnw clean package
```

El resultado queda en `target/sistemacomercial-0.0.1-SNAPSHOT.jar`.

## Ejecucion

Ejecutar desde una carpeta permanente de la aplicacion:

```bash
java -jar target/sistemacomercial-0.0.1-SNAPSHOT.jar
```

Abrir:

```text
http://localhost:8081/login
```

## Archivos que deben respaldarse

- Base de datos MySQL.
- `data/product-images`.
- Variables de entorno o configuracion de inicio.

## Prueba final

- Iniciar sesion con el administrador.
- Crear un usuario operativo.
- Crear o importar productos.
- Cargar una imagen.
- Crear una venta con descuentos por producto y general.
- Confirmar que el stock se descuenta.
- Imprimir y reimprimir la nota.
- Verificar que la impresora aparezca en el dialogo del navegador.
- Desactivar `Encabezados y pies de pagina` para evitar que aparezca la URL en el papel.
- Confirmar que el usuario operativo no pueda administrar usuarios, precios ni imagenes.
