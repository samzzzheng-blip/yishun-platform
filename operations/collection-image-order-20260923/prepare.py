import zipfile,io,os,subprocess
from pathlib import Path
p=Path('/work/backups/image-order-20260923')
with zipfile.ZipFile(p/'classes.zip') as z: replacements={n:z.read(n) for n in z.namelist()}
def patch(data, rep):
 out=io.BytesIO()
 with zipfile.ZipFile(io.BytesIO(data)) as src, zipfile.ZipFile(out,'w') as dst:
  names=set(src.namelist())
  for info in src.infolist(): dst.writestr(info,rep.get(info.filename,src.read(info.filename)))
  for n,v in rep.items():
   if n not in names:dst.writestr(n,v,compress_type=zipfile.ZIP_DEFLATED)
 return out.getvalue()
with zipfile.ZipFile(p/'app-before.jar') as z:
 module=next(n for n in z.namelist() if n.startswith('BOOT-INF/lib/onebook-module-app-'))
 newmodule=patch(z.read(module),replacements)
(p/'app.jar').write_bytes(patch((p/'app-before.jar').read_bytes(),{module:newmodule}))
with zipfile.ZipFile(p/'app.jar') as z:
 assert z.getinfo(module).compress_type==zipfile.ZIP_STORED
 with zipfile.ZipFile(io.BytesIO(z.read(module))) as m:
  assert all(m.read(n)==v for n,v in replacements.items())
(p/'Dockerfile').write_text('FROM yudao-server:20260921-category\nCOPY app.jar /yudao-server/app.jar\n')
subprocess.run(['docker','build','-t','yudao-server:20260923-image-order',str(p)],check=True)
print('PREPARED_AND_VERIFIED')
