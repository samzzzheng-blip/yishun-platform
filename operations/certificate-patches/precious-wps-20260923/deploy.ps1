$ErrorActionPreference='Stop'
$ProgressPreference='SilentlyContinue'
$stage='D:\deploy\precious-wps-20260923'
$jar='D:\project\yishun-0.0.1-SNAPSHOT.jar'
$index='D:\dist\index.html'
$java='D:\deploy\java8-ready-20260914-153727\jre8\bin\java.exe'
$backup=Join-Path $stage 'backup'
$ready=Join-Path $stage 'ready.jar'
function Hash($p){(Get-FileHash -LiteralPath $p -Algorithm SHA256).Hash.ToLowerInvariant()}
function App { @(Get-CimInstance Win32_Process | Where-Object {$_.Name -eq 'java.exe' -and $_.CommandLine -like '*-jar D:\project\yishun-0.0.1-SNAPSHOT.jar *'}) }
function Healthy {try{$r=Invoke-RestMethod 'http://127.0.0.1:8081/api/usr/queryPreciousList?page=1&pageSize=1' -TimeoutSec 3;return $r.code -eq 401}catch{return $false}}
function Launch($tag){
 $log=Join-Path $stage ($tag+'.log')
 $cmd='C:\Windows\System32\cmd.exe /d /s /c ""'+$java+'" -Xms512m -Xmx1024m -Dfile.encoding=UTF-8 -Djava.io.tmpdir=D:\project\tmp -jar D:\project\yishun-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod --spring.jpa.hibernate.ddl-auto=none --grading.private-photo-dir=D:\yishun-private\grading >"'+$log+'" 2>&1"'
 $r=Invoke-CimMethod -ClassName Win32_Process -MethodName Create -Arguments @{CommandLine=$cmd;CurrentDirectory='D:\project'}
 if($r.ReturnValue -ne 0){throw 'Launch failed'}
 for($i=0;$i -lt 60;$i++){Start-Sleep -Seconds 2;if(Healthy){return}}
 throw 'Health check failed'
}
$manifest=Get-Content (Join-Path $stage 'payload\manifest.json') -Raw | ConvertFrom-Json
if((Hash $jar) -ne $manifest.jar -or (Hash $index) -ne $manifest.index -or (Hash 'D:\dist\client.search-20260918.js') -ne $manifest.client){throw 'Baseline changed; not deploying'}
foreach($item in $manifest.files){if((Hash (Join-Path "$stage\payload" $item.path)) -ne $item.sha256){throw 'Payload hash mismatch'}}
$running=@(App)
if($running.Count -ne 1 -or $running[0].ExecutablePath -ne $java -or !(Healthy)){throw 'Unexpected application state'}
if(Test-Path $backup){throw 'Backup already exists; do not rerun blindly'}
New-Item -ItemType Directory $backup | Out-Null
Copy-Item $jar "$backup\application.jar"
Copy-Item $index "$backup\index.html"
Copy-Item 'D:\dist\client.search-20260918.js' "$backup\client.search-20260918.js"
if((Hash "$backup\application.jar") -ne $manifest.jar -or (Hash "$backup\index.html") -ne $manifest.index){throw 'Backup failed validation'}
$running | ConvertTo-Json | Set-Content "$backup\process.json" -Encoding UTF8
Copy-Item $jar $ready
Add-Type -AssemblyName System.IO.Compression.FileSystem
$zip=[IO.Compression.ZipFile]::Open($ready,'Update')
try{foreach($item in $manifest.files | Where-Object {$_.path -like 'BOOT-INF/*'}){
 $old=$zip.GetEntry($item.path);if($old){$old.Delete()}
 [IO.Compression.ZipFileExtensions]::CreateEntryFromFile($zip,(Join-Path "$stage\payload" $item.path),$item.path,[IO.Compression.CompressionLevel]::Optimal)|Out-Null
}}finally{$zip.Dispose()}
function EntryHash($entry){$s=$entry.Open();$h=[Security.Cryptography.SHA256]::Create();try{[BitConverter]::ToString($h.ComputeHash($s)).Replace('-','').ToLowerInvariant()}finally{$h.Dispose();$s.Dispose()}}
$before=[IO.Compression.ZipFile]::OpenRead("$backup\application.jar");$after=[IO.Compression.ZipFile]::OpenRead($ready)
try{
 foreach($e in $before.Entries){$n=$after.GetEntry($e.FullName);if(!$n){throw 'Missing archive entry'};$change=@($manifest.files|Where-Object path -eq $e.FullName);$expected=if($change.Count){$change[0].sha256}else{EntryHash $e};if((EntryHash $n) -ne $expected){throw "Unexpected archive difference $($e.FullName)"}}
 foreach($e in $after.Entries){if(!$before.GetEntry($e.FullName) -and !($manifest.files|Where-Object path -eq $e.FullName)){throw 'Undeclared new archive entry'}}
 foreach($item in $manifest.files|Where-Object {$_.path -like 'BOOT-INF/*'}){if((EntryHash $after.GetEntry($item.path)) -ne $item.sha256){throw 'Patched class mismatch'}}
}finally{$before.Dispose();$after.Dispose()}
Write-Output 'BACKUP_AND_PATCH_VERIFIED'
$stopped=$false
try{
 $current=@(App);if($current.Count -ne 1 -or $current[0].ProcessId -ne $running[0].ProcessId){throw 'Process changed'}
 $stopped=$true;Stop-Process -Id $current[0].ProcessId
 for($i=0;$i -lt 20 -and @(App).Count;$i++){Start-Sleep -Milliseconds 500}
 if(@(App).Count){throw 'Process still running'}
 Copy-Item $ready $jar -Force
 if((Hash $jar) -ne (Hash $ready)){throw 'Installed JAR mismatch'}
 Launch 'new'
 if(!(Healthy)){throw 'Final health check failed'}
 Write-Output 'DEPLOY_OK';Write-Output "BACKUP=$backup";Get-FileHash $jar,$index
}catch{
 $failure=$_
 if($stopped){Copy-Item "$backup\index.html" $index -Force;App|ForEach-Object {Stop-Process -Id $_.ProcessId};Start-Sleep -Seconds 2;Copy-Item "$backup\application.jar" $jar -Force;Launch 'rollback';Write-Output 'ROLLBACK_OK'}
 throw $failure
}
