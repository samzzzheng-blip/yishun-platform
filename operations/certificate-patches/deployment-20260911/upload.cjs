const fs = require('fs');
const crypto = require('crypto');
async function main() {
  const origin = 'https://yishunqianming.com';
  if(!process.env.DEPLOY_PASSWORD) throw new Error('DEPLOY_PASSWORD required');
  const login = await fetch(origin + '/api/base/login', {method:'POST', headers:{'Content-Type':'application/json'}, body:JSON.stringify({username:'codexadmin',password:crypto.createHash('md5').update(process.env.DEPLOY_PASSWORD).digest('hex')})}).then(r=>r.json());
  if(login.code !== 200 || !login.data.token) throw new Error('Deployment login failed: ' + login.msg);
  for(const path of process.argv.slice(2)) {
    const bytes = fs.readFileSync(path);
    const form = new FormData(); form.append('file',new Blob([bytes],{type:'application/octet-stream'}),require('path').basename(path));
    const result = await fetch(origin+'/api/usr/uploadImg',{method:'POST',headers:{Authorization:login.data.token},body:form}).then(r=>r.json());
    if(result.status !== 'done') throw new Error('Upload failed');
    console.log(JSON.stringify({file:require('path').basename(path),sha256:crypto.createHash('sha256').update(bytes).digest('hex'),url:result.url}));
  }
}
main().catch(e=>{console.error(e.message);process.exit(1)});
