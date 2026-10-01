@echo off
cd /d "%~dp0"
title API de puntos - no cierre esta ventana
echo Iniciando la API REST en el puerto 8080...
echo Deje esta ventana abierta mientras use la API.
echo Para detener la API presione Ctrl + C.
echo.
powershell -NoProfile -ExecutionPolicy Bypass -File "ejecutar.ps1" -Api
echo.
echo La API se detuvo.
pause