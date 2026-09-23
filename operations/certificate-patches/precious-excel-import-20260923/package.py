from pathlib import Path
import zipfile, hashlib, json, shutil
root=Path(__file__).parent
payload=root/'payload'
classes=Path('/tmp/yishun-precious-import.wj2qxK/target/classes')
name='com/kiss/yishun/service/PreciousImportService.class'
dest=payload/'BOOT-INF/classes'/name
dest.parent.mkdir(parents=True,exist_ok=True)
shutil.copy2(classes/name,dest)
manifest={'jar':'eadd260ccbe220029ed41dbb23d8249f851fe8dbe789e9ebccb4c3e837cee500','index':'f248dd4ad2f6057a03e25c2b3127b80773daec681368b17088d7ae1240d7e498','client':'a6089091c3ec889db3644367d15b9e5bc6489e04e6cbed698515351eeedb916c','files':[]}
for p in payload.rglob('*'):
 if p.is_file() and p.name!='manifest.json':
  data=p.read_bytes()
  if p.suffix=='.class': assert int.from_bytes(data[6:8],'big')==52
  manifest['files'].append({'path':p.relative_to(payload).as_posix(),'sha256':hashlib.sha256(data).hexdigest()})
(payload/'manifest.json').write_text(json.dumps(manifest),encoding='utf8')
with zipfile.ZipFile(root/'payload.zip','w',zipfile.ZIP_DEFLATED) as z:
 for p in payload.rglob('*'):
  if p.is_file():z.write(p,p.relative_to(payload))
print('Payload SHA256',hashlib.sha256((root/'payload.zip').read_bytes()).hexdigest())
