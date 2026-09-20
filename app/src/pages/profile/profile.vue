<script setup lang="ts">
import { computed, onBeforeUnmount, ref } from 'vue'
import NativeNavigation from '../../components/NativeNavigation.vue'
import { onLoad } from '@dcloudio/uni-app'
import { currentAvatar, uploadAvatar, uploadAvatarFromMiniPath, type FileViewUrl } from '../../utils/file'
import { request } from '../../utils/api'
import { encryptPassword } from '../../utils/passwordCrypto'
import { useLedger } from '../../stores/ledger'
import { staticResource } from '../../utils/staticResource'
import { handleMpTouchStart } from '../../utils/tapFeedback'
import { registerWechatShare } from '../../utils/wechatShare'

type Profile = {
  userId: string
  nickname: string
  loginAccount?: string
  passwordConfigured: boolean
  avatarAuthorized: boolean
  avatarFileUrl?: string
}

const ACCOUNT_PATTERN = /^[a-z0-9]{2,64}$/
const ledger = useLedger()
registerWechatShare()
const DEFAULT_AVATAR_URL = staticResource('brand.png')
const avatarUrl = ref(DEFAULT_AVATAR_URL)
const avatarConfigured = ref(false)
const nickname = ref('')
const loginAccount = ref('')
const savedAccount = ref('')
const loading = ref(true)
const loadError = ref('')
const uploading = ref(false)
const saving = ref(false)
const passwordOpen = ref(false)
const changingPassword = ref(false)
const newPassword = ref('')
const firstPassword = ref('')
const firstPasswordVisible = ref(false)
const modalPasswordVisible = ref(false)
const saveSuccessOpen = ref(false)
const avatarFileInput = ref<HTMLInputElement | null>(null)
const pendingAvatar = ref<FileViewUrl | null>(null)
let saveReturnTimer: ReturnType<typeof setTimeout> | undefined

const accountLocked = computed(() => Boolean(savedAccount.value))
const passwordConfigured = ref(false)

function normalizedAccount(): string {
  return loginAccount.value.trim().toLowerCase()
}

function validateProfile(): string | undefined {
  if (!avatarConfigured.value && !pendingAvatar.value) return '请先设置头像'
  if (!nickname.value.trim()) return '请输入用户昵称'
  if (nickname.value.trim().length > 40) return '昵称最多 40 个字符'
  return validateAccount()
}

function validateAccount(): string | undefined {
  if (!normalizedAccount()) return '请输入账号'
  if (!ACCOUNT_PATTERN.test(normalizedAccount())) return '账号需为 2-64 位英文字母或数字'
  return undefined
}

function filterAccount() {
  loginAccount.value = loginAccount.value.replace(/[^a-zA-Z0-9]/g, '')
}

function validatePassword(password: string): string | undefined {
  if (!password) return '请输入密码'
  if (password.length < 8 || password.length > 64) return '密码需为 8-64 位字符'
  return undefined
}

async function loadProfile() {
  if (!ledger.state.token) {
    uni.reLaunch({ url: '/pages/auth/login/login' })
    return
  }
  loading.value = true
  loadError.value = ''
  try {
    const profile = await request<Profile>('/api/app/user/profile')
    nickname.value = profile.nickname || ''
    loginAccount.value = profile.loginAccount || ''
    savedAccount.value = profile.loginAccount || ''
    passwordConfigured.value = profile.passwordConfigured
    avatarConfigured.value = profile.avatarAuthorized
    pendingAvatar.value = null
    avatarUrl.value = DEFAULT_AVATAR_URL
    if (profile.avatarFileUrl) {
      const avatar = await currentAvatar()
      if (avatar?.viewUrl) avatarUrl.value = avatar.viewUrl
    }
  } catch (error) {
    loadError.value = error instanceof Error ? error.message : '个人资料加载失败，请重试'
  } finally {
    loading.value = false
  }
}

function handleAvatarClick() {
  if (uploading.value || saving.value) return
  // #ifdef H5
  if (typeof document === 'undefined') return
  const input = avatarFileInput.value || createAvatarFileInput()
  input.value = ''
  input.click()
  // #endif
  // #ifdef MP-WEIXIN
  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: result => {
      const selected = result.tempFiles?.[0]
      const filePath = selected?.path || result.tempFilePaths?.[0]
      if (filePath) void chooseMiniAvatar(filePath, selected?.name || 'avatar.jpg')
      else uni.showToast({ title: '未选择头像，请重试', icon: 'none' })
    },
    fail: error => {
      if (!String(error.errMsg || '').includes('cancel')) uni.showToast({ title: '头像选择失败，请重试', icon: 'none' })
    },
  })
  // #endif
}

function createAvatarFileInput(): HTMLInputElement {
  const input = document.createElement('input')
  input.type = 'file'
  input.accept = 'image/jpeg,image/png,image/webp,image/gif'
  input.style.cssText = 'position:fixed;left:-9999px;width:1px;height:1px;padding:0;opacity:0;pointer-events:none;'
  input.addEventListener('change', event => { void chooseAvatar(event) })
  document.body.appendChild(input)
  avatarFileInput.value = input
  return input
}

async function chooseAvatar(event: Event) {
  const file = (event.target as HTMLInputElement).files?.[0]
  if (!file || uploading.value) return
  uploading.value = true
  try {
    const result = await uploadAvatar(file)
    pendingAvatar.value = result
    avatarUrl.value = result.viewUrl
    uni.showToast({ title: '新头像已预览，点击保存后更新', icon: 'none' })
  } catch (error) {
    uni.showToast({ title: error instanceof Error ? error.message : '头像上传失败，请重试', icon: 'none' })
  } finally {
    uploading.value = false
    ;(event.target as HTMLInputElement).value = ''
  }
}

async function chooseMiniAvatar(filePath: string, originalName: string) {
  if (uploading.value) return
  uploading.value = true
  try {
    const result = await uploadAvatarFromMiniPath(filePath, originalName)
    pendingAvatar.value = result
    avatarUrl.value = result.viewUrl
    uni.showToast({ title: '新头像已预览，点击保存后更新', icon: 'none' })
  } catch (error) {
    uni.showToast({ title: error instanceof Error ? error.message : '头像上传失败，请重试', icon: 'none' })
  } finally {
    uploading.value = false
  }
}

function openPassword() {
  if (!passwordConfigured.value) return
  const error = validateAccount()
  if (error) {
    uni.showToast({ title: error, icon: 'none' })
    return
  }
  newPassword.value = ''
  modalPasswordVisible.value = false
  passwordOpen.value = true
}

function closePassword() {
  if (!changingPassword.value) passwordOpen.value = false
}

async function confirmPassword() {
  if (changingPassword.value) return
  const error = validatePassword(newPassword.value)
  if (error) {
    uni.showToast({ title: error, icon: 'none' })
    return
  }
  changingPassword.value = true
  try {
    await request('/api/app/user/profile/password', {
      method: 'PUT',
      data: {
        encryptedPassword: await encryptPassword(newPassword.value),
        ...(accountLocked.value ? {} : { loginAccount: normalizedAccount() }),
      },
    })
    if (!accountLocked.value) {
      loginAccount.value = normalizedAccount()
      savedAccount.value = loginAccount.value
    }
    passwordOpen.value = false
    newPassword.value = ''
    modalPasswordVisible.value = false
    uni.showToast({ title: '密码修改成功', icon: 'none' })
  } catch (error) {
    uni.showToast({ title: error instanceof Error ? error.message : '密码修改失败，请重试', icon: 'none' })
  } finally {
    changingPassword.value = false
  }
}

async function saveProfile() {
  if (saving.value) return
  const error = validateProfile()
  if (error) {
    uni.showToast({ title: error, icon: 'none' })
    return
  }
  const passwordError = passwordConfigured.value ? undefined : validatePassword(firstPassword.value)
  if (passwordError) {
    uni.showToast({ title: passwordError, icon: 'none' })
    return
  }
  saving.value = true
  try {
    const selectedAvatar = pendingAvatar.value
    const updated = await request<Profile>('/api/app/user/profile', {
      method: 'PUT',
      data: {
        nickname: nickname.value.trim(),
        loginAccount: normalizedAccount(),
        ...(!passwordConfigured.value ? { encryptedPassword: await encryptPassword(firstPassword.value) } : {}),
        ...(selectedAvatar ? { avatarFileId: selectedAvatar.fileId } : {}),
      },
    })
    nickname.value = updated.nickname
    loginAccount.value = updated.loginAccount || normalizedAccount()
    savedAccount.value = loginAccount.value
    passwordConfigured.value = updated.passwordConfigured
    avatarConfigured.value = updated.avatarAuthorized
    if (selectedAvatar) avatarUrl.value = selectedAvatar.viewUrl
    pendingAvatar.value = null
    firstPassword.value = ''
    firstPasswordVisible.value = false
    ledger.state.user = { id: updated.userId, nickname: updated.nickname }
    saveSuccessOpen.value = true
    saveReturnTimer = setTimeout(() => {
      saveSuccessOpen.value = false
      backToMine()
    }, 500)
  } catch (requestError) {
    uni.showToast({ title: requestError instanceof Error ? requestError.message : '资料保存失败，请重试', icon: 'none' })
  } finally {
    saving.value = false
  }
}

function backToMine() {
  uni.switchTab({ url: '/pages/mine/mine' })
}

onLoad(loadProfile)
onBeforeUnmount(() => {
  if (saveReturnTimer) clearTimeout(saveReturnTimer)
  avatarFileInput.value?.remove()
  avatarFileInput.value = null
})
</script>

<template>
  <view class="page profile-page" @touchstart.capture="handleMpTouchStart">
    <NativeNavigation variant="help" title="个人中心" back-label="返回我的" :page-top-extra="20" @back="backToMine" />

    <view v-if="loading" class="profile-loading">正在加载个人资料…</view>
    <view v-else-if="loadError" class="profile-error"><text>{{ loadError }}</text><button data-tap-feedback="true" class="text-button" @click="loadProfile">重新加载</button></view>
    <view v-else class="profile-content">
      <view class="profile-hero">
        <text class="profile-eyebrow">YOUR PROFILE</text>
        <text class="profile-hero-title">管理你的记账身份</text>
        <text class="profile-hero-copy">头像、昵称和账号都只属于当前登录用户。</text>
        <view class="profile-orbit profile-orbit-one" /><view class="profile-orbit profile-orbit-two" />
      </view>

      <view class="profile-form-card">
        <button data-tap-feedback="true" type="button" class="profile-avatar-row" :disabled="uploading || saving" @click="handleAvatarClick">
          <image :src="avatarUrl" mode="aspectFill" />
          <view class="profile-avatar-copy"><text>头像 <text class="profile-required">必填</text></text><text>{{ uploading ? '正在上传…' : pendingAvatar ? '新头像已准备，保存后生效' : '点击更换头像' }}</text></view>
          <text class="setting-arrow">›</text>
        </button>
        <view class="profile-field">
          <text class="profile-field-label">用户昵称 <text class="profile-required">必填</text></text>
          <input v-model="nickname" maxlength="40" class="profile-field-input" placeholder="请输入用户昵称" aria-label="用户昵称" />
        </view>
        <view class="profile-field">
          <view class="profile-field-label-row"><text class="profile-field-label">账号 <text class="profile-required">必填</text></text><text v-if="accountLocked" class="profile-field-lock">已设置，不可修改</text></view>
          <input v-model="loginAccount" :disabled="accountLocked" maxlength="64" class="profile-field-input" :class="{ disabled: accountLocked }" placeholder="请输入英文字母或数字" aria-label="账号" @input="filterAccount" />
          <text v-if="!accountLocked" class="profile-field-tip">仅支持 2-64 位英文字母或数字，首次设置后不可修改。</text>
        </view>
        <view v-if="!passwordConfigured" class="profile-field">
          <text class="profile-field-label">密码 <text class="profile-required">必填</text></text>
          <view class="profile-password-field">
            <input v-model="firstPassword" class="profile-password-text" :type="firstPasswordVisible ? 'text' : 'password'" :password="!firstPasswordVisible" autocomplete="new-password" placeholder="请输入密码（8-64 位）" maxlength="64" aria-label="密码" />
            <button data-tap-feedback="true" class="auth-password-toggle" :aria-label="firstPasswordVisible ? '隐藏密码' : '显示密码'" :title="firstPasswordVisible ? '隐藏密码' : '显示密码'" @click="firstPasswordVisible = !firstPasswordVisible">
              <view class="auth-password-eye" :class="{ visible: firstPasswordVisible }" aria-hidden="true"><view class="auth-password-eye-pupil" /></view>
            </button>
          </view>
          <text class="profile-field-tip">首次保存将同时设置账号和密码。</text>
        </view>
      </view>
    </view>

    <view v-if="!loading && !loadError" class="profile-actions">
      <button data-tap-feedback="true" v-if="passwordConfigured" class="profile-password-action" :disabled="saving || uploading" @click="openPassword">修改密码</button>
      <button data-tap-feedback="true" class="profile-save-action" :disabled="saving || uploading" @click="saveProfile">{{ saving ? '保存中…' : '保存' }}</button>
    </view>

    <view v-if="passwordOpen" class="asset-create-backdrop" @click.self="closePassword" @touchmove.stop.prevent>
      <view class="account-delete-modal profile-password-modal" role="dialog" aria-modal="true" aria-label="修改密码">
        <view class="asset-create-handle" />
        <text class="account-delete-title">修改密码</text>
        <text class="account-delete-copy">新密码需为 8-64 位字符。{{ accountLocked ? '修改后立即生效。' : '首次设置会同时绑定当前填写的账号。' }}</text>
        <view class="profile-password-input">
          <input v-model="newPassword" class="profile-password-text" :type="modalPasswordVisible ? 'text' : 'password'" :password="!modalPasswordVisible" autocomplete="new-password" placeholder="请输入新密码" maxlength="64" aria-label="新密码" />
          <button data-tap-feedback="true" class="auth-password-toggle" :aria-label="modalPasswordVisible ? '隐藏密码' : '显示密码'" :title="modalPasswordVisible ? '隐藏密码' : '显示密码'" @click="modalPasswordVisible = !modalPasswordVisible">
            <view class="auth-password-eye" :class="{ visible: modalPasswordVisible }" aria-hidden="true"><view class="auth-password-eye-pupil" /></view>
          </button>
        </view>
        <view class="account-delete-actions"><button data-tap-feedback="true" class="account-delete-cancel" :disabled="changingPassword" @click="closePassword">取消</button><button data-tap-feedback="true" class="account-delete-confirm profile-password-confirm" :disabled="changingPassword" @click="confirmPassword">{{ changingPassword ? '修改中…' : '确定修改' }}</button></view>
      </view>
    </view>

    <view v-if="saveSuccessOpen" class="asset-create-backdrop" @touchmove.stop.prevent>
      <view class="account-delete-modal profile-success-modal" role="dialog" aria-modal="true" aria-label="保存成功">
        <view class="asset-create-handle" />
        <view class="profile-success-icon">✓</view>
        <text class="account-delete-title">保存成功</text>
        <text class="account-delete-copy">个人资料已更新，正在返回我的…</text>
      </view>
    </view>

  </view>
</template>
