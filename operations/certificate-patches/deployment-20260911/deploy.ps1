$ErrorActionPreference='Stop'
$work='D:\deploy\grading-workflow-20260911'
$jar='D:\project\yishun-0.0.1-SNAPSHOT.jar'
$front='D:\dist'
$upload='D:\home\usr_upload\tmp\20260911'
$backend="$upload\1789110022603_201.zip"
$frontend="$upload\1789110034283_424.zip"
$schema="$upload\1789110036458_897.sql"
function CheckHash($path,$hash) { if((Get-FileHash $path -Algorithm SHA256).Hash -ne $hash){throw "Checksum mismatch: $path"} }
CheckHash $backend '5c88d19d727c8ddc16231a4d3c0d83da48d8721d19f7c453f2eb55d21b4b58a1'
CheckHash $frontend 'e8bacfd436b838b8de91a76a4a4a35b3f12c4330311171acf7b040b7ceab57af'
CheckHash $schema '4ed57963e279c3d85c5b65e962fe5ea9eb608fbea82e336e2c2289ccaf0a4d7f'
if(!(Test-Path "$work\BACKUP_OK")){throw 'Verified backup required'}
if(Test-Path "$work\DEPLOY_OK"){throw 'Already deployed'}
CheckHash $jar (Get-FileHash "$work\backup\application.jar" -Algorithm SHA256).Hash
$processes=@(Get-CimInstance Win32_Process -Filter "name='java.exe'" | Where-Object {$_.CommandLine -like '*-jar D:\project\yishun-0.0.1-SNAPSHOT.jar*'})
if($processes.Count -ne 1){throw 'Cannot identify exact application process'}
$oldProcess=$processes[0]
$java=$oldProcess.ExecutablePath
Add-Type -AssemblyName System.IO.Compression.FileSystem
$archive=[IO.Compression.ZipFile]::OpenRead($jar)
try {
 $reader=New-Object IO.StreamReader($archive.GetEntry('BOOT-INF/classes/application-prod.yml').Open())
 $config=$reader.ReadToEnd();$reader.Dispose()
} finally {$archive.Dispose()}
$jdbc=[regex]::Match($config,'(?m)^\s*url:\s*jdbc:mysql://([^:/]+):(\d+)/([^?\s]+)').Groups
if($jdbc.Count -ne 4){throw 'Cannot parse database connection'}
$dbUser=[regex]::Match($config,'(?m)^\s*username:\s*(.+)$').Groups[1].Value.Trim().Trim('"',"'")
$env:MYSQL_PWD=[regex]::Match($config,'(?m)^\s*password:\s*(.+)$').Groups[1].Value.Trim().Trim('"',"'")
$config=$null
$dbArgs=@('-h',$jdbc[1].Value,'-P',$jdbc[2].Value,'-u',$dbUser,'--batch','--skip-column-names','--default-character-set=utf8mb4',$jdbc[3].Value)
function Query($sql) {
 $result=& 'D:\mysql-8.0.19-winx64\bin\mysql.exe' @dbArgs -e $sql
 if($LASTEXITCODE -ne 0){throw 'Database command failed'}
 return $result
}
$stopped=$false;$newProcess=$null
$arguments=@('-Xms512m','-Xmx1024m','-Dfile.encoding=UTF-8','-Djava.io.tmpdir=D:\project\tmp','-jar',$jar,'--spring.profiles.active=prod','--spring.jpa.hibernate.ddl-auto=none','--grading.private-photo-dir=D:\yishun-private\grading')
try {
 if([int](Query "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema=DATABASE() AND table_name LIKE 'grading_%'") -ne 0){throw 'Unexpected existing workflow tables; migration review required'}
 if([int](Query "SELECT COUNT(*) FROM (SELECT cert_number FROM rate GROUP BY cert_number HAVING COUNT(*)>1) d") -ne 0){throw 'Existing duplicate certificates; no data changed'}
 $unique=[int](Query "SELECT COUNT(*) FROM (SELECT index_name FROM information_schema.statistics WHERE table_schema=DATABASE() AND table_name='rate' GROUP BY index_name HAVING MIN(non_unique)=0 AND GROUP_CONCAT(column_name ORDER BY seq_in_index)='cert_number') i")
 if($unique -lt 1){throw 'Certificate unique index missing; review required'}
 Expand-Archive $backend "$work\backend" -Force
 Expand-Archive $frontend "$work\frontend" -Force
 Copy-Item $jar "$work\new-application.jar" -Force
 $archive=[IO.Compression.ZipFile]::Open("$work\new-application.jar",[IO.Compression.ZipArchiveMode]::Update)
 try {
  Get-ChildItem "$work\backend\BOOT-INF\classes" -Recurse -File | ForEach-Object {
   $entry=$_.FullName.Substring(("$work\backend\").Length).Replace('\','/')
   if(!$entry.EndsWith('.class') -or $entry -notlike 'BOOT-INF/classes/com/kiss/yishun/*'){throw 'Unexpected patch content'}
   $existing=$archive.GetEntry($entry);if($null -ne $existing){$existing.Delete()}
   [IO.Compression.ZipFileExtensions]::CreateEntryFromFile($archive,$_.FullName,$entry,[IO.Compression.CompressionLevel]::Optimal)|Out-Null
  }
 } finally {$archive.Dispose()}
 Stop-Process -Id $oldProcess.ProcessId -ErrorAction Stop
 $stopped=$true
 Wait-Process -Id $oldProcess.ProcessId -Timeout 30 -ErrorAction SilentlyContinue
 $countsSql="SELECT 'rate',COUNT(*) FROM rate UNION ALL SELECT 'precious',COUNT(*) FROM precious UNION ALL SELECT 'cartoon',COUNT(*) FROM cartoon UNION ALL SELECT 'user',COUNT(*) FROM user"
 $before=@(Query $countsSql); $before | Set-Content "$work\counts-before.txt"
 # Take an additional consistent backup after stopping writes; preserve the first backup as well.
 & 'D:\mysql-8.0.19-winx64\bin\mysqldump.exe' -h $jdbc[1].Value -P $jdbc[2].Value -u $dbUser --lock-all-tables --routines --events --triggers --hex-blob --set-gtid-purged=OFF --default-character-set=utf8mb4 "--result-file=$work\backup\database-at-cutover.sql" $jdbc[3].Value
 if($LASTEXITCODE -ne 0){throw 'Cutover backup failed'}
 Query (Get-Content $schema -Raw) | Out-Null
 $after=@(Query $countsSql); $after | Set-Content "$work\counts-after.txt"
 if(($before -join "`n") -ne ($after -join "`n")){throw 'Business record counts changed'}
 $empty=[long](Query "SELECT (SELECT COUNT(*) FROM grading_job)+(SELECT COUNT(*) FROM grading_event)+(SELECT COUNT(*) FROM grading_label_template)+(SELECT COUNT(*) FROM grading_batch_template)+(SELECT COUNT(*) FROM grading_batch_sequence)+(SELECT COUNT(*) FROM grading_batch_day)")
 if($empty -ne 0){throw 'Workflow tables must be empty'}
 Copy-Item "$work\new-application.jar" $jar -Force
 $newProcess=Start-Process $java -ArgumentList $arguments -WorkingDirectory 'D:\project' -WindowStyle Hidden -RedirectStandardOutput "$work\application.out.log" -RedirectStandardError "$work\application.err.log" -PassThru
 $healthy=$false
 for($i=0;$i -lt 36;$i++) {
  Start-Sleep -Seconds 5
  if($newProcess.HasExited){throw 'Application exited during startup'}
  try {
   $r=Invoke-RestMethod 'http://127.0.0.1:8081/api/base/login' -Method Post -ContentType 'application/json' -Body '{"username":"__deployment_probe__","password":"invalid"}' -TimeoutSec 5
   if($r.code -eq -1 -and $r.msg){$healthy=$true;break}
  } catch {}
 }
 if(!$healthy){throw 'Application health check failed'}
 $protected=Invoke-RestMethod 'http://127.0.0.1:8081/api/usr/gradingJobs' -TimeoutSec 10
 if($protected.code -ne 401){throw 'Workflow authentication check failed'}
 # Copy assets first and switch index last. Retain old assets for rollback/cached pages.
 Get-ChildItem "$work\frontend" | Where-Object {$_.Name -ne 'index.html'} | Copy-Item -Destination $front -Recurse -Force
 Copy-Item "$work\frontend\index.html" "$front\index.html" -Force
 $after | ForEach-Object {Write-Output $_}
 Write-Output ('WORKFLOW_ROWS='+$empty)
 Set-Content "$work\DEPLOY_OK" (Get-Date -Format o)
 Write-Output ('DEPLOY_OK PID='+$newProcess.Id)
} catch {
 if($stopped) {
  if($null -ne $newProcess -and !$newProcess.HasExited){Stop-Process -Id $newProcess.Id -ErrorAction SilentlyContinue;Start-Sleep -Seconds 2}
  Copy-Item "$work\backup\application.jar" $jar -Force
  Copy-Item "$work\backup\dist\*" $front -Recurse -Force
  Start-Process $java -ArgumentList @('-Xms512m','-Xmx1024m','-Dfile.encoding=UTF-8','-Djava.io.tmpdir=D:\project\tmp','-jar',$jar,'--spring.profiles.active=prod') -WorkingDirectory 'D:\project' -WindowStyle Hidden -RedirectStandardOutput "$work\rollback.out.log" -RedirectStandardError "$work\rollback.err.log"
  Write-Output 'CODE_ROLLED_BACK; database backups preserved; inspect any completed additive schema changes.'
 }
 throw
} finally {Remove-Item Env:MYSQL_PWD -ErrorAction SilentlyContinue}
