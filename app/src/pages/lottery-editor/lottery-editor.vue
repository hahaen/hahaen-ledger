<script lang="ts">
// #ifdef MP-WEIXIN
import { createWechatShareMessage } from '../../utils/wechatShare'
// #endif
export default {
// #ifdef MP-WEIXIN
  onShareAppMessage: createWechatShareMessage,
// #endif
}
</script>
<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import NativeNavigation from '../../components/NativeNavigation.vue'
import MoneyDisplay from '../../components/MoneyDisplay.vue'
import EntryDateTimePicker from '../../components/EntryDateTimePicker.vue'
import LotteryConfirm from '../../components/LotteryConfirm.vue'
import { lotteryApi, request } from '../../utils/api'
import { gameName, today, validateLottery, type LotteryGame, type LotteryPayload } from '../../utils/lottery'
const boundPlan = ref(false)
const mode = ref('single'), id = ref(''), loading = ref(true), saving = ref(false), error = ref(''), dateOpen = ref(false), discard = ref(false)
const form = reactive({ game: 'DLT' as LotteryGame, name: '', red: '', blue: '', multiple: '1', additional: false, issue: '', periods: '20', purchasedOn: today(), note: '', confirmedPurchase: false })
const title = computed(() => mode.value === 'purchase' ? '登记实际购买' : mode.value === 'plan' ? id.value ? '编辑定投计划' : '新建定投计划' : id.value ? '修改购买记录' : '添加购买记录')
const cost = computed(() => { const m = Number(form.multiple), n = mode.value === 'purchase' ? Number(form.periods) : 1; return Number.isInteger(m) && m > 0 && m <= 99 && Number.isInteger(n) && n > 0 && n <= 20 ? (form.additional ? 300 : 200) * m * n : null })
let initial = '', lastSignature = '', key = ''
function checked(event: Event) { return (event as Event & { detail: { value: boolean } }).detail.value }
function signature() { return JSON.stringify(form) }
function leave() { if (saving.value) return; if (!loading.value && signature() !== initial) { discard.value = true; return } goBack() }
function goBack() { if (getCurrentPages().length > 1) uni.navigateBack(); else uni.redirectTo({ url: '/pages/lottery/lottery' }) }
async function initialize(options: Record<string, string | undefined>) {
  try {
    if (!['single', 'plan', 'purchase', 'entry'].includes(options.mode || 'single')) throw new Error('页面参数无效')
    mode.value = options.mode || 'single'; id.value = options.id || ''; form.game = options.game === 'SSQ' ? 'SSQ' : 'DLT'
    const profile = await request<{ lotteryEnabled: boolean }>('/api/app/user/profile'); if (!profile.lotteryEnabled) throw new Error('未开通彩票功能')
    if (id.value) {
      const record = await lotteryApi.detail(mode.value === 'entry' ? 'entry' : 'plan', id.value)
      if (mode.value === 'entry' && !record.editable) throw new Error('此记录已锁定，不能修改')
      boundPlan.value = Boolean(record.planId); form.game = record.game; form.name = record.name || ''; form.red = record.red || ''; form.blue = record.blue || ''; form.multiple = String(record.multiple || 1); form.additional = record.additional === 1
      if (mode.value !== 'purchase') { form.issue = record.issue || ''; form.purchasedOn = record.purchasedOn || today(); form.note = record.note || '' }
    }
    initial = signature(); loading.value = false
  } catch (e) { error.value = e instanceof Error ? e.message : '加载失败，请重试' }
}
let optionsSnapshot: Record<string, string | undefined> = {}
onLoad(options => { optionsSnapshot = options || {}; void initialize(optionsSnapshot) })
async function save() {
  if (saving.value || loading.value) return
  try {
    const payload: LotteryPayload = validateLottery({ ...form, multiple: Number(form.multiple), periods: mode.value === 'purchase' ? Number(form.periods) : 1, idempotencyKey: '' }, mode.value)
    const sig = JSON.stringify(payload); if (lastSignature !== sig) { lastSignature = sig; key = `lottery-${Date.now()}-${Math.random().toString(36).slice(2)}` } payload.idempotencyKey = key
    saving.value = true; error.value = ''
    let result: string
    if (mode.value === 'purchase') result = await lotteryApi.purchase(id.value, payload)
    else if (id.value) result = await lotteryApi.edit(mode.value === 'entry' ? 'entry' : 'plan', id.value, payload)
    else result = await lotteryApi.create(mode.value === 'plan' ? 'plan' : 'single', payload)
    initial = signature(); uni.showToast({ title: '已保存', icon: 'none' })
    if (id.value) goBack()
    else uni.redirectTo({ url: `/pages/lottery-detail/lottery-detail?kind=${mode.value === 'plan' ? 'plan' : 'entry'}&id=${encodeURIComponent(result)}` })
  } catch (e) { uni.showToast({ title: e instanceof Error ? e.message : '保存失败，请重试', icon: 'none' }) }
  finally { saving.value = false }
}
</script>
<template>
  <view class="page profile-page lottery-page lottery-with-actions">
    <NativeNavigation variant="help" :page-top-extra="20" :title="title" back-label="返回" @back="leave" />
    <text v-if="loading && !error" class="profile-loading">正在加载…</text>
    <view v-if="loading && error" class="lottery-notice"><text>{{ error }}</text><button class="text-button" @click="error = ''; initialize(optionsSnapshot)">重新加载</button></view>
    <template v-if="!loading">
      <view class="profile-hero lottery-hero"><text class="profile-eyebrow">{{ gameName(form.game) }}</text><text class="profile-hero-title">{{ title }}</text><text class="profile-hero-copy">{{ mode === 'plan' ? '计划只保存默认号码，确认实际购买后才计成本' : '请按实际票面填写，号码与期次独立留档' }}</text></view>
      <view class="lottery-card lottery-form">
        <view v-if="mode === 'plan'" class="field"><label>计划名称 *</label><input v-model="form.name" maxlength="60" :disabled="saving" placeholder="给这组固定号码起个名字" /></view>
        <view class="field"><label>{{ form.game === 'DLT' ? '前区：01～35，选5个' : '红球：01～33，选6个' }} *</label><input v-model="form.red" maxlength="40" :disabled="saving || mode === 'purchase'" placeholder="用空格或逗号分隔号码" /><text v-if="mode === 'purchase'" class="lottery-hint">本批沿用当前计划号码，换号请先编辑计划</text></view>
        <view class="field"><label>{{ form.game === 'DLT' ? '后区：01～12，选2个' : '蓝球：01～16，选1个' }} *</label><input v-model="form.blue" maxlength="12" :disabled="saving || mode === 'purchase'" placeholder="填写号码" /></view>
        <view class="field"><label>投注倍数 *</label><input v-model="form.multiple" type="number" maxlength="2" :disabled="saving" placeholder="1～99倍" /></view>
        <view v-if="form.game === 'DLT'" class="lottery-row field"><text>追加投注</text><switch :checked="form.additional" color="#49ad9c" :disabled="saving" @change="form.additional = checked($event)" /></view>
        <view v-if="mode !== 'plan'" class="field"><label>{{ mode === 'purchase' ? '本批起始期号 *' : '票面期号（未来定投位置可待核对）' }}</label><input v-model="form.issue" type="number" :maxlength="form.game === 'DLT' ? 5 : 7" :disabled="saving || boundPlan" :placeholder="form.game === 'DLT' ? '大乐透5位期号' : '双色球7位期号'" /></view>
        <view v-if="mode === 'purchase'" class="field"><label>本批实际购买期数 *</label><input v-model="form.periods" type="number" maxlength="2" :disabled="saving" placeholder="1～20期" /><text class="lottery-hint">按官方期次核对，休市不计期；未确认的未来期号保留待核对。</text></view>
        <view v-if="mode !== 'plan'" class="field"><label>实际购买日期 *</label><button class="lottery-date" :disabled="saving" @click="dateOpen = true">{{ form.purchasedOn }}</button></view>
        <view class="field"><label>备注（选填）</label><textarea v-model="form.note" maxlength="500" :disabled="saving" placeholder="最多500字" /></view>
      </view>
      <view v-if="mode !== 'plan'" class="lottery-card"><view class="lottery-row"><text>本次成本</text><MoneyDisplay v-if="cost != null" :value="cost" /><text v-else>待填写</text></view><view v-if="mode === 'single' || mode === 'purchase'" class="lottery-row field"><text>我已实际购买以上彩票</text><switch :checked="form.confirmedPurchase" color="#49ad9c" :disabled="saving" @change="form.confirmedPurchase = checked($event)" /></view><text class="lottery-hint">{{ mode === 'purchase' ? '逐期自动核对，只在本批到期时提醒续买。' : '开奖后中奖与未中奖都会提醒；渠道受理后不可修改，仍可删除。' }}</text></view>
    </template>
    <view v-if="!loading" class="lottery-actions"><button class="primary-btn" :disabled="saving" :loading="saving" @click="save">{{ saving ? '保存中…' : mode === 'purchase' ? '确认已购买并保存' : '保存' }}</button></view>
    <EntryDateTimePicker v-if="dateOpen" mode="date" :value="form.purchasedOn" min-date="2000-01-01" :max-date="today()" title="购买日期" @close="dateOpen = false" @select="form.purchasedOn = $event; dateOpen = false" />
    <LotteryConfirm v-if="discard" title="放弃本次编辑？" copy="尚未保存的内容将丢失。" :busy="false" confirm="放弃编辑" @cancel="discard = false" @confirm="discard = false; goBack()" />
  </view>
</template>
