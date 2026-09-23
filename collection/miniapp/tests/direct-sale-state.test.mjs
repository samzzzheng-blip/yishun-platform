import test from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { canDelist, directSaleStatus } from '../sheep/components/s-my-sell-list/direct-sale-state.mjs';

test('待上架商品显示状态且可以取消上架', () => {
  for (const status of [0, '0']) {
    const item = { saleType: 'direct', status };
    assert.equal(directSaleStatus(item), '待上架');
    assert.equal(canDelist(item), true);
  }
});

test('已上架直购保留下架操作，其他状态和竞拍不使用直购下架', () => {
  for (const status of [3, '3']) {
    assert.equal(canDelist({ saleType: 'direct', status }), true);
    assert.equal(directSaleStatus({ saleType: 'direct', status }), '已上架');
  }
  for (const status of [undefined, null, 1, 2, 4, 5, 6]) {
    assert.equal(canDelist({ saleType: 'direct', status }), false);
  }
  assert.equal(canDelist({ saleType: 'auction', status: 3 }), false);
});

test('模板和事件处理均限制下架操作', () => {
  const source = readFileSync(new URL('../sheep/components/s-my-sell-list/s-my-sell-list.vue', import.meta.url), 'utf8');
  assert.match(source, /v-if="canDelist\(item\)"/);
  assert.match(source, /if \(!canDelist\(item\) \|\| delistingId/);
  assert.match(source, /等待工作人员上架/);
});

test('卖家列表保留用户隔离且只包括待上架和已上架', () => {
  const source = readFileSync(new URL('../../onebook-backend/onebook-module-app/src/main/java/com/techtron/onebook/module/app/service/yikoujia/YikoujiaServiceImpl.java', import.meta.url), 'utf8');
  const method = source.split('public List<YikoujiaRespVO> getMyYikoujiaList(')[1].split('return result;')[0];
  assert.match(method, /\.eq\(YikoujiaDO::getUserId, pageReqVO.getUserId\(\)\)/);
  assert.match(method, /\.in\(YikoujiaDO::getStatus, STATUS_OFF_SHELF, STATUS_ON_SALE\)/);
});
