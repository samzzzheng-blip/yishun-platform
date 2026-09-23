// 兼容历史单图及新多图，空数组不能遮住仍有效的旧图片。
export function collectionPictures(item) {
  function parse(value) {
    if (typeof value === 'string') {
      const text = value.trim();
      if (!text) return [];
      if (text.startsWith('[')) {
        try { return parse(JSON.parse(text)); } catch { return []; }
      }
      return [text];
    }
    return Array.isArray(value) ? value.filter(v => typeof v === 'string' && v.trim()).map(v => v.trim()) : [];
  }
  const pictures = parse(item?.picUrls);
  return pictures.length ? pictures : parse(item?.picUrl);
}

export function collectionCover(item) {
  return collectionPictures(item)[0] || '';
}
