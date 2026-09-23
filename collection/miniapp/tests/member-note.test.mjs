import test from 'node:test';
import assert from 'node:assert/strict';
import { memberNumber, memberNote } from '../pages/collection/member-note.mjs';
test('纸条仅包含当前会员号，不包含备注或包裹编号', () => {
  assert.equal(memberNumber(674), '674');
  assert.equal(memberNote(674, '卡砖2件'), '会员号：674');
  assert.equal(memberNote(305, ''), '会员号：305');
});
test('会员号缺失时不生成错误纸条', () => {
  for (const id of [null, undefined, '', 0, -1, 'undefined']) {
    assert.equal(memberNumber(id), '');
    assert.equal(memberNote(id, '备注'), '');
  }
});
