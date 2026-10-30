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
import { onLoad } from '@dcloudio/uni-app'
import NativeNavigation from '../../components/NativeNavigation.vue'
import LotteryNumbers from '../../components/LotteryNumbers.vue'
import { lotteryApi } from '../../utils/api'
import { gameName, type LotteryGame, type LotteryRecord, type LotterySync } from '../../utils/lottery'
const game = ref<LotteryGame>('DLT'), items = ref<LotteryRecord[]>([]), page = ref(1), hasMore = ref(false), loading = ref(false), error = ref(''), sync = ref<LotterySync>({})
function back() { if (getCurrentPages().length > 1) uni.navigateBack(); else uni.redirectTo({ url: '/pages/lottery/lottery' }) }
async function load(reset = true) {
  if (loading.value) return; loading.value = true; error.value = ''
  try { const next = await lotteryApi.draws(game.value, reset ? 1 : page.value + 1); items.value = reset ? next.items : [...items.value, ...next.items]; page.value = reset ? 1 : page.value + 1; hasMore.value = next.hasMore; sync.value = next.sync }
  catch (e) { error.value = e instanceof Error ? e.message : '读取失败，请重试' }
  finally { loading.value = false }
}
onLoad(options => { game.value = options?.game === 'SSQ' ? 'SSQ' : 'DLT'; void load() })
function open(item: LotteryRecord) { uni.navigateTo({ url: `/pages/lottery-detail/lottery-detail?kind=draw&id=${encodeURIComponent(item.id)}` }) }
</script>
<template>
  <view class="page profile-page lottery-page">
    <NativeNavigation variant="help" :page-top-extra="20" :title="`${gameName(game)}开奖`" back-label="返回彩票" @back="back" />
    <view class="profile-hero lottery-hero"><text class="profile-eyebrow">DRAW HISTORY</text><text class="profile-hero-title">每期号码，本地留存</text><text class="profile-hero-copy">查看号码与当期奖金，历史数据持续补齐</text></view>
    <text v-if="sync.lastSuccessAt" class="lottery-footnote">最近同步：{{ sync.lastSuccessAt.replace('T', ' ') }}{{ sync.lastError ? ' · 同步异常，展示本地记录' : '' }}{{ sync.historyComplete ? '' : ' · 历史回填中' }}</text>
    <text v-if="loading && !items.length" class="profile-loading">正在读取本地开奖…</text>
    <view v-if="error" class="lottery-notice"><text>{{ error }}</text><button class="text-button" @click="load()">重新加载</button></view>
    <text v-if="!loading && !error && !items.length" class="lottery-empty">暂无本地开奖记录，后台正在同步。</text>
    <button v-for="item in items" :key="item.id" class="lottery-card lottery-record" @click="open(item)"><view class="lottery-row"><text class="lottery-title">第{{ item.issue }}期</text><text class="lottery-muted">{{ item.drawDate }}</text></view><LotteryNumbers :red="item.red" :blue="item.blue" /><text class="lottery-muted">查看当期奖金与官方依据 ›</text></button>
    <button v-if="hasMore" class="secondary-btn" :disabled="loading" @click="load(false)">{{ loading ? '加载中…' : '加载更多' }}</button>
  </view>
</template>
