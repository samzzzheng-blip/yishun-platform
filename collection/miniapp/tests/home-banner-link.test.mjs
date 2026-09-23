import test from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { getAdProductUrl } from '../pages/index/ad-link.mjs';

test('每个广告使用后台配置的商品，修改和清空均生效', () => {
  assert.equal(getAdProductUrl({id:8,targetProductId:672}), '/pages/collection/good?id=672');
  assert.equal(getAdProductUrl({id:8,targetProductId:700}), '/pages/collection/good?id=700');
  assert.equal(getAdProductUrl({id:7,targetProductId:'701'}), '/pages/collection/good?id=701');
  for (const targetProductId of [null, undefined, '', 0, -1, 1.5, 'bad', '7&other=1']) {
    assert.equal(getAdProductUrl({id:8,targetProductId}), '');
  }
});
test('广告点击商品不同时弹出图片预览', () => {
  const source = readFileSync(new URL('../pages/index/index.vue', import.meta.url), 'utf8');
  assert.match(source, /:isPreview="false"/);
  assert.match(source, /url: getAdProductUrl\(item\)/);
  const swiper = readFileSync(new URL('../sheep/ui/su-swiper/su-swiper.vue', import.meta.url), 'utf8');
  assert.match(swiper, /sheep\.\$router\.go\(item.url\)/);
  assert.match(swiper, /if \(!props.isPreview\) return/);
});
