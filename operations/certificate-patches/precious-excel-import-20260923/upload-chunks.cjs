const fs=require('fs'),cp=require('child_process'),crypto=require('crypto'),path=require('path');
function run(ps){return cp.execFileSync('ssh',['-T','-i','/Users/mac/.ssh/id_ed25519_yishun_admin_20260918','-o','StrictHostKeyChecking=yes','-o','UserKnownHostsFile=/Users/mac/.ssh/known_hosts_yishun_admin','administrator@47.111.232.58','powershell -NoProfile -EncodedCommand '+Buffer.from("$ErrorActionPreference='Stop';$ProgressPreference='SilentlyContinue';"+ps,'utf16le').toString('base64')],{encoding:'utf8',timeout:30000});}
const stage='D:\\deploy\\precious-import-20260923-v2';
run("if(Test-Path '"+stage+"'){throw 'Stage exists'};New-Item -ItemType Directory '"+stage+"'|Out-Null");
for(const name of ['payload.zip','deploy.ps1']){
 const bytes=fs.readFileSync(path.join(__dirname,name)),b64=bytes.toString('base64');
 for(let i=0;i<b64.length;i+=2200){run("[IO.File]::AppendAllText('"+stage+"\\"+name+".b64','"+b64.slice(i,i+2200)+"')");if(i%22000===0)console.log(name,Math.round(i/b64.length*100)+'%');}
 run("[IO.File]::WriteAllBytes('"+stage+"\\"+name+"',[Convert]::FromBase64String([IO.File]::ReadAllText('"+stage+"\\"+name+".b64')));if((Get-FileHash '"+stage+"\\"+name+"').Hash.ToLowerInvariant() -ne '"+crypto.createHash('sha256').update(bytes).digest('hex')+"'){throw 'Hash mismatch'}");
}
console.log(run("Expand-Archive '"+stage+"\\payload.zip' '"+stage+"\\payload';Write-Output 'UPLOAD_VERIFIED'"));
