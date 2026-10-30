<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref } from 'vue'

const props = defineProps<{ refresh: () => Promise<void>; manual?: boolean }>()
const dialog = ref<HTMLDialogElement>()
const busy = ref(false)
const error = ref('')

function preventCancel(event: Event) { event.preventDefault() }
onMounted(() => {
  dialog.value?.addEventListener('cancel', preventCancel)
  dialog.value?.showModal()
})
onBeforeUnmount(() => dialog.value?.removeEventListener('cancel', preventCancel))

async function refresh() {
  if (busy.value) return
  busy.value = true
  error.value = ''
  try { await props.refresh() }
  catch {
    error.value = '暂时无法更新，请检查网络后重试。'
    busy.value = false
  }
}
</script>

<template>
  <dialog ref="dialog" class="h5-required-refresh" closedby="none" aria-labelledby="h5-refresh-title" aria-describedby="h5-refresh-copy h5-refresh-note">
    <div class="h5-refresh-handle" aria-hidden="true" />
    <div class="h5-refresh-icon" aria-hidden="true">
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round"><path d="M20 7v5h-5M4 17v-5h5" /><path d="M6.1 7a7 7 0 0 1 11.5-1L20 9M4 15l2.4 3A7 7 0 0 0 17.9 17" /></svg>
    </div>
    <h2 id="h5-refresh-title">{{ manual ? '强制刷新' : '新版本已就绪' }}</h2>
    <p id="h5-refresh-copy">{{ manual ? '重新获取最新页面代码，刷新后继续使用。' : '哈记账已更新，刷新后即可继续使用。' }}</p>
    <p id="h5-refresh-note">刷新将重新加载页面，未保存的内容会丢失。</p>
    <p v-if="error" class="h5-refresh-error" role="alert">{{ error }}</p>
    <!-- 动态原生标签避免 uni-app 将 button 转为无原生键盘行为的 uni-button。 -->
    <component :is="'button'" type="button" class="h5-refresh-button" :disabled="busy" :aria-busy="busy" autofocus @click="refresh">{{ busy ? '正在获取最新版本…' : error ? '重试刷新' : manual ? '确认刷新' : '刷新到最新版本' }}</component>
  </dialog>
</template>

<style scoped>
.h5-required-refresh { box-sizing:border-box; width:calc(100% - 32px); max-width:365px; max-height:calc(100dvh - 32px); margin:auto; padding:12px 26px 26px; overflow-y:auto; overscroll-behavior:contain; border:0; border-radius:30px; color:var(--text); background:var(--surface); box-shadow:0 14px 34px rgba(21,51,45,.16); text-align:center; font-family:inherit; }
.h5-required-refresh::backdrop { background:rgba(30,47,44,.3); }
.h5-refresh-handle { width:36px; height:4px; margin:0 auto 24px; border-radius:4px; background:var(--primary-light); }
.h5-refresh-icon { display:grid; place-items:center; width:58px; height:58px; margin:0 auto 18px; border-radius:20px; color:var(--primary-dark); background:var(--primary-light); }
.h5-refresh-icon svg { width:28px; height:28px; }
.h5-required-refresh h2 { margin:0 0 14px; font-size:23px; font-weight:750; line-height:1.3; }
.h5-required-refresh p { margin:0; color:#5e706b; font-size:14px; line-height:1.75; }
.h5-required-refresh #h5-refresh-note { margin-top:10px; font-size:12px; }
.h5-required-refresh .h5-refresh-error { margin-top:12px; color:#a34f43; font-size:13px; }
.h5-refresh-button { display:flex; align-items:center; justify-content:center; width:100%; min-height:54px; margin:24px 0 0; padding:12px; border:0; border-radius:17px; color:var(--surface); background:var(--primary-dark); font:inherit; font-size:16px; font-weight:700; line-height:1.4; cursor:pointer; transition:opacity .15s; }
.h5-refresh-button:active { opacity:.85; }
.h5-refresh-button:focus-visible { outline:3px solid var(--primary-dark); outline-offset:4px; }
.h5-refresh-button:disabled { opacity:.65; cursor:wait; }
@media (prefers-reduced-motion:reduce) { .h5-refresh-button { transition:none; } }
</style>
