$ErrorActionPreference = 'Stop'

$backendPatch = 'D:\home\usr_upload\tmp\20260903\1788414907357_955.zip'
$frontendPatch = 'D:\home\usr_upload\tmp\20260903\1788414907688_49.zip'
$jarPath = 'D:\project\yishun-0.0.1-SNAPSHOT.jar'
$frontendRoot = 'D:\dist'
$javaExe = 'C:\Program Files (x86)\Common Files\Oracle\Java\javapath\java.exe'
$workRoot = 'D:\deploy\rate-import-field-skip-20260903-1355'
$backupRoot = Join-Path $workRoot 'backup'
$backendFiles = Join-Path $workRoot 'backend-patch'

if ((Get-FileHash $backendPatch -Algorithm SHA256).Hash -ne '5F4524CDBD2831B4FDBF7217BCB579F0973DC89C1A1C4D147AE391EBCD8C2FBB') {
    throw 'Backend patch checksum mismatch.'
}
if ((Get-FileHash $frontendPatch -Algorithm SHA256).Hash -ne '705E15A7176A48B24EC3D13CC40CECCFF85E63962264980BB4651F51C2942F16') {
    throw 'Frontend patch checksum mismatch.'
}

New-Item -ItemType Directory -Path $backupRoot -Force | Out-Null
Copy-Item $jarPath (Join-Path $backupRoot 'yishun-0.0.1-SNAPSHOT.jar') -Force
Copy-Item $frontendRoot (Join-Path $backupRoot 'dist') -Recurse -Force
Expand-Archive -Path $frontendPatch -DestinationPath $frontendRoot -Force
New-Item -ItemType Directory -Path $backendFiles -Force | Out-Null
Expand-Archive -Path $backendPatch -DestinationPath $backendFiles -Force

$arguments = @(
    '-Xms512m', '-Xmx1024m', '-Dfile.encoding=UTF-8',
    '-Djava.io.tmpdir=D:\project\tmp', '-jar', $jarPath,
    '--spring.profiles.active=prod'
)

Get-Process java -ErrorAction Stop | Stop-Process -Force
Start-Sleep -Seconds 5

try {
    Add-Type -AssemblyName System.IO.Compression.FileSystem
    $archive = $null
    for ($attempt = 1; $attempt -le 10 -and $null -eq $archive; $attempt++) {
        try {
            $archive = [System.IO.Compression.ZipFile]::Open($jarPath, [System.IO.Compression.ZipArchiveMode]::Update)
        }
        catch {
            if ($attempt -eq 10) { throw }
            Start-Sleep -Seconds 2
        }
    }
    try {
        Get-ChildItem (Join-Path $backendFiles 'BOOT-INF\classes') -File -Recurse | ForEach-Object {
            $entryName = $_.FullName.Substring($backendFiles.Length + 1).Replace('\', '/')
            $existing = $archive.GetEntry($entryName)
            if ($null -ne $existing) { $existing.Delete() }
            [System.IO.Compression.ZipFileExtensions]::CreateEntryFromFile(
                $archive, $_.FullName, $entryName,
                [System.IO.Compression.CompressionLevel]::Optimal
            ) | Out-Null
        }
    }
    finally {
        $archive.Dispose()
    }

    Start-Process -FilePath $javaExe -ArgumentList $arguments -WorkingDirectory 'D:\project' `
        -WindowStyle Hidden -RedirectStandardOutput (Join-Path $workRoot 'application.out.log') `
        -RedirectStandardError (Join-Path $workRoot 'application.err.log')
    $response = $null
    $lastHealthError = $null
    for ($attempt = 1; $attempt -le 9 -and $null -eq $response; $attempt++) {
        Start-Sleep -Seconds 10
        if (-not (Get-Process java -ErrorAction SilentlyContinue)) {
            throw 'Java process failed to start.'
        }
        try {
            $response = Invoke-WebRequest -Uri 'http://127.0.0.1:8081/api/base/login' -Method Post `
                -ContentType 'application/json' -Body '{"username":"__deployment_probe__","password":"invalid"}' `
                -UseBasicParsing -TimeoutSec 10
        }
        catch {
            $lastHealthError = $_
        }
    }
    if ($null -eq $response) {
        throw $lastHealthError
    }
    Write-Output ('DEPLOY_OK HTTP=' + $response.StatusCode)
}
catch {
    Get-Process java -ErrorAction SilentlyContinue | Stop-Process -Force
    Start-Sleep -Seconds 3
    Copy-Item (Join-Path $backupRoot 'yishun-0.0.1-SNAPSHOT.jar') $jarPath -Force
    Copy-Item (Join-Path $backupRoot 'dist\*') $frontendRoot -Recurse -Force
    Start-Process -FilePath $javaExe -ArgumentList $arguments -WorkingDirectory 'D:\project' `
        -WindowStyle Hidden -RedirectStandardOutput (Join-Path $workRoot 'rollback.out.log') `
        -RedirectStandardError (Join-Path $workRoot 'rollback.err.log')
    throw
}
