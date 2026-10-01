param(
    [string]$RutaDriver = ""
)

$ErrorActionPreference = "Continue"
$raiz = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $raiz

if (-not $RutaDriver) {
    if (-not (Test-Path -LiteralPath "lib\ojdbc11.jar")) {
        Write-Host "Falta el driver de Oracle." -ForegroundColor Red
        Write-Host "Descargue ojdbc11.jar en la carpeta lib\, o indique la ruta:" -ForegroundColor Yellow
        Write-Host "  .\compilar.ps1 -RutaDriver 'C:\ruta\ojdbc11.jar'" -ForegroundColor Yellow
        exit 1
    }
    $RutaDriver = "lib\*"
}

$carpetaLib = (Resolve-Path -LiteralPath "lib").Path

if (-not (Test-Path -LiteralPath "target\classes")) {
    New-Item -ItemType Directory -Force -Path "target\classes" | Out-Null
}
$salida = "target\compilacion.log"

$fuentes = Get-ChildItem -Recurse -Filter *.java "src\main\java" | ForEach-Object { $_.FullName }
Write-Host "Compilando $($fuentes.Count) archivos hacia Java 21..." -ForegroundColor Cyan

& javac -encoding UTF-8 --release 21 -Xlint:all -Xlint:-path -cp $carpetaLib -d "target\classes" $fuentes *> $salida
$codigo = $LASTEXITCODE

$log = Get-Content $salida -ErrorAction SilentlyContinue
$errores = @($log | Where-Object { $_ -match "error:" })
$avisos  = @($log | Where-Object { $_ -match "warning:" -or $_ -match "^\d+ (warning|error)" })

if ($avisos.Count -gt 0) {
    Write-Host "Avisos del compilador: $($avisos.Count)" -ForegroundColor Yellow
}

if ($codigo -ne 0 -or $errores.Count -gt 0) {
    Write-Host ""
    Write-Host "La compilacion termino con $($errores.Count) error(es):" -ForegroundColor Red
    $log | Select-Object -First 40 | ForEach-Object { Write-Host $_ -ForegroundColor Red }
    exit 1
}

Copy-Item -Path "src\main\resources\*" -Destination "target\classes" -Force

if (-not (Test-Path -LiteralPath "target\classes\database.properties")) {
    Write-Host "No se pudo copiar database.properties a target\classes" -ForegroundColor Red
    exit 1
}

$total = (Get-ChildItem -Recurse -Filter *.class "target\classes").Count
Write-Host "Compilacion exitosa. $total clases y database.properties en target\classes" -ForegroundColor Green
