@echo off
chcp 65001 >nul
set "TARGET=%~dp0"
set "EXE=%TARGET%OffTimer.exe"
set "ICON=%TARGET%OffTimer.ico"

if not exist "%EXE%" (
    echo Не найден OffTimer.exe в этой папке.
    pause
    exit /b 1
)

powershell -NoProfile -Command ^
  "$s = (New-Object -ComObject WScript.Shell).CreateShortcut('%USERPROFILE%\Desktop\OffTimer.lnk');" ^
  "$s.TargetPath = '%EXE%';" ^
  "$s.WorkingDirectory = '%TARGET%';" ^
  "if (Test-Path '%ICON%') { $s.IconLocation = '%ICON%,0' };" ^
  "$s.Description = 'OffTimer';" ^
  "$s.Save()"

echo Ярлык создан на рабочем столе.
pause
