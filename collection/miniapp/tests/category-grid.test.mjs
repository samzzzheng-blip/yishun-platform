import test from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
test('登记类别改为三列且保留选择和自定义', () => {
 const source = readFileSync(new URL('../pages/collection/add.vue', import.meta.url), 'utf8');
 assert.match(source, /grid-template-columns: repeat\(3, minmax\(0, 1fr\)\)/);
 assert.match(source, /@tap="onSelect\(item\)"/);
 assert.match(source, /@tap="addCustom"/);
 assert.match(source, /@tap="saveCustom"/);
 assert.match(source, /grid-column: 1 \/ -1/);
 assert.match(source, /min-height: 96rpx/);
 assert.match(source, /white-space: normal/);
});
