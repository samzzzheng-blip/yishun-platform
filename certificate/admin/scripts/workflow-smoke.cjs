// Local sandbox only: full three-state workflow and atomic batch publication.
const {chromium}=require('playwright');const assert=require('assert');const crypto=require('crypto');const fs=require('fs');const path=require('path');
(async()=>{
 const b=await chromium.launch({headless:true,executablePath:'/Applications/Google Chrome.app/Contents/MacOS/Google Chrome'});
 try{
  const p=await b.newPage({viewport:{width:1440,height:1000}}),origin='http://127.0.0.1:4188',errors=[];p.on('pageerror',e=>errors.push(e.message));
  await p.goto(origin+'/rateIntake');await p.getByPlaceholder('请输入用户名').fill('demo');await p.getByPlaceholder('请输入密码').fill('demo123');await p.getByRole('button',{name:/^登\s*录$/}).click();
  await p.getByRole('button',{name:'保存档案并继续收卡'}).waitFor();const token=await p.evaluate(()=>localStorage.getItem('token'));
  const api=async(endpoint,data)=>{const body=await(await p.request.post(origin+'/api/usr/'+endpoint,{headers:{Authorization:token},data})).json();assert.equal(body.code,200,JSON.stringify(body));return body.data;};
  const batchResponse=p.waitForResponse(r=>r.url().endsWith('/gradingNewBatch'));await p.getByRole('button',{name:'新增批次',exact:true}).click();const batch=(await(await batchResponse).json()).data.batch;
  const saved=p.waitForResponse(r=>r.url().endsWith('/gradingCreate'));await p.getByRole('button',{name:'保存档案并继续收卡'}).click();const first=(await(await saved).json()).data;
  let second=await api('gradingCreate',{jobNumber:'G-'+crypto.randomUUID(),batch});
  second=await api('gradingSave',{...second,cardName:'第二张测试卡',surface:'10',center:'10',edge:'10',corner:'10',score:'10'});
  second=await api('gradingAdvance',{id:second.id,version:second.version,action:'NEXT'});
  await p.goto(origin+'/rateWorkflow?job='+first.id);await p.locator('.grading-fields input').first().fill('第一张测试卡');
  for(const name of ['表面','居中','边缘','角落','总分']){await p.waitForFunction(()=>!document.querySelector('.ant-select-dropdown:not(.ant-select-dropdown-hidden)'));await p.getByLabel(name,{exact:true}).click();await p.locator('.ant-select-dropdown:not(.ant-select-dropdown-hidden) li').filter({hasText:/^10$/}).click();}
  await p.getByRole('button',{name:'提交到待发布',exact:true}).click();await p.getByRole('region',{name:'批次发布'}).waitFor();
  assert.deepEqual(await p.locator('.grading-progress li').allTextContents(),['待整理','待发布','已完成']);
  for(const name of ['提交评分','提交确认','打印标签','标记已打印','完成并发布成品照片'])assert.equal(await p.getByRole('button',{name,exact:true}).count(),0);
  const start=String(Date.now()).slice(-12);await p.getByLabel('批次起始编号',{exact:true}).fill(start);await p.getByRole('button',{name:'预览整批编号',exact:true}).click();
  const modal=p.getByRole('dialog');await modal.locator('tbody tr').first().waitFor();assert.equal(await modal.locator('tbody tr').count(),2);
  assert.equal(await modal.locator('tbody tr').first().locator('td').last().innerText(),start);assert.equal(await modal.locator('tbody tr').last().locator('td').last().innerText(),String(Number(start)+1));
  const out=path.resolve(__dirname,'../.impeccable/review/batch-publish');fs.mkdirSync(out,{recursive:true});await modal.screenshot({path:path.join(out,'desktop.png'),animations:'disabled'});await p.setViewportSize({width:390,height:844});await modal.screenshot({path:path.join(out,'mobile.png'),animations:'disabled'});assert(await p.evaluate(()=>document.documentElement.scrollWidth<=innerWidth+1));
  await p.getByRole('button',{name:'确认发布并标记已完成',exact:true}).click();await p.getByRole('link',{name:'在评级管理中查看证书 '+start}).waitFor();
  assert.equal(await p.locator('.grading-progress .active').innerText(),'已完成');assert.equal(await p.getByRole('button',{name:'保存',exact:true}).count(),0);
  const other=(await(await p.request.get(origin+'/api/usr/gradingJob?id='+second.id,{headers:{Authorization:token}})).json()).data;assert.equal(other.stage,'DONE');assert.equal(other.certNumber,String(Number(start)+1));
  await p.getByRole('link',{name:'在评级管理中查看证书 '+start}).click();await p.waitForURL('**/rateManage?certNumber=*');await p.getByText(start,{exact:true}).first().waitFor();
  assert.deepEqual(errors,[]);console.log('PASS: collect -> publish preview -> entire batch DONE; mapped numbers, no photo/print steps, original rating list, desktop/mobile');
 }finally{await b.close();}
})().catch(e=>{console.error(e);process.exit(1)});
