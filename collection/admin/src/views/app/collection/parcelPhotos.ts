// JDBC returns the JSON gallery as text; also accept already-decoded API arrays.
export function parcelPhotos(item: { pic_urls?: unknown; pic_url?: unknown }): string[] {
  let gallery = item.pic_urls
  if (typeof gallery === 'string') {
    try {
      gallery = JSON.parse(gallery)
    } catch {
      gallery = []
    }
  }
  const values = [item.pic_url, ...(Array.isArray(gallery) ? gallery : [])]
  return [...new Set(values.filter((v): v is string => typeof v === 'string')
    .map((v) => v.trim()).filter(Boolean))]
}
