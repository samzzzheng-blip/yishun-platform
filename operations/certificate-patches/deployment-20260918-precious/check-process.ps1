$list=@(Get-CimInstance Win32_Process -Filter "Name='java.exe'")
$list | ForEach-Object { [pscustomobject]@{Id=$_.ProcessId;Path=$_.ExecutablePath;Command=$_.CommandLine;Match=($_.CommandLine -like '*-jar D:\project\yishun-0.0.1-SNAPSHOT.jar *')} } | ConvertTo-Json -Depth 3
