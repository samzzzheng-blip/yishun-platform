$ErrorActionPreference='Stop'
$work='D:\deploy\grading-temporary-20260911'
$jar='D:\project\yishun-0.0.1-SNAPSHOT.jar'
$patch='D:\home\usr_upload\tmp\20260911\1789117344047_634.zip'
if(Test-Path $work){throw 'Release directory already exists; do not rerun'}
if((Get-FileHash $patch -Algorithm SHA256).Hash -ne '72e1b744bc4a879dcb762de8dfdf2318ac2fdfda4909149cc9d5eca98a9dcb9d'){throw 'Checksum mismatch'}
$processes=@(Get-CimInstance Win32_Process -Filter "name='java.exe'" | Where-Object {$_.CommandLine -like '*-jar D:\project\yishun-0.0.1-SNAPSHOT.jar*'})
if($processes.Count -ne 1){throw 'Application process ambiguous'}
$old=$processes[0];$java=$old.ExecutablePath
New-Item -ItemType Directory "$work\backup" -Force|Out-Null
Copy-Item $jar "$work\backup\application.jar"
Copy-Item 'D:\dist\index.html' "$work\backup\index.html"
Copy-Item $jar "$work\patched.jar"
Expand-Archive $patch "$work\patch"
Add-Type -AssemblyName System.IO.Compression.FileSystem
$a=[IO.Compression.ZipFile]::Open("$work\patched.jar",[IO.Compression.ZipArchiveMode]::Update)
try {
 foreach($relative in @('service/GradingTemporaryService.class','service/GradingTemporaryService$Range.class','service/GradingTemporaryService$Preview.class','service/GradingWorkflowService.class','service/GradingWorkflowService$ManagePreview.class','service/GradingWorkflowService$ManageRequest.class','service/GradingBatchPublishService.class','service/GradingBatchPublishService$Row.class','service/GradingBatchPublishService$Preview.class','service/GradingTemplateService.class','controller/admin/GradingWorkflowController.class')){
  $name='BOOT-INF/classes/com/kiss/yishun/'+$relative
  $entry=$a.GetEntry($name);if($entry){$entry.Delete()}elseif(!$relative.StartsWith('service/GradingTemporaryService')){throw "Missing class $name"}
  [IO.Compression.ZipFileExtensions]::CreateEntryFromFile($a,"$work\patch\$name",$name,[IO.Compression.CompressionLevel]::Optimal)|Out-Null
 }
}finally{$a.Dispose()}
$arguments=@('-Xms512m','-Xmx1024m','-Dfile.encoding=UTF-8','-Djava.io.tmpdir=D:\project\tmp','-jar',$jar,'--spring.profiles.active=prod','--spring.jpa.hibernate.ddl-auto=none','--grading.private-photo-dir=D:\yishun-private\grading')
$new=$null
Stop-Process -Id $old.ProcessId
Wait-Process -Id $old.ProcessId -Timeout 30 -ErrorAction SilentlyContinue
try {
 Copy-Item "$work\patched.jar" $jar -Force
 $new=Start-Process $java -ArgumentList $arguments -WorkingDirectory 'D:\project' -WindowStyle Hidden -RedirectStandardOutput "$work\application.out.log" -RedirectStandardError "$work\application.err.log" -PassThru
 $ok=$false
 for($i=0;$i -lt 36;$i++){
  Start-Sleep -Seconds 5
  if($new.HasExited){throw 'Application exited'}
  try {$r=Invoke-RestMethod 'http://127.0.0.1:8081/api/base/login' -Method Post -ContentType 'application/json' -Body '{"username":"__deployment_probe__","password":"invalid"}' -TimeoutSec 5;if($r.code -eq -1 -and $r.msg){$ok=$true;break}}catch{}
 }
 if(!$ok){throw 'Health check failed'}
 foreach($asset in @('client.b066.js','common.b066.js','style.b066.css')){Copy-Item "$work\patch\frontend\$asset" "D:\dist\$asset"}
 Copy-Item "$work\patch\frontend\index.html" 'D:\dist\index.html' -Force
 Set-Content "$work\DEPLOY_OK" (Get-Date -Format o)
 Write-Output ('TEMPORARY_DEPLOY_OK PID='+$new.Id)
}catch{
 if($new -and !$new.HasExited){Stop-Process -Id $new.Id;Start-Sleep -Seconds 2}
 Copy-Item "$work\backup\application.jar" $jar -Force
 Copy-Item "$work\backup\index.html" 'D:\dist\index.html' -Force
 Start-Process $java -ArgumentList $arguments -WorkingDirectory 'D:\project' -WindowStyle Hidden -RedirectStandardOutput "$work\rollback.out.log" -RedirectStandardError "$work\rollback.err.log"
 throw
}
