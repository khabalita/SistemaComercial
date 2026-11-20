param(
    [string]$PayloadDir = (Join-Path $PSScriptRoot "payload")
)

$ErrorActionPreference = "Stop"
$password = "SmokeTest-Admin-123"
$setup = Join-Path $PayloadDir "setup.ps1"
$java = Join-Path $PayloadDir "java\bin\java.exe"
$jar = Join-Path $PayloadDir "app\sistemacomercial.jar"
$config = Join-Path $PayloadDir "config\application.properties"
$service = "SistemaComercialMySQL"
$app = $null

try {
    if (-not (Test-Path $setup)) { throw "No se encontró setup.ps1 en el payload." }
    & powershell.exe -NoProfile -ExecutionPolicy Bypass -File $setup -AdminPassword $password
    if ($LASTEXITCODE -ne 0) { throw "La configuración inicial devolvió código $LASTEXITCODE." }

    $mysql = Get-Service $service -ErrorAction Stop
    if ($mysql.Status -ne "Running") { throw "El servicio MySQL no quedó iniciado." }
    if (-not (Test-NetConnection 127.0.0.1 -Port 3306 -InformationLevel Quiet)) {
        throw "MySQL no escucha en el puerto 3306."
    }

    $arguments = '-jar "{0}" --spring.config.location="optional:file:{1}"' -f $jar, $config
    $app = Start-Process $java -ArgumentList $arguments -WorkingDirectory $PayloadDir `
        -RedirectStandardOutput (Join-Path $PayloadDir "smoke-out.log") `
        -RedirectStandardError (Join-Path $PayloadDir "smoke-error.log") -PassThru

    $ready = $false
    for ($i = 0; $i -lt 90; $i++) {
        Start-Sleep 1
        try {
            $response = Invoke-WebRequest http://127.0.0.1:8081/login -UseBasicParsing -TimeoutSec 2
            if ($response.StatusCode -eq 200) { $ready = $true; break }
        } catch { }
    }
    if (-not $ready) { throw "La aplicación no respondió en el smoke test." }
    Write-Host "Smoke test Windows OK."
} finally {
    if ($app -and -not $app.HasExited) { Stop-Process $app.Id -Force -ErrorAction SilentlyContinue }
    Stop-Service $service -Force -ErrorAction SilentlyContinue
    $mysqld = Join-Path $PayloadDir "mysql\bin\mysqld.exe"
    if (Test-Path $mysqld) { & $mysqld --remove $service | Out-Null }
}
