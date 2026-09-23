$ErrorActionPreference='Stop'
$work='D:\deploy\grading-workflow-photo-copy-20260911'
$jar='D:\project\yishun-0.0.1-SNAPSHOT.jar'
$patch='D:\home\usr_upload\tmp\20260911\1789112537296_747.zip'
if(Test-Path $work){throw 'Release directory already exists; do not rerun'}
if((Get-FileHash $patch -Algorithm SHA256).Hash -ne '887232dfb152a34107fb851f90218e7c3b8995b6c0892b28740b0a0524687cb4'){throw 'Checksum mismatch'}
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
 foreach($relative in @('config/ShiroConfig.class','service/GradingWorkflowService.class','controller/admin/GradingWorkflowController.class')){
  $name='BOOT-INF/classes/com/kiss/yishun/'+$relative
  $entry=$a.GetEntry($name);if(!$entry){throw "Missing class $name"};$entry.Delete()
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
 foreach($asset in @('client.6b0a.js','common.6b0a.js','style.6b0a.css')){Copy-Item "$work\patch\frontend\$asset" "D:\dist\$asset"}
 Copy-Item "$work\patch\frontend\index.html" 'D:\dist\index.html' -Force
 Set-Content "$work\DEPLOY_OK" (Get-Date -Format o)
 Write-Output ('PHOTO_COPY_OK PID='+$new.Id)
}catch{
 if($new -and !$new.HasExited){Stop-Process -Id $new.Id;Start-Sleep -Seconds 2}
 Copy-Item "$work\backup\application.jar" $jar -Force
 Copy-Item "$work\backup\index.html" 'D:\dist\index.html' -Force
 Start-Process $java -ArgumentList $arguments -WorkingDirectory 'D:\project' -WindowStyle Hidden -RedirectStandardOutput "$work\rollback.out.log" -RedirectStandardError "$work\rollback.err.log"
 throw
}
