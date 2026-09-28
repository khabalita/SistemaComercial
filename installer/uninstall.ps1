$ErrorActionPreference = "SilentlyContinue"
Unregister-ScheduledTask -TaskName "Sistema Comercial Backup" -Confirm:$false
Stop-Service "SistemaComercialMySQL"
& (Join-Path (Split-Path -Parent $MyInvocation.MyCommand.Path) "mysql\bin\mysqld.exe") --remove "SistemaComercialMySQL"
