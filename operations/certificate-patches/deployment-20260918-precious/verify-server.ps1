$ProgressPreference='SilentlyContinue'
Get-Process java | Select-Object Id,Path | Format-List
Get-FileHash 'D:\project\yishun-0.0.1-SNAPSHOT.jar','D:\dist\index.html','D:\deploy\precious-search-20260918\backup\application.jar' | Format-List Path,Hash
Get-Content 'D:\deploy\precious-search-20260918\application-new.log' -Tail 12
