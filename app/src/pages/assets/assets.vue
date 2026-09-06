<script setup lang="ts">
import { onShow } from '@dcloudio/uni-app'
import { computed } from 'vue'
import BottomNav from '../../components/BottomNav.vue'
import { useLedger } from '../../stores/ledger'
import { yuan } from '../../utils/money'
const ledger = useLedger(); const s = ledger.state
const funds = computed(() => s.accounts.filter(a => a.kind === 'FUND' && a.status === 'ACTIVE'))
const credits = computed(() => s.accounts.filter(a => a.kind === 'CREDIT' && a.status === 'ACTIVE'))
const total = computed(() => ({ assets: funds.value.filter(a => a.includedInNetAsset).reduce((x, a) => x + a.balanceCents, 0), debt: credits.value.filter(a => a.includedInNetAsset).reduce((x, a) => x + a.balanceCents, 0) }))
onShow(() => { if (s.token) ledger.refresh() })
function account(kind: 'FUND' | 'CREDIT') { uni.navigateTo({ url: `/pages/account/account?kind=${kind}` }) }
function open(id: number) { uni.navigateTo({ url: `/pages/account/account?id=${id}` }) }
</script>
<template><view class="page"><view class="brand"><image src="/static/brand.png"/><view><text class="brand-name">资产</text><text class="brand-sub">净资产 = 总资产 − 总负债</text></view></view><view class="card summary"><text class="summary-title">净资产</text><text class="asset-number">{{yuan(total.assets-total.debt)}}</text><view class="summary-grid"><view><text class="label">总资产</text><text class="value">{{yuan(total.assets)}}</text></view><view><text class="label">总负债</text><text class="value" style="color:#e58a5b">{{yuan(total.debt)}}</text></view></view></view><view class="topbar"><text class="section-title" style="margin:0">资金账户</text><button class="secondary-btn" style="width:auto;padding:8px 13px" @click="account('FUND')">新增</button></view><view v-if="!funds.length" class="card empty">暂无资金账户</view><button v-for="a in funds" :key="a.id" class="account-card" @click="open(a.id)"><text class="transaction-icon">￥</text><view class="grow"><text class="name">{{a.name}}</text><text class="meta">{{a.includedInNetAsset?'计入净资产':'不计入净资产'}}</text></view><text>{{yuan(a.balanceCents)}}</text><text class="arrow">›</text></button><view class="topbar" style="margin-top:24px"><text class="section-title" style="margin:0">信贷账户</text><button class="secondary-btn" style="width:auto;padding:8px 13px" @click="account('CREDIT')">新增</button></view><view v-if="!credits.length" class="card empty">暂无信贷账户</view><button v-for="a in credits" :key="a.id" class="account-card" @click="open(a.id)"><text class="transaction-icon" style="background:#fff1e9;color:#d47769">信</text><view class="grow"><text class="name">{{a.name}}</text><text class="meta">可用额度 {{yuan(a.creditLimitCents-a.balanceCents)}}</text></view><text style="color:#d47769">{{yuan(a.balanceCents)}}</text><text class="arrow">›</text></button><BottomNav active="assets"/></view></template>
