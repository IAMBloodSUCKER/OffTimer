@echo off
chcp 65001 >nul
title OffTimer — удаление

echo Закрываю OffTimer...
taskkill /IM OffTimer.exe /F >nul 2>&1

for /f "tokens=2" %%p in ('wmic process where "name='java.exe'" get ProcessId /value 2^>nul ^| find "="') do (
    wmic process where "ProcessId=%%p" get CommandLine 2>nul | find /I "OffTimer" >nul
    if not errorlevel 1 taskkill /PID %%p /F >nul 2>&1
)

timeout /t 1 >nul
echo.
echo OffTimer закрыт.
echo Чтобы удалить программу, удали эту папку:
echo %~dp0
echo.
pause
