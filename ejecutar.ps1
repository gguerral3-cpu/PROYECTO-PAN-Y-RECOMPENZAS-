param(
    [string]$RutaDriver = "",
    [switch]$Api
)

$ErrorActionPreference = "Stop"
$raiz = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $raiz

if (-not (Test-Path -LiteralPath "target\classes\panpuntos\Main.class")) {
    Write-Host "El proyecto no esta compilado. Ejecute .\compilar.ps1 primero." -ForegroundColor Yellow
    exit 1
}

if (-not $RutaDriver) {
    $RutaDriver = Join-Path $raiz "lib\ojdbc11.jar"
}

$clase = if ($Api) { "panpuntos.api.ApiPuntos" } else { "panpuntos.Main" }
$cp = "target\classes"
if (Test-Path -LiteralPath $RutaDriver) {
    $cp = "$cp;$RutaDriver"
}

Write-Host "Iniciando $clase..." -ForegroundColor Cyan
& java "-Dfile.encoding=UTF-8" -cp $cp $clase
