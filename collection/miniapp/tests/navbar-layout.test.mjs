// Run after mini-program build: PLAYWRIGHT_CORE_PATH=/path/to/playwright-core node --test tests/navbar-layout.test.mjs
import { test } from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { createRequire } from 'node:module';
const require = createRequire(import.meta.url);
const source = readFileSync(new URL('../sheep/components/s-layout/s-layout.vue', import.meta.url), 'utf8');
test('normal navigation has an independent non-shrinking host and no scroll container transform', () => {
  assert.match(source, /class="page-navbar"/);
  assert.match(source, /<su-navbar\s+fixed/);
  assert.match(source, /flex: 0 0 auto/);
  assert.doesNotMatch(source, /animation:\s*ys-page-arrive|transform:\s*translateY/);
});
test('compiled layout keeps navigation visible on scroll and return at phone sizes', { skip: !process.env.PLAYWRIGHT_CORE_PATH }, async () => {
  const { chromium } = require(process.env.PLAYWRIGHT_CORE_PATH);
  const browser = await chromium.launch({ executablePath: '/Applications/Google Chrome.app/Contents/MacOS/Google Chrome', headless: true });
  try {
    const css = ['sheep/components/s-layout/s-layout.wxss', 'sheep/ui/su-navbar/su-navbar.wxss']
      .map(file => readFileSync(new URL('../unpackage/dist/build/mp-weixin/' + file, import.meta.url), 'utf8'))
      .join('\n').replace(/\[data-v-[^\]]+\]|\.data-v-[\da-f]+/g, '').replace(/@import[^;]+;/g, '');
    for (const [width, height, status] of [[375,812,44], [393,852,59], [812,375,0]]) {
      const page = await browser.newPage({ viewport: { width, height }, reducedMotion: 'reduce' });
      await page.setContent(`<style>${css.replace(/([\d.]+)rpx/g, (_, n) => Number(n)*width/750+'px')}
        body{margin:0}.bg-white{background:white}.row{height:120px}button{min-height:44px;min-width:44px}</style>
        <div class="page-app"><div class="page-main"><div class="page-navbar">
        <div class="uni-navbar" style="height:${44+status}px"><div class="uni-navbar__content uni-navbar__content--fixed">
        <div class="fixed-bg bg-white"></div><div style="height:${status}px"></div>
        <div class="uni-navbar__header" style="height:44px"><button id="back">返回</button><span>我的订单</span></div></div></div></div>
        <div class="page-body"><div id="tabs">全部 待付款 已完成</div>${'<div class="row">订单</div>'.repeat(30)}</div></div></div>`);
      for (let i=0;i<3;i++) {
        await page.evaluate(() => { document.querySelector('.page-body').scrollTop=900; window.scrollTo(0,500); });
        const result = await page.evaluate(() => {
          const nav=document.querySelector('.uni-navbar__content'), back=document.querySelector('#back');
          const r=back.getBoundingClientRect();
          return {top:nav.getBoundingClientRect().top, clickable:document.elementFromPoint(r.left+r.width/2,r.top+r.height/2)===back,
            bodyTransform:getComputedStyle(document.querySelector('.page-body')).transform};
        });
        assert.equal(result.top,0); assert.equal(result.clickable,true); assert.equal(result.bodyTransform,'none');
        await page.evaluate(() => { document.querySelector('.page-app').style.display='none'; });
        await page.evaluate(() => { document.querySelector('.page-app').style.display='flex'; });
      }
      await page.close();
    }
  } finally { await browser.close(); }
});
