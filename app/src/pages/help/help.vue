<script setup lang="ts">
import { ref } from 'vue'
import { backToLedger } from '../../utils/entry'

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

function showReserved(message: string) {
  uni.showToast({ title: message, icon: 'none' })
}
</script>

<template>
  <view class="help-page">
    <scroll-view scroll-y class="help-scroll">
      <view class="help-nav">
        <button class="help-back" aria-label="返回我的" @click="backToLedger">‹</button>
        <text class="help-title">关于与帮助</text>
        <view class="help-nav-side" />
      </view>

      <view class="about-hero">
        <view class="about-brand"><image src="/static/brand.png" mode="aspectFill" /><text>哈记账</text></view>
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

      <view class="about-section">
        <view class="about-heading"><view><text class="section-kicker">YOUR DATA</text><text class="about-section-title">数据</text></view></view>
        <view class="about-settings">
          <view class="about-setting"><text class="about-setting-icon">↗</text><view class="about-setting-copy"><text>数据导出</text><text>首版暂未开放，不会生成不可用操作</text></view><text class="about-status">暂未开放</text></view>
          <view class="about-setting"><text class="about-setting-icon">↙</text><view class="about-setting-copy"><text>数据导入</text><text>首版暂未开放，不会生成不可用操作</text></view><text class="about-status">暂未开放</text></view>
        </view>
      </view>

      <view class="about-note"><text class="about-note-icon">✦</text><view class="about-note-copy"><text>需要反馈？</text><text>告诉我们哪里还可以更好，帮助哈记账变得更顺手。</text></view><button class="about-status" @click="showReserved('反馈暂未开放')">暂未开放</button></view>
      <view class="about-footer"><image src="/static/brand.png" mode="aspectFill" /><text>哈记账 · v1.0</text><text>简单记账，安心生活</text></view>
    </scroll-view>
  </view>
</template>
