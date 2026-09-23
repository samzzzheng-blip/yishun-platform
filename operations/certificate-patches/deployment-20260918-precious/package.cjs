const fs=require('fs'),path=require('path'),crypto=require('crypto');
const root=path.join(__dirname,'payload');
function walk(dir){return fs.readdirSync(dir,{withFileTypes:true}).flatMap(e=>e.isDirectory()?walk(path.join(dir,e.name)):[path.join(dir,e.name)]);}
const files=walk(root).filter(p=>!p.endsWith('manifest.json')).map(p=>({path:path.relative(root,p).split(path.sep).join('/'),sha256:crypto.createHash('sha256').update(fs.readFileSync(p)).digest('hex')}));
if(files.length!==5)throw Error('Expected three classes and two frontend files');
fs.writeFileSync(path.join(root,'manifest.json'),JSON.stringify({baseline:{jar:'563aa42be2ac864641ea0bc90440b8692d9697db64ee628eab8f36808e7ddbda',index:'f8b07bcc09a85f5d2741c1720aa54060c9a565bb38fa46653632bccfbd2acbe0',client:'c7b3e79b0857d4efd293970a4fc99357a07cd0e33ee220187c37aa32c78744e5'},files},null,2));
console.log(JSON.stringify(files,null,2));
