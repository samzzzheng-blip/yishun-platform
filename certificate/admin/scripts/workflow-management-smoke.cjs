// Local-only synthetic data: cross-page management, naming concurrency, desktop/mobile UI.
const {chromium}=require('playwright'),assert=require('assert'),crypto=require('crypto'),fs=require('fs'),path=require('path');
(async()=>{
 const browser=await chromium.launch({headless:true,executablePath:'/Applications/Google Chrome.app/Contents/MacOS/Google Chrome'});
 try{
  const p=await browser.newPage({viewport:{width:1440,height:1000}}),origin='http://127.0.0.1:4188',errors=[];p.on('pageerror',e=>errors.push(e.message));
  await p.goto(origin+'/rateIntake');await p.getByPlaceholder('请输入用户名').fill('demo');await p.getByPlaceholder('请输入密码').fill('demo123');await p.getByRole('button',{name:/^登\s*录$/}).click();await p.getByRole('button',{name:'保存档案并继续收卡'}).waitFor();
  const token=await p.evaluate(()=>localStorage.getItem('token'));
  const api=async(name,data)=>{const body=await(await p.request.post(origin+'/api/usr/'+name,{headers:{Authorization:token},data})).json();assert.equal(body.code,200,JSON.stringify(body));return body.data;};
  const get=async(name)=>{const body=await(await p.request.get(origin+'/api/usr/'+name,{headers:{Authorization:token}})).json();assert.equal(body.code,200,JSON.stringify(body));return body.data;};
  const concurrent=await Promise.all(Array.from({length:8},()=>api('gradingNewBatch',{requestId:crypto.randomUUID()})));assert.equal(new Set(concurrent.map(b=>b.batch)).size,8);for(const b of concurrent)assert(/^\d+\.\d+demo\d+$/.test(b.batch));
  const requestId=crypto.randomUUID(),retry=await Promise.all(Array.from({length:3},()=>api('gradingNewBatch',{requestId})));assert.equal(new Set(retry.map(b=>b.batch)).size,1);
  const createdBatch=p.waitForResponse(r=>r.url().endsWith('/gradingNewBatch'));await p.getByRole('button',{name:'新增批次',exact:true}).click();const batch=(await(await createdBatch).json()).data.batch;
  const saved=p.waitForResponse(r=>r.url().endsWith('/gradingCreate'));await p.getByRole('button',{name:'保存档案并继续收卡'}).click();const first=(await(await saved).json()).data;assert.equal(first.batch,batch);assert.equal(first.batchNumber,1);
  for(let i=0;i<22;i++)await api('gradingCreate',{jobNumber:'G-'+crypto.randomUUID(),batch});
  await p.goto(origin+'/rateWorkflow');await p.getByLabel('按批次筛选',{exact:true}).click();await p.locator('.ant-select-dropdown:not(.ant-select-dropdown-hidden) li').filter({hasText:new RegExp('^'+batch.replace(/\./g,'\\.')+'$')}).click();await p.getByText('共 23 张卡片',{exact:false}).waitFor();assert.equal(await p.locator('.grading-row').count(),20);
  await p.getByRole('button',{name:'修改筛选结果批次',exact:true}).click();await p.getByRole('button',{name:'新增目标批次'}).click();await p.getByRole('button',{name:'预览影响范围'}).click();await p.getByRole('button',{name:'确认修改 23 张'}).waitFor();
  const out=path.resolve(__dirname,'../.impeccable/review/workflow-management');fs.mkdirSync(out,{recursive:true});await p.getByRole('dialog').screenshot({path:path.join(out,'desktop.png'),animations:'disabled'});await p.setViewportSize({width:390,height:844});await p.getByRole('dialog').screenshot({path:path.join(out,'mobile.png'),animations:'disabled'});assert(await p.evaluate(()=>document.documentElement.scrollWidth<=innerWidth+1));
  const moving=p.waitForResponse(r=>r.url().endsWith('/gradingManageApply'));await p.getByRole('button',{name:'确认修改 23 张'}).click();assert.equal((await(await moving).json()).data.count,23);await p.getByText('共 0 张卡片',{exact:false}).waitFor();
  const moved=await get('gradingJob?id='+first.id);assert.notEqual(moved.batch,batch);assert.equal(moved.batchNumber,1);
  await p.goto(origin+'/rateWorkflow?job='+first.id);await p.getByRole('button',{name:'删除此商品'}).click();await p.getByRole('button',{name:'预览影响范围'}).click();await p.getByRole('button',{name:'确认删除 1 张'}).click();await p.getByRole('dialog').waitFor({state:'hidden'});
  const single=(await(await p.request.get(origin+'/api/usr/gradingJob?id='+first.id,{headers:{Authorization:token}})).json());assert.notEqual(single.code,200);
  await p.getByLabel('按批次筛选',{exact:true}).click();await p.locator('.ant-select-dropdown:not(.ant-select-dropdown-hidden) li').filter({hasText:new RegExp('^'+moved.batch.replace(/\./g,'\\.')+'$')}).click();await p.getByText('共 22 张卡片',{exact:false}).waitFor();
  await p.getByRole('button',{name:'删除筛选结果',exact:true}).click();await p.getByRole('button',{name:'预览影响范围'}).click();await p.getByRole('button',{name:'确认删除 22 张'}).click();await p.getByText('共 0 张卡片',{exact:false}).waitFor();
  assert.deepEqual(errors,[]);console.log('PASS: auto batch concurrency/idempotency; 23-row filtered move; single and cross-page soft delete; desktop/mobile');
 }finally{await browser.close();}
})().catch(e=>{console.error(e);process.exit(1)});
