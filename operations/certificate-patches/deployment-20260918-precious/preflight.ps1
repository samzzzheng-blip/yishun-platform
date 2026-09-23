Get-Item 'D:\project\yishun-0.0.1-SNAPSHOT.jar' | Select-Object Length,LastWriteTime
Get-FileHash 'D:\project\yishun-0.0.1-SNAPSHOT.jar','D:\dist\index.html','D:\dist\client.b066.js'
Get-CimInstance Win32_Process -Filter "Name='java.exe'" | Select-Object ProcessId,ExecutablePath,CommandLine | Format-List
Get-PSDrive C,D | Select-Object Name,Used,Free
Get-ChildItem 'D:\project' -File | Select-Object Name,Length
