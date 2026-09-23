$ErrorActionPreference='Stop'
$work='D:\deploy\temporary-import-20260914'
$jar='D:\project\yishun-0.0.1-SNAPSHOT.jar'
$front='D:\dist'
$bundle=$PSScriptRoot
if(Test-Path $work){throw 'Release directory exists. Inspect previous attempt; do not rerun.'}
$manifest=Get-Content (Join-Path $bundle 'manifest.json') -Raw|ConvertFrom-Json
foreach($item in $manifest){
 if($item.path -match '\.\.' -or $item.path.StartsWith('/')){throw 'Invalid manifest path'}
 if((Get-FileHash (Join-Path $bundle $item.path) -Algorithm SHA256).Hash -ne $item.sha256){throw ('Checksum mismatch: '+$item.path)}
}
$processes=@(Get-CimInstance Win32_Process -Filter "name='java.exe'"|Where-Object {$_.CommandLine -like '*-jar D:\project\yishun-0.0.1-SNAPSHOT.jar*'})
if($processes.Count -ne 1){throw 'Application process ambiguous'}
$old=$processes[0];$java=$old.ExecutablePath
if($old.CommandLine -notlike '*--spring.jpa.hibernate.ddl-auto=none*' -or $old.CommandLine -notlike '*--grading.private-photo-dir=D:\yishun-private\grading*'){throw 'Unexpected startup configuration; review required'}
if((Get-Content "$front\index.html" -Raw) -notmatch 'client\.b066\.js'){throw 'Production frontend differs from inspected version; review required'}
New-Item -ItemType Directory "$work\backup"|Out-Null
Copy-Item $jar "$work\backup\application.jar"
Copy-Item "$front\index.html" "$work\backup\index.html"
Copy-Item $jar "$work\patched.jar"
Get-FileHash "$work\backup\application.jar"|Format-List|Out-File "$work\backup\hash.txt"
Add-Type -AssemblyName System.IO.Compression.FileSystem
$archive=[IO.Compression.ZipFile]::Open("$work\patched.jar",[IO.Compression.ZipArchiveMode]::Update)
try{
 foreach($required in @('service/GradingTemporaryService.class','service/GradingWorkflowService.class','service/GradingPhotoStore.class','entity/GradingJob.class')){
  if(!$archive.GetEntry('BOOT-INF/classes/com/kiss/yishun/'+$required)){throw ('Missing existing workflow class: '+$required)}
 }
 foreach($item in $manifest|Where-Object {$_.path.EndsWith('.class')}){
  if($item.path -notlike 'BOOT-INF/classes/com/kiss/yishun/*'){throw 'Unexpected class'}
  $entry=$archive.GetEntry($item.path);if($entry){$entry.Delete()}
  [IO.Compression.ZipFileExtensions]::CreateEntryFromFile($archive,(Join-Path $bundle $item.path),$item.path,[IO.Compression.CompressionLevel]::Optimal)|Out-Null
 }
}finally{$archive.Dispose()}
# No database writes, configuration changes, credentials, test classes or test data.
$arguments=@('-Xms512m','-Xmx1024m','-Dfile.encoding=UTF-8','-Djava.io.tmpdir=D:\project\tmp','-jar',$jar,'--spring.profiles.active=prod','--spring.jpa.hibernate.ddl-auto=none','--grading.private-photo-dir=D:\yishun-private\grading')
$new=$null;$stopped=$false
try{
 Stop-Process -Id $old.ProcessId -ErrorAction Stop
 $stopped=$true
 Wait-Process -Id $old.ProcessId -Timeout 30 -ErrorAction SilentlyContinue
 if(Get-Process -Id $old.ProcessId -ErrorAction SilentlyContinue){throw 'Old process did not stop'}
 Copy-Item "$work\patched.jar" $jar -Force
 $new=Start-Process $java -ArgumentList $arguments -WorkingDirectory 'D:\project' -WindowStyle Hidden -RedirectStandardOutput "$work\application.out.log" -RedirectStandardError "$work\application.err.log" -PassThru
 $healthy=$false
 for($i=0;$i -lt 36;$i++){
  Start-Sleep -Seconds 5
  $new.Refresh();if($new.HasExited){throw 'Application exited'}
  try{$r=Invoke-RestMethod 'http://127.0.0.1:8081/api/base/login' -Method Post -ContentType 'application/json' -Body '{"username":"__deployment_probe__","password":"invalid"}' -TimeoutSec 5;if($r.code -eq -1 -and $r.msg){$healthy=$true;break}}catch{}
 }
 if(!$healthy){throw 'Health check failed'}
 $r=Invoke-RestMethod 'http://127.0.0.1:8081/api/usr/gradingJobs' -TimeoutSec 10
 if($r.code -ne 401){throw 'Authentication check failed'}
 foreach($item in $manifest|Where-Object {$_.path -like 'frontend/*' -and $_.path -ne 'frontend/index.html'}){
  $name=Split-Path $item.path -Leaf;$dest=Join-Path $front $name
  if((Test-Path $dest) -and (Get-FileHash $dest).Hash -ne $item.sha256){throw 'Frontend asset collision'}
  Copy-Item (Join-Path $bundle $item.path) $dest -Force
 }
 Copy-Item (Join-Path $bundle 'frontend/index.html') "$front\index.html" -Force
 Set-Content "$work\DEPLOY_OK" (Get-Date -Format o)
 Write-Output ('TEMP_IMPORT_DEPLOY_OK PID='+$new.Id)
}catch{
 $failure=$_
 if($stopped){
  if($new){$new.Refresh();if(!$new.HasExited){Stop-Process -Id $new.Id;Wait-Process -Id $new.Id -Timeout 30 -ErrorAction SilentlyContinue}}
  Copy-Item "$work\backup\application.jar" $jar -Force
  Copy-Item "$work\backup\index.html" "$front\index.html" -Force
  Start-Process $java -ArgumentList $arguments -WorkingDirectory 'D:\project' -WindowStyle Hidden -RedirectStandardOutput "$work\rollback.out.log" -RedirectStandardError "$work\rollback.err.log"
  Write-Output 'CODE_ROLLBACK_STARTED; verify service logs. Database unchanged.'
 }
 throw $failure
}
