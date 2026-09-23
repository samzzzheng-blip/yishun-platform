import pathlib, zipfile, json, hashlib, struct
root=pathlib.Path(__file__).parent
classes=pathlib.Path('/tmp/yishun-temporary-import.dfS2dX/target/classes')
front=pathlib.Path('/Volumes/Lenovo K102/yishun/react-master/dist')
patterns=['service/GradingTemporaryImportService*.class','service/RateImportService*.class','service/GradingPhotoStore*.class','entity/vo/RateImportRow.class','controller/admin/GradingWorkflowController*.class']
files={}
for pattern in patterns:
    matches=list((classes/'com/kiss/yishun').glob(pattern))
    assert matches, pattern
    for p in matches:
        data=p.read_bytes()
        assert data[:4]==b'\xca\xfe\xba\xbe' and struct.unpack('>H',data[6:8])[0]==52
        files['BOOT-INF/classes/'+p.relative_to(classes).as_posix()]=data
for name in ['index.html','client.680b.js','common.680b.js','style.680b.css']:
    files['frontend/'+name]=(front/name).read_bytes()
files['deploy.ps1']=(root/'deploy.ps1').read_bytes()
manifest=[{'path':p,'sha256':hashlib.sha256(data).hexdigest()} for p,data in files.items()]
with zipfile.ZipFile(root/'temporary-import.zip','w',zipfile.ZIP_DEFLATED) as z:
    for p,data in files.items(): z.writestr(p,data)
    z.writestr('manifest.json',json.dumps(manifest))
print('ZIP_SHA256='+hashlib.sha256((root/'temporary-import.zip').read_bytes()).hexdigest())
print('CLASSES='+str(sum(p.endswith('.class') for p in files)))
