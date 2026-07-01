# Removes old OffTimer MSI installs (v1.0.7 and earlier).
# Portable versions (zip) are just deleted as a folder.

$ErrorActionPreference = "Stop"

Write-Host "Stopping OffTimer..."
Get-Process -Name "OffTimer" -ErrorAction SilentlyContinue | Stop-Process -Force
Start-Sleep -Seconds 1

$uninstallRoots = @(
    "HKLM:\SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\*",
    "HKLM:\SOFTWARE\WOW6432Node\Microsoft\Windows\CurrentVersion\Uninstall\*",
    "HKCU:\SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\*"
)

$found = $false

foreach ($root in $uninstallRoots) {
    Get-ItemProperty $root -ErrorAction SilentlyContinue |
        Where-Object { $_.DisplayName -like "*OffTimer*" } |
        ForEach-Object {
            $found = $true
            Write-Host "Found: $($_.DisplayName)"

            $cmd = $_.UninstallString
            if (-not $cmd) {
                return
            }

            if ($cmd -match 'msiexec(\.exe)?\s+/I\{([^}]+)\}' -or $cmd -match '\{([^}]+)\}') {
                $guid = $Matches[1]
                if ($guid -notmatch '^\{') { $guid = "{$guid}" }
                Write-Host "Uninstalling MSI $guid ..."
                Start-Process "msiexec.exe" -ArgumentList "/x $guid /quiet /norestart" -Wait -NoNewWindow
            } elseif ($cmd -match '\.exe') {
                $exe = $cmd.Split('"')[1]
                if (-not $exe) { $exe = ($cmd -split ' ')[0] }
                Write-Host "Running uninstaller: $exe"
                if ($exe -like "*OffTimer*.exe") {
                    Start-Process $exe -ArgumentList "--uninstall" -Wait
                } else {
                    Start-Process $exe -Wait
                }
            }
        }
}

if (-not $found) {
    Write-Host "No installed OffTimer found in Programs and Features."
    Write-Host "If you use the portable zip version, just delete the OffTimer folder."
} else {
    Write-Host "Done. You can install the portable version from OffTimer-Windows.zip."
}
