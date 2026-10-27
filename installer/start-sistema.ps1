$ErrorActionPreference = "Stop"

$AppDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$Java = Join-Path $AppDir "java\bin\java.exe"
$Jar = Join-Path $AppDir "app\sistemacomercial.jar"
$Config = Join-Path $AppDir "config\application.properties"
$LoginUrl = "http://127.0.0.1:8081/login"
$ServiceName = "SistemaComercialMySQL"
$LogsDir = Join-Path $AppDir "logs"
$Timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
$OutputLog = Join-Path $LogsDir "startup-$Timestamp.log"
$ErrorLog = Join-Path $LogsDir "startup-error-$Timestamp.log"

function Test-ApplicationReady {
    try {
        $response = Invoke-WebRequest -Uri $LoginUrl -UseBasicParsing -TimeoutSec 2
        return $response.StatusCode -eq 200
    } catch {
        return $false
    }
}

function Get-StartupLogTail {
    $lines = @()
    foreach ($log in @($OutputLog, $ErrorLog)) {
        if (Test-Path $log) {
            $lines += Get-Content -Path $log -Tail 25 -ErrorAction SilentlyContinue
        }
    }
    return ($lines -join [Environment]::NewLine)
}

try {
    if (-not (Test-Path $Java)) { throw "No se encontró Java en: $Java" }
    if (-not (Test-Path $Jar)) { throw "No se encontró la aplicación en: $Jar" }

    New-Item -Path $LogsDir -ItemType Directory -Force | Out-Null

    # Si ya está respondiendo, no iniciar otra instancia.
    if (Test-ApplicationReady) {
        Start-Process "http://localhost:8081/login"
        exit 0
    }

    $mysql = Get-Service -Name $ServiceName -ErrorAction Stop
    if ($mysql.Status -ne "Running") {
        Start-Service -Name $ServiceName
        $mysql.WaitForStatus("Running", [TimeSpan]::FromSeconds(30))
    }

    $arguments = '-jar "{0}" --spring.config.location="optional:file:{1}"' -f $Jar, $Config
    $appProcess = Start-Process -FilePath $Java -ArgumentList $arguments -WorkingDirectory $AppDir `
        -RedirectStandardOutput $OutputLog -RedirectStandardError $ErrorLog -WindowStyle Hidden -PassThru

    $deadline = (Get-Date).AddSeconds(120)
    while ((Get-Date) -lt $deadline) {
        if (Test-ApplicationReady) {
            Start-Process "http://localhost:8081/login"
            exit 0
        }

        if ($appProcess.HasExited) {
            throw "La aplicación terminó durante el inicio (código $($appProcess.ExitCode))."
        }

        Start-Sleep -Seconds 1
    }

    if (-not $appProcess.HasExited) {
        Stop-Process -Id $appProcess.Id -Force -ErrorAction SilentlyContinue
    }
    throw "La aplicación no respondió en 120 segundos."
} catch {
    $details = Get-StartupLogTail
    $message = "No se pudo iniciar Sistema Comercial.`r`n`r`n$($_.Exception.Message)"
    if ($details) { $message += "`r`n`r`nÚltimos mensajes del inicio:`r`n$details" }
    $message += "`r`n`r`nRegistros: $LogsDir"

    try {
        Add-Type -AssemblyName System.Windows.Forms
        [System.Windows.Forms.MessageBox]::Show(
            $message,
            "Error al iniciar Sistema Comercial",
            [System.Windows.Forms.MessageBoxButtons]::OK,
            [System.Windows.Forms.MessageBoxIcon]::Error
        ) | Out-Null
    } catch {
        Write-Error $message
    }
    exit 1
}
