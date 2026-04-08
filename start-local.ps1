$localConfig = Join-Path $PSScriptRoot "src\main\resources\application-local.yml"

if (-not (Test-Path $localConfig)) {
  Write-Host "Local config not found." -ForegroundColor Yellow
  Write-Host "Create it from src\main\resources\application-local.yml.example and add your MySQL username/password." -ForegroundColor Yellow
  exit 1
}

Push-Location $PSScriptRoot
try {
  mvn spring-boot:run "-Dspring-boot.run.profiles=local"
} finally {
  Pop-Location
}
