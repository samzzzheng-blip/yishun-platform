$stage='D:\deploy\precious-search-20260918'
if((Get-FileHash "$stage\precious-search.zip").Hash -ne '8b02b511b8ce9eddcdae28905335968e2a6aad90c5f45efc763dfbce3134c91b'){throw 'Package hash mismatch'}
if((Get-FileHash "$stage\deploy.ps1").Hash -ne 'fb8e38702f099db251cef73be65f4b1aa62c753ab46e957776455b8f2d98326b'){throw 'Script hash mismatch'}
$tokens=$null;$errors=$null
[Management.Automation.Language.Parser]::ParseFile("$stage\deploy.ps1",[ref]$tokens,[ref]$errors) | Out-Null
if($errors.Count){$errors | Format-List;throw 'PowerShell parse errors'}
Expand-Archive "$stage\precious-search.zip" $stage -ErrorAction Stop
Write-Output 'UPLOAD_HASH_AND_SYNTAX_OK'
