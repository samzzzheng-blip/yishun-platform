const fs=require('fs'),path=require('path'),crypto=require('crypto');
const root=path.join(__dirname,'payload');
function walk(dir){return fs.readdirSync(dir,{withFileTypes:true}).flatMap(e=>e.isDirectory()?walk(path.join(dir,e.name)):[path.join(dir,e.name)]);}
const files=walk(root).filter(p=>!p.endsWith('manifest.json')).map(p=>({path:path.relative(root,p).split(path.sep).join('/'),sha256:crypto.createHash('sha256').update(fs.readFileSync(p)).digest('hex')}));
if(files.length!==8)throw Error('Expected six classes and two frontend files');
fs.writeFileSync(path.join(root,'manifest.json'),JSON.stringify({baseline:{jar:'10c36a3aef34b04ca1e5f467e24af649ba3df52f0a5d77ba3754cb4ea21b40c7',index:'329cdf37022268a8a833f40260fd556d1819ba7a1be41ce73fae88b1c0f33cbe',client:'ffcf8e1c25b5956c4b19391f33874cdf76ab6014619ee442a19327b2a41a9a2f'},files},null,2));
console.log(JSON.stringify(files,null,2));
