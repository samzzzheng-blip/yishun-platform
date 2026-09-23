$ErrorActionPreference='Stop'
Add-Type -AssemblyName System.IO.Compression.FileSystem
$jar='D:\project\yishun-0.0.1-SNAPSHOT.jar'
$archive=[IO.Compression.ZipFile]::OpenRead($jar)
try {
  $reader=New-Object IO.StreamReader($archive.GetEntry('BOOT-INF/classes/application-prod.yml').Open())
  $config=$reader.ReadToEnd(); $reader.Dispose()
  $archive.Entries | Where-Object { $_.FullName -match 'BOOT-INF/lib/(mysql|fastjson|spring-boot-[0-9])' } | ForEach-Object { Write-Output $_.FullName }
  Write-Output ('EXISTING_GRADING_CLASSES='+@($archive.Entries | Where-Object {$_.FullName -match '/Grading.*class$'}).Count)
} finally {$archive.Dispose()}
$jdbc=[regex]::Match($config,'(?m)^\s*url:\s*jdbc:mysql://([^:/]+):(\d+)/([^?\s]+)').Groups
if($jdbc.Count -ne 4){throw 'Cannot parse database connection'}
$dbUser=[regex]::Match($config,'(?m)^\s*username:\s*(.+)$').Groups[1].Value.Trim().Trim('"',"'")
$dbPassword=[regex]::Match($config,'(?m)^\s*password:\s*(.+)$').Groups[1].Value.Trim().Trim('"',"'")
$dbArgs=@('-h',$jdbc[1].Value,'-P',$jdbc[2].Value,'-u',$dbUser,'--batch','--skip-column-names',$jdbc[3].Value)
$env:MYSQL_PWD=$dbPassword
try {
  & 'D:\mysql-8.0.19-winx64\bin\mysql.exe' @dbArgs -e "SELECT VERSION(); SELECT table_name,engine FROM information_schema.tables WHERE table_schema=DATABASE() AND (table_name LIKE 'grading_%' OR table_name IN ('rate','precious','cartoon','user'));"
  if($LASTEXITCODE -ne 0){throw 'Database inspection failed'}
} finally {Remove-Item Env:MYSQL_PWD; $dbPassword=$null; $config=$null}
Write-Output ('FREE_BYTES='+(Get-PSDrive D).Free)
Write-Output ('JAR_SHA256='+(Get-FileHash $jar -Algorithm SHA256).Hash)
Write-Output 'PREFLIGHT_OK'
