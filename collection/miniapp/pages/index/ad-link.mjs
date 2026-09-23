export function getAdProductUrl(ad) {
  const id = Number(ad?.targetProductId)
  return Number.isSafeInteger(id) && id > 0 ? `/pages/collection/good?id=${id}` : ''
}
