import { readFile } from 'node:fs/promises'
import assert from 'node:assert/strict'
import { test } from 'node:test'
import ts from 'typescript'
import { computed } from 'vue'

const navigation = await readFile(new URL('../src/components/NativeNavigation.vue', import.meta.url), 'utf8')
const script = navigation.match(/<script setup lang="ts">([\s\S]*?)<\/script>/)[1]
const compiled = ts.transpileModule(script + '\nexport { menuAlignedStyle }', {
  compilerOptions: { module: ts.ModuleKind.CommonJS, target: ts.ScriptTarget.ESNext },
}).outputText
function navigationStyle(input, menu) {
  const exports = {}
  new Function('require', 'exports', 'defineProps', 'withDefaults', 'defineEmits', compiled)(
    name => name === 'vue' ? { computed, ref: value => ({ value }), getCurrentInstance: () => null, nextTick: () => {}, onMounted: () => {} } : name.includes('nativeNavigation') ? { getNativeMenuMetrics: () => menu } : {},
    exports, () => input, (props, defaults) => ({ ...defaults, ...props }), () => () => {},
  )
  return exports.menuAlignedStyle.value
}

test('待办三个实际调用方与通知中心标题同高，微信胶囊上下对齐', async () => {
  const menu = { top: 60, height: 32, rootInset: 54, rightPadding: 100 }
  for (const name of ['notification-center', 'ha-todo', 'ha-todo-detail', 'ha-todo-editor']) {
    const page = await readFile(new URL(`../src/pages/${name}/${name}.vue`, import.meta.url), 'utf8')
    const call = page.match(/<NativeNavigation\b[^>]+>/)[0]
    const extra = Number(call.match(/:page-top-extra="(\d+)"/)?.[1] ?? 0)
    const style = navigationStyle({ variant: 'help', pageTopExtra: extra }, menu)
    const actualTop = menu.rootInset + 20 + parseFloat(style.marginTop)
    assert.equal(actualTop, menu.top, `${name} 应对齐胶囊顶部`)
    assert.equal(style.height, `${menu.height}px`)
  }
})

test('H5无微信胶囊时20px补偿不改变原布局', () => {
  assert.deepEqual(navigationStyle({ variant: 'help', pageTopExtra: 20 }, null),
    navigationStyle({ variant: 'help', pageTopExtra: 0 }, null))
})
