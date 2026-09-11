<script setup lang="ts">
import { computed, ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import BottomNav from '../../components/BottomNav.vue'
import PageHeader from '../../components/PageHeader.vue'
import { currentAvatar } from '../../utils/file'
import { request } from '../../utils/api'
import { useLedger } from '../../stores/ledger'

type Profile = {
  userId: string
  nickname: string
  createdAt?: string
  cumulativeDays: number
  avatarAuthorized: boolean
  avatarFileUrl?: string
}

const ledger = useLedger()
const avatarUrl = ref('/static/brand.png')
const profile = ref<Profile | null>(null)
const loading = ref(false)
const loggingOut = ref(false)
const logoutOpen = ref(false)

const loggedIn = computed(() => Boolean(ledger.state.token))
const nickname = computed(() => profile.value?.nickname || ledger.state.user?.nickname || '账本主人')
const avatarStatus = computed(() => {
  return loggedIn.value ? (profile.value?.avatarAuthorized ? '资料已设置 · 数据随时可用' : '') : '登录后同步你的资料和记账数据'
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
    if (profile.value.avatarFileUrl) {
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

function openHelp() {
  uni.navigateTo({ url: '/pages/help/help' })
}

function openProfile() {
  if (!loggedIn.value) {
    openLogin()
    return
  }
  uni.navigateTo({ url: '/pages/profile/profile' })
}

function openLogout() {
  if (loggingOut.value || !loggedIn.value) return
  logoutOpen.value = true
}

function closeLogout() {
  if (!loggingOut.value) logoutOpen.value = false
}

async function confirmLogout() {
  if (loggingOut.value || !loggedIn.value) return
  loggingOut.value = true
  try {
    await ledger.logout()
    uni.showToast({ title: '已退出登录', icon: 'none' })
  } catch {
    // logout() 无论服务端响应如何都会清除本地会话，页面切换到未登录状态即可。
  } finally {
    profile.value = null
    avatarUrl.value = '/static/brand.png'
    logoutOpen.value = false
    loggingOut.value = false
    // #ifdef H5
    if (typeof window !== 'undefined') uni.reLaunch({ url: '/pages/auth/login/login' })
    // #endif
  }
}

onShow(loadProfile)
</script>

<template>
  <view class="page mine-page">
    <PageHeader />

    <view class="profile-card">
      <view class="profile-avatar-button">
        <image :src="avatarUrl" mode="aspectFill" />
      </view>
      <view class="profile-copy">
        <text class="profile-name" :class="{ 'profile-login-name': !loggedIn }" :role="loggedIn ? undefined : 'button'" :aria-label="loggedIn ? undefined : '登录'" @click="!loggedIn && openLogin()">{{ loggedIn ? nickname : '登录哈记账' }}</text>
        <text v-if="loading || avatarStatus" class="profile-status">{{ loading ? '正在加载个人资料…' : avatarStatus }}</text>
      </view>
    </view>

    <view class="days-card">
      <text>坚持记录，正在变成习惯</text>
      <text class="days-value">累计记账 {{ loggedIn ? (profile?.cumulativeDays || 0) : 0 }} 天</text>
    </view>

    <view class="settings-group">
      <text class="settings-label">更多</text>
      <view class="settings-list">
        <button class="setting-item" aria-label="个人中心" @click="openProfile">
          <text class="setting-icon setting-icon-profile">个</text>
          <text class="setting-text">个人中心</text>
          <text class="setting-arrow">›</text>
        </button>
        <button class="setting-item" @click="openHelp">
          <text class="setting-icon setting-icon-help">?</text>
          <text class="setting-text">关于与帮助</text>
          <text class="setting-arrow">›</text>
        </button>
        <button v-if="loggedIn" class="setting-item logout-item" :disabled="loggingOut" @click="openLogout">
          <text class="setting-icon logout-icon">↪</text>
          <text class="setting-text">{{ loggingOut ? '退出中…' : '退出登录' }}</text>
          <text class="setting-arrow">›</text>
        </button>
      </view>
    </view>

    <view v-if="logoutOpen" class="asset-create-backdrop" @click.self="closeLogout" @touchmove.stop.prevent>
      <view class="account-delete-modal" role="dialog" aria-modal="true" aria-label="退出登录确认">
        <view class="asset-create-handle" />
        <text class="account-delete-title">退出登录？</text>
        <text class="account-delete-copy">退出后需要重新登录，确定退出当前账号吗？</text>
        <view class="account-delete-actions">
          <button class="account-delete-cancel" :disabled="loggingOut" @click="closeLogout">取消</button>
          <button class="account-delete-confirm" :disabled="loggingOut" @click="confirmLogout">{{ loggingOut ? '退出中…' : '退出登录' }}</button>
        </view>
      </view>
    </view>

    <BottomNav active="mine" />
  </view>
</template>
