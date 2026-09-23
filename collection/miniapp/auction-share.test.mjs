import { test } from 'node:test';
import assert from 'node:assert/strict';
import { readFile } from 'node:fs/promises';
const source = await readFile(new URL('./sheep/api/collection/auction-share.js', import.meta.url), 'utf8');
const { copyAuctionShare } = await import('data:text/javascript;base64,' + Buffer.from(source).toString('base64'));
function fixture(fail = false) {
  const calls = {};
  const ui = {
    setClipboardData(options) { calls.text = options.data; fail ? options.fail() : options.success(); },
    showModal(options) { calls.modal = options; },
    showToast(options) { calls.toast = options; }
  };
  return { calls, ui };
}
test('preserves full original share text instead of generated URL', () => {
  const { calls, ui } = fixture();
  const shareText = '【闲鱼】https://m.tb.cn/test?tk=abc CZ321「拍品」\n点击链接直接打开';
  copyAuctionShare({ shareText, goofishUrl: 'https://www.goofish.com/item?id=1' }, ui);
  assert.equal(calls.text, shareText);
  assert.match(calls.modal.content, /在闲鱼搜索用户/);
});
test('legacy URL gets browser instructions', () => {
  const { calls, ui } = fixture();
  copyAuctionShare({ goofishUrl: 'https://www.goofish.com/item?id=1' }, ui);
  assert.equal(calls.text, 'https://www.goofish.com/item?id=1');
  assert.match(calls.modal.content, /浏览器/);
});
test('missing link and mini-program token do not copy', () => {
  for (const lot of [{}, { shareText: '#小程序://闲鱼/test' }]) {
    const { calls, ui } = fixture();
    copyAuctionShare(lot, ui);
    assert.equal(calls.text, undefined);
    assert.equal(calls.modal.title, '分享内容待补充');
  }
});
test('clipboard failure gives retry feedback', () => {
  const { calls, ui } = fixture(true);
  copyAuctionShare({ goofishUrl: 'https://www.goofish.com/item?id=1' }, ui);
  assert.match(calls.toast.title, /复制失败/);
  assert.equal(calls.modal, undefined);
});
const { auctionMiniLink, openAuctionMiniProgram } = await import('data:text/javascript;base64,' + Buffer.from(source).toString('base64'));
test('用户点击后将真实闲鱼短链接交给微信，成功时不复制', () => {
  const { calls, ui } = fixture(); let jump;
  const lot = { shareText: '#小程序://闲鱼/AQ8Fyn9vNCBpqUD', goofishUrl: 'https://www.goofish.com/item?id=1085809256320' };
  openAuctionMiniProgram(lot, ui, { navigateToMiniProgram(options) { jump = options; } });
  assert.equal(jump.shortLink, lot.shareText); assert.equal(calls.text, undefined);
  jump.fail({ errMsg: 'navigateToMiniProgram:fail cancel' }); assert.equal(calls.modal, undefined);
  jump.fail({ errMsg: 'invalid short link' }); assert.equal(calls.modal.confirmText, '复制链接');
  calls.modal.success({ confirm: true }); assert.equal(calls.text, lot.goofishUrl);
});
test('不支持跳转时可回退，其他小程序链接不可当闲鱼跳转', () => {
  const { calls, ui } = fixture();
  openAuctionMiniProgram({ shareText: '#小程序://闲鱼/test' }, ui, {});
  assert.equal(calls.modal.title, '暂时无法打开闲鱼小程序');
  assert.equal(auctionMiniLink({ shareText: '#小程序://其他/test' }), '');
  assert.equal(auctionMiniLink({ shareText: 'https://www.goofish.com/item?id=1' }), '');
});
test('旧小程序分享配置回退网页，不再阻止复制', () => {
  const { calls, ui } = fixture();
  const url = 'https://www.goofish.com/item?id=1085809256320';
  copyAuctionShare({ shareText: '#小程序://闲鱼/AQ8Fyn9vNCBpqUD', goofishUrl: url }, ui);
  assert.equal(calls.text, url);
  assert.equal(calls.modal.title, '商品网址已复制');
});
test('完整保留用户提供的 App 口令、商品名和换行', () => {
  const { calls, ui } = fixture();
  const shareText = '【闲鱼】https://m.tb.cn/h.8HkG2Af?tk=eVHiTQmtGCt CZ356 「快来捡漏【树脂老货松果老人摆件，头上装饰，不是树脂，不重，具体品相如图】」\n点击链接直接打开';
  copyAuctionShare({ shareText }, ui);
  assert.equal(calls.text, shareText);
});
