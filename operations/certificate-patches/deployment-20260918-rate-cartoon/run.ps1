$stage='D:\deploy\rate-cartoon-search-20260918'
$zip=Join-Path $stage 'rate-cartoon-search.zip'
if((Get-FileHash $zip -Algorithm SHA256).Hash -ne '685e29d3288f680710aabc7e1ff577c64977385aae0dec6b7c09b557dc737f73'){throw 'Upload hash mismatch'}
if(Test-Path (Join-Path $stage 'payload')){throw 'Payload exists; inspect first'}
Expand-Archive -LiteralPath $zip -DestinationPath $stage
$script=Join-Path $stage 'deploy.ps1'
if((Get-FileHash $script -Algorithm SHA256).Hash -ne '470946d8bd759315e5e4218c1cafba78d59c204406b5226a5bc51a5f99cb9748'){throw 'Deploy script hash mismatch'}
& $script
