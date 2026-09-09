# Chat App Infrastructure Startup Script
# Khởi động infrastructure và kiểm tra health

Write-Host "🚀 Starting Chat App Infrastructure..." -ForegroundColor Cyan
Write-Host ""

# Start docker-compose
Write-Host "Starting Docker Compose services..." -ForegroundColor Yellow
docker-compose up -d

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Failed to start services!" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "⏳ Waiting for services to be healthy..." -ForegroundColor Yellow
Write-Host ""

# Wait function
function Wait-ForService {
    param(
        [string]$ServiceName,
        [int]$MaxRetries = 30
    )
    
    $retries = 0
    while ($retries -lt $MaxRetries) {
        $status = docker inspect --format='{{.State.Health.Status}}' "chat-$ServiceName" 2>$null
        
        if ($status -eq "healthy") {
            Write-Host "✅ $ServiceName is healthy" -ForegroundColor Green
            return $true
        }
        
        Write-Host "   Waiting for $ServiceName... ($retries/$MaxRetries)" -ForegroundColor Gray
        Start-Sleep -Seconds 2
        $retries++
    }
    
    Write-Host "⚠️  $ServiceName did not become healthy in time" -ForegroundColor Yellow
    return $false
}

# Check Oracle (takes longest)
Write-Host "Checking Oracle Database..." -ForegroundColor Cyan
$oracleHealthy = Wait-ForService -ServiceName "oracle" -MaxRetries 60

# Check other services
Write-Host ""
Write-Host "Checking other services..." -ForegroundColor Cyan
Wait-ForService -ServiceName "zookeeper" | Out-Null
Wait-ForService -ServiceName "kafka" | Out-Null
Wait-ForService -ServiceName "redis" | Out-Null
Wait-ForService -ServiceName "minio" | Out-Null
Wait-ForService -ServiceName "keycloak" | Out-Null

Write-Host ""
Write-Host "📊 Service Status:" -ForegroundColor Cyan
docker-compose ps

Write-Host ""
Write-Host "🌐 Access URLs:" -ForegroundColor Cyan
Write-Host "   Oracle:       jdbc:oracle:thin:@localhost:1521/XEPDB1 (system/123)" -ForegroundColor White
Write-Host "   Kafka:        localhost:9092" -ForegroundColor White
Write-Host "   Kafka UI:     http://localhost:8090" -ForegroundColor White
Write-Host "   Redis:        localhost:6379" -ForegroundColor White
Write-Host "   MinIO:        http://localhost:9000 (minioadmin/minioadmin)" -ForegroundColor White
Write-Host "   MinIO Console: http://localhost:9001" -ForegroundColor White
Write-Host "   Keycloak:     http://localhost:8081 (admin/admin)" -ForegroundColor White

Write-Host ""
if ($oracleHealthy) {
    Write-Host "✅ All services are ready!" -ForegroundColor Green
    Write-Host "You can now run: mvn spring-boot:run" -ForegroundColor Cyan
} else {
    Write-Host "⚠️  Oracle might still be initializing. Wait a few more minutes." -ForegroundColor Yellow
    Write-Host "Check logs: docker logs chat-oracle" -ForegroundColor Cyan
}
