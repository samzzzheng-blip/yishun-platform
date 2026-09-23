import {spawnSync} from 'node:child_process';
import {readFileSync} from 'node:fs';
const script=readFileSync(process.argv[2],'utf8');
const encoded=Buffer.from("[Console]::OutputEncoding = [Text.Encoding]::UTF8; $ErrorActionPreference='Stop';\n"+script,'utf16le').toString('base64');
const r=spawnSync('ssh',['-T','-o','BatchMode=yes','-o','IdentitiesOnly=yes','-o','StrictHostKeyChecking=yes','-o','UserKnownHostsFile=/Users/mac/.ssh/known_hosts_yishun_admin','-o','ConnectTimeout=10','-i','/Users/mac/.ssh/id_ed25519_yishun_admin_20260918','administrator@47.111.232.58','powershell.exe -NoProfile -EncodedCommand '+encoded],{stdio:'inherit'});
process.exit(r.status??1);
