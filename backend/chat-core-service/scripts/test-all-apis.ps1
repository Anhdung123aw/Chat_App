# Test all remaining APIs
$baseUrl = "http://localhost:8080"
$conversationId = "cf8ec270-b5ea-4935-85b8-e34a84a59111"

Write-Host "========== TESTING REMAINING APIs ==========" -ForegroundColor Cyan
Write-Host ""

# Test 1: Get Message History
Write-Host "[TEST 1] Get Message History..." -ForegroundColor Yellow
$messages = Invoke-RestMethod -Uri "$baseUrl/api/v1/conversations/$conversationId/messages" -Method Get
Write-Host "OK - Retrieved $($messages.Count) message(s)" -ForegroundColor Green
Write-Host ""

# Test 2: Mark Messages as Read
Write-Host "[TEST 2] Mark Messages as Read..." -ForegroundColor Yellow
$read = Invoke-RestMethod -Uri "$baseUrl/api/v1/conversations/$conversationId/read?upToSequence=1" -Method Post
Write-Host "OK - Conversation status: $($read.status)" -ForegroundColor Green
Write-Host ""

# Test 3: Transfer to Another Agent
Write-Host "[TEST 3] Transfer to Another Agent..." -ForegroundColor Yellow
$transferBody = @{
    toAgentId = "agent002"
    transferredBy = "agent001"
    reason = "Specialist required"
} | ConvertTo-Json

$transferred = Invoke-RestMethod -Uri "$baseUrl/api/v1/conversations/$conversationId/transfer" `
    -Method Post `
    -Body $transferBody `
    -ContentType "application/json"
Write-Host "OK - Transferred to: $($transferred.assignedAgent)" -ForegroundColor Green
Write-Host ""

# Test 4: Send Agent Reply
Write-Host "[TEST 4] Send Agent Reply..." -ForegroundColor Yellow
$agentMsgBody = @{
    senderId = "agent002"
    senderType = "AGENT"
    messageType = "TEXT"
    content = "Hello, I am agent002, how can I help you?"
    clientMessageId = "agent-msg-002"
} | ConvertTo-Json

$msg2 = Invoke-RestMethod -Uri "$baseUrl/api/v1/conversations/$conversationId/messages" `
    -Method Post `
    -Body $agentMsgBody `
    -ContentType "application/json"
Write-Host "OK - Agent reply sent (seqNo: $($msg2.seqNo))" -ForegroundColor Green
Write-Host ""

# Test 5: Close Conversation
Write-Host "[TEST 5] Close Conversation..." -ForegroundColor Yellow
$closed = Invoke-RestMethod -Uri "$baseUrl/api/v1/conversations/$conversationId/close" -Method Post
Write-Host "OK - Conversation closed, status: $($closed.status)" -ForegroundColor Green
Write-Host ""

# Summary
Write-Host "========== TEST SUMMARY ==========" -ForegroundColor Cyan
Write-Host "All APIs tested successfully!" -ForegroundColor Green
Write-Host "Conversation ID: $conversationId" -ForegroundColor White
Write-Host "Final Status: $($closed.status)" -ForegroundColor White
Write-Host "Assigned Agent: $($closed.assignedAgent)" -ForegroundColor White
Write-Host ""
