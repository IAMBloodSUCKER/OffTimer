@echo off
chcp 65001 >nul
title OffTimer - Uninstall

echo Closing OffTimer...
taskkill /IM OffTimer.exe /F >nul 2>&1

for /f "tokens=2" %%p in ('wmic process where "name='java.exe'" get ProcessId /value 2^>nul ^| find "="') do (
    wmic process where "ProcessId=%%p" get CommandLine 2>nul | find /I "OffTimer" >nul
    if not errorlevel 1 taskkill /PID %%p /F >nul 2>&1
)

timeout /t 1 >nul
echo.
echo OffTimer stopped.
echo Delete this folder to remove the program:
echo %~dp0
echo.
pause
