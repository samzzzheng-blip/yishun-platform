import test from 'node:test';
import assert from 'node:assert/strict';
import { createMergedOrders } from '../pages/order/merged-orders.mjs';
const feed = rows => async ({ pageNo, pageSize }) => ({ code: 0, data: {
  total: rows.length, list: rows.slice((pageNo - 1) * pageSize, pageNo * pageSize),
} });
test('全部合并两类订单，保留同号记录，跨页按时间排序', async () => {
  const a = Array.from({ length: 25 }, (_, i) => ({ id: 30-i, createTime: 100-i }));
  const b = [{ id: 30, exchangeName: '测试', createTime: 98.5 }, { id: 29, createTime: 1 }];
  const loader = createMergedOrders(feed(a), feed(b));
  let rows = [], result;
  do { result = await loader.next(); rows.push(...result.list); } while (!result.finished);
  assert.equal(rows.length, 27);
  assert.equal(rows[2].name, '测试');
  assert.equal(rows.filter(r => r.id === 30).length, 2);
  assert.deepEqual(rows.map(r => r.createTime), [...a, ...b].map(r => r.createTime).sort((x,y) => y-x));
  loader.reset(); assert.equal((await loader.next()).list.length, 10);
});
test('任一接口失败后重试不丢记录，不假装加载完成', async () => {
  let failed = true;
  const loader = createMergedOrders(feed([{ id: 1, createTime: 2 }]), async params => {
    if (failed) throw new Error('network');
    return feed([{ id: 2, createTime: 1 }])(params);
  });
  await assert.rejects(loader.next()); failed = false;
  const result = await loader.next();
  assert.equal(result.list.length, 2); assert.equal(result.finished, true);
});
test('只有兑换记录以及两类均空时正常显示', async () => {
  const loader = createMergedOrders(feed([]), feed([{ id: 105, exchangeName: '测试' }]));
  assert.equal((await loader.next()).list[0].isExchange, true);
  assert.deepEqual(await createMergedOrders(feed([]), feed([])).next(), { list: [], finished: true });
});
test('拍卖成交作为第三类分页，保留同号订单、金额和完整图片', async () => {
  const auctions = Array.from({ length: 23 }, (_, i) => ({ id: 30-i, createTime: 99-i, status: 3,
    collectionName: '签名卡砖', currentPrice: 100, grossAmount: 2500, feeAmount: 125,
    sellerIncome: 2375, displayPicUrls: ['full.jpg'] }));
  const loader = createMergedOrders(feed([{ id: 30, createTime: 100 }]), feed([{ id: 30, createTime: 98.5 }]), 10, feed(auctions));
  let rows = [], result;
  do { result = await loader.next(); rows.push(...result.list); } while (!result.finished);
  assert.equal(rows.length, 25);
  assert.equal(rows.filter(r => r.id === 30).length, 3);
  assert.equal(rows.filter(r => r.isAuction).length, 23);
  assert.equal(rows[1].name, '签名卡砖');
  assert.equal(rows[1].price, 2500);
  assert.equal(rows[1].sellerIncome, 2375);
  assert.deepEqual(rows[1].picUrl, ['full.jpg']);
  assert.deepEqual(rows.map(r => r.createTime), [...rows.map(r => r.createTime)].sort((a,b) => b-a));
  loader.reset(); assert.equal((await loader.next()).list[1].isAuction, true);
});
test('拍卖接口失败后重试不会丢失其他来源，已完成可仅有拍卖', async () => {
  let fail = true;
  const loader = createMergedOrders(feed([]), feed([]), 10, async params => {
    if (fail) return { code: 500 };
    return feed([{ id: 1, status: 3, createTime: 1 }])(params);
  });
  await assert.rejects(loader.next()); fail = false;
  const result = await loader.next();
  assert.equal(result.list.length, 1);
  assert.equal(result.list[0].isAuction, true);
  assert.equal(result.finished, true);
});
