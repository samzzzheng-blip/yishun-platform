import { floatToFixed2 } from '@/utils'

// 格式化金额【分转元】
// @ts-ignore
export const fenToYuanFormat = (_, __, cellValue: any, ___) => {
  if (cellValue && cellValue > 0) return `￥${floatToFixed2(cellValue)}`
  else {
    return ''
  }
}
