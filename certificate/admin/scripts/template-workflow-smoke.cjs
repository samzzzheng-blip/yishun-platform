// Local sandbox only; no production records or credentials.
const {chromium}=require('playwright');const assert=require('assert');const path=require('path');const fs=require('fs');const crypto=require('crypto');
const origin='http://127.0.0.1:4188',out=path.resolve(__dirname,'../.impeccable/review/templates'),templateName='本地标签模板测试-'+Date.now();
(async()=>{
 fs.mkdirSync(out,{recursive:true});const browser=await chromium.launch({headless:true,executablePath:'/Applications/Google Chrome.app/Contents/MacOS/Google Chrome'});
 try {
  const page=await browser.newPage({viewport:{width:1440,height:1000}});const errors=[];page.on('pageerror',e=>errors.push(e.message));
  await page.goto(origin+'/rateWorkflow');await page.getByPlaceholder('请输入用户名').fill('demo');await page.getByPlaceholder('请输入密码').fill('demo123');await page.getByRole('button',{name:/^登\s*录$/}).click();
  await page.getByRole('button',{name:'管理卡片信息模板',exact:true}).click();const manager=page.getByRole('region',{name:'卡片信息模板管理'});
  await manager.getByLabel('模板名称',{exact:true}).fill(templateName);
  assert(await manager.getByLabel('字段 1 名称',{exact:true}).isDisabled());
  await manager.getByRole('button',{name:'添加字段',exact:true}).click();await manager.getByLabel('字段 2 名称',{exact:true}).fill('年份');
  await manager.getByRole('button',{name:'上移字段 2',exact:true}).click();
  const savedResponse=page.waitForResponse(r=>r.url().endsWith('/gradingTemplateSave'));
  await manager.getByRole('button',{name:'保存模板',exact:true}).click();const template=(await(await savedResponse).json()).data;
  await page.waitForFunction(()=>document.querySelectorAll('.ant-message-notice').length===0);
  await manager.screenshot({path:path.join(out,'desktop.png')});await page.setViewportSize({width:390,height:844});
  assert(await page.evaluate(()=>document.documentElement.scrollWidth<=innerWidth+1));await manager.screenshot({path:path.join(out,'mobile.png')});
  const token=await page.evaluate(()=>localStorage.getItem('token'));const req=page.request;
  const api=async(endpoint,data,auth=token)=>{const r=await req.post(origin+'/api/usr/'+endpoint,{headers:{Authorization:auth},data});return r.json()};
  const batch='模板测试-'+Date.now();const create=async(number)=>{const r=await api('gradingCreate',{jobNumber:'G-'+crypto.randomUUID(),certNumber:number,batch});assert.equal(r.code,200);return r.data;};
  let a=await create(String(Date.now()).slice(-12)),b=await create(String(Date.now()+1).slice(-12));
  await page.goto(origin+'/rateWorkflow?job='+a.id);await page.locator('.grading-fields input').first().fill('标签测试卡');await page.getByRole('button',{name:/^保\s*存$/}).click();await page.waitForFunction(()=>document.querySelector('.grading-actions>span').textContent==='已保存');
  await page.getByRole('button',{name:'切换模板',exact:true}).click();
  await page.locator('.grading-template-select .ant-select').click();await page.locator('.ant-select-dropdown:not(.ant-select-dropdown-hidden) li').getByText(templateName,{exact:true}).click();
  const preview=page.getByRole('region',{name:'卡片信息',exact:true});assert(await preview.getByLabel('年份',{exact:true}).isDisabled());assert(await preview.getByLabel('名称',{exact:true}).isDisabled());
  await page.waitForFunction(()=>!document.querySelector('.ant-select-dropdown:not(.ant-select-dropdown-hidden)'));
  await page.setViewportSize({width:390,height:844});await preview.screenshot({path:path.join(out,'template-preview-mobile.png')});
  await page.getByRole('button',{name:'应用模板',exact:true}).click();await page.getByRole('dialog').screenshot({path:path.join(out,'template-scope-mobile.png')});await page.getByRole('button',{name:'仅当前卡片',exact:true}).click();
  await preview.getByLabel('年份',{exact:true}).fill('2026');
  const singleB=await(await req.get(origin+'/api/usr/gradingJob?id='+b.id,{headers:{Authorization:token}})).json();assert(!singleB.data.templateSchema);
  await page.getByRole('button',{name:/^保\s*存$/}).click();await page.waitForFunction(()=>document.querySelector('.grading-actions>span').textContent==='已保存');
  await page.reload();await page.getByRole('region',{name:'卡片信息',exact:true}).getByLabel('年份',{exact:true}).waitFor();assert.equal(await page.getByLabel('年份',{exact:true}).inputValue(),'2026');
  await page.getByRole('button',{name:'切换模板',exact:true}).click();await page.locator('.grading-template-select .ant-select').click();await page.locator('.ant-select-dropdown:not(.ant-select-dropdown-hidden) li').getByText(templateName,{exact:true}).click();
  await page.getByRole('button',{name:'应用模板',exact:true}).click();await page.getByRole('button',{name:'同批次一键应用',exact:true}).click();
  const label=page.getByRole('region',{name:'卡片信息',exact:true});await label.getByLabel('年份',{exact:true}).fill('2026');
  assert.equal(await page.getByRole('region',{name:'打印标签信息'}).count(),0);
  assert.equal(await label.getByLabel('名称',{exact:true}).isEnabled(),true);
  for(const oldField of ['系列','语言','卡号'])assert.equal(await label.getByLabel(oldField,{exact:true}).count(),0);
  assert.equal(await label.getByLabel('收卡批次',{exact:true}).count(),1);
  assert.equal(await label.locator('.grading-label-preview').innerText(),'2026 标签测试卡');
  await page.getByRole('button',{name:/^保\s*存$/}).click();await page.waitForFunction(()=>document.querySelector('.grading-actions>span').textContent==='已保存');
  await page.waitForFunction(()=>document.querySelectorAll('.ant-message-notice').length===0);
  await page.setViewportSize({width:390,height:844});assert(await page.evaluate(()=>document.documentElement.scrollWidth<=innerWidth+1));await label.screenshot({path:path.join(out,'card-fields-mobile.png')});
  await page.setViewportSize({width:1440,height:1000});await label.screenshot({path:path.join(out,'card-fields.png')});
  const bResult=await (await req.get(origin+'/api/usr/gradingJob?id='+b.id,{headers:{Authorization:token}})).json();assert.equal(JSON.parse(bResult.data.templateSchema).id,template.id);assert.equal(bResult.data.templateValues,null);
  const c=await create(String(Date.now()).slice(-12));assert.equal(JSON.parse(c.templateSchema).id,template.id);
  let ready=(await(await req.get(origin+'/api/usr/gradingJob?id='+a.id,{headers:{Authorization:token}})).json()).data;
  ready=(await api('gradingSave',{...ready,surface:'9.5',center:'9.5',edge:'9.5',corner:'9.5',score:'9.5'})).data;
  for(const other of [b,c]) {
    let item=(await(await req.get(origin+'/api/usr/gradingJob?id='+other.id,{headers:{Authorization:token}})).json()).data;
    item=(await api('gradingSave',{...item,cardName:'同批卡',surface:'10',center:'10',edge:'10',corner:'10',score:'10'})).data;
    assert.equal((await api('gradingAdvance',{id:item.id,version:item.version,action:'NEXT'})).code,200);
  }
  ready=(await api('gradingAdvance',{id:ready.id,version:ready.version,action:'NEXT'})).data;
  const batchPreview=(await api('gradingBatchPreview',{id:ready.id,startNumber:String(Date.now()).slice(-12)})).data;
  assert.equal((await api('gradingBatchPublish',{id:ready.id,startNumber:batchPreview.startNumber,token:batchPreview.token})).code,200);
  ready=(await(await req.get(origin+'/api/usr/gradingJob?id='+ready.id,{headers:{Authorization:token}})).json()).data;
  assert.equal(ready.stage,'DONE');assert.equal(ready.labelText,'2026 标签测试卡');
  await page.goto(origin+'/rateWorkflow?job='+ready.id);await page.getByRole('region',{name:'卡片信息',exact:true}).waitFor();assert.equal(await page.getByRole('button',{name:'打印标签',exact:true}).count(),0);
  const peerLogin=await(req.post(origin+'/api/base/login',{data:{username:'peer',password:process.env.YISHUN_TEST_PASSWORD}}));const peer=(await peerLogin.json()).data.token;
  const peerTemplates=await(await req.get(origin+'/api/usr/gradingTemplates',{headers:{Authorization:peer}})).json();assert.equal(peerTemplates.data.canManage,true);assert(peerTemplates.data.list.some(t=>t.id===template.id));
  assert.equal((await api('gradingTemplateSave',{name:'员工模板-'+Date.now(),fieldsJson:template.fieldsJson},peer)).code,200);
  assert.deepEqual(errors,[]);console.log('PASS: dynamic card information form, editable name, field order, summary, desktop/mobile, batch structure, independent values, employee template creation, batch publication');
 } finally {await browser.close();}
})().catch(e=>{console.error(e);process.exit(1)});
