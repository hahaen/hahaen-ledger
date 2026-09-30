/** 仅转换绘图坐标，不参与业务金额计算。带符号 log1p 保留零值与负值。 */
export function itemCostScale(values: number[]) {
  const max = Math.max(0, ...values)
  const min = Math.min(0, ...values)
  const magnitudes = values.filter(value => value !== 0).map(Math.abs)
  const logarithmic = magnitudes.length > 1
    && Math.max(...magnitudes) >= 100
    && Math.max(...magnitudes) / Math.min(...magnitudes) >= 20
  const transform = (value: number) => logarithmic ? Math.sign(value) * Math.log1p(Math.abs(value) / 100) : value
  const inverse = (value: number) => logarithmic ? Math.sign(value) * Math.expm1(Math.abs(value)) * 100 : value
  const low = transform(min), high = transform(max), range = high - low || 100
  return {
    logarithmic,
    position: (value: number) => (transform(value) - low) / range,
    ticks: Array.from({ length: 5 }, (_, index) => inverse(low + range * index / 4)),
  }
}
