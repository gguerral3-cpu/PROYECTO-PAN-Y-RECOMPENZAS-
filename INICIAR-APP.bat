@echo off
cd /d "%~dp0"
title Pan Puntos y Premios
echo Iniciando la aplicacion, espere unos segundos...
echo.
powershell -NoProfile -ExecutionPolicy Bypass -File "ejecutar.ps1"
echo.
echo La aplicacion se cerro.
pause