// Local H2 sandbox only; authentication implementation is tested separately in AdminSessionsTest.
const assert=require('assert');const fs=require('fs');const path=require('path');const crypto=require('crypto');
const origin='http://127.0.0.1:4188';
async function request(url,token,data) {
 const response=await fetch(origin+url,{method:data?'POST':'GET',headers:{...(token?{Authorization:token}:{}),...(data?{'Content-Type':'application/json'}:{})},body:data?JSON.stringify(data):undefined});
 return {status:response.status,body:response.headers.get('content-type')?.includes('json')?await response.json():null};
}
async function login(username) {return (await request('/api/base/login',null,{username,password:process.env.YISHUN_TEST_PASSWORD})).body.data.token;}
(async()=>{
 const phone=await login('demo'),desktop=await login('demo'),peer=await login('peer');assert.notEqual(phone,desktop);
 const form=new FormData();form.append('file',new Blob([fs.readFileSync(path.join(__dirname,'../app/images/teach/collect/card1.png'))],{type:'image/png'}),'card.png');
 const upload=await (await fetch(origin+'/api/usr/gradingPhoto',{method:'POST',headers:{Authorization:phone},body:form})).json();assert.equal(upload.code,200);
 const photo=upload.data;assert(photo.startsWith('/api/usr/gradingPhotoFile/'));
 assert.equal((await fetch(origin+photo)).status,401);assert.equal((await fetch(origin+photo,{headers:{Authorization:peer}})).status,404);
 assert.equal((await fetch(origin+photo,{headers:{Authorization:desktop}})).status,200);
 const created=(await request('/api/usr/gradingCreate',phone,{jobNumber:'G-'+crypto.randomUUID(),frontPhoto:photo,createdBy:'peer',rateId:123})).body;
 assert.equal(created.code,200);const job=created.data;assert.equal(job.createdBy,'demo');assert.equal(job.rateId,null);
 const listing=(await request('/api/usr/gradingJobs',desktop)).body;assert(listing.data.content.some(j=>j.id===job.id));
 const other=(await request('/api/usr/gradingJobs',peer)).body;assert(!other.data.content.some(j=>j.id===job.id));
 for(const endpoint of ['gradingJob','gradingHistory'])assert.notEqual((await request('/api/usr/'+endpoint+'?id='+job.id,peer)).body.code,200);
 assert.notEqual((await request('/api/usr/gradingSave',peer,{...job,createdBy:'peer',cardName:'forged'})).body.code,200);
 assert.notEqual((await request('/api/usr/gradingAdvance',peer,{id:job.id,version:job.version,action:'NEXT'})).body.code,200);
 await request('/api/usr/logout',phone);assert.equal((await request('/api/usr/gradingJobs',phone)).status,401);
 assert.equal((await request('/api/usr/gradingJob?id='+job.id,desktop)).body.code,200);
 console.log('PASS: separate same-account sessions, cross-device photo read, private list/detail/history/save/advance, owner spoof rejection, single-device logout');
})().catch(e=>{console.error(e);process.exit(1)});
