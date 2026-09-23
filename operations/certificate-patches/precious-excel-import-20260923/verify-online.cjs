const fs=require('fs'),assert=require('assert'),crypto=require('crypto'),path=require('path');
(async()=>{
 const origin='https://yishunqianming.com';
 const page=await fetch(origin+'/preciousManage',{cache:'no-store'});
 assert.equal(page.status,200);const html=await page.text();
 for(const name of ['client.precious-import-20260923.js','precious-import-20260923.js']){
  assert(html.includes(name));const r=await fetch(origin+'/'+name,{cache:'no-store'});assert.equal(r.status,200);
  const remote=Buffer.from(await r.arrayBuffer()),local=fs.readFileSync(path.join(__dirname,'payload/frontend',name));
  assert.equal(crypto.createHash('sha256').update(remote).digest('hex'),crypto.createHash('sha256').update(local).digest('hex'));
 }
 for(const url of ['/api/usr/queryPreciousList?page=1&pageSize=1','/api/usr/previewPreciousExcel','/api/usr/uploadPreciousExcel']){
  const r=await fetch(origin+url,{method:url.includes('Excel')?'POST':'GET'});const result=await r.json();assert.equal(result.code,401);
 }
 console.log('PASS public page, exact asset hashes, backend health and import authentication');
})().catch(e=>{console.error(e);process.exitCode=1});
