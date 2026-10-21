@echo off
setlocal
set "APP_DIR=%~dp0"

:: 1. Asegurar que el servicio de MySQL esté corriendo antes de abrir el sistema
net start SistemaComercialMySQL >nul 2>&1

:: 2. Levantar la aplicación pasando la ruta de configuración externa obligatoria
start "Sistema Comercial" "%APP_DIR%java\bin\javaw.exe" -jar "%APP_DIR%app\sistemacomercial.jar" --spring.config.location="optional:file:%APP_DIR%config/application.properties"

:: 3. Esperar a que Spring Boot levante el Tomcat (ajustado a 6 o 7 segundos por si la PC es lenta)
timeout /t 6 /nobreak >nul

:: 4. Abrir el navegador en el login
start "" "http://localhost:8081/login"

endlocal
