Push-Location $PSScriptRoot
try {
  if (-not (Test-Path Env:MYSQL_PASSWORD)) {
    Write-Host "MYSQL_PASSWORD is not set." -ForegroundColor Yellow
    Write-Host 'Run this first in PowerShell:' -ForegroundColor Yellow
    Write-Host '$env:MYSQL_USERNAME="root"' -ForegroundColor Cyan
    Write-Host '$env:MYSQL_PASSWORD="your_mysql_password"' -ForegroundColor Cyan
    exit 1
  }

  mvn spring-boot:run
} finally {
  Pop-Location
}
