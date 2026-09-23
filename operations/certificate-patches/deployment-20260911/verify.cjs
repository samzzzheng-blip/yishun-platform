const fs=require('fs'), crypto=require('crypto'), assert=require('assert');
const origin='https://yishunqianming.com';
async function get(path,token){const r=await fetch(origin+path,{headers:token?{Authorization:token}:{}});return r.json();}
async function login(){assert(process.env.DEPLOY_PASSWORD);const r=await fetch(origin+'/api/base/login',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({username:'codexadmin',password:crypto.createHash('md5').update(process.env.DEPLOY_PASSWORD).digest('hex')})}).then(r=>r.json());assert.equal(r.code,200,r.msg);return r.data.token;}
async function main(){
 const first=await login(), second=await login();
 for(const token of [first,second]) {
  for(const path of ['/api/usr/gradingJobs','/api/usr/gradingBatches','/api/usr/gradingTemplates']) {
   const r=await get(path,token);assert.equal(r.code,200,r.msg);console.log(path,JSON.stringify(r.data));
  }
 }
 assert.equal((await get('/api/usr/gradingJobs')).code,401);
 const rates=await get('/api/usr/queryRateList?page=1&pageSize=1&keywords=',second);
 assert.equal(rates.code,200,rates.msg);console.log('Original rate API successful; keys:',Object.keys(rates.data));
 const local=fs.readFileSync('/Volumes/Lenovo K102/yishun/react-master/dist/client.0196.js');
 const remote=Buffer.from(await fetch(origin+'/client.0196.js').then(r=>r.arrayBuffer()));
 assert.equal(crypto.createHash('sha256').update(remote).digest('hex'),crypto.createHash('sha256').update(local).digest('hex'));
 const html=await fetch(origin+'/rateWorkflow').then(r=>r.text());assert(html.includes('0196'));
 console.log('VERIFIED: new API, original API, two concurrent sessions, authentication, frontend checksum; no business writes.');
}
main().catch(e=>{console.error(e.message);process.exit(1)});
