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
import { ref } from 'vue'
import NativeNavigation from '../../components/NativeNavigation.vue'
import { backToLedger } from '../../utils/entry'
import { staticResource } from '../../utils/staticResource'
// #ifdef H5
import { requestH5Refresh } from '../../utils/h5UpdateBrowser'
// #endif

const openFaq = ref('quick-start')

const faqs = [
  { id: 'quick-start', title: '如何开始记一笔？', answer: '点击首页右下角的“＋”，选择支出、收入或转账，输入金额后选择账户并确认日期时间，最后点击“确定”即可。常用账户会优先为你保留。' },
  { id: 'transfer', title: '转账为什么不算收支？', answer: '转账只是资金在不同账户之间移动，不会改变你的总资产，也不会进入本月收入或支出统计。' },
  { id: 'balance', title: '账户余额是怎么计算的？', answer: '余额会根据期初余额和每一笔已保存的流水自动计算。需要调整余额时，请通过记账或转账完成，避免流水对不上。' },
  { id: 'account', title: '记账账户会自动选择吗？', answer: '会。新增记账默认使用上一次保存记账时使用的账户，也可以在当前页面手动切换。' },
]

function toggleFaq(id: string) {
  openFaq.value = openFaq.value === id ? '' : id
}

</script>

<template>
  <view class="help-page">
    <scroll-view scroll-y class="help-scroll">
      <NativeNavigation variant="help" title="关于与帮助" compact back-label="返回我的" @back="backToLedger" />

      <view class="about-hero">
        <view class="about-brand"><image :src="staticResource('brand.png')" mode="aspectFill" /><text>哈记账</text></view>
        <text class="about-eyebrow">A LITTLE BOOKKEEPER</text>
        <text class="about-hero-title">记账，从简单开始</text>
        <text class="about-hero-copy">记录每一笔，让生活更清晰。<br />这里整理了常用功能和使用说明。</text>
        <view class="about-orbit orbit-one" /><view class="about-orbit orbit-two" />
      </view>

      <view class="about-section">
        <view class="about-heading"><view><text class="section-kicker">QUICK START</text><text class="about-section-title">三步，开始看懂生活</text></view></view>
        <view class="help-cards">
          <view class="help-card"><text class="help-card-icon mint">＋</text><text class="help-card-title">记下一笔</text><text class="help-card-copy">3 秒完成记录</text></view>
          <view class="help-card"><text class="help-card-icon peach">◷</text><text class="help-card-title">看懂变化</text><text class="help-card-copy">日历查看趋势</text></view>
          <view class="help-card"><text class="help-card-icon lavender">▣</text><text class="help-card-title">管理账户</text><text class="help-card-copy">资产清晰可见</text></view>
        </view>
      </view>

      <view class="about-section faq-section">
        <view class="about-heading"><view><text class="section-kicker">FAQ</text><text class="about-section-title">常见问题</text></view><text class="section-meta">点击查看</text></view>
        <view class="faq-list">
          <view v-for="faq in faqs" :key="faq.id" class="about-faq" :class="{ open: openFaq === faq.id }">
            <button class="faq-question" :aria-expanded="openFaq === faq.id" @click="toggleFaq(faq.id)"><text>{{ faq.title }}</text><text class="faq-plus" /></button>
            <text v-if="openFaq === faq.id" class="about-faq-answer">{{ faq.answer }}</text>
          </view>
        </view>
      </view>

      <!-- #ifdef H5 -->
      <view class="about-section">
        <view class="about-settings">
          <component :is="'button'" type="button" class="about-setting about-refresh" aria-label="强制刷新" @click="requestH5Refresh">
            <view class="about-setting-icon" aria-hidden="true"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round"><path d="M20 7v5h-5M4 17v-5h5" /><path d="M6.1 7a7 7 0 0 1 11.5-1L20 9M4 15l2.4 3A7 7 0 0 0 17.9 17" /></svg></view>
            <view class="about-setting-copy"><text>强制刷新</text><text>重新加载，获取最新页面代码</text></view>
            <text class="about-refresh-arrow" aria-hidden="true">›</text>
          </component>
        </view>
      </view>
      <!-- #endif -->

      <view class="about-footer"><image :src="staticResource('brand.png')" mode="aspectFill" /><text>哈记账</text><text>简单记账，安心生活</text></view>
    </scroll-view>
  </view>
</template>

<style scoped>
/* #ifdef H5 */
.about-refresh { box-sizing:border-box; min-height:76px; margin:0; border:0; appearance:none; -webkit-appearance:none; font:inherit; cursor:pointer; }
.about-refresh .about-setting-icon { width:38px; height:38px; border-radius:12px; }
.about-refresh svg { width:22px; height:22px; }
.about-refresh .about-setting-copy > text:last-child { white-space:normal; line-height:1.5; }
.about-refresh-arrow { color:#8ea49e; font-size:24px; }
.about-refresh:focus-visible { outline:2px solid var(--primary-dark); outline-offset:-4px; }
/* #endif */
</style>
