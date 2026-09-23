const fs=require('fs'),crypto=require('crypto'),assert=require('assert');
const origin='https://yishunqianming.com';
async function main(){
 const login=await fetch(origin+'/api/base/login',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({username:'codexadmin',password:crypto.createHash('md5').update(process.env.DEPLOY_PASSWORD).digest('hex')})}).then(r=>r.json());assert.equal(login.code,200);
 const headers={Authorization:login.data.token};
 for(const [id,size] of [['7b9747f0-0df9-4818-87ec-af61062c28cc',2732357],['5695e15b-147d-461b-b941-20fe1b7904f3',2845895]]){
  const url=origin+'/api/usr/gradingPhotoFile/'+id+'.jpg';const r=await fetch(url,{headers});assert.equal(r.status,200);const b=Buffer.from(await r.arrayBuffer());assert.equal(b.length,size);assert.equal(b[0],255);assert.equal(b[1],216);console.log('Private photo verified:',size,'bytes');
  const anon=await fetch(url);const data=await anon.json();assert.equal(data.code,401);console.log('Anonymous photo access denied');
 }
 for(const path of ['/api/usr/gradingJobs','/api/usr/queryRateList?page=1&pageSize=1&keywords=']){const r=await fetch(origin+path,{headers}).then(r=>r.json());assert.equal(r.code,200);console.log('API OK:',path)}
 const next=await fetch(origin+'/api/usr/gradingNextInBatch?id=1',{headers}).then(r=>r.json());assert.equal(next.code,200,next.msg);assert(Object.prototype.hasOwnProperty.call(next.data,'next'));console.log('Next-in-batch API OK (read only)');
 for(const name of ['client.6b0a.js','common.6b0a.js','style.6b0a.css','index.html']){const local=fs.readFileSync('/Volumes/Lenovo K102/yishun/react-master/dist/'+name);const remote=Buffer.from(await fetch(origin+'/'+name).then(r=>r.arrayBuffer()));assert.equal(crypto.createHash('sha256').update(local).digest('hex'),crypto.createHash('sha256').update(remote).digest('hex'));console.log('Asset verified:',name)}
 console.log('PHOTO_COPY_VERIFIED: no business records written');
}
main().catch(e=>{console.error(e.message);process.exitCode=1});
