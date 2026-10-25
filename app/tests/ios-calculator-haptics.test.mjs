import { readFileSync } from 'node:fs'
import { test } from 'node:test'
import assert from 'node:assert/strict'
import ts from 'typescript'

const source = readFileSync(new URL('../src/utils/iosCalculatorHaptics.ts', import.meta.url), 'utf8')
const code = ts.transpileModule(source, { compilerOptions: { module: ts.ModuleKind.CommonJS } }).outputText
function supports(navigator, document) {
  const exports = {}
  new Function('exports', 'navigator', 'document', code)(exports, navigator, document)
  return exports.supportsIosCalculatorHaptics()
}
const safari = 'Mozilla/5.0 (iPhone; CPU iPhone OS 18_0 like Mac OS X) AppleWebKit/605.1.15 Version/18.0 Mobile/15E148 Safari/604.1'
const nativeSwitch = { createElement: () => ({ switch: false }) }
test('只对支持原生 switch 的 iPhone Safari 启用覆盖层', () => {
  assert.equal(supports({ userAgent: safari }, nativeSwitch), true)
  assert.equal(supports({ userAgent: safari }, { createElement: () => ({}) }), false)
  assert.equal(supports({ userAgent: 'Mozilla/5.0 (Macintosh; Intel Mac OS X 15_0) AppleWebKit/605.1.15 Version/18.0 Safari/605.1.15' }, nativeSwitch), false)
  assert.equal(supports({ userAgent: safari.replace('Version/18.0', 'CriOS/130.0') }, nativeSwitch), false)
  assert.equal(supports({ userAgent: 'Mozilla/5.0 (Linux; Android 16) Chrome/140.0 Mobile Safari/537.36' }, nativeSwitch), false)
})
test('非浏览器环境不访问原生 DOM', () => {
  assert.equal(supports(undefined, undefined), false)
  assert.equal(supports({ userAgent: safari }, undefined), false)
})
