// Merge newest-first paginated feeds without losing records or reordering earlier pages.
export function createMergedOrders(fetchOrders, fetchExchanges, pageSize = 10, fetchAuctions) {
  const fetchers = [fetchOrders, fetchExchanges, ...(fetchAuctions ? [fetchAuctions] : [])];
  let feeds;
  const reset = () => { feeds = fetchers.map(() => ({ page: 1, done: false, buffer: [] })); };
  reset();
  const timestamp = item => {
    if (typeof item.createTime === 'number') return item.createTime;
    const raw = String(item.createTime || '');
    const value = raw.includes('T') ? Date.parse(raw) : Date.parse(raw.replace(/-/g, '/'));
    return Number.isNaN(value) ? 0 : value;
  };
  return {
    reset,
    async next() {
      // Commit only after the whole page succeeds, so retries cannot skip either feed.
      const work = feeds.map(feed => ({ ...feed, buffer: [...feed.buffer] }));
      const list = [];
      while (list.length < pageSize) {
        await Promise.all(work.map(async (feed, index) => {
          if (feed.buffer.length || feed.done) return;
          const response = await fetchers[index]({ pageNo: feed.page, pageSize });
          if (response?.code !== 0 || !Array.isArray(response.data?.list)) {
            throw new Error(response?.msg || '订单加载失败');
          }
          const rows = response.data.list;
          feed.buffer = rows.map(item => index === 2
            ? { ...item, isAuction: true, isExchange: false, name: item.collectionName || '拍卖藏品', price: item.grossAmount ?? item.currentPrice, picUrl: item.displayPicUrls?.length ? item.displayPicUrls : item.picUrls || [] }
            : index === 1
            ? { ...item, isExchange: true, name: item.exchangeName || '兑换商品' }
            : { ...item, isExchange: false });
          feed.done = rows.length < pageSize || feed.page * pageSize >= Number(response.data.total);
          feed.page += 1;
        }));
        let index = -1;
        work.forEach((feed, candidate) => {
          if (feed.buffer.length && (index < 0 || timestamp(feed.buffer[0]) > timestamp(work[index].buffer[0]))) index = candidate;
        });
        if (index < 0) break;
        list.push(work[index].buffer.shift());
      }
      feeds = work;
      return { list, finished: feeds.every(feed => feed.done && !feed.buffer.length) };
    },
  };
}
