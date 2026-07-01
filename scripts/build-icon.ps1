param(
    [string]$ProjectRoot = (Split-Path $PSScriptRoot -Parent)
)

$java = Get-Command java -ErrorAction SilentlyContinue
if (-not $java) {
    throw "Java is required to build icons"
}

Push-Location $ProjectRoot
try {
    java scripts/IconBuilder.java .
} finally {
    Pop-Location
}
