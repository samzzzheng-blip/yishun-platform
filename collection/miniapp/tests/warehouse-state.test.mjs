import test from 'node:test';
import assert from 'node:assert/strict';
import { warehouseStatus, warehouseAction, isGetbackSelectable } from '../sheep/helper/warehouse-state.mjs';
const held = { stock: 1, availableStock: 0, getbackStatus: 0, tradeStatus: 2, listingId: 1 };
test('待上架和在售仍显示持有库存，提供不同操作', () => {
  const pending = { ...held, saleType: 'direct', listingStatus: 0 };
  assert.equal(warehouseStatus(pending), '待上架');
  assert.equal(warehouseAction(pending), '取消上架');
  assert.equal(isGetbackSelectable(pending), true);
  assert.equal(warehouseAction({ ...pending, listingStatus: 3 }), '下架');
});
test('竞拍必须独立管理，不能走直购取回', () => {
  const auction = { ...held, saleType: 'auction', listingStatus: 0 };
  assert.equal(warehouseAction(auction), '取消送拍');
  assert.equal(isGetbackSelectable(auction), false);
  assert.equal(warehouseAction({ ...auction, listingStatus: 1 }), '管理竞拍');
  assert.equal(warehouseStatus({ ...auction, listingStatus: 2 }), '待确认成交');
});
test('零库存和取回处理中无重复操作', () => {
  for (const item of [{ ...held, stock: 0 }, { ...held, getbackStatus: 1 }, { ...held, getbackStatus: 2 }]) {
    assert.equal(warehouseAction(item), '');
    assert.equal(isGetbackSelectable(item), false);
  }
});
