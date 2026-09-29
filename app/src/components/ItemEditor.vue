<script setup lang="ts">
import { ref } from 'vue'
import CenterModal from './CenterModal.vue'
import EntryDateTimePicker from './EntryDateTimePicker.vue'
import { itemApi } from '../utils/api'
import { ITEM_MIN_DATE, itemToday, itemAmount, validItemDate, itemSubmission, type Item } from '../utils/items'
const props = defineProps<{ item?: Item }>()
const emit = defineEmits<{ close: []; saved: [item: Item] }>()
const name = ref(''), price = ref(''), purchasedOn = ref(itemToday()), serving = ref(true)
const retiredOn = ref(itemToday()), resale = ref(''), saving = ref(false), error = ref('')
const datePicker = ref<'purchase' | 'retirement' | ''>('')
const submission = itemSubmission()
function close() { if (!saving.value) emit('close') }
function selectDate(value: string) {
  if (datePicker.value === 'purchase') purchasedOn.value = value
  if (datePicker.value === 'retirement') retiredOn.value = value
  datePicker.value = ''
}
function servingChange(event: Event) {
  serving.value = Boolean((event as Event & { detail?: { value?: boolean } }).detail?.value)
}
async function save() {
  if (saving.value) return
  error.value = ''
  saving.value = true
  try {
    const retirement = Boolean(props.item) || !serving.value
    if (retirement) {
      validItemDate(retiredOn.value)
      if (retiredOn.value < (props.item?.purchasedOn || purchasedOn.value)) throw new Error('退役日期不能早于购买日期')
    }
    let result: Item
    if (props.item) {
      const data = { retiredOn: retiredOn.value, resaleCents: itemAmount(resale.value) }
      result = await itemApi.retire(props.item.id, { ...data, idempotencyKey: submission(data).idempotencyKey })
    } else {
      const title = name.value.trim()
      if (!title || [...title].length > 40) throw new Error('物品名称需为1至40个字符')
      validItemDate(purchasedOn.value)
      const data = { name: title, priceCents: itemAmount(price.value), purchasedOn: purchasedOn.value,
        serving: serving.value, retiredOn: retirement ? retiredOn.value : null, resaleCents: retirement ? itemAmount(resale.value) : null }
      result = await itemApi.create({ ...data, idempotencyKey: submission(data).idempotencyKey })
    }
    emit('saved', result)
    uni.showToast({ title: props.item ? '物品已退役' : '物品已添加', icon: 'success' })
  } catch (cause) { error.value = cause instanceof Error ? cause.message : '网络异常，请重试' }
  finally { saving.value = false }
}
</script>
<template>
  <CenterModal :title="item ? '退役物品' : '添加物品'" @close="close">
    <view class="item-form">
      <template v-if="!item">
        <view class="item-field"><text>物品名称</text><input v-model="name" maxlength="80" placeholder="例如：耳机" :disabled="saving" aria-label="物品名称" /></view>
        <view class="item-field"><text>购买价格</text><input v-model="price" type="digit" inputmode="decimal" placeholder="0" :disabled="saving" aria-label="购买价格" /></view>
        <button class="item-field item-date-field" :disabled="saving" @click="datePicker = 'purchase'"><text>购买日期</text><view class="item-date">{{ purchasedOn }} <text>›</text></view></button>
        <view class="item-field"><text>正在服役</text><switch :checked="serving" color="#49AD9C" :disabled="saving" @change="servingChange" /></view>
      </template>
      <template v-if="item || !serving">
        <view v-if="item" class="item-form-caption">{{ item.name }} · 退役后停止累计服役天数</view>
        <button class="item-field item-date-field" :disabled="saving" @click="datePicker = 'retirement'"><text>退役日期</text><view class="item-date">{{ retiredOn }} <text>›</text></view></button>
        <view class="item-field"><text>二手出售价格</text><input v-model="resale" type="digit" inputmode="decimal" placeholder="未出售填 0" :disabled="saving" aria-label="二手出售价格" /></view>
        <text class="item-form-caption">出售金额只用于计算物品净成本，不会自动记账。</text>
      </template>
      <text v-if="error" class="item-form-error" role="alert">{{ error }}</text>
    </view>
    <template #actions><view class="item-modal-actions"><button class="item-secondary" :disabled="saving" @click="close">取消</button><button class="item-primary" :loading="saving" :disabled="saving" @click="save">{{ saving ? '保存中' : item ? '确认退役' : '添加物品' }}</button></view></template>
  </CenterModal>
  <EntryDateTimePicker v-if="datePicker" mode="date" :title="datePicker === 'purchase' ? '选择购买日期' : '选择退役日期'" :min-date="datePicker === 'purchase' ? ITEM_MIN_DATE : (item?.purchasedOn && item.purchasedOn > ITEM_MIN_DATE ? item.purchasedOn : ITEM_MIN_DATE)" :max-date="itemToday()" :value="datePicker === 'purchase' ? purchasedOn : retiredOn" @close="datePicker = ''" @select="selectDate" />
</template>
<style scoped>
.item-form { padding:4px 0; }.item-field { display:flex; align-items:center; justify-content:space-between; gap:12px; min-height:60px; border-bottom:1px solid #eff2f0; font-size:14px; }.item-field>text { flex-shrink:0; }.item-field input { flex:1; min-width:0; text-align:right; font-size:14px; height:44px; }.item-date-field { width:100%; padding:0; border-radius:0; color:inherit; background:transparent; text-align:left; }.item-date-field::after { border:0; }.item-date { min-height:44px; display:flex; align-items:center; gap:14px; color:#455651; }.item-date text { color:#a4afab; }.item-form-caption { display:block; margin:14px 0 4px; font-size:12px; line-height:1.7; color:#858b8b; }.item-form-error { display:block; margin-top:12px; color:#ce6256; font-size:13px; }.item-modal-actions { display:flex; gap:10px; padding-top:16px; }.item-modal-actions button { flex:1; min-width:0; min-height:46px; line-height:46px; border-radius:15px; font-size:14px; }.item-secondary { background:#eef6f3; color:#455651; }.item-primary { background:#49ad9c; color:white; }
</style>
