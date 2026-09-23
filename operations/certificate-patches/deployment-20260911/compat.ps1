$ErrorActionPreference='Stop'
$work='D:\deploy\grading-workflow-20260911'
$jar='D:\project\yishun-0.0.1-SNAPSHOT.jar'
$patch='D:\home\usr_upload\tmp\20260911\1789110729361_998.zip'
if((Get-FileHash $patch -Algorithm SHA256).Hash -ne '6d55f337f35f1b026ec2b57c2f28228fb4b874a2dbfcac0f043c560dfa3af7d5'){throw 'Checksum mismatch'}
if(!(Test-Path "$work\DEPLOY_OK")){throw 'Initial deployment not verified'}
$processes=@(Get-CimInstance Win32_Process -Filter "name='java.exe'" | Where-Object {$_.CommandLine -like '*-jar D:\project\yishun-0.0.1-SNAPSHOT.jar*'})
if($processes.Count -ne 1){throw 'Application process ambiguous'}
$old=$processes[0];$java=$old.ExecutablePath
Copy-Item $jar "$work\backup\before-compat.jar"
Copy-Item $jar "$work\compat.jar"
Expand-Archive $patch "$work\compat" -Force
Add-Type -AssemblyName System.IO.Compression.FileSystem
$a=[IO.Compression.ZipFile]::Open("$work\compat.jar",[IO.Compression.ZipArchiveMode]::Update)
try {
 $name='BOOT-INF/classes/com/kiss/yishun/controller/admin/GradingWorkflowController.class'
 $a.GetEntry($name).Delete()
 [IO.Compression.ZipFileExtensions]::CreateEntryFromFile($a,"$work\compat\$name",$name,[IO.Compression.CompressionLevel]::Optimal)|Out-Null
}finally{$a.Dispose()}
$arguments=@('-Xms512m','-Xmx1024m','-Dfile.encoding=UTF-8','-Djava.io.tmpdir=D:\project\tmp','-jar',$jar,'--spring.profiles.active=prod','--spring.jpa.hibernate.ddl-auto=none','--grading.private-photo-dir=D:\yishun-private\grading')
$new=$null
Stop-Process -Id $old.ProcessId
Wait-Process -Id $old.ProcessId -Timeout 30 -ErrorAction SilentlyContinue
try {
 Copy-Item "$work\compat.jar" $jar -Force
 $new=Start-Process $java -ArgumentList $arguments -WorkingDirectory 'D:\project' -WindowStyle Hidden -RedirectStandardOutput "$work\compat.out.log" -RedirectStandardError "$work\compat.err.log" -PassThru
 $ok=$false
 for($i=0;$i -lt 36;$i++){
  Start-Sleep -Seconds 5
  if($new.HasExited){throw 'Application exited'}
  try {$r=Invoke-RestMethod 'http://127.0.0.1:8081/api/base/login' -Method Post -ContentType 'application/json' -Body '{"username":"__deployment_probe__","password":"invalid"}' -TimeoutSec 5;if($r.code -eq -1 -and $r.msg){$ok=$true;break}}catch{}
 }
 if(!$ok){throw 'Health check failed'}
 Set-Content "$work\COMPAT_OK" (Get-Date -Format o)
 Write-Output ('COMPAT_OK PID='+$new.Id)
}catch{
 if($new -and !$new.HasExited){Stop-Process -Id $new.Id;Start-Sleep -Seconds 2}
 Copy-Item "$work\backup\before-compat.jar" $jar -Force
 Start-Process $java -ArgumentList $arguments -WorkingDirectory 'D:\project' -WindowStyle Hidden -RedirectStandardOutput "$work\compat-rollback.out.log" -RedirectStandardError "$work\compat-rollback.err.log"
 throw
}
