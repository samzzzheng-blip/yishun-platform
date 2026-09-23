// Run only against the disposable local H2 WorkflowSandbox; never production.
const {chromium}=require('playwright'),assert=require('assert'),path=require('path'),fs=require('fs');
(async()=>{
 const origin=process.env.TEMP_IMPORT_ORIGIN||'http://127.0.0.1:4190';
 assert(/^http:\/\/127\.0\.0\.1:\d+$/.test(origin),'Local sandbox only');
 const b=await chromium.launch({headless:true,executablePath:'/Applications/Google Chrome.app/Contents/MacOS/Google Chrome'});
 try{
  const p=await b.newPage({viewport:{width:1440,height:1000}}),errors=[];p.on('pageerror',e=>errors.push(e.message));
  await p.goto(origin+'/rateTemporary');await p.getByPlaceholder('请输入用户名').fill('demo');await p.getByPlaceholder('请输入密码').fill('demo123');await p.getByRole('button',{name:/^登\s*录$/}).click();
  await p.getByRole('heading',{name:'临时',exact:true}).waitFor();await p.waitForFunction(()=>!document.querySelector('.ant-spin-spinning'));
  const initial=Number((await p.locator('.grading-management-actions>span').innerText()).match(/\d+/)[0]);
  await p.getByRole('button',{name:'导入临时商品'}).click();
  const dialog=p.getByRole('dialog');await dialog.waitFor();await p.waitForTimeout(500);assert(await dialog.getByRole('button',{name:'导入临时区',exact:true}).isDisabled());
  const out=path.resolve(__dirname,'../.impeccable/review/temporary-import');fs.mkdirSync(out,{recursive:true});
  await p.screenshot({path:path.join(out,'desktop.png'),fullPage:true});await p.setViewportSize({width:390,height:844});await p.screenshot({path:path.join(out,'mobile.png'),fullPage:true});assert(await p.evaluate(()=>document.documentElement.scrollWidth<=innerWidth+1));
  const xml='<评级记录><记录><标签>2026 测试系列\n测试卡甲\n#'+Date.now()+'</标签><总分>9.0</总分></记录><记录><标签>测试卡乙</标签></记录></评级记录>';
  await dialog.locator('input[type=file]').setInputFiles({name:'temporary.xml',mimeType:'application/xml',buffer:Buffer.from(xml)});
  await dialog.getByRole('button',{name:'导入临时区',exact:true}).click();await dialog.getByText('读取 2 条，新导入 2 条，已导入跳过 0 条').waitFor();assert(await dialog.getByRole('button',{name:'导入临时区',exact:true}).isDisabled());
  await dialog.getByRole('button',{name:/^完\s*成$/}).click();await p.getByText('共 '+(initial+2)+' 件',{exact:true}).waitFor();
  await p.setViewportSize({width:1440,height:1000});await p.screenshot({path:path.join(out,'result.png'),fullPage:true});
  await p.getByRole('button',{name:'导入临时商品'}).click();await dialog.locator('input[type=file]').setInputFiles({name:'temporary.xml',mimeType:'application/xml',buffer:Buffer.from(xml)});await dialog.getByRole('button',{name:'导入临时区',exact:true}).click();await dialog.getByText('读取 2 条，新导入 0 条，已导入跳过 2 条').waitFor();
  assert.deepEqual(errors,[]);console.log('PASS: local UI upload, complete, preserved summary, no duplicate, desktop/mobile layout; screenshots '+out);
 }finally{await b.close()}
})().catch(e=>{console.error(e);process.exit(1)});
