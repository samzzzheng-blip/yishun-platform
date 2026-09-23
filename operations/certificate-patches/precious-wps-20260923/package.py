from pathlib import Path
import hashlib,json,zipfile
root=Path(__file__).parent
classes=Path('/tmp/yishun-precious-import.wj2qxK/target/classes')
paths=[classes/'com/kiss/yishun/service/PreciousImportService.class']
paths+=sorted((classes/'com/kiss/yishun/service').glob('PreciousCellImages*.class'))
assert len(paths)==3
manifest={'jar':'94e544d2ec5440a7172ce8e2be79ace3dfb0d16f63eddb369f445a3a4319f46a','index':'3bf69f7f54a050919eb557011812bb0f99661f679344134450589cbf0110b206','client':'a6089091c3ec889db3644367d15b9e5bc6489e04e6cbed698515351eeedb916c','files':[]}
with zipfile.ZipFile(root/'payload.zip','w',zipfile.ZIP_DEFLATED) as z:
 for p in paths:
  data=p.read_bytes()
  assert data[:4]==b'\xca\xfe\xba\xbe' and int.from_bytes(data[6:8],'big')==52
  name='BOOT-INF/classes/'+p.relative_to(classes).as_posix()
  z.writestr(name,data)
  manifest['files'].append({'path':name,'sha256':hashlib.sha256(data).hexdigest()})
 z.writestr('manifest.json',json.dumps(manifest))
(root/'manifest.json').write_text(json.dumps(manifest,indent=2))
print('Three Java 8 class files packaged; no frontend or data files.')
