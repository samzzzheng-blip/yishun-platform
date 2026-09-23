$ProgressPreference='SilentlyContinue'
Add-Type -AssemblyName System.IO.Compression.FileSystem
$archive=[IO.Compression.ZipFile]::OpenRead('D:\project\yishun-0.0.1-SNAPSHOT.jar')
try{
  $entry=$archive.GetEntry('BOOT-INF/classes/com/kiss/yishun/utils/PageRequestUtils.class')
  [IO.Compression.ZipFileExtensions]::ExtractToFile($entry,'D:\deploy\precious-search-20260918\PageRequestUtils.class',$false)
}finally{$archive.Dispose()}
