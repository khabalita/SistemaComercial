@echo off
setlocal
set "APP_DIR=%~dp0"
start "Sistema Comercial" "%APP_DIR%java\bin\java.exe" -jar "%APP_DIR%app\sistemacomercial.jar" --spring.config.additional-location="optional:file:%APP_DIR%config/"
timeout /t 5 /nobreak >nul
start "" "http://localhost:8081/login"
endlocal
