$ProgressPreference='SilentlyContinue'
Add-Type -AssemblyName System.IO.Compression.FileSystem
$stage='D:\deploy\rate-cartoon-search-20260918'
if(Test-Path $stage){throw 'Staging exists; inspect before reuse'}
New-Item -ItemType Directory $stage | Out-Null
$jar='D:\project\yishun-0.0.1-SNAPSHOT.jar'
$zip=[IO.Compression.ZipFile]::OpenRead($jar)
$output=[IO.Compression.ZipFile]::Open((Join-Path $stage 'baseline-classes.zip'),'Create')
try {
 foreach($type in @('Rate','Cartoon')) {
  foreach($name in @("dao/${type}Dao","service/impl/${type}ServiceImpl","controller/admin/${type}Controller")) {
   $path='BOOT-INF/classes/com/kiss/yishun/'+$name+'.class'
   $entry=$zip.GetEntry($path); if(!$entry){throw "Missing $path"}
   $target=$output.CreateEntry($path); $inputStream=$entry.Open();$outputStream=$target.Open()
   try{$inputStream.CopyTo($outputStream)}finally{$inputStream.Dispose();$outputStream.Dispose()}
  }
 }
}finally{$output.Dispose();$zip.Dispose()}
Get-FileHash $jar,'D:\dist\index.html','D:\dist\client.precious-20260918.js' | Format-List Path,Hash
Get-CimInstance Win32_Process -Filter "Name='java.exe'" | Select-Object ProcessId,ExecutablePath,CommandLine | Format-List
