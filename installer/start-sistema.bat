@echo off
setlocal
set "APP_DIR=%~dp0"
start "" powershell.exe -NoProfile -WindowStyle Hidden -ExecutionPolicy Bypass -File "%APP_DIR%start-sistema.ps1"
endlocal
