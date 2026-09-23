import { readFileSync } from 'node:fs'
import assert from 'node:assert/strict'
import test from 'node:test'

const source = readFileSync(new URL('../src/views/app/yikoujia/ConfigForm.vue', import.meta.url), 'utf8')
const expression = source.match(/const data = (\{ \.\.\.formData\.value[^\r\n]+\})/)[1]
const requestData = new Function('formData', `return ${expression}`)
for (const price of [7299, 0.29, 19.99]) {
  test(`price ${price} stays in yuan across failed retries`, () => {
    const formData = { value: { price, name: 'test' } }
    for (let retry = 0; retry < 3; retry++) {
      const request = requestData(formData)
      assert.equal(request.price, Math.round(price * 100))
      assert.equal(formData.value.price, price)
      assert.notEqual(request, formData.value)
    }
  })
}
