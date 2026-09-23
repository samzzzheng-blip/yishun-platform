export function memberNumber(id) {
  const text = String(id ?? '');
  return /^[1-9]\d*$/.test(text) ? text : '';
}
export function memberNote(id) {
  const number = memberNumber(id);
  if (!number) return '';
  return `会员号：${number}`;
}
