const fs=require('fs'),path=require('path'),assert=require('assert'),vm=require('vm');
const parser=require('/Volumes/Lenovo K102/yishun/react-master/node_modules/babylon');
const dir=__dirname,src=fs.readFileSync(path.join(dir,'baseline/client.precious-20260918.js'),'utf8');
let result=src;
for(const [list,label,receiver,oldKeyword] of [['cartoonList','证书编号、角色名或动漫名','a','keyword:a.toLowerCase()'],['rateList','证书编号或名称','l','keyword:(a||"").toLowerCase()']]){
 const props=parser.parse(result).program.body[0].expression.arguments[1].properties;
 const found=props.filter(p=>result.slice(p.start,p.end).includes(list+':[]'));assert.equal(found.length,1);
 const mod=found[0];let code=result.slice(mod.start,mod.end);
 const helper=code.match(/\(0,([a-zA-Z]+)\.default\)\(\{\},this\.state\.searchKey,\{keyword:/)[1];
 function replace(a,b){assert.equal(code.split(a).length-1,1,a);code=code.replace(a,b);}
 replace(oldKeyword,'keyword:(a||"").trim()');
 replace('placeholder:"请输入关键字进行搜索"','placeholder:"输入'+label+'","aria-label":"'+label+'"');
 const page=`function(e){var t=Math.max(1,Math.ceil(${receiver}.state.totalCount/${receiver}.state.searchKey.pageSize));if(!Number.isInteger(e)||e<1||e>t)return;${receiver}.setState({searchKey:(0,${helper}.default)({},${receiver}.state.searchKey,{pageNo:e})},function(){${receiver}.getData()})}`;
 const size=`function(e,t){${receiver}.setState({searchKey:(0,${helper}.default)({},${receiver}.state.searchKey,{pageNo:1,pageSize:t})},function(){${receiver}.getData()})}`;
 replace(`${receiver}.pageChange=function(e){${receiver}.state.searchKey.pageNo=e,${receiver}.getData()}`,`${receiver}.pageChange=`+page);
 replace(`${receiver}.pageSizeChange=function(e,t){${receiver}.state.searchKey.pageNo=1,${receiver}.state.searchKey.pageSize=t,${receiver}.getData()}`,`${receiver}.pageSizeChange=`+size);
 replace('dataSource:this.state.'+list+',currentPage:','dataSource:this.state.'+list+',showQuickJumper:!0,currentPage:');
 result=result.slice(0,mod.start)+code+result.slice(mod.end);
 let calls=0;const stateful={state:{searchKey:{pageNo:1,pageSize:10,keyword:'角色',startNumber:'100',endNumber:'999'},totalCount:251},setState(s,cb){Object.assign(this.state,s);cb()},getData(){calls++}};
 const context=vm.createContext({[receiver]:stateful,[helper]:{default:Object.assign}});
 const jump=vm.runInContext('('+page+')',context),resize=vm.runInContext('('+size+')',context);
 jump(26);assert.equal(stateful.state.searchKey.pageNo,26);
 for(const n of [0,-1,27,1.5,NaN])jump(n);assert.equal(calls,1);
 resize(26,20);assert.equal(stateful.state.searchKey.pageNo,1);assert.equal(stateful.state.searchKey.pageSize,20);
 assert.equal(stateful.state.searchKey.keyword,'角色');assert.equal(stateful.state.searchKey.startNumber,'100');assert.equal(stateful.state.searchKey.endNumber,'999');
 console.log('PASS '+list+': pagination bounds and filters preserved');
}
const oldModules=parser.parse(src).program.body[0].expression.arguments[1].properties;
const newModules=parser.parse(result).program.body[0].expression.arguments[1].properties;
assert.equal(oldModules.length,newModules.length);
oldModules.forEach((m,i)=>{const old=src.slice(m.start,m.end);if(!/rateList:\[\]|cartoonList:\[\]/.test(old))assert.equal(old,result.slice(newModules[i].start,newModules[i].end));});
fs.mkdirSync(path.join(dir,'payload/frontend'),{recursive:true});
fs.writeFileSync(path.join(dir,'payload/frontend/client.search-20260918.js'),result);
const html=fs.readFileSync(path.join(dir,'baseline/index.html'),'utf8');assert.equal(html.split('client.precious-20260918.js').length-1,1);
fs.writeFileSync(path.join(dir,'payload/frontend/index.html'),html.replace('client.precious-20260918.js','client.search-20260918.js'));
console.log('PASS: all unrelated modules, including precious, byte-for-byte preserved');
