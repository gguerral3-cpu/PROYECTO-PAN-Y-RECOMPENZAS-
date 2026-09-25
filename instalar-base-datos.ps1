param(
    [string]$Servicio = 'XEPDB1',
    [string]$Puerto   = '1521',
    [string]$Usuario  = 'PANPUNTOS',
    [string]$Password = '123456',
    [string]$RutaScripts = "$PSScriptRoot\database"
)

$ErrorActionPreference = 'Stop'

function Find-SqlPlus {
    $candidatos = @(
        'C:\app\Usuario\product\21c\dbhomeXE\bin\sqlplus.exe',
        "$env:ORACLE_HOME\bin\sqlplus.exe"
    )
    $enContrario = Get-ChildItem 'C:\app' -Recurse -Filter 'sqlplus.exe' -ErrorAction SilentlyContinue |
                   Select-Object -First 1
    if ($enContrario) { $candidatos += $enContrario.FullName }
    foreach ($c in $candidatos) {
        if (Test-Path $c) { return $c }
    }
    return $null
}

$sqlplus = Find-SqlPlus
if (-not $sqlplus) {
    Write-Host 'ERROR: no se encontro sqlplus.exe' -ForegroundColor Red
    exit 1
}
Write-Host "SQL*Plus   : $sqlplus" -ForegroundColor DarkGray
Write-Host "Servicio   : $Servicio"   -ForegroundColor DarkGray
Write-Host "Usuario    : $Usuario"    -ForegroundColor DarkGray
Write-Host ''

$segura = Read-Host "Contrasena de SYSTEM" -AsSecureString
$pwdSystem = [System.Runtime.InteropServices.Marshal]::PtrToStringAuto(
    [System.Runtime.InteropServices.Marshal]::SecureStringToBSTR($segura))
if ([string]::IsNullOrWhiteSpace($pwdSystem)) {
    Write-Host 'ERROR: contrasena vacia' -ForegroundColor Red
    exit 1
}

$logon = "SYSTEM/$pwdSystem@//localhost:$Puerto/$Servicio"
$pwdSystem = $null

$sqlCrear = @"
WHENEVER SQLERROR EXIT SQL.SQLCODE
SET FEEDBACK OFF
SET LINESIZE 200
ALTER SESSION SET CONTAINER=XEPDB1;
BEGIN
  EXECUTE IMMEDIATE 'DROP USER $Usuario CASCADE';
EXCEPTION
  WHEN OTHERS THEN
    IF SQLCODE != -1918 THEN RAISE; END IF;
END;
/
CREATE USER $Usuario IDENTIFIED BY "$Password"
  DEFAULT TABLESPACE USERS
  TEMPORARY TABLESPACE TEMP
  QUOTA UNLIMITED ON USERS;
GRANT CREATE SESSION, CREATE TABLE, CREATE SEQUENCE, CREATE VIEW,
      CREATE TRIGGER, CREATE PROCEDURE, CREATE TYPE,
      CREATE SYNONYM, CREATE LIBRARY TO $Usuario;
GRANT UNLIMITED TABLESPACE TO $Usuario;
PROMPT USUARIO_CREADO_OK
EXIT
"@

Write-Host '--- 1/2 Creando usuario ---' -ForegroundColor Cyan
$salida = $sqlCrear | & $sqlplus -S $logon 2>&1

if ($salida -match 'USUARIO_CREADO_OK') {
    Write-Host '  OK: usuario creado y privileges asignados' -ForegroundColor Green
} else {
    Write-Host '  FALLO al crear el usuario:' -ForegroundColor Red
    $salida | ForEach-Object { Write-Host "    $_" -ForegroundColor Red }
    if ($salida -match 'ORA-28003|ORA-00904') {
        Write-Host ''
        Write-Host '  La contrasena no cumple la politica de Oracle.' -ForegroundColor Yellow
        Write-Host '  Reejecuta con una contrasena mas larga, por ejemplo:' -ForegroundColor Yellow
        Write-Host '    .\instalar-base-datos.ps1 -Password PanPuntos2026' -ForegroundColor Yellow
    }
    exit 1
}

Write-Host ''
Write-Host '--- 2/2 Ejecutando database\instalar.sql ---' -ForegroundColor Cyan
$logonApp = "$Usuario/$Password@//localhost:$Puerto/$Servicio"

$salida2 = "& '$RutaScripts\instalar.sql'`nexit;" | & $sqlplus -S $logonApp 2>&1

$errores = $salida2 | Where-Object { $_ -match 'ORA-\d+' } | Where-Object { $_ -notmatch 'FALLO:' }
Write-Host ''
Write-Host '=== RESULTADO DE LAS PRUEBAS DE NEGOCIO ===' -ForegroundColor Cyan
$salida2 | Where-Object { $_ -match 'PRUEBA|ERROR ESPERADO|FALLO:|Objetos eliminados' } |
    ForEach-Object { Write-Host "  $_" }

if ($errores) {
    Write-Host ''
    Write-Host '=== ERRORES DETECTADOS ===' -ForegroundColor Yellow
    $errores | Select-Object -Unique | ForEach-Object { Write-Host "  $_" -ForegroundColor Yellow }
    exit 2
}

Write-Host ''
Write-Host 'ESQUEMA INSTALADO CORRECTAMENTE' -ForegroundColor Green
exit 0
