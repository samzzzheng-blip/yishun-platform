import { test } from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync, readdirSync } from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
const root = fileURLToPath(new URL('../', import.meta.url));
const packages = [...readFileSync(path.join(root, 'pages.json'), 'utf8').matchAll(/"root"\s*:\s*"([^"]+)"/g)].map(m => m[1] + '/');
const owner = name => packages.find(p => name.startsWith(p)) || '';
function walk(dir) { return readdirSync(dir,{withFileTypes:true}).flatMap(e=>e.isDirectory()?walk(path.join(dir,e.name)):[path.join(dir,e.name)]); }
test('shared and main-package imports never depend on another subpackage', () => {
 const failures=[];
 for(const dir of ['pages','sheep']) for(const file of walk(path.join(root,dir)).filter(f=>/\.(vue|m?js)$/.test(f))) {
  const from=path.relative(root,file);
  for(const match of readFileSync(file,'utf8').matchAll(/(?:from\s*|import\s*\()\s*['"]([^'"]+)['"]/g)) {
   const spec=match[1]; if(!spec.startsWith('@/')&&!spec.startsWith('.'))continue;
   const to=spec.startsWith('@/')?spec.slice(2):path.relative(root,path.resolve(path.dirname(file),spec));
   if(owner(to)&&owner(to)!==owner(from)) failures.push(`${from} -> ${to}`);
  }
 }
 assert.deepEqual(failures,[]);
});
test('compiled mini program has no cross-package JS or component dependency', {skip:!process.env.CHECK_MP_BUILD}, () => {
 const build=path.join(root,'unpackage/dist/build/mp-weixin');const failures=[];
 for(const file of walk(build).filter(f=>/\.(js|json)$/.test(f))) {
  const from=path.relative(build,file), text=readFileSync(file,'utf8');
  const specs=file.endsWith('.js')?[...text.matchAll(/require\(['"]([^'"]+)['"]\)/g)].map(m=>m[1]):Object.values(JSON.parse(text).usingComponents||{});
  for(const spec of specs) {
   if(spec.startsWith('plugin://'))continue;
   const to=spec.startsWith('/')?spec.slice(1):path.relative(build,path.resolve(path.dirname(file),spec));
   if(owner(to)&&owner(to)!==owner(from))failures.push(`${from} -> ${to}`);
  }
 }
 assert.deepEqual(failures,[]);
});
