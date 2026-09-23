$ErrorActionPreference='Stop'
$ProgressPreference='SilentlyContinue'
$stage='D:\deploy\rate-cartoon-search-20260918'
$liveJar='D:\project\yishun-0.0.1-SNAPSHOT.jar'
$liveIndex='D:\dist\index.html'
$newClient='D:\dist\client.search-20260918.js'
$java='D:\deploy\java8-ready-20260914-153727\jre8\bin\java.exe'
$backup=Join-Path $stage 'backup'
$ready=Join-Path $stage 'application.ready.jar'
function Hash($path){(Get-FileHash -LiteralPath $path -Algorithm SHA256).Hash.ToLowerInvariant()}
function AppProcesses {
  @(Get-CimInstance Win32_Process -Filter "Name='java.exe'" | Where-Object { $_.CommandLine -like '*-jar D:\project\yishun-0.0.1-SNAPSHOT.jar *' })
}
function Healthy {
  try { $r=Invoke-RestMethod 'http://127.0.0.1:8081/api/usr/queryPreciousList?page=1&pageSize=1' -TimeoutSec 3; return $r.code -eq 401 } catch {return $false}
}
function Launch($tag) {
  $log=Join-Path $stage ($tag+'.log')
  $command='C:\Windows\System32\cmd.exe /d /s /c ""'+$java+'" -Xms512m -Xmx1024m -Dfile.encoding=UTF-8 -Djava.io.tmpdir=D:\project\tmp -jar D:\project\yishun-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod --spring.jpa.hibernate.ddl-auto=none --grading.private-photo-dir=D:\yishun-private\grading >"'+$log+'" 2>&1"'
  $created=Invoke-CimMethod -ClassName Win32_Process -MethodName Create -Arguments @{CommandLine=$command;CurrentDirectory='D:\project'}
  if($created.ReturnValue -ne 0){throw "Launch failed: $($created.ReturnValue)"}
  for($i=0;$i -lt 60;$i++) {
    Start-Sleep -Seconds 2
    if(Healthy){return}
  }
  Get-Content $log -Tail 50 -ErrorAction SilentlyContinue
  throw 'Application did not pass health check within 120 seconds.'
}
$manifest=Get-Content (Join-Path $stage 'payload\manifest.json') -Raw | ConvertFrom-Json
if((Test-Path $newClient) -and (Hash $newClient) -ne ($manifest.files | Where-Object path -eq 'frontend/client.search-20260918.js').sha256){throw 'New frontend filename exists with unexpected contents.'}
foreach($item in $manifest.files) {
  $file=Join-Path (Join-Path $stage 'payload') $item.path
  if((Hash $file) -ne $item.sha256){throw "Payload hash mismatch: $($item.path)"}
}
if((Hash $liveJar) -ne $manifest.baseline.jar){throw 'Online JAR changed since inspection.'}
if((Hash $liveIndex) -ne $manifest.baseline.index){throw 'Online index changed since inspection.'}
if((Hash 'D:\dist\client.precious-20260918.js') -ne $manifest.baseline.client){throw 'Online frontend changed since inspection.'}
if(!(Test-Path $java)){throw 'Java missing; application left running.'}
$check=Start-Process -FilePath $java -ArgumentList '-version' -Wait -PassThru -RedirectStandardOutput (Join-Path $stage 'java-version.out') -RedirectStandardError (Join-Path $stage 'java-version.err')
if($check.ExitCode -ne 0){throw 'Java validation failed; application left running.'}
$running=@(AppProcesses)
if($running.Count -ne 1 -or $running[0].ExecutablePath -ne $java){throw 'Unexpected application process; stop for review.'}
if(!(Healthy)){throw 'Current application unhealthy; stop for review.'}
$running | Select-Object ProcessId,ExecutablePath,CommandLine | ConvertTo-Json | Set-Content (Join-Path $stage 'process-before.json') -Encoding UTF8
if(!(Test-Path $backup)) {
  New-Item -ItemType Directory $backup | Out-Null
  Copy-Item $liveJar (Join-Path $backup 'application.jar')
  Copy-Item $liveIndex (Join-Path $backup 'index.html')
  Copy-Item 'D:\dist\client.precious-20260918.js' (Join-Path $backup 'client.precious-20260918.js')
}
if((Hash (Join-Path $backup 'application.jar')) -ne $manifest.baseline.jar -or (Hash (Join-Path $backup 'index.html')) -ne $manifest.baseline.index){throw 'Backup validation failed.'}
Copy-Item $liveJar $ready
Add-Type -AssemblyName System.IO.Compression.FileSystem
$zip=[IO.Compression.ZipFile]::Open($ready,'Update')
try {
  foreach($item in $manifest.files | Where-Object {$_.path -like 'BOOT-INF/*'}) {
    $old=$zip.GetEntry($item.path)
    if(!$old){throw "Missing original class $($item.path)"}
    $old.Delete()
    [IO.Compression.ZipFileExtensions]::CreateEntryFromFile($zip,(Join-Path (Join-Path $stage 'payload') $item.path),$item.path,[IO.Compression.CompressionLevel]::Optimal) | Out-Null
  }
}finally{$zip.Dispose()}
# Verify every archive entry. Only the six declared classes may differ.
$before=[IO.Compression.ZipFile]::OpenRead((Join-Path $backup 'application.jar'))
$after=[IO.Compression.ZipFile]::OpenRead($ready)
function EntryHash($entry){$stream=$entry.Open();$sha=[Security.Cryptography.SHA256]::Create();try{[BitConverter]::ToString($sha.ComputeHash($stream)).Replace('-','').ToLowerInvariant()}finally{$sha.Dispose();$stream.Dispose()}}
try {
  if($before.Entries.Count -ne $after.Entries.Count){throw 'Archive entry count differs.'}
  foreach($entry in $before.Entries) {
    $next=$after.GetEntry($entry.FullName)
    if(!$next){throw "Missing archive entry $($entry.FullName)"}
    $patch=@($manifest.files | Where-Object path -eq $entry.FullName)
    $expected=if($patch.Count -eq 1){$patch[0].sha256}else{EntryHash $entry}
    if((EntryHash $next) -ne $expected){throw "Unexpected archive difference: $($entry.FullName)"}
  }
}finally{$before.Dispose();$after.Dispose()}
$readyHash=Hash $ready
Write-Output 'BACKUP_AND_STAGED_JAR_VERIFIED'
$stopped=$false
try {
  $current=@(AppProcesses)
  if($current.Count -ne 1 -or $current[0].ProcessId -ne $running[0].ProcessId){throw 'Application process changed before cutover.'}
  $stopped=$true
  Stop-Process -Id $current[0].ProcessId -ErrorAction Stop
  for($i=0;$i -lt 20 -and @(AppProcesses).Count -gt 0;$i++){Start-Sleep -Milliseconds 500}
  if(@(AppProcesses).Count -gt 0){throw 'Application did not stop.'}
  Copy-Item $ready $liveJar -Force
  if((Hash $liveJar) -ne $readyHash){throw 'Installed JAR hash mismatch.'}
  Launch 'application-new'
  Copy-Item (Join-Path $stage 'payload\frontend\client.search-20260918.js') $newClient
  Copy-Item (Join-Path $stage 'payload\frontend\index.html') 'D:\dist\index.search-20260918.pending'
  [IO.File]::Replace('D:\dist\index.search-20260918.pending',$liveIndex,(Join-Path $stage ('index-before-switch-'+[guid]::NewGuid().ToString('N')+'.html')))
  if((Hash $newClient) -ne ($manifest.files | Where-Object path -eq 'frontend/client.search-20260918.js').sha256){throw 'Installed frontend hash mismatch.'}
  if(!(Healthy)){throw 'Final health check failed.'}
  Write-Output 'DEPLOY_OK'
  Write-Output "BACKUP=$backup"
  Write-Output "JAR_SHA256=$readyHash"
  AppProcesses | Select-Object ProcessId,ExecutablePath | Format-List
}catch {
  $failure=$_
  if($stopped) {
    Write-Warning 'Deployment failed. Restoring verified original program and frontend.'
    Copy-Item (Join-Path $backup 'index.html') $liveIndex -Force
    AppProcesses | ForEach-Object {Stop-Process -Id $_.ProcessId -ErrorAction Stop}
    Start-Sleep -Seconds 2
    Copy-Item (Join-Path $backup 'application.jar') $liveJar -Force
    if((Hash $liveJar) -ne $manifest.baseline.jar){throw 'Rollback JAR hash mismatch; manual recovery required.'}
    Launch 'application-rollback'
    Write-Output 'ROLLBACK_OK'
  }
  throw $failure
}
