param(
    [string]$OutputPath = "BloodSUCKER-signing.pfx",
    [string]$Password = "change-me"
)

$cert = New-SelfSignedCertificate `
    -Type CodeSigningCert `
    -Subject "CN=BloodSUCKER, O=BloodSUCKER, C=RU" `
    -KeyAlgorithm RSA `
    -KeyLength 4096 `
    -HashAlgorithm SHA256 `
    -KeyUsage DigitalSignature `
    -CertStoreLocation "Cert:\CurrentUser\My" `
    -NotAfter (Get-Date).AddYears(5) `
    -FriendlyName "BloodSUCKER Code Signing"

$secure = ConvertTo-SecureString -String $Password -Force -AsPlainText
Export-PfxCertificate -Cert $cert -FilePath $OutputPath -Password $secure | Out-Null

Write-Host "Certificate created: $OutputPath"
Write-Host "Thumbprint: $($cert.Thumbprint)"
Write-Host ""
Write-Host "For GitHub Actions add secrets:"
Write-Host "  WINDOWS_SIGNING_CERT = base64 of the pfx file"
Write-Host "  WINDOWS_SIGNING_PASSWORD = certificate password"
Write-Host ""
Write-Host "PowerShell to encode:"
Write-Host "  [Convert]::ToBase64String([IO.File]::ReadAllBytes('$OutputPath'))"
