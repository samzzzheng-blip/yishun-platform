const fs=require('fs'),crypto=require('crypto'),assert=require('assert');
const origin='https://yishunqianming.com';
async function main(){
 const login=await fetch(origin+'/api/base/login',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({username:'codexadmin',password:crypto.createHash('md5').update(process.env.DEPLOY_PASSWORD).digest('hex')})}).then(r=>r.json());assert.equal(login.code,200,login.msg);
 const headers={Authorization:login.data.token};
 for(const path of ['/api/usr/gradingTemporary','/api/usr/gradingJobs','/api/usr/queryRateList?page=1&pageSize=1&keywords=']){const r=await fetch(origin+path,{headers}).then(r=>r.json());assert.equal(r.code,200,r.msg);if(path.endsWith('gradingTemporary'))assert.equal(r.data.totalElements,0,'Temporary list already contains real records; inspect rather than deleting');console.log('API OK',path)}
 const anonymous=await fetch(origin+'/api/usr/gradingTemporary').then(r=>r.json());assert.equal(anonymous.code,401);console.log('Anonymous access denied');
 const invalid=await fetch(origin+'/api/usr/gradingTemporary?from=invalid&to=2026-09-11',{headers}).then(r=>r.json());assert.notEqual(invalid.code,200);assert(invalid.msg.includes('日期'));console.log('Invalid date rejected');
 for(const name of ['client.b066.js','common.b066.js','style.b066.css','index.html']){const local=fs.readFileSync('/Volumes/Lenovo K102/yishun/react-master/dist/'+name);const remote=Buffer.from(await fetch(origin+'/'+name).then(r=>r.arrayBuffer()));assert.equal(crypto.createHash('sha256').update(local).digest('hex'),crypto.createHash('sha256').update(remote).digest('hex'));console.log('Asset verified',name)}
 const route=await fetch(origin+'/rateTemporary');assert.equal(route.status,200);assert((await route.text()).includes('client.b066.js'));
 console.log('TEMPORARY_VERIFIED: no records created, published, or deleted.');
}
main().catch(e=>{console.error(e.message);process.exitCode=1});
