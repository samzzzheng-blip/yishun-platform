Get-CimInstance Win32_Process | Where-Object {$_.Name -eq 'java.exe'} | Select-Object ProcessId,ExecutablePath,CommandLine | ConvertTo-Json
Get-FileHash D:\project\yishun-0.0.1-SNAPSHOT.jar,D:\dist\index.html,D:\dist\client.search-20260918.js
Get-ChildItem D:\deploy -Directory | Select-Object -Last 10 Name
