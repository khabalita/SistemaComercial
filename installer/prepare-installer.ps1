param(
    [string]$MySqlVersion = "8.4.6",
    [string]$MySqlUrl = "",
    [switch]$SkipDownloads
)

$ErrorActionPreference = "Stop"
$InstallerDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$ProjectDir = Split-Path -Parent $InstallerDir
$PayloadDir = Join-Path $InstallerDir "payload"
$Jar = Join-Path $ProjectDir "target\sistemacomercial-0.0.1-SNAPSHOT.jar"

if (-not (Test-Path $Jar)) {
    throw "No se encontró $Jar. Ejecutá .\mvnw.cmd clean package antes de preparar el instalador."
}

if (Test-Path $PayloadDir) {
    Remove-Item $PayloadDir -Recurse -Force
}
New-Item $PayloadDir -ItemType Directory | Out-Null
New-Item (Join-Path $PayloadDir "app") -ItemType Directory | Out-Null
New-Item (Join-Path $PayloadDir "config") -ItemType Directory | Out-Null
New-Item (Join-Path $PayloadDir "data\product-images") -ItemType Directory -Force | Out-Null

Copy-Item $Jar (Join-Path $PayloadDir "app\sistemacomercial.jar")
Copy-Item (Join-Path $InstallerDir "setup.ps1") $PayloadDir
Copy-Item (Join-Path $InstallerDir "backup.ps1") $PayloadDir
Copy-Item (Join-Path $InstallerDir "uninstall.ps1") $PayloadDir
Copy-Item (Join-Path $InstallerDir "start-sistema.bat") $PayloadDir

$JavaDir = Join-Path $PayloadDir "java"
if (-not (Test-Path $JavaDir)) {
    $JavaHome = $env:JAVA_HOME
    if (-not $JavaHome -or -not (Test-Path (Join-Path $JavaHome "bin\jlink.exe"))) {
        throw "Definí JAVA_HOME apuntando a un JDK 21 antes de ejecutar este script."
    }

    $modules = "java.base,java.compiler,java.datatransfer,java.desktop,java.instrument,java.management,java.naming,java.net.http,java.prefs,java.rmi,java.scripting,java.security.jgss,java.sql,java.transaction.xa,java.xml,jdk.crypto.ec,jdk.unsupported,jdk.zipfs"
    & (Join-Path $JavaHome "bin\jlink.exe") --module-path (Join-Path $JavaHome "jmods") --add-modules $modules --strip-debug --no-man-pages --no-header-files --compress=2 --output $JavaDir
    if ($LASTEXITCODE -ne 0) { throw "No se pudo generar el JRE portable." }
}

$MySqlDir = Join-Path $PayloadDir "mysql"
if (-not $SkipDownloads -and -not (Test-Path (Join-Path $MySqlDir "bin\mysqld.exe"))) {
    if ([string]::IsNullOrWhiteSpace($MySqlUrl)) {
        $MySqlUrl = "https://dev.mysql.com/get/Downloads/MySQL-$MySqlVersion/mysql-$MySqlVersion-winx64.zip"
    }
    $zip = Join-Path $env:TEMP "mysql-$MySqlVersion-winx64.zip"
    Write-Host "Descargando MySQL desde $MySqlUrl"
    Invoke-WebRequest -Uri $MySqlUrl -OutFile $zip
    $extract = Join-Path $env:TEMP "mysql-extract-$MySqlVersion"
    if (Test-Path $extract) { Remove-Item $extract -Recurse -Force }
    Expand-Archive $zip -DestinationPath $extract
    $root = Get-ChildItem $extract -Directory | Select-Object -First 1
    if (-not $root) { throw "El ZIP de MySQL no tiene una carpeta raíz válida." }
    Move-Item $root.FullName $MySqlDir
    Remove-Item $extract -Recurse -Force
}

if (-not (Test-Path (Join-Path $MySqlDir "bin\mysqld.exe"))) {
    throw "No se encontró MySQL portable. Ejecutá nuevamente sin -SkipDownloads o colocá el ZIP extraído en installer\payload\mysql."
}

Write-Host "Payload preparado en $PayloadDir"
Write-Host "Siguiente paso: compilá installer\SistemaComercial.iss con Inno Setup."
