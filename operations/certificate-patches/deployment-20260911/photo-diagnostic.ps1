$ErrorActionPreference='Stop'
Get-ChildItem 'D:\yishun-private\grading' | ForEach-Object {Write-Output ($_.Name+' bytes='+$_.Length)}
foreach($id in @('7b9747f0-0df9-4818-87ec-af61062c28cc','5695e15b-147d-461b-b941-20fe1b7904f3')) {
 $file='D:\yishun-private\grading\'+$id+'.jpg'
 Write-Output ($id+' EXISTS='+(Test-Path $file))
 if(Test-Path ($file+'.owner')){Write-Output ('OWNER_MATCH='+([IO.File]::ReadAllText($file+'.owner') -eq 'codexadmin'))}
}
Get-CimInstance Win32_Process -Filter "name='java.exe'" | ForEach-Object {Write-Output $_.CommandLine}
Write-Output 'PHOTO_DIAGNOSTIC_DONE'
