# Script PowerShell para gerar JWT Secret seguro
# Execute este script e use a saída como valor para JWT_SECRET no Koyeb

Write-Host "Gerando JWT Secret seguro..." -ForegroundColor Green
Write-Host ""

# Gerar uma chave de 64 bytes aleatórios e converter para Base64
$bytes = New-Object byte[] 64
$rng = New-Object System.Security.Cryptography.RNGCryptoServiceProvider
$rng.GetBytes($bytes)
$jwtSecret = [System.Convert]::ToBase64String($bytes)

Write-Host "JWT_SECRET gerado:" -ForegroundColor Yellow
Write-Host $jwtSecret -ForegroundColor Cyan
Write-Host ""
Write-Host "Copie este valor e configure como variável de ambiente JWT_SECRET no Koyeb Dashboard" -ForegroundColor Green
Write-Host ""
Write-Host "Para testar localmente, adicione ao seu .env ou application-local.properties:" -ForegroundColor Blue
Write-Host "JWT_SECRET=$jwtSecret" -ForegroundColor Gray

# Copiar para clipboard se disponível
try {
    $jwtSecret | Set-Clipboard
    Write-Host ""
    Write-Host "✓ JWT Secret copiado para a área de transferência!" -ForegroundColor Green
} catch {
    Write-Host ""
    Write-Host "Copie manualmente o valor acima." -ForegroundColor Yellow
}
