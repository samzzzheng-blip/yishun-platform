import {readFileSync} from 'node:fs';
import {createHash} from 'node:crypto';
import assert from 'node:assert/strict';
const origin='https://yishunqianming.com';
for(const route of ['rateManage','cartoonManage','preciousManage']){
 const r=await fetch(origin+'/'+route,{cache:'no-store',signal:AbortSignal.timeout(15000)});assert.equal(r.status,200);assert((await r.text()).includes('client.search-20260918.js'));
}
const r=await fetch(origin+'/client.search-20260918.js',{cache:'no-store',signal:AbortSignal.timeout(15000)});assert.equal(r.status,200);
const actual=Buffer.from(await r.arrayBuffer()),local=readFileSync(new URL('./payload/frontend/client.search-20260918.js',import.meta.url));
const hash=b=>createHash('sha256').update(b).digest('hex');assert.equal(hash(actual),hash(local));
for(const kind of ['Rate','Cartoon','Precious']){const response=await fetch(origin+'/api/usr/query'+kind+'List?page=1&pageSize=20',{signal:AbortSignal.timeout(15000)}).then(r=>r.json());assert.equal(response.code,401);}
console.log('PASS: three public routes reference verified new bundle; all three APIs retain authentication');
