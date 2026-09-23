export function auctionAppShare(lot) {
  const text = (lot?.shareText || '').trim();
  return text.includes('#小程序://') ? '' : text;
}

export function copyAuctionShare(lot, ui) {
  const share = auctionAppShare(lot);
  const text = share || (lot.goofishUrl || '').trim();
  if (!text || text.startsWith('#小程序://')) {
    ui.showModal({ title: '分享内容待补充', content: '请联系客服获取此拍品的闲鱼分享链接。', showCancel: false });
    return;
  }
  ui.setClipboardData({ data: text,
    success: () => ui.showModal({
      title: share ? '闲鱼分享内容已复制' : '商品网址已复制',
      content: share
        ? '请在浏览器中打开分享内容里的网址，或在闲鱼搜索用户“一瞬收藏仓”，进入商铺查看商品。'
        : '请将网址粘贴到浏览器地址栏打开，或在闲鱼搜索用户“一瞬收藏仓”，进入商铺查看商品。',
      showCancel: false
    }),
    fail: () => ui.showToast({ title: '复制失败，请重试或联系客服', icon: 'none' })
  });
}

export function auctionMiniLink(lot) {
  const value = (lot?.shareText || '').trim();
  return /^#小程序:\/\/闲鱼\/[^\s]+$/.test(value) ? value : '';
}

export function openAuctionMiniProgram(lot, ui, wxApi) {
  const shortLink = auctionMiniLink(lot);
  const fallback = () => ui.showModal({
    title: '暂时无法打开闲鱼小程序',
    content: '可以复制商品网页链接，在浏览器中打开。',
    confirmText: '复制链接',
    success: ({ confirm }) => { if (confirm) copyAuctionShare({ goofishUrl: lot.goofishUrl }, ui); },
  });
  if (!shortLink) { copyAuctionShare(lot, ui); return; }
  if (typeof wxApi?.navigateToMiniProgram !== 'function') { fallback(); return; }
  try {
    wxApi.navigateToMiniProgram({ shortLink,
      fail: (error) => { if (!/cancel/i.test(error?.errMsg || '')) fallback(); },
    });
  } catch { fallback(); }
}

export function copyAuctionShop(ui) {
  ui.setClipboardData({ data: '一瞬收藏仓',
    success: () => ui.showModal({
      title: '闲鱼用户名已复制',
      content: '请打开闲鱼，粘贴“一瞬收藏仓”并搜索用户，进入该用户的商铺查看商品。',
      showCancel: false,
    }),
    fail: () => ui.showToast({ title: '复制失败，请重试', icon: 'none' }),
  });
}
