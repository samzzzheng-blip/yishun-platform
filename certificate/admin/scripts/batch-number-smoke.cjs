// Local sandbox only. Verify server allocation, not browser-generated numbering.
const {chromium}=require('playwright');const assert=require('assert');const crypto=require('crypto');const fs=require('fs');const path=require('path');
(async()=>{
 const browser=await chromium.launch({headless:true,executablePath:'/Applications/Google Chrome.app/Contents/MacOS/Google Chrome'});
 try {
  const page=await browser.newPage({viewport:{width:1440,height:1000}});const errors=[];page.on('pageerror',e=>errors.push(e.message));
  const origin='http://127.0.0.1:4188';await page.goto(origin+'/rateIntake');await page.getByPlaceholder('请输入用户名').fill('demo');await page.getByPlaceholder('请输入密码').fill('demo123');await page.getByRole('button',{name:/^登\s*录$/}).click();
  await page.getByRole('button',{name:'保存档案并继续收卡',exact:true}).waitFor();
  const token=await page.evaluate(()=>localStorage.getItem('token'));const api=async(endpoint,data,auth=token)=>{const r=await page.request.post(origin+'/api/usr/'+endpoint,{headers:{Authorization:auth},data});const body=await r.json();assert.equal(body.code,200,JSON.stringify(body));return body.data;};
  const newJob=batch=>({jobNumber:'G-'+crypto.randomUUID(),batch});
  const peer=(await(await page.request.post(origin+'/api/base/login',{data:{username:'peer',password:process.env.YISHUN_TEST_PASSWORD}})).json()).data.token;
  const batch='并发自动编号-'+Date.now();
  const jobs=await Promise.all(Array.from({length:12},(_,i)=>api('gradingCreate',newJob(batch),i%2?peer:token)));
  assert.deepEqual(jobs.map(j=>j.batchNumber).sort((a,b)=>a-b),Array.from({length:12},(_,i)=>i+1));
  const duplicate=newJob(batch);const retried=await Promise.all(Array.from({length:3},()=>api('gradingCreate',duplicate)));
  assert.equal(new Set(retried.map(j=>j.id)).size,1);assert.equal(retried[0].batchNumber,13);
  assert.equal((await api('gradingCreate',newJob(batch))).batchNumber,14);
  assert.equal((await api('gradingCreate',newJob(batch+'-B'))).batchNumber,1);
  const blank=await api('gradingCreate',newJob(''));assert(blank.batchNumber>0);assert.equal(blank.certNumber,'');
  const form=page.locator('.grading-detail');assert.equal(await form.getByLabel('编号',{exact:true}).count(),0);
  const batchResponse=page.waitForResponse(r=>r.url().endsWith('/gradingNewBatch'));await page.getByRole('button',{name:'新增批次',exact:true}).click();await batchResponse;
  for(let number=1;number<=2;number++) {
    const saved=page.waitForResponse(r=>r.url().endsWith('/gradingCreate'));
    await page.getByRole('button',{name:'保存档案并继续收卡',exact:true}).click();assert.equal((await(await saved).json()).data.batchNumber,number);
    await page.waitForFunction(n=>document.querySelector('.grading-created').textContent.includes('编号 '+n),number);
  }
  await page.waitForFunction(()=>document.querySelectorAll('.ant-message-notice').length===0);
  const out=path.resolve(__dirname,'../.impeccable/review/batch-number');fs.mkdirSync(out,{recursive:true});
  await form.screenshot({path:path.join(out,'desktop.png')});await page.setViewportSize({width:390,height:844});assert(await page.evaluate(()=>document.documentElement.scrollWidth<=innerWidth+1));await form.screenshot({path:path.join(out,'mobile.png')});
  assert.deepEqual(errors,[]);console.log('PASS: empty intake, per-batch sequence, concurrent multi-account allocation, idempotent retries, desktop/mobile UI');
 } finally {await browser.close();}
})().catch(e=>{console.error(e);process.exit(1)});
