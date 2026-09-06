<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { request } from '../utils/api'
import { encryptPassword } from '../utils/passwordCrypto'
import { useLedger } from '../stores/ledger'

type CaptchaVO = { captchaId: string; image: string; expiresInSeconds: number }
type LoginVO = { token: string; userId: number; nickname: string }

const props = withDefaults(defineProps<{ mode?: 'login' | 'register' }>(), { mode: 'login' })
const account = ref('')
const password = ref('')
const passwordVisible = ref(false)
const captcha = ref('')
const captchaId = ref('')
const captchaImage = ref('')
const captchaLoading = ref(false)
const errorMessage = ref('')
const submitting = ref(false)
const ledger = useLedger()

const isLogin = computed(() => props.mode === 'login')
const accountValid = computed(() => /^[a-zA-Z0-9][a-zA-Z0-9._-]{1,63}$/.test(account.value.trim()))
const passwordValid = computed(() => password.value.length >= 8 && password.value.length <= 64)
const canSubmit = computed(() => Boolean(accountValid.value && passwordValid.value && captcha.value.trim() && captchaId.value && !submitting.value))

async function refreshCaptcha() {
  captcha.value = ''
  errorMessage.value = ''
  await loadCaptcha()
}

async function loadCaptcha() {
  captchaLoading.value = true
  try {
    const result = await request<CaptchaVO>('/api/app/auth/captcha')
    captchaId.value = result.captchaId
    captchaImage.value = result.image
  } catch (error) {
    captchaId.value = ''
    captchaImage.value = ''
    errorMessage.value = error instanceof Error ? error.message : '验证码加载失败，请点击重试'
  } finally {
    captchaLoading.value = false
  }
}

async function submit() {
  if (!canSubmit.value) return
  submitting.value = true
  errorMessage.value = ''
  try {
    const payload = { account: account.value.trim(), encryptedPassword: await encryptPassword(password.value), captchaId: captchaId.value, captchaCode: captcha.value.trim() }
    if (isLogin.value) {
      const result = await request<LoginVO>('/api/app/auth/h5/login', { method: 'POST', data: payload })
      ledger.state.token = result.token
      ledger.state.user = { id: result.userId, nickname: result.nickname }
      uni.setStorageSync('auth-token', result.token)
      uni.setStorageSync('auth-user', ledger.state.user)
      uni.reLaunch({ url: '/pages/index/index' })
    } else {
      await request<null>('/api/app/auth/h5/register', { method: 'POST', data: payload })
      uni.showToast({ title: '注册成功，请登录', icon: 'none' })
      setTimeout(() => uni.redirectTo({ url: '/pages/auth/login/login' }), 350)
    }
  } catch (error) {
    const message = error instanceof Error ? error.message : '认证失败，请检查后重试'
    await loadCaptcha()
    errorMessage.value = message
  } finally {
    submitting.value = false
  }
}

function switchMode() {
  uni.navigateTo({ url: isLogin.value ? '/pages/auth/register/register' : '/pages/auth/login/login' })
}

onMounted(loadCaptcha)
</script>

<template>
  <view class="auth-page">
    <view class="auth-decoration auth-decoration-large" aria-hidden="true" />
    <view class="auth-decoration auth-decoration-small" aria-hidden="true" />
    <view class="auth-shell">
      <view class="auth-header">
        <view class="auth-brand">
          <image class="auth-brand-image" src="/static/brand.png" mode="aspectFit" />
          <text class="auth-brand-name">哈记账</text>
        </view>
        <view class="auth-context-row">
          <text class="auth-eyebrow">{{ isLogin ? 'WELCOME BACK' : 'A LITTLE BOOKKEEPER' }}</text>
          <text class="auth-context">H5 · 账号{{ isLogin ? '登录' : '注册' }}</text>
        </view>
        <text class="auth-title">{{ isLogin ? '欢迎回来' : '创建你的账号' }}</text>
        <text class="auth-subtitle">{{ isLogin ? '登录后继续记录每一笔生活' : '用一个轻量账号，开启你的记账空间' }}</text>
      </view>

      <view class="auth-card">
        <view class="auth-field">
          <text class="auth-field-icon" aria-hidden="true">◎</text>
          <view class="auth-field-copy">
            <text class="auth-field-label">账号</text>
            <input v-model="account" class="auth-input" type="text" autocomplete="username" placeholder="输入账号" @input="errorMessage = ''" />
          </view>
        </view>
        <view class="auth-field">
          <text class="auth-field-icon" aria-hidden="true">⌁</text>
          <view class="auth-field-copy">
            <text class="auth-field-label">密码</text>
            <view class="auth-password-input">
              <input v-model="password" class="auth-input" :type="passwordVisible ? 'text' : 'password'" :password="!passwordVisible" :autocomplete="isLogin ? 'current-password' : 'new-password'" placeholder="请输入密码" @input="errorMessage = ''" />
              <button class="auth-password-toggle" :aria-label="passwordVisible ? '隐藏密码' : '显示密码'" :title="passwordVisible ? '隐藏密码' : '显示密码'" @click="passwordVisible = !passwordVisible">{{ passwordVisible ? '◉' : '◌' }}</button>
            </view>
          </view>
        </view>
        <view class="auth-captcha-row">
          <view class="auth-field auth-captcha-field">
            <text class="auth-field-icon" aria-hidden="true">◇</text>
            <view class="auth-field-copy">
              <text class="auth-field-label">图形验证码</text>
              <input v-model="captcha" class="auth-input" type="text" maxlength="4" placeholder="输入图形验证码" @input="errorMessage = ''" />
            </view>
          </view>
          <button class="captcha-button" aria-label="刷新图形验证码" :disabled="captchaLoading" @click="refreshCaptcha">
            <image v-if="captchaImage" class="captcha-image" :src="captchaImage" mode="aspectFit" />
            <text v-else class="captcha-art">{{ captchaLoading ? '加载中…' : '点击重试' }}</text>
          </button>
        </view>
        <text class="auth-code-status" :class="{ 'has-error': errorMessage }">
          <text class="auth-status-dot" aria-hidden="true" />
          {{ errorMessage || (isLogin ? '请输入右侧图形验证码' : '密码需为 8-64 位字符') }}
        </text>
        <button class="auth-submit" :class="{ enabled: canSubmit }" :disabled="!canSubmit" @click="submit">{{ submitting ? (isLogin ? '登录中…' : '注册中…') : (isLogin ? '登录' : '注册') }}</button>
        <button class="auth-switch" @click="switchMode">{{ isLogin ? '还没有账号？去注册' : '已有账号？返回登录' }}</button>
      </view>

      <text class="auth-footnote">{{ isLogin ? '微信小程序会自动完成微信登录，无需账号密码。' : '注册仅需要账号、密码和验证码，不收集昵称或头像。' }}</text>
    </view>
  </view>
</template>
