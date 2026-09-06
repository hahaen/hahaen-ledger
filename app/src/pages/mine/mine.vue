<script setup lang="ts">
import { computed, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import BottomNav from '../../components/BottomNav.vue'
import { currentAvatar, uploadAvatar } from '../../utils/file'
import { request } from '../../utils/api'
import { useLedger } from '../../stores/ledger'

type Profile = {
  userId: number
  nickname: string
  createdAt?: string
  cumulativeDays: number
  avatarAuthorized: boolean
  avatarFileId?: number
}

const ledger = useLedger()
const avatarUrl = ref('/static/brand.png')
const profile = ref<Profile | null>(null)
const uploading = ref(false)
const loading = ref(false)
const loggingOut = ref(false)

const loggedIn = computed(() => Boolean(ledger.state.token))
const nickname = computed(() => profile.value?.nickname || ledger.state.user?.nickname || '账本主人')
const avatarStatus = computed(() => {
  if (uploading.value) return '正在上传头像…'
  return loggedIn.value ? '' : '登录后同步你的头像和记账数据'
})
const avatarActionText = computed(() => {
  if (!loggedIn.value) return '登录'
  return ''
})

async function loadProfile() {
  if (!ledger.state.token) {
    profile.value = null
    avatarUrl.value = '/static/brand.png'
    return
  }
  loading.value = true
  try {
    profile.value = await request<Profile>('/api/app/user/profile')
    avatarUrl.value = '/static/brand.png'
    if (profile.value.avatarFileId) {
      try {
        const avatar = await currentAvatar()
        if (avatar?.viewUrl) avatarUrl.value = avatar.viewUrl
      } catch {
        // 头像预览失败不影响个人资料和累计天数展示。
      }
    }
  } catch {
    if (!uni.getStorageSync('auth-token')) {
      ledger.state.token = ''
      profile.value = null
    }
  } finally {
    loading.value = false
  }
}

function openLogin() {
  uni.reLaunch({ url: '/pages/auth/login/login' })
}

function handleAvatarClick() {
  if (!loggedIn.value) {
    openLogin()
    return
  }
  if (uploading.value) return
  // #ifdef H5
  const input = document.querySelector('.avatar-file-input') as HTMLInputElement | null
  input?.click()
  // #endif
  // #ifdef MP-WEIXIN
  uni.showToast({ title: '微信头像入口将在后续版本接入', icon: 'none' })
  // #endif
}

async function chooseAvatar(event: Event) {
  const file = (event.target as HTMLInputElement).files?.[0]
  if (!file || uploading.value || !loggedIn.value) return
  uploading.value = true
  try {
    const result = await uploadAvatar(file)
    avatarUrl.value = result.viewUrl
    if (profile.value) {
      profile.value.avatarAuthorized = true
      profile.value.avatarFileId = result.fileId
    }
    uni.showToast({ title: '头像上传成功', icon: 'none' })
  } catch (error) {
    uni.showToast({ title: error instanceof Error ? error.message : '头像上传失败，请重试', icon: 'none' })
  } finally {
    uploading.value = false
    ;(event.target as HTMLInputElement).value = ''
  }
}

function openHelp() {
  uni.navigateTo({ url: '/pages/help/help' })
}

function confirmLogout() {
  if (loggingOut.value || !loggedIn.value) return
  uni.showModal({
    title: '退出登录',
    content: '退出后仍会停留在本页，确定退出当前账号吗？',
    confirmText: '退出',
    confirmColor: '#D47769',
    success: async ({ confirm }) => {
      if (!confirm || loggingOut.value) return
      loggingOut.value = true
      try {
        await ledger.logout()
        profile.value = null
        avatarUrl.value = '/static/brand.png'
        uni.showToast({ title: '已退出登录', icon: 'none' })
      } catch {
        profile.value = null
        avatarUrl.value = '/static/brand.png'
      } finally {
        loggingOut.value = false
      }
    },
  })
}

onShow(loadProfile)
</script>

<template>
  <view class="page mine-page">
    <view class="mine-header">
      <view class="brand-lockup"><image src="/static/brand.png" mode="aspectFill" /></view>
      <text class="mine-subtitle">记录每一笔，让生活更清晰</text>
    </view>

    <view class="profile-card">
      <view class="profile-avatar-button" @click="handleAvatarClick">
        <image :src="avatarUrl" mode="aspectFill" />
        <view v-if="loggedIn && !uploading" class="avatar-edit-badge">↗</view>
        <!-- #ifdef H5 -->
        <input class="avatar-file-input" type="file" accept="image/jpeg,image/png,image/webp,image/gif" @change="chooseAvatar" />
        <!-- #endif -->
      </view>
      <view class="profile-copy">
        <text class="profile-name">{{ loggedIn ? nickname : '登录哈记账' }}</text>
        <text v-if="loading || avatarStatus" class="profile-status">{{ loading ? '正在加载个人资料…' : avatarStatus }}</text>
      </view>
      <button v-if="!loggedIn" class="text-button" @click="openLogin">{{ avatarActionText }}</button>
    </view>

    <view class="days-card">
      <text>坚持记录，正在变成习惯</text>
      <text class="days-value">累计记账 {{ loggedIn ? (profile?.cumulativeDays || 0) : 0 }} 天</text>
    </view>

    <view class="settings-group">
      <text class="settings-label">更多</text>
      <view class="settings-list">
        <button class="setting-item" @click="openHelp">
          <text class="setting-icon setting-icon-help">?</text>
          <text class="setting-text">关于与帮助</text>
          <text class="setting-arrow">›</text>
        </button>
        <button v-if="loggedIn" class="setting-item logout-item" :disabled="loggingOut" @click="confirmLogout">
          <text class="setting-icon logout-icon">↪</text>
          <text class="setting-text">{{ loggingOut ? '退出中…' : '退出登录' }}</text>
          <text class="setting-arrow">›</text>
        </button>
      </view>
    </view>

    <BottomNav active="mine" />
  </view>
</template>
