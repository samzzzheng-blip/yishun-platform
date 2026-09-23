$ErrorActionPreference='Stop'
$work='D:\deploy\grading-workflow-20260911'
if(Test-Path $work){throw 'Release directory already exists; inspect before retry'}
New-Item -ItemType Directory "$work\backup" | Out-Null
Add-Type -AssemblyName System.IO.Compression.FileSystem
$jar='D:\project\yishun-0.0.1-SNAPSHOT.jar'
Copy-Item $jar "$work\backup\application.jar"
Copy-Item 'D:\dist' "$work\backup\dist" -Recurse
$archive=[IO.Compression.ZipFile]::OpenRead($jar)
try {
 $reader=New-Object IO.StreamReader($archive.GetEntry('BOOT-INF/classes/application-prod.yml').Open())
 $config=$reader.ReadToEnd();$reader.Dispose()
} finally {$archive.Dispose()}
$jdbc=[regex]::Match($config,'(?m)^\s*url:\s*jdbc:mysql://([^:/]+):(\d+)/([^?\s]+)').Groups
if($jdbc.Count -ne 4){throw 'Cannot parse database connection'}
$dbUser=[regex]::Match($config,'(?m)^\s*username:\s*(.+)$').Groups[1].Value.Trim().Trim('"',"'")
$env:MYSQL_PWD=[regex]::Match($config,'(?m)^\s*password:\s*(.+)$').Groups[1].Value.Trim().Trim('"',"'")
try {
 & 'D:\mysql-8.0.19-winx64\bin\mysqldump.exe' -h $jdbc[1].Value -P $jdbc[2].Value -u $dbUser --lock-all-tables --routines --events --triggers --hex-blob --set-gtid-purged=OFF --default-character-set=utf8mb4 "--result-file=$work\backup\database.sql" $jdbc[3].Value
 if($LASTEXITCODE -ne 0){throw 'Database backup failed'}
} finally {Remove-Item Env:MYSQL_PWD; $config=$null}
if((Get-Item "$work\backup\database.sql").Length -lt 1000){throw 'Database backup unexpectedly small'}
Get-ChildItem "$work\backup" -File | ForEach-Object {Write-Output ($_.Name+' bytes='+$_.Length+' SHA256='+(Get-FileHash $_.FullName -Algorithm SHA256).Hash)}
Set-Content "$work\BACKUP_OK" (Get-Date -Format o)
Write-Output 'BACKUP_OK'
