$ProgressPreference='SilentlyContinue'
Add-Type -AssemblyName System.IO.Compression.FileSystem
$stage='D:\deploy\precious-wps-20260923'
$manifest=Get-Content "$stage\payload\manifest.json" -Raw | ConvertFrom-Json
$zip=[IO.Compression.ZipFile]::OpenRead('D:\project\yishun-0.0.1-SNAPSHOT.jar')
try{
 foreach($item in $manifest.files){
  $entry=$zip.GetEntry($item.path);if(!$entry){throw 'Missing deployed class'}
  $s=$entry.Open();$sha=[Security.Cryptography.SHA256]::Create()
  try{$hash=[BitConverter]::ToString($sha.ComputeHash($s)).Replace('-','').ToLowerInvariant()}finally{$sha.Dispose();$s.Dispose()}
  if($hash -ne $item.sha256){throw 'Deployed class hash mismatch'}
 }
}finally{$zip.Dispose()}
if((Get-FileHash 'D:\dist\index.html').Hash.ToLowerInvariant() -ne $manifest.index){throw 'Unexpected frontend change'}
$health=Invoke-RestMethod 'http://127.0.0.1:8081/api/usr/queryPreciousList?page=1&pageSize=1' -TimeoutSec 5
if($health.code -ne 401){throw 'Health/authentication check failed'}
Write-Output 'PASS: WPS deployed classes verified; frontend unchanged; backend health/authentication preserved'
Get-FileHash 'D:\project\yishun-0.0.1-SNAPSHOT.jar'
