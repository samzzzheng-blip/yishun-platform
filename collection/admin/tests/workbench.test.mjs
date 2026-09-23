import { readFileSync } from 'node:fs'
import { createRequire } from 'node:module'
import assert from 'node:assert/strict'
import test from 'node:test'
const require = createRequire(import.meta.url)
const ts = require('typescript')
const source = readFileSync(new URL('../src/views/Home/queues.ts', import.meta.url), 'utf8')
const js = ts.transpileModule(source, { compilerOptions: { module: ts.ModuleKind.ESNext, target: ts.ScriptTarget.ES2022 } }).outputText
const { queues, findMenuPath, todoFilters, applyTodoQuery } = await import('data:text/javascript;base64,' + Buffer.from(js).toString('base64'))
test('queue keys are unique and match backend coverage', () => {
  assert.equal(new Set(queues.map(q => q.key)).size, 15)
  const backend = readFileSync(new URL('../../onebook-backend/onebook-module-app/src/main/java/com/techtron/onebook/module/app/controller/admin/workbench/WorkbenchController.java', import.meta.url), 'utf8')
  assert.deepEqual([...backend.matchAll(/add\(rows, "([^"]+)"/g)].map(m=>m[1]).sort(), queues.map(q=>q.key).sort())
})
test('menu paths use configured parent path, never guessed business names', () => {
  const menus = [{path:'/operations',children:[{path:'return-orders',component:'app/getback/index'}]}]
  assert.equal(findMenuPath(menus,'app/getback/index'),'/operations/return-orders')
  assert.equal(findMenuPath(menus,'app/auction/index'),undefined)
  assert.equal(findMenuPath([{path:'/absolute',component:'/app/getback/index.vue'}],'app/getback/index'),'/absolute')
})
test('normal visits retain existing filters', () => assert.deepEqual(todoFilters({status:'0'},{status:[0]}),{}))
test('status zero and settlement flags survive navigation', () => {
  assert.deepEqual(todoFilters({todo:'x',status:'0'},{status:[0,1]}),{status:0})
  assert.deepEqual(todoFilters({todo:'x',status:'4',pendingSettlement:'true'},{status:[0,4],pendingSettlement:[true,false]}),{status:4,pendingSettlement:true})
})
test('switching queued filters clears previous special filters and rejects invalid values', () => {
  assert.deepEqual(todoFilters({todo:'auction-review',status:'0'},{status:[0,1],delistStatus:[1]}),{status:0,delistStatus:undefined})
  assert.deepEqual(todoFilters({todo:'x',status:['0','1']},{status:[0,1]}),{status:undefined})
  assert.deepEqual(todoFilters({todo:'x',status:'99'},{status:[0,1]}),{status:undefined})
})

test('workbench entry clears cached keyword and user filters but keeps page size', () => {
  const params = {pageNo:8,pageSize:20,keyword:'old search',userId:44,status:3}
  assert.equal(applyTodoQuery(params,{todo:'x',status:'0'},{status:[0]}),true)
  assert.deepEqual(params,{pageNo:1,pageSize:20,keyword:undefined,userId:undefined,status:0})
})

test('every business page connects the filter to existing setup bindings', () => {
  const { parse, compileScript } = require('vue/compiler-sfc')
  for (const component of new Set(queues.map(q => q.component))) {
    const filename = new URL(`../src/views/${component}.vue`, import.meta.url)
    const source = readFileSync(filename, 'utf8')
    const { descriptor } = parse(source)
    const compiled = compileScript(descriptor, { id: component })
    const call = descriptor.scriptSetup.content.match(/useTodoFilter\((\w+),\s*(\w+),/)
    assert.ok(call, `${component}: missing filter integration`)
    assert.ok(compiled.bindings[call[1]], `${component}: undefined filter object ${call[1]}`)
    assert.ok(compiled.bindings[call[2]], `${component}: undefined reload handler ${call[2]}`)
  }
})
