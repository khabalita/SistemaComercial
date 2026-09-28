param(
    [Parameter(Mandatory = $true)]
    [string]$AdminPassword
)

$ErrorActionPreference = "Stop"
$AppDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$MySqlDir = Join-Path $AppDir "mysql"
$DataDir = Join-Path $MySqlDir "data"
$ConfigDir = Join-Path $AppDir "config"
$Ini = Join-Path $MySqlDir "my.ini"
$ServiceName = "SistemaComercialMySQL"
$DbName = "sistemacomercial"
$DbUser = "sistema_app"
$DbPassword = -join ((48..57) + (65..90) + (97..122) | Get-Random -Count 32 | ForEach-Object {[char]$_})

if ($AdminPassword.Length -lt 8) {
    throw "La contraseña inicial del administrador debe tener al menos 8 caracteres."
}

New-Item $ConfigDir -ItemType Directory -Force | Out-Null
New-Item (Join-Path $AppDir "data\product-images") -ItemType Directory -Force | Out-Null

@"
[mysqld]
basedir=$($MySqlDir.Replace('\','/'))
datadir=$($DataDir.Replace('\','/'))
port=3306
bind-address=127.0.0.1
character-set-server=utf8mb4
collation-server=utf8mb4_unicode_ci
log-error=$($AppDir.Replace('\','/'))/logs/mysql.err
"@ | Set-Content -Path $Ini -Encoding ASCII
New-Item (Join-Path $AppDir "logs") -ItemType Directory -Force | Out-Null

$Mysqld = Join-Path $MySqlDir "bin\mysqld.exe"
$Mysql = Join-Path $MySqlDir "bin\mysql.exe"
if (-not (Get-Service $ServiceName -ErrorAction SilentlyContinue)) {
    if (-not (Test-Path (Join-Path $DataDir "mysql"))) {
        & $Mysqld --defaults-file=$Ini --initialize-insecure --console
        if ($LASTEXITCODE -ne 0) { throw "No se pudo inicializar MySQL." }
    }
    & $Mysqld --install $ServiceName --defaults-file=$Ini
    if ($LASTEXITCODE -ne 0) { throw "No se pudo instalar el servicio de MySQL." }
}

Start-Service $ServiceName
$started = $false
for ($i = 0; $i -lt 30; $i++) {
    Start-Sleep -Seconds 1
    & $Mysql --protocol=tcp -h 127.0.0.1 -u root -e "SELECT 1" 2>$null
    if ($LASTEXITCODE -eq 0) { $started = $true; break }
}
if (-not $started) { throw "MySQL no respondió después de iniciar el servicio." }

$sql = "CREATE DATABASE IF NOT EXISTS ``$DbName`` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci; CREATE USER IF NOT EXISTS '$DbUser'@'localhost' IDENTIFIED BY '$DbPassword'; ALTER USER '$DbUser'@'localhost' IDENTIFIED BY '$DbPassword'; GRANT ALL PRIVILEGES ON ``$DbName``.* TO '$DbUser'@'localhost'; FLUSH PRIVILEGES;"
& $Mysql --protocol=tcp -h 127.0.0.1 -u root -e $sql
if ($LASTEXITCODE -ne 0) { throw "No se pudo crear la base o el usuario de la aplicación." }

@"
spring.datasource.url=jdbc:mysql://localhost:3306/$DbName?serverTimezone=America/Argentina/Buenos_Aires&useSSL=false&allowPublicKeyRetrieval=true
spring.datasource.username=$DbUser
spring.datasource.password=$DbPassword
app.security.initial-admin-username=admin
app.security.initial-admin-password=$AdminPassword
app.storage.product-images=$($AppDir.Replace('\','/'))/data/product-images
"@ | Set-Content (Join-Path $ConfigDir "application.properties") -Encoding UTF8

& (Join-Path $AppDir "backup.ps1") -InstallTask -DbPassword $DbPassword
Write-Host "Instalación inicial completada. Usuario inicial: admin"
