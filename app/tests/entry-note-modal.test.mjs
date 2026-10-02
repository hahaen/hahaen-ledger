import { readFileSync } from 'node:fs'
import { test } from 'node:test'
import assert from 'node:assert/strict'
import { parse } from 'vue/compiler-sfc'
import ts from 'typescript'
import { ref } from 'vue'

const source = readFileSync(new URL('../src/pages/entry/entry.vue', import.meta.url), 'utf8')
const { descriptor } = parse(source)
const nodes = []
function walk(node) {
  nodes.push(node)
  for (const child of node.children || []) walk(child)
}
walk(descriptor.template.ast)
const hasClass = (node, name) => node.props?.some(prop => prop.name === 'class' && prop.value?.content === name)
const events = node => node.props.filter(prop => prop.type === 7 && prop.name === 'on')
const backdrop = nodes.find(node => hasClass(node, 'entry-note-picker-backdrop'))

test('新增和编辑共用备注输入框，内容点击与触摸不能触发祖先关闭或拦截', () => {
  assert.deepEqual(events(backdrop), [])
  const mask = backdrop.children.find(node => hasClass(node, 'picker-touch-mask'))
  assert.ok(mask, '关闭路径只绑定独立遮罩')
  assert.equal(events(mask).find(prop => prop.arg.content === 'click').exp.content, "modal = ''")
  const modal = backdrop.children.find(node => hasClass(node, 'entry-note-picker-modal'))
  assert.ok(modal.children.some(node => node.tag === 'textarea'))
  assert.ok(!events(modal).some(prop => prop.arg.content === 'click'))
})

test('新增空备注和编辑已有备注：完成回填、取消丢弃草稿、禁用时不打开', () => {
  const start = source.indexOf('function openModal(')
  const end = source.indexOf('function selectAccount(', start)
  const js = ts.transpileModule(source.slice(start, end) + '\nexports.openModal = openModal; exports.confirmModal = confirmModal;', { compilerOptions: { module: ts.ModuleKind.CommonJS } }).outputText
  for (const original of ['', '已有备注']) {
    const state = Object.fromEntries(['modal', 'note', 'draftNote', 'dateTime', 'draftDate', 'draftTime', 'draftAccountIndex', 'accountIndex', 'toIndex', 'locked', 'refundedCents'].map(key => [key, ref('')]))
    state.note.value = original
    state.dateTime.value = '2026-10-03T12:30:00'
    const actions = {}
    new Function(...Object.keys(state), 'exports', js)(...Object.values(state), actions)
    actions.openModal('note')
    assert.equal(state.draftNote.value, original)
    state.draftNote.value = '测试备注\n第二行'
    actions.confirmModal()
    assert.equal(state.note.value, '测试备注\n第二行')
    assert.equal(state.modal.value, '')
    actions.openModal('note')
    state.draftNote.value = '取消的修改'
    const cancel = nodes.find(node => hasClass(node, 'entry-note-picker-cancel'))
    new Function('modal', events(cancel)[0].exp.content.replace('modal =', 'modal.value ='))(state.modal)
    actions.openModal('note')
    assert.equal(state.draftNote.value, '测试备注\n第二行')
    state.modal.value = ''
    state.locked.value = true
    actions.openModal('note')
    assert.equal(state.modal.value, '')
  }
})
