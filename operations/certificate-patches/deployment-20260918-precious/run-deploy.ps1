$file='D:\deploy\precious-search-20260918\deploy.ps1'
if((Get-FileHash $file).Hash -ne '265e4cb708265e5f1ead5911be72181b1e0ddc3321644b5334ee9eca82599995'){throw 'Deployment script hash mismatch'}
& $file
