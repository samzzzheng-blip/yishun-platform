$ProgressPreference='SilentlyContinue'
Add-Type -AssemblyName System.IO.Compression.FileSystem
$stage='D:\deploy\precious-search-20260918'
if(Test-Path $stage){throw 'Deployment staging already exists; inspect before reuse.'}
New-Item -ItemType Directory $stage | Out-Null
$jar='D:\project\yishun-0.0.1-SNAPSHOT.jar'
$zip=[IO.Compression.ZipFile]::OpenRead($jar)
$output=[IO.Compression.ZipFile]::Open((Join-Path $stage 'baseline-classes.zip'),'Create')
try {
  foreach($name in @('dao/PreciousDao','service/impl/PreciousServiceImpl','controller/admin/PreciousController','util/PageRequestUtils')) {
    $path='BOOT-INF/classes/com/kiss/yishun/'+$name+'.class'
    $entry=$zip.GetEntry($path)
    if(!$entry){ if($name -eq 'util/PageRequestUtils'){continue}; throw "Missing $path" }
    $target=$output.CreateEntry($path)
    $inputStream=$entry.Open(); $outputStream=$target.Open()
    try{$inputStream.CopyTo($outputStream)}finally{$inputStream.Dispose();$outputStream.Dispose()}
  }
}finally{$output.Dispose();$zip.Dispose()}
Get-FileHash $jar,'D:\dist\index.html','D:\dist\client.b066.js' | Format-List Path,Hash
