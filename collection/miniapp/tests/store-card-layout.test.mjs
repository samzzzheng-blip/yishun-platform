import { test } from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { createRequire } from 'node:module';
const require = createRequire(import.meta.url);

test('collection cards keep long titles and actions inside the card', { skip: !process.env.PLAYWRIGHT_CORE_PATH }, async () => {
  const { chromium } = require(process.env.PLAYWRIGHT_CORE_PATH);
  const browser = await chromium.launch({ executablePath: '/Applications/Google Chrome.app/Contents/MacOS/Google Chrome', headless: true });
  try {
    const css = readFileSync(new URL('../unpackage/dist/build/mp-weixin/pages/index/store.wxss', import.meta.url), 'utf8')
      .replace(/\[data-v-[^\]]+\]|\.data-v-[\da-f]+/g, '').replace(/@import[^;]+;/g, '');
    for (const width of [320, 375, 430, 812]) {
      const page = await browser.newPage({ viewport: { width, height: 900 } });
      for (const scale of [1, 1.5]) {
        const scaled = css.replace(/([\d.]+)rpx/g, (_, n) => Number(n) * width / 750 + 'px');
        await page.setContent(`<style>${scaled}body{margin:16px}view{display:block}button{box-sizing:border-box}
          .list-item .name,.status-wrap{font-size:${15 * scale}px}</style>
          <view class="list-item"><view class="photo"></view><view class="right">
          <view class="name">矢岛晶子 蜡笔小新 签名卡psa双10分ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890</view>
          <view class="stock">库存1</view><view class="id">ID:0000013131</view></view>
          <view class="status-wrap"><view>已登记，待收货</view><view>包裹详情</view>
          <button class="registration-delete">删除登记</button></view></view>`);
        const bounds = await page.evaluate(() => {
          const card = document.querySelector('.list-item').getBoundingClientRect();
          const title = document.querySelector('.right').getBoundingClientRect();
          const status = document.querySelector('.status-wrap').getBoundingClientRect();
          return { overlap: title.right > status.left, statusWidth: status.width,
            overflow: [...document.querySelectorAll('.list-item *')].some(el => {
              const r = el.getBoundingClientRect();
              return r.right > card.right + 1 || r.bottom > card.bottom + 1 || el.scrollWidth > el.clientWidth + 1;
            }) };
        });
        assert.equal(bounds.overlap, false, `overlap at ${width}/${scale}`);
        assert.equal(bounds.overflow, false, `overflow at ${width}/${scale}`);
        assert.ok(bounds.statusWidth >= 95);
      }
      await page.close();
    }
  } finally { await browser.close(); }
});
