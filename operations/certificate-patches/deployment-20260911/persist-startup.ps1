$ErrorActionPreference='Stop'
$path='D:\project\start-yishun.bat'
$backup='D:\deploy\grading-workflow-20260911\backup\start-yishun.bat'
if(Test-Path $backup){throw 'Startup script already backed up; inspect before retry'}
$encoding=[Text.Encoding]::GetEncoding(28591)
$text=[IO.File]::ReadAllText($path,$encoding)
$old='--spring.profiles.active=prod'
if(([regex]::Matches($text,[regex]::Escape($old))).Count -ne 1){throw 'Unexpected startup command'}
Copy-Item $path $backup
$new=$old+' --spring.jpa.hibernate.ddl-auto=none --grading.private-photo-dir=D:\yishun-private\grading'
[IO.File]::WriteAllText($path,$text.Replace($old,$new),$encoding)
Write-Output 'STARTUP_CONFIG_OK'
