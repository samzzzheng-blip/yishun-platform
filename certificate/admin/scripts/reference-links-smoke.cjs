// Local synthetic images only. Intercept external destinations to assert safe multipart navigation.
const {chromium}=require('playwright'),assert=require('assert'),crypto=require('crypto'),path=require('path'),fs=require('fs');
(async()=>{const browser=await chromium.launch({headless:true,executablePath:'/Applications/Google Chrome.app/Contents/MacOS/Google Chrome'});
 try{const context=await browser.newContext({viewport:{width:1440,height:1000}}),p=await context.newPage(),origin='http://127.0.0.1:4188',posts=[],errors=[];p.on('pageerror',e=>errors.push(e.message));
 await context.route('https://lens.google.com/**',async route=>{posts.push(route.request());await route.fulfill({status:200,contentType:'text/html',body:'<p>Mock Google image result</p>'})});
 await context.route('https://www.google.com/**',route=>route.fulfill({status:200,contentType:'text/html',body:'<p>Mock Google page</p>'}));
await p.goto(origin+'/rateWorkflow');await p.getByPlaceholder('请输入用户名').fill('demo');await p.getByPlaceholder('请输入密码').fill('demo123');await p.getByRole('button',{name:/^登\s*录$/}).click();await p.getByRole('heading',{name:'评级流程',exact:true}).waitFor();const token=await p.evaluate(()=>localStorage.getItem('token'));
 const api=async(name,data)=>{const b=await(await p.request.post(origin+'/api/usr/'+name,{headers:{Authorization:token},data})).json();assert.equal(b.code,200,JSON.stringify(b));return b.data};
 const png=Buffer.from('iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVQIHWP4z8DwHwAFgAI/ScLbtAAAAABJRU5ErkJggg==','base64');
 const uploaded=await(await p.request.post(origin+'/api/usr/gradingPhoto',{headers:{Authorization:token},multipart:{file:{name:'test.png',mimeType:'image/png',buffer:png}}})).json();assert.equal(uploaded.code,200,JSON.stringify(uploaded));
 const jobs=[];for(const photos of [{},{frontPhoto:uploaded.data},{backPhoto:uploaded.data}])jobs.push(await api('gradingCreate',{jobNumber:'G-'+crypto.randomUUID(),batch:'资料查询测试-'+Date.now(),...photos}));
 for(let i=0;i<jobs.length;i++){
  await p.goto(origin+'/rateWorkflow?job='+jobs[i].id);await p.getByRole('button',{name:'谷歌识图',exact:true}).waitFor();assert.equal(await p.getByRole('link',{name:'豆包网页版'}).getAttribute('href'),'https://www.doubao.com/chat/');
  const popup=context.waitForEvent('page');await p.getByRole('button',{name:'谷歌识图',exact:true}).click();const tab=await popup;await tab.waitForLoadState('domcontentloaded');await tab.waitForURL(i?'https://lens.google.com/**':'https://www.google.com/**');assert.equal(await tab.evaluate(()=>window.opener),null);await tab.close();
  if(i){await p.getByText('已提交照片并打开 Google。',{exact:false}).waitFor();const req=posts[posts.length-1],body=req.postDataBuffer();assert.equal(req.method(),'POST');assert(body.includes(Buffer.from('name="encoded_image"')));assert(body.includes(png));assert(!body.includes(Buffer.from(token)));assert(!body.includes(Buffer.from(uploaded.data)));assert(!req.headers().authorization);assert(!req.headers().referer);}
 }
 const out=path.resolve(__dirname,'../.impeccable/review/reference-links');fs.mkdirSync(out,{recursive:true});const section=p.getByRole('region',{name:'资料查询'});await section.screenshot({path:path.join(out,'desktop.png')});await p.setViewportSize({width:390,height:844});await section.screenshot({path:path.join(out,'mobile.png')});assert(await p.evaluate(()=>document.documentElement.scrollWidth<=innerWidth+1));
 await p.evaluate(()=>{window.open=()=>null});await p.getByRole('button',{name:'谷歌识图',exact:true}).click();await p.getByText('浏览器拦截了新标签页',{exact:false}).waitFor();assert.deepEqual(errors,[]);
 console.log('PASS: no-photo navigation, front/back image multipart, no credential/referrer leakage, popup blocker, links and responsive layout');
 }finally{await browser.close()}
})().catch(e=>{console.error(e);process.exit(1)});
