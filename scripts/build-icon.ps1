param(
    [string]$ProjectRoot = (Split-Path $PSScriptRoot -Parent)
)

Push-Location $ProjectRoot
try {
    mvn -q -DskipTests compile
    java -cp target/classes com.offtimer.build.IconBuilderMain .
} finally {
    Pop-Location
}
