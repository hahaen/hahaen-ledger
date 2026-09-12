<script setup lang="ts">
import { computed } from 'vue'
import { legalDocuments, type LegalDocumentType } from '../constants/legalDocuments'

const props = defineProps<{ type: LegalDocumentType }>()
const document = computed(() => legalDocuments[props.type])

function backToAuth() {
  if (getCurrentPages().length > 1) {
    uni.navigateBack()
    return
  }
  uni.reLaunch({ url: '/pages/auth/login/login' })
}
</script>

<template>
  <view class="help-page legal-page">
    <scroll-view scroll-y class="help-scroll">
      <view class="help-nav">
        <button class="help-back" aria-label="返回登录" @click="backToAuth">‹</button>
        <text class="help-title">{{ document.title }}</text>
        <view class="help-nav-side" />
      </view>

      <view class="about-hero legal-hero">
        <view class="about-brand"><image src="/static/brand.png" mode="aspectFill" /><text>哈记账</text></view>
        <text class="about-eyebrow">{{ document.eyebrow }}</text>
        <text class="about-hero-title">{{ document.heroTitle }}</text>
        <text class="about-hero-copy">{{ document.heroCopy }}</text>
        <view class="about-orbit orbit-one" /><view class="about-orbit orbit-two" />
      </view>

      <view class="legal-version"><text class="legal-version-dot" /><text>{{ document.version }}</text></view>

      <view v-for="section in document.sections" :key="section.title" class="about-section legal-section">
        <view class="about-heading"><view><text class="section-kicker">{{ document.eyebrow }}</text><text class="about-section-title">{{ section.title }}</text></view></view>
        <view class="legal-content-card">
          <text v-for="paragraph in section.paragraphs" :key="paragraph" class="legal-paragraph">{{ paragraph }}</text>
          <view v-if="section.items" class="legal-list">
            <view v-for="(item, index) in section.items" :key="item" class="legal-list-item"><text class="legal-list-index">{{ index + 1 }}</text><text>{{ item }}</text></view>
          </view>
        </view>
      </view>

      <view class="about-note legal-note"><text class="about-note-icon">✦</text><view class="about-note-copy"><text>请完整阅读</text><text>如不同意本协议或隐私协议，请勿注册、登录或继续使用服务。</text></view></view>
      <view class="about-footer"><image src="/static/brand.png" mode="aspectFill" /><text>哈记账 · {{ document.title }}</text><text>简单记账，安心生活</text></view>
    </scroll-view>
  </view>
</template>
