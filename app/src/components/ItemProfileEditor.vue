<script setup lang="ts">
import { ref } from 'vue'
import CenterModal from './CenterModal.vue'
import EntryDateTimePicker from './EntryDateTimePicker.vue'
import { itemApi } from '../utils/api'
import { inputYuan } from '../utils/money'
import { ITEM_MIN_DATE, itemAmount, itemSubmission, itemToday, validItemDate, type Item } from '../utils/items'

const props = defineProps<{ item: Item }>()
const emit = defineEmits<{ close: []; saved: [] }>()
const name = ref(props.item.name)
const price = ref(inputYuan(props.item.priceCents))
const purchasedOn = ref(props.item.purchasedOn)
const saving = ref(false)
const error = ref('')
const datePicker = ref(false)
const submission = itemSubmission()

function close() { if (!saving.value) emit('close') }
async function save() {
  if (saving.value) return
  error.value = ''
  try {
    const title = name.value.trim()
    if (!title || [...title].length > 40) throw new Error('物品名称需为1至40个字符')
    validItemDate(purchasedOn.value)
    if (props.item.retiredOn && purchasedOn.value > props.item.retiredOn) throw new Error('购买日期不能晚于退役日期')
    const data = { name: title, priceCents: itemAmount(price.value), purchasedOn: purchasedOn.value }
    saving.value = true
    await itemApi.edit(props.item.id, { ...data, ...submission(data) })
    uni.showToast({ title: '物品资料已更新', icon: 'success' })
    emit('saved')
  } catch (cause) {
    error.value = cause instanceof Error ? cause.message : '保存失败，请重试'
  } finally { saving.value = false }
}
</script>

<template>
  <CenterModal title="编辑物品资料" @close="close">
    <view class="item-form">
      <view class="item-field"><text>物品名称</text><input v-model="name" maxlength="80" placeholder="物品名称" :disabled="saving" aria-label="物品名称" /></view>
      <view class="item-field"><text>购买价格</text><input v-model="price" type="digit" inputmode="decimal" placeholder="0" :disabled="saving" aria-label="购买价格" /></view>
      <button class="item-field item-date-field" :disabled="saving" @click="datePicker = true"><text>购买日期</text><view class="item-date">{{ purchasedOn }} <text>›</text></view></button>
      <text v-if="error" class="item-form-error">{{ error }}</text>
    </view>
    <template #actions><view class="item-modal-actions"><button class="item-secondary" :disabled="saving" @click="close">取消</button><button class="item-primary" :loading="saving" :disabled="saving" @click="save">{{ saving ? '保存中' : '保存修改' }}</button></view></template>
  </CenterModal>
  <EntryDateTimePicker v-if="datePicker" mode="date" title="选择购买日期" :min-date="ITEM_MIN_DATE" :max-date="item.retiredOn || itemToday()" :value="purchasedOn" @close="datePicker = false" @select="purchasedOn = $event; datePicker = false" />
</template>

<style scoped>
.item-form { padding:4px 0; }.item-field { display:flex; align-items:center; justify-content:space-between; gap:12px; min-height:60px; border-bottom:1px solid #eff2f0; font-size:14px; }.item-field>text { flex-shrink:0; }.item-field input { flex:1; min-width:0; text-align:right; font-size:14px; height:44px; }.item-date-field { width:100%; padding:0; border-radius:0; color:inherit; background:transparent; text-align:left; }.item-date-field::after { border:0; }.item-date { min-height:44px; display:flex; align-items:center; gap:14px; color:#455651; }.item-date text { color:#a4afab; }.item-form-error { display:block; margin-top:12px; color:#ce6256; font-size:13px; }.item-modal-actions { display:flex; gap:10px; padding-top:16px; }.item-modal-actions button { flex:1; min-width:0; min-height:46px; line-height:46px; border-radius:15px; font-size:14px; }.item-secondary { background:#eef6f3; color:#455651; }.item-primary { background:#49ad9c; color:white; }
</style>
