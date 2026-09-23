const assert=require('assert'),fs=require('fs'),vm=require('vm'),path=require('path');
const source=fs.readFileSync(path.join(__dirname,'../app/pages/grading/copyPhoto.js'),'utf8').replace(/export function /g,'function ');
function setup(options={}){
  const calls=[];
  class Image {set src(value){this.naturalWidth=1200;this.naturalHeight=1600;queueMicrotask(()=>options.invalid?this.onerror():this.onload())}}
  const png={type:'image/png'};
  const context={Promise,Error,setTimeout,clearTimeout,Image,URL:{createObjectURL:()=>{calls.push('url');return 'blob:test'},revokeObjectURL:()=>calls.push('revoke')},document:{createElement:()=>({getContext:()=>({drawImage:()=>calls.push('draw')}),toBlob:cb=>cb(png)})},window:{isSecureContext:!options.unsupported},navigator:{clipboard:{write:items=>{calls.push('write');if(options.denied)return Promise.reject(Object.assign(new Error('denied'),{name:'NotAllowedError'}));return items[0].data['image/png'].then(value=>{assert.equal(value.type,'image/png');calls.push('written')})}}},ClipboardItem:class{constructor(data){this.data=data}}};
  vm.createContext(context);vm.runInContext(source,context);return {context,calls,png};
}
(async()=>{
  const a=setup();await a.context.copyPhoto(()=>{a.calls.push('read');return a.png});assert.deepEqual(a.calls,['write','read','written']);
  const b=setup();await b.context.copyPhoto(()=>({type:'image/jpeg'}));assert.deepEqual(b.calls,['write','url','draw','revoke','written']);
  const c=setup({unsupported:true});await assert.rejects(c.context.copyPhoto(()=>{throw Error('must not read')}),/不支持/);assert.equal(c.calls.length,0);
  const d=setup({denied:true});await assert.rejects(d.context.copyPhoto(()=>d.png),/denied/);
  const e=setup();await assert.rejects(e.context.copyPhoto(()=>Promise.reject(Error('fetch failed'))),/fetch failed/);
  const f=setup({invalid:true});await assert.rejects(f.context.copyPhoto(()=>({type:'image/jpeg'})),/无法读取/);assert(f.calls.includes('revoke'));
  console.log('PASS: PNG, JPEG conversion and cleanup, immediate clipboard write, unsupported browser, denied permission, network and invalid image');
})().catch(e=>{console.error(e);process.exitCode=1});
