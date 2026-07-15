$ErrorActionPreference = 'Stop'
$admin = if ($env:SALESTOP_ADMIN_KEY) { $env:SALESTOP_ADMIN_KEY } else { 'local-admin-key' }
$email = "smoke-$([DateTimeOffset]::UtcNow.ToUnixTimeMilliseconds())@example.com"
$auth = Invoke-RestMethod -Method Post -Uri 'http://localhost:8081/api/v1/auth/register' -ContentType 'application/json' -Body (@{email=$email;password='correct-horse-battery-staple';fullName='Smoke Test'} | ConvertTo-Json)
$product = Invoke-RestMethod -Method Post -Uri 'http://localhost:8082/api/v1/products' -Headers @{'X-Admin-Key'=$admin} -ContentType 'application/json' -Body (@{title='Smoke Test Drop';price=99.00;initialStock=10} | ConvertTo-Json)
$inventoryReady = $false
for ($i = 0; $i -lt 30; $i++) {
    try { $stock = Invoke-RestMethod "http://localhost:8085/api/v1/inventory/$($product.id)"; if ($stock.availableStock -eq 10) { $inventoryReady = $true; break } } catch {}
    Start-Sleep -Seconds 1
}
if (-not $inventoryReady) { throw 'Inventory seed did not converge within 30 seconds' }
$headers = @{Authorization="Bearer $($auth.accessToken)";'Idempotency-Key'="smoke-$([guid]::NewGuid())"}
$checkout = Invoke-RestMethod -Method Post -Uri 'http://localhost:8084/api/v1/checkout' -Headers $headers -ContentType 'application/json' -Body (@{productId=$product.id;quantity=1} | ConvertTo-Json)
$order = $null
for ($i = 0; $i -lt 30; $i++) {
    try { $order = Invoke-RestMethod -Method Get -Uri "http://localhost:8086/api/v1/orders/$($checkout.orderId)"; if ($order.status -eq 'PAID') { break } } catch {}
    Start-Sleep -Seconds 1
}
if ($null -eq $order -or $order.status -ne 'PAID') { throw "Order did not reach PAID within 30 seconds (last status: $($order.status))" }
Write-Host "Smoke test passed for order $($checkout.orderId)" -ForegroundColor Green
