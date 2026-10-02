<script setup lang="ts">
import { onBeforeUnmount, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import NativeNavigation from '../../components/NativeNavigation.vue'
import { notificationConfigApi, type NotificationType } from '../../utils/api'
import { encryptPassword } from '../../utils/passwordCrypto'
import { useLedger } from '../../stores/ledger'

const ledger = useLedger()
const providers: { type: NotificationType; label: string }[] = [
  { type: 'BARK', label: 'Bark' }, { type: 'PUSHPLUS', label: 'pushplus' },
]
const keys = ref<Record<NotificationType, string>>({ BARK: '', PUSHPLUS: '' })
const savedKeys = ref<Record<NotificationType, string>>({ BARK: '', PUSHPLUS: '' })
const loading = ref(true)
const loadError = ref('')
const saving = ref(false)
type PendingRequest = {
  value: string
  data: { encryptedKey?: string; remove: boolean; idempotencyKey: string }
}
const pendingRequests: Partial<Record<NotificationType, PendingRequest>> = {}

function backToMine() {
  if (!saving.value) uni.switchTab({ url: '/pages/mine/mine' })
}

async function loadNotifications() {
  if (saving.value) return
  if (!ledger.state.token) {
    uni.reLaunch({ url: '/pages/auth/login/login' })
    return
  }
  loading.value = true
  loadError.value = ''
  try {
    const configs = await notificationConfigApi.list()
    for (const { type } of providers) {
      const config = configs.find(row => row.notificationType === type && row.configured)
      keys.value[type] = config?.notificationKey || ''
      savedKeys.value[type] = keys.value[type]
      delete pendingRequests[type]
    }
  } catch (error) {
    loadError.value = error instanceof Error ? error.message : '通知配置加载失败，请重试'
  } finally {
    loading.value = false
  }
}

async function saveNotifications() {
  if (saving.value || loading.value || loadError.value) return
  const changes = providers.filter(({ type }) => keys.value[type].trim() !== savedKeys.value[type])
  if (!changes.length) {
    uni.showToast({ title: '通知配置未变更', icon: 'none' })
    return
  }
  for (const { type, label } of changes) {
    if (encodeURIComponent(keys.value[type].trim()).replace(/%[0-9A-F]{2}/gi, 'x').length > 190) {
      uni.showToast({ title: `${label} Key 不得超过190字节`, icon: 'none' })
      return
    }
  }
  saving.value = true
  try {
    for (const { type } of changes) {
      const value = keys.value[type].trim()
      let pending = pendingRequests[type]
      if (!pending || pending.value !== value) {
        pending = {
          value,
          data: {
            remove: !value,
            ...(value ? { encryptedKey: await encryptPassword(value) } : {}),
            idempotencyKey: `${Date.now()}-${Math.random().toString(36).slice(2)}`,
          },
        }
        pendingRequests[type] = pending
      }
      await notificationConfigApi.save(type, pending.data)
      savedKeys.value[type] = value
      keys.value[type] = value
      delete pendingRequests[type]
    }
    uni.showToast({ title: '通知配置已保存', icon: 'none' })
  } catch (error) {
    uni.showToast({ title: error instanceof Error ? error.message : '通知配置保存失败，请重试', icon: 'none' })
  } finally {
    saving.value = false
  }
}

onShow(loadNotifications)
onBeforeUnmount(() => {
  for (const { type } of providers) {
    keys.value[type] = ''
    savedKeys.value[type] = ''
    delete pendingRequests[type]
  }
})
</script>

<template>
  <view class="page profile-page notification-center-page">
    <NativeNavigation variant="help" title="通知中心" back-label="返回我的" :page-top-extra="20" @back="backToMine" />
    <view v-if="loading" class="profile-loading">正在加载通知配置…</view>
    <view v-else-if="loadError" class="profile-error"><text>{{ loadError }}</text><button class="text-button" @click="loadNotifications">重新加载</button></view>
    <view v-else class="profile-content">
      <view class="profile-hero">
        <text class="profile-eyebrow">NOTIFICATION CENTER</text>
        <text class="profile-hero-title">通知中心</text>
        <text class="profile-hero-copy">按需配置 Bark 和 pushplus，保存后可随时查看和调整。</text>
        <view class="profile-orbit profile-orbit-one" aria-hidden="true" /><view class="profile-orbit profile-orbit-two" aria-hidden="true" />
      </view>

      <view class="profile-form-card">
        <view v-for="provider in providers" :key="provider.type" class="profile-field">
          <view class="profile-field-label-row">
            <text class="profile-field-label">{{ provider.label }} <text class="notification-optional">选填</text></text>
            <text v-if="savedKeys[provider.type]" class="profile-field-lock">已配置</text>
          </view>
          <input v-model="keys[provider.type]" class="profile-field-input" type="text" maxlength="190" :placeholder="`请输入 ${provider.label} Key`" :aria-label="`${provider.label} 通知 Key`" :disabled="saving" />
        </view>
      </view>
      <text class="notification-center-tip">两项均为选填。清空后保存，可移除对应通知配置。</text>
    </view>
    <view v-if="!loading && !loadError" class="profile-actions">
      <button class="profile-save-action" :disabled="saving" @click="saveNotifications">{{ saving ? '保存中…' : '保存' }}</button>
    </view>
  </view>
</template>
