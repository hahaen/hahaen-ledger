/** 使用可安全表示的整数分数运算，最终结果四舍五入到分，遵循乘除优先级。 */
import { cents, inputYuan } from './money'

export function calculateAmount(input: string): string {
  const expression = input.replaceAll('−', '-').replaceAll('×', '*').replaceAll('÷', '/')
  if (!/^\d+(\.\d{1,2})?([+\-*/]\d+(\.\d{1,2})?)*$/.test(expression)) throw new Error('算式有误，请检查后重试')
  const tokens = expression.match(/\d+(?:\.\d{1,2})?|[+\-*/]/g) || []
  type Fraction = { numerator: number; denominator: number }
  const gcd = (left: number, right: number): number => right ? gcd(right, left % right) : Math.abs(left)
  const reduce = (numerator: number, denominator: number): Fraction => {
    if (!Number.isSafeInteger(numerator) || !Number.isSafeInteger(denominator)) throw new Error('算式超出精确计算范围，请分步计算')
    const factor = gcd(numerator, denominator)
    return { numerator: numerator / factor, denominator: denominator / factor }
  }
  const values: Fraction[] = []
  const operators: string[] = []
  const apply = () => {
    const right = values.pop()!
    const left = values.pop()!
    const operator = operators.pop()!
    if (operator === '/' && right.numerator === 0) throw new Error('除数不能为 0，请重新输入')
    if (operator === '+' || operator === '-') {
      const common = gcd(left.denominator, right.denominator)
      const l = left.numerator * (right.denominator / common)
      const r = right.numerator * (left.denominator / common)
      if (!Number.isSafeInteger(l) || !Number.isSafeInteger(r)) throw new Error('算式超出精确计算范围，请分步计算')
      values.push(reduce(operator === '+' ? l + r : l - r, left.denominator * (right.denominator / common)))
    } else {
      const numerator = operator === '*' ? right.numerator : right.denominator
      const denominator = operator === '*' ? right.denominator : right.numerator
      const first = gcd(left.numerator, denominator)
      const second = gcd(numerator, left.denominator)
      values.push(reduce((left.numerator / first) * (numerator / second), (left.denominator / second) * (denominator / first)))
    }
  }
  for (const token of tokens) {
    if (/^\d/.test(token)) values.push(reduce(/^0+(\.0{1,2})?$/.test(token) ? 0 : cents(token), 100))
    else {
      while (operators.length && ('*/'.includes(operators[operators.length - 1]) || '+-'.includes(token))) apply()
      operators.push(token)
    }
  }
  while (operators.length) apply()
  const final = values[0]
  const scaled = final.numerator * 100
  if (!Number.isSafeInteger(scaled)) throw new Error('算式超出精确计算范围，请分步计算')
  const result = inputYuan(Math.round(scaled / final.denominator))
  cents(result)
  return result
}

export function validLocalDateTime(value: string): boolean {
  const match = /^(\d{4})-(\d{2})-(\d{2})T(\d{2}):(\d{2})(?::(\d{2}))?$/.exec(value)
  if (!match) return false
  const [, year, month, day, hour, minute, second = 0] = match.map(Number)
  if (year < 1000 || year > 9999 || month < 1 || month > 12 || hour > 23 || minute > 59 || Number(second) > 59) return false
  return day >= 1 && day <= new Date(year, month, 0).getDate()
}

export function backToLedger() {
  if (getCurrentPages().length > 1) uni.navigateBack()
  else uni.switchTab({ url: '/pages/index/index' })
}
