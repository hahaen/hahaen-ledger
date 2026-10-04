import { readFile } from 'node:fs/promises'
import assert from 'node:assert/strict'
import { test } from 'node:test'
import ts from 'typescript'
const page = await readFile(new URL('../src/pages/account/account.vue', import.meta.url), 'utf8')
const money = ts.transpile(await readFile(new URL('../src/utils/money.ts', import.meta.url), 'utf8'), {module:ts.ModuleKind.ES2022})
const { inputYuan } = await import(`data:text/javascript;base64,${Buffer.from(money).toString('base64')}`)
const parse = new Function(`${ts.transpile(page.slice(page.indexOf('function centsFromInput'), page.indexOf('async function save()')))};return centsFromInput`)()
test('账户编辑金额回填必须可以原样保存，包括千元、分、负余额', () => {
  assert.match(page, /fundBalance\.value = inputYuan/)
  assert.match(page, /creditLimit\.value = inputYuan/)
  assert.match(page, /currentDebt\.value = inputYuan/)
  for (const cents of [100000, 12345678, -123456, 0, 99999999999]) assert.equal(parse(inputYuan(cents), '金额', true), cents)
})
test('账户流水接入后续分页并显示服务端总数', () => {
  assert.match(page, /@scrolltolower="loadMoreRecords"/)
  assert.match(page, /recordsTotal/)
  assert.match(page, /pageSize=/)
})
test('物品图表使用普通视图，避免微信原生canvas穿透固定操作栏', async () => {
  const chart = await readFile(new URL('../src/components/ItemCostChart.vue', import.meta.url), 'utf8')
  assert.doesNotMatch(chart, /<canvas\b/)
})

test('流水快速切换筛选后丢弃旧响应，失败重试保留已经加载的记录', async () => {
  const { ref, computed } = await import('vue')
  const records = ref([]), recordsTotal = ref(0), recordsPage = ref(0), recordsLoading = ref(false), recordsError = ref(false), recordFilter = ref(''), id = ref('1')
  const recordsHasMore = computed(() => recordsPage.value === 0 || records.value.length < recordsTotal.value)
  const pending = []
  const request = url => new Promise((resolve, reject) => pending.push({url,resolve,reject}))
  const body = ts.transpile(page.slice(page.indexOf('async function loadRecords()'),page.indexOf('function openEdit()')), {target:ts.ScriptTarget.ES2022})
  const {loadRecords,loadMoreRecords} = new Function('records','recordsTotal','recordsPage','recordsLoading','recordsError','recordFilter','id','recordsHasMore','request',`let recordsSequence=0;${body};return {loadRecords,loadMoreRecords}`)(records,recordsTotal,recordsPage,recordsLoading,recordsError,recordFilter,id,recordsHasMore,request)
  const old = loadRecords()
  recordFilter.value = 'EXPENSE'
  const fresh = loadRecords()
  assert.match(pending[1].url, /page=1&pageSize=50&type=EXPENSE/)
  pending[1].resolve({items:[{id:'new'}],total:2,page:1,pageSize:50})
  await fresh
  pending[0].resolve({items:[{id:'old'}],total:10,page:1,pageSize:50})
  await old
  assert.deepEqual(records.value.map(row=>row.id),['new'])
  const failure = loadMoreRecords()
  await loadMoreRecords() // 同一页进行中不重复发起
  assert.equal(pending.length,3)
  pending[2].reject(new Error('fixture'))
  await failure
  assert.equal(recordsError.value,true)
  assert.deepEqual(records.value.map(row=>row.id),['new'])
  const retry = loadMoreRecords()
  assert.match(pending[3].url,/page=2/)
  pending[3].resolve({items:[{id:'last'}],total:2,page:2,pageSize:50})
  await retry
  assert.deepEqual(records.value.map(row=>row.id),['new','last'])
  assert.equal(recordsHasMore.value,false)
})
