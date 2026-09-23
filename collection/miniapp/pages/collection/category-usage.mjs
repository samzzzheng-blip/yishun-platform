// 2026-09-14 线上 app_collection 按 category_id 统计记录数。
// 排除 deleted=1、status=2；同数量稳定排序，未统计类型保留原序。
// 本文件是统计快照，不是实时排名。
export const categoryUsage = { 1: 4605, 76: 281, 75: 214, 73: 203, 3: 200, 2: 134, 74: 97, 513: 5, 677: 1 };
export function sortCategoriesByUsage(categories) {
  const sorted = categories.map((item, index) => ({ item, index }))
    .sort((a, b) => (categoryUsage[b.item.id] || 0) - (categoryUsage[a.item.id] || 0) || a.index - b.index)
    .map(({ item }) => item);
  // 公共签名卡砖固定第二位，其余分类仍按使用量排序。
  const signatureIndex = sorted.findIndex(item => Number(item.id) === 680);
  if (signatureIndex !== -1) {
    const [signature] = sorted.splice(signatureIndex, 1);
    sorted.splice(Math.min(1, sorted.length), 0, signature);
  }
  return sorted;
}
