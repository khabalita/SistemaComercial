param(
    [switch]$InstallTask,
    [string]$DbPassword
)

$ErrorActionPreference = "Stop"
$AppDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$MySql = Join-Path $AppDir "mysql\bin\mysqldump.exe"
$BackupDir = Join-Path $AppDir "backups"
$ImagesDir = Join-Path $AppDir "data\product-images"
$TaskName = "SistemaComercial Backup"

if ($InstallTask) {
    if ([string]::IsNullOrWhiteSpace($DbPassword)) { throw "Falta la contraseña de base de datos." }
    $escaped = $DbPassword.Replace("'", "''")
    $script = Join-Path $AppDir "backup-sistema.ps1"
    @"
`$ErrorActionPreference = 'Stop'
`$AppDir = '$($AppDir.Replace("'", "''"))'
`$BackupDir = Join-Path `$AppDir 'backups'
`$MysqlDump = Join-Path `$AppDir 'mysql\bin\mysqldump.exe'
`$Images = Join-Path `$AppDir 'data\product-images'
New-Item `$BackupDir -ItemType Directory -Force | Out-Null
`$stamp = Get-Date -Format 'yyyy-MM-dd_HH-mm-ss'
`$sql = Join-Path `$BackupDir "sistemacomercial_`$stamp.sql"
& `$MysqlDump --protocol=tcp -h 127.0.0.1 -u sistema_app -p'$escaped' sistemacomercial > `$sql
if (`$LASTEXITCODE -ne 0) { throw 'mysqldump falló.' }
Compress-Archive -Path `$Images -DestinationPath (Join-Path `$BackupDir "product-images_`$stamp.zip") -Force
Get-ChildItem `$BackupDir -File | Where-Object { `$_.LastWriteTime -lt (Get-Date).AddDays(-30) } | Remove-Item -Force
"@ | Set-Content $script -Encoding UTF8
    $action = New-ScheduledTaskAction -Execute "powershell.exe" -Argument "-NoProfile -ExecutionPolicy Bypass -File `"$script`""
    $trigger = New-ScheduledTaskTrigger -Daily -At 23:00
    $principal = New-ScheduledTaskPrincipal -UserId "SYSTEM" -LogonType ServiceAccount -RunLevel Highest
    Register-ScheduledTask -TaskName $TaskName -Action $action -Trigger $trigger -Principal $principal -Force | Out-Null
    return
}

if (-not (Test-Path $AppDir)) { throw "No se encontró la instalación." }
& (Join-Path $AppDir "backup-sistema.ps1")
