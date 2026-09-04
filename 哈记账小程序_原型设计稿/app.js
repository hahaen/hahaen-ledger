const root = document.getElementById('screen-root');
const backdrop = document.getElementById('modal-backdrop');
const sheet = document.getElementById('bottom-sheet');
const toast = document.getElementById('toast');
const specPanel = document.getElementById('spec-panel');
let activeTrigger = null;
let sheetAnchor = null;
let sheetAnchorRect = null;

const state = {
  view: 'home',
  entryType: 'expense',
  amount: '128.00',
  expression: '',
  category: '餐饮',
  incomeCategory: '工资',
  account: '微信',
  transferFrom: '微信',
  transferTo: '银行卡',
  calendarMonth: 9,
  selectedDay: 15,
  editMode: false,
  saving: false,
  firstUse: false,
  formError: '',
  avatarAuthState: 'idle',
  receiptState: 'empty',
  accountFormType: 'asset',
  calculatorFirst: null,
  calculatorOperator: '',
  calculatorWaiting: false
};

const categories = [
  ['🍜', '餐饮'], ['🚇', '交通'], ['🛍', '购物'], ['🎬', '娱乐'],
  ['⌂', '住房'], ['✚', '医疗'], ['▦', '日用'], ['＋', '其他']
];

const incomeCategories = [['↗', '工资'], ['✦', '奖金'], ['＋', '兼职'], ['↩', '退款'], ['＋', '其他']];

function resetCalculator() {
  state.amount = '';
  state.expression = '';
  state.formError = '';
  state.calculatorFirst = null;
  state.calculatorOperator = '';
  state.calculatorWaiting = false;
}

function formatNumber(value) {
  if (!Number.isFinite(value)) return '0';
  return String(Number(value.toFixed(2)));
}

function calculate(first, operator, second) {
  if (operator === '+') return first + second;
  if (operator === '−') return first - second;
  if (operator === '×') return first * second;
  if (operator === '÷') return second === 0 ? null : first / second;
  return second;
}

function nav(active) {
  return `<nav class="bottom-nav" aria-label="主导航">
    <button class="nav-item ${active === 'home' ? 'active' : ''}" data-nav="home" aria-label="首页"><span class="nav-icon">⌂</span><span>首页</span></button>
    <button class="nav-item ${active === 'calendar' ? 'active' : ''}" data-nav="calendar" aria-label="日历"><span class="nav-icon">◷</span><span>日历</span></button>
    <button class="nav-item ${active === 'assets' ? 'active' : ''}" data-nav="assets" aria-label="资产"><span class="nav-icon">▣</span><span>资产</span></button>
    <button class="nav-item ${active === 'mine' ? 'active' : ''}" data-nav="mine" aria-label="我的"><span class="nav-icon">◎</span><span>我的</span></button>
  </nav>`;
}

function appScreen(content, active, add = '') {
  return `<section class="app-screen"><div class="screen-scroll">${content}</div>${add}${nav(active)}</section>`;
}

function homeView() {
  const content = `
    <header class="page-top home-page-top"><div><div class="brand-lockup home-brand-icon"><img src="../哈记账.png" alt="" /></div><p class="page-subtitle">记录每一笔，让生活更清晰</p></div><button class="icon-button" data-action="show-search" aria-label="搜索账单">⌕</button></header>
    <section class="summary-card" aria-label="本月收支概览">
      <div class="summary-kicker">2026 年 9 月 · 日均消费</div>
      <div class="summary-amount"><small>¥</small>86.40</div>
      <div class="summary-foot"><div>本月支出<strong>¥2,592.00</strong></div><div>本月收入<strong class="income-text">¥8,500.00</strong></div></div>
    </section>
    <div class="section-row"><h3 class="section-title">最近记账</h3></div>
    <div class="date-group"><div class="date-heading"><div class="date-heading-copy"><b>今天 · 9月15日</b><span>周二</span></div><div class="date-flow" aria-label="今日收支"><span class="date-income">收 ¥0.00</span><span class="date-expense">支 ¥38.00</span></div></div><div class="transaction-list">
      <button class="transaction-item" data-action="edit-entry"><span class="category-icon type-icon expense" aria-hidden="true"><img src="assets/expense-coin.png" alt="" /></span><span class="transaction-copy"><span class="transaction-title type-label expense-label">支出</span><span class="transaction-note">12:20 · 微信</span></span><span class="transaction-amount expense">− ¥32</span><span class="arrow">›</span></button>
      <button class="transaction-item" data-action="edit-entry"><span class="category-icon type-icon expense" aria-hidden="true"><img src="assets/expense-coin.png" alt="" /></span><span class="transaction-copy"><span class="transaction-title type-label expense-label">支出</span><span class="transaction-note">08:41 · 交通卡</span></span><span class="transaction-amount expense">− ¥6</span><span class="arrow">›</span></button>
      <button class="transaction-item transfer-row" data-action="edit-entry"><span class="category-icon type-icon transfer" aria-hidden="true"><img src="assets/transfer-card.png" alt="" /></span><span class="transaction-copy"><span class="transaction-title type-label transfer-label">转账</span><span class="transaction-note">15:30 · 微信 → 银行卡</span></span><span class="transaction-amount transfer">¥100.00</span><span class="arrow">›</span></button>
    </div></div>
    <div class="date-group"><div class="date-heading"><div class="date-heading-copy"><b>9月14日</b><span>周一</span></div><div class="date-flow" aria-label="9月14日收支"><span class="date-income">收 ¥8,500.00</span><span class="date-expense">支 ¥89.90</span></div></div><div class="transaction-list">
      <button class="transaction-item" data-action="edit-entry"><span class="category-icon type-icon income" aria-hidden="true"><img src="assets/income-piggy-bank.png" alt="" /></span><span class="transaction-copy"><span class="transaction-title type-label income-label">收入</span><span class="transaction-note">09:00 · 银行卡</span></span><span class="transaction-amount income">＋ ¥8,500</span><span class="arrow">›</span></button>
      <button class="transaction-item" data-action="edit-entry"><span class="category-icon type-icon expense" aria-hidden="true"><img src="assets/expense-coin.png" alt="" /></span><span class="transaction-copy"><span class="transaction-title type-label expense-label">支出</span><span class="transaction-note">18:26 · 支付宝</span></span><span class="transaction-amount expense">− ¥89.90</span><span class="arrow">›</span></button>
    </div></div>`;
  return appScreen(content, 'home', '<button class="circle-add" data-action="new-entry" aria-label="新增记账">＋</button>');
}

function calendarView() {
  const year = 2026;
  const monthNumber = state.calendarMonth === 8 ? 8 : 9;
  const month = `${monthNumber}月`;
  const firstDay = new Date(year, monthNumber - 1, 1).getDay();
  const daysInMonth = new Date(year, monthNumber, 0).getDate();
  const previousMonthDays = new Date(year, monthNumber - 1, 0).getDate();
  const markedDays = new Set(monthNumber === 9 ? ['1', '5', '9', '12', '14', '15', '18', '22', '26'] : ['2', '6', '10', '15', '19', '24', '28']);
  const cells = Array.from({ length: 42 }, (_, i) => {
    const dayIndex = i - firstDay + 1;
    const muted = dayIndex < 1 || dayIndex > daysInMonth;
    const day = muted ? (dayIndex < 1 ? previousMonthDays + dayIndex : dayIndex - daysInMonth) : dayIndex;
    const dayText = String(day);
    const selected = !muted && dayText === String(state.selectedDay);
    const today = monthNumber === 9 && dayText === '15' && !muted;
    const marked = !muted && markedDays.has(dayText);
    return `<button class="calendar-cell ${muted ? 'muted' : ''} ${selected ? 'selected' : ''} ${today ? 'today' : ''}" data-day="${muted ? '' : dayText}"><span class="day-num">${dayText}</span>${marked ? '<span class="dots"><i></i><i class="income-dot"></i></span>' : ''}</button>`;
  }).join('');
  const content = `
    <header class="page-top"><div><h2 class="page-title">日历</h2><p class="page-subtitle">按日期回看每一笔生活</p></div><button class="icon-button" data-action="show-month-picker" aria-label="选择月份">⌄</button></header>
    <section class="calendar-card"><div class="section-row" style="margin:0 0 15px"><button class="month-switch" data-action="prev-month" aria-label="上个月">‹ <b>${month} 2026</b></button><button class="month-switch" data-action="next-month"><b>今天</b> ›</button></div>
      <div class="week-row"><span>日</span><span>一</span><span>二</span><span>三</span><span>四</span><span>五</span><span>六</span></div><div class="calendar-grid">${cells}</div>
      <div class="day-summary"><div><span>收入</span><strong class="red">¥8,500</strong></div><div><span>支出</span><strong class="green">¥38</strong></div><div><span>当日结余</span><strong>¥8,462</strong></div></div>
    </section>
    <div class="section-row"><h3 class="section-title">${month}${state.selectedDay}日 · ${new Date(year, monthNumber - 1, state.selectedDay).toLocaleDateString('zh-CN', { weekday: 'short' })}</h3><span class="section-meta">2 笔</span></div>
    <div class="transaction-list"><button class="transaction-item" data-action="edit-entry"><span class="category-icon">🍜</span><span class="transaction-copy"><span class="transaction-title">午餐 <em class="type-tag">支出</em></span><span class="transaction-note">12:20 · 微信</span></span><span class="transaction-amount expense">− ¥32</span><span class="arrow">›</span></button><button class="transaction-item" data-action="edit-entry"><span class="category-icon">🚇</span><span class="transaction-copy"><span class="transaction-title">地铁通勤 <em class="type-tag">支出</em></span><span class="transaction-note">08:41 · 交通卡</span></span><span class="transaction-amount expense">− ¥6</span><span class="arrow">›</span></button></div>`;
  return appScreen(content, 'calendar', '<button class="circle-add" data-action="new-entry" aria-label="新增记账">＋</button>');
}

function assetsView() {
  const content = `
    <header class="page-top"><div><h2 class="page-title">资产</h2><p class="page-subtitle">账户清晰，心里有数</p></div><button class="icon-button" data-action="show-spec" aria-label="查看资产说明">i</button></header>
    <section class="asset-hero"><div class="asset-label">净资产</div><div class="asset-net"><small>¥</small>12,680.00</div><div class="asset-breakdown"><div><span>总资产</span><b>¥15,200.00</b></div><div><span>总负债</span><b class="liability">¥2,520.00</b></div></div><div class="formula">净资产 = 总资产 − 总负债</div></section>
    <section class="account-section"><div class="account-heading"><h3>资金账户</h3><span>按余额从高到低</span></div><div class="account-list"><button class="account-item" data-action="edit-account"><span class="account-icon">◈</span><span class="account-main"><span class="account-name">银行卡</span><span class="account-desc">资金账户 · 尾号 2688</span></span><span class="account-balance">¥8,600.00</span><span class="arrow">›</span></button><button class="account-item" data-action="edit-account"><span class="account-icon">◉</span><span class="account-main"><span class="account-name">微信</span><span class="account-desc">资金账户</span></span><span class="account-balance">¥4,280.00</span><span class="arrow">›</span></button><button class="account-item" data-action="edit-account"><span class="account-icon">▱</span><span class="account-main"><span class="account-name">现金</span><span class="account-desc">资金账户</span></span><span class="account-balance">¥2,320.00</span><span class="arrow">›</span></button></div></section>
    <section class="account-section"><div class="account-heading"><h3>信贷账户</h3><span>按欠款从高到低</span></div><div class="account-list"><button class="account-item" data-action="edit-account" data-account-type="credit"><span class="account-icon credit">▤</span><span class="account-main"><span class="account-name">花呗</span><span class="account-desc">信贷账户 · 当前欠款</span></span><span class="account-balance credit">¥2,520.00</span><span class="arrow">›</span></button></div></section>`;
  return appScreen(content, 'assets', '<button class="circle-add" data-action="new-account" aria-label="新增资产账户">＋</button>');
}

function mineView() {
  const content = `
    <header class="page-top"><div><h2 class="page-title">我的</h2><p class="page-subtitle">把偏好交给自己</p></div><button class="icon-button" data-action="show-spec" aria-label="查看设计说明">i</button></header>
    <section class="profile-card"><div class="avatar ${state.avatarAuthState === 'success' ? 'authorized' : ''}" aria-label="${state.avatarAuthState === 'success' ? '已授权头像' : '默认头像'}"><img src="../哈记账.png" alt="" /></div><div class="profile-copy"><div class="profile-name">账本主人</div><div class="profile-status">${state.avatarAuthState === 'success' ? '头像已授权 · 数据随时可用' : state.avatarAuthState === 'loading' ? '正在获取微信头像…' : '头像未授权 · 可继续使用'}</div></div><button class="text-button" data-action="avatar-auth" ${state.avatarAuthState === 'loading' ? 'disabled' : ''}>${state.avatarAuthState === 'success' ? '更换头像' : state.avatarAuthState === 'loading' ? '授权中' : '去授权'}</button></section>
    <div class="days-card"><span>坚持记录，正在变成习惯</span><strong>累计记账 28 日</strong></div>
    <section class="settings-group"><p class="settings-label">记账工具</p><div class="settings-list"><button class="setting-item" data-action="recurring"><span class="setting-icon">↻</span><span>周期记账</span><span class="setting-badge">2 条规则</span><span class="setting-arrow">›</span></button><button class="setting-item" data-action="goto-assets"><span class="setting-icon">▣</span><span>资金账户管理</span><span class="setting-arrow">›</span></button><button class="setting-item" data-action="show-toast"><span class="setting-icon">⌘</span><span>分类管理</span><span class="setting-arrow">›</span></button></div></section>
    <section class="settings-group"><p class="settings-label">更多</p><div class="settings-list"><button class="setting-item" data-action="show-toast"><span class="setting-icon">◌</span><span>记账提醒</span><span class="setting-badge">即将支持</span><span class="setting-arrow">›</span></button><button class="setting-item" data-action="show-toast"><span class="setting-icon">?</span><span>关于与帮助</span><span class="setting-arrow">›</span></button></div></section>`;
  return appScreen(content, 'mine');
}

function entryView() {
  const isIncome = state.entryType === 'income';
  const isTransfer = state.entryType === 'transfer';
  const title = state.editMode ? '编辑记账' : '新增记账';
  const currentCategory = isIncome ? (state.incomeCategory || '工资') : state.category;
  const accountRows = isTransfer ? `<button class="field-row" data-action="show-account-picker" data-account-role="from"><span class="field-icon">↗</span><span class="field-label">转出账户</span><span class="field-value">${state.transferFrom}</span><span class="arrow">›</span></button><button class="field-row" data-action="show-account-picker" data-account-role="to"><span class="field-icon">↘</span><span class="field-label">转入账户</span><span class="field-value">${state.transferTo}</span><span class="arrow">›</span></button>` : `<button class="field-row" data-action="show-account-picker" data-account-role="account"><span class="field-icon">◉</span><span class="field-label">资金账户</span><span class="field-value">${state.account || '请选择'}</span><span class="arrow">›</span></button>`;
  const categoryRow = isTransfer ? '' : `<button class="field-row" data-action="show-category-picker"><span class="field-icon">${isIncome ? '↗' : '⌁'}</span><span class="field-label">分类</span><span class="field-value">${currentCategory || '请选择'}</span><span class="arrow">›</span></button>`;
  const receipts = `<section class="receipt-section"><div class="receipt-title"><b>账单凭证</b><span>可选 · JPG/PNG/WEBP，单张 ≤10MB，最多 9 张</span></div><div class="receipt-grid">${state.receiptState === 'uploading' ? '<div class="receipt uploading" aria-label="凭证上传中">▧</div>' : state.receiptState === 'failed' ? '<div class="receipt failed" data-action="retry-upload" aria-label="凭证上传失败">失败<br><u>重试</u></div>' : state.receiptState === 'ready' ? '<div class="receipt ready" aria-label="凭证已上传">✓</div>' : ''}<button class="receipt add" data-action="add-receipt" aria-label="添加凭证">＋</button></div>${state.receiptState === 'failed' ? '<p class="receipt-hint error">凭证未上传成功，不影响保存账单</p>' : state.receiptState === 'ready' ? '<p class="receipt-hint">已上传 1 张凭证 · 点击缩略图预览</p>' : '<p class="receipt-hint">凭证不会识别金额，可在详情中继续补充</p>'}</section>`;
  return `<section class="entry-screen"><header class="screen-nav"><div class="nav-side"><button class="back-button" data-action="back" aria-label="返回">‹</button></div><h2>${title}</h2><div class="nav-side">${state.editMode ? '<button class="delete-text" data-action="delete-entry">删除</button>' : ''}</div></header>
    <div class="entry-type"><button class="${state.entryType === 'expense' ? 'active' : ''}" data-type="expense">支出</button><button class="${isIncome ? 'active income-active' : ''}" data-type="income">收入</button><button class="${isTransfer ? 'active' : ''}" data-type="transfer">转账</button></div>
    <section class="amount-panel"><div class="amount-label">${isTransfer ? '转账金额' : '记账金额'}</div><div class="amount-display"><small>¥</small><strong class="${state.amount ? '' : 'amount-placeholder'}">${state.amount || '输入金额'}</strong></div><div class="calculation-line">${state.expression || '支持 +  −  ×  ÷  连续计算'}</div>${state.formError ? `<div class="inline-error">${state.formError}</div>` : ''}</section>
    <div class="fields-card">${categoryRow}${accountRows}<button class="field-row" data-action="show-date-picker"><span class="field-icon">◷</span><span class="field-label">日期与时间</span><span class="field-value">2026年09月15日 12:20</span><span class="arrow">›</span></button><button class="field-row" data-action="show-note"><span class="field-icon">⌁</span><span class="field-label">备注</span><span class="field-value placeholder">写点说明...</span></button></div>
    ${receipts}<button class="primary-button ${state.saving ? 'loading' : ''}" data-action="save-entry" ${state.saving ? 'disabled' : ''}>${state.saving ? '正在保存' : (state.editMode ? '保存修改' : '保存记账')}</button>
    <div class="keypad"><button class="key" data-key="7">7</button><button class="key" data-key="8">8</button><button class="key" data-key="9">9</button><button class="key action" data-key="÷">÷</button><button class="key" data-key="4">4</button><button class="key" data-key="5">5</button><button class="key" data-key="6">6</button><button class="key action" data-key="×">×</button><button class="key" data-key="1">1</button><button class="key" data-key="2">2</button><button class="key" data-key="3">3</button><button class="key action" data-key="−">−</button><button class="key" data-key="0">0</button><button class="key" data-key=".">.</button><button class="key action" data-key="⌫">⌫</button><button class="key action" data-key="+">＋</button><button class="key action" data-key="C">清除</button><button class="key equal" data-key="=">=</button></div>
  </section>`;
}

function firstUseView() {
  return `<section class="first-use"><div class="welcome-brand"><img src="../哈记账.png" alt="哈记账" /><span>哈记账</span></div><span class="eyebrow">A LITTLE BOOKKEEPER</span><div class="first-illustration"><span class="leaf"></span></div><h2>记录每一笔，<br><span style="color:var(--primary-dark)">让生活更清晰</span></h2><p>用 3 秒记下一笔，<br>用 10 秒看懂这个月。</p><button class="primary-button" data-action="start-use">开始记账</button><button class="skip-link" data-action="skip-use">稍后设置账户</button></section>`;
}

function render() {
  if (state.firstUse) { root.innerHTML = firstUseView(); return; }
  if (state.view === 'entry') root.innerHTML = entryView();
  else if (state.view === 'calendar') root.innerHTML = calendarView();
  else if (state.view === 'assets') root.innerHTML = assetsView();
  else if (state.view === 'mine') root.innerHTML = mineView();
  else root.innerHTML = homeView();
}

function showToast(message) {
  toast.textContent = message; toast.classList.add('show');
  clearTimeout(showToast.timer); showToast.timer = setTimeout(() => toast.classList.remove('show'), 2200);
}

function positionSheet(anchor = sheetAnchor) {
  if (backdrop.hidden) return;
  const padding = 16;
  const gap = 10;
  const backdropRect = backdrop.getBoundingClientRect();
  const viewportWidth = backdrop.clientWidth;
  const viewportHeight = backdrop.clientHeight;
  const sheetWidth = sheet.offsetWidth;
  const sheetHeight = sheet.offsetHeight;
  const triggerRect = anchor?.isConnected ? anchor.getBoundingClientRect() : sheetAnchorRect;
  let left = triggerRect ? triggerRect.left - backdropRect.left : (viewportWidth - sheetWidth) / 2;
  let top = triggerRect ? triggerRect.bottom - backdropRect.top + gap : (viewportHeight - sheetHeight) / 2;

  left = Math.min(Math.max(left, padding), viewportWidth - sheetWidth - padding);
  if (top + sheetHeight > viewportHeight - padding && triggerRect) top = triggerRect.top - sheetHeight - gap;
  top = Math.min(Math.max(top, padding), viewportHeight - sheetHeight - padding);

  sheet.style.left = `${Math.round(left)}px`;
  sheet.style.top = `${Math.round(top)}px`;
}

function openSheet(html, wide = false) {
  sheetAnchor = activeTrigger?.isConnected ? activeTrigger : null;
  sheetAnchorRect = sheetAnchor?.getBoundingClientRect() || null;
  sheet.className = `bottom-sheet${wide ? ' sheet-wide' : ''}`;
  sheet.innerHTML = `<div class="sheet-handle"></div>${html}`;
  backdrop.hidden = false;
  requestAnimationFrame(() => positionSheet());
}

function closeSheet() { backdrop.hidden = true; sheet.innerHTML = ''; sheetAnchor = null; sheetAnchorRect = null; }

function categoryPicker() {
  const items = (state.entryType === 'income' ? incomeCategories : categories).map(([icon, name]) => `<button class="sheet-option ${(state.entryType === 'income' ? state.incomeCategory : state.category) === name ? 'selected' : ''}" data-pick-category="${name}"><span class="category-icon">${icon}</span>${name}</button>`).join('');
  openSheet(`<h3 class="sheet-title">选择分类</h3><div class="sheet-options">${items}</div><div class="sheet-actions"><button class="secondary-button" data-action="close-sheet">取消</button><button class="primary-button" data-action="close-sheet">完成</button></div>`);
}

function accountPicker() {
  const role = state.pickerRole || 'account';
  const selected = role === 'from' ? state.transferFrom : role === 'to' ? state.transferTo : state.account;
  const title = role === 'from' ? '选择转出账户' : role === 'to' ? '选择转入账户' : '选择资金账户';
  const items = [['◉', '微信', '¥4,280.00'], ['◈', '银行卡', '¥8,600.00'], ['▱', '现金', '¥2,320.00']].map(([icon, name, balance]) => `<button class="field-row ${selected === name ? 'selected-row' : ''}" data-pick-account="${name}"><span class="field-icon">${icon}</span><span class="field-label">${name}</span><span class="field-value">${balance}</span><span class="arrow">${selected === name ? '✓' : '›'}</span></button>`).join('');
  openSheet(`<h3 class="sheet-title">${title}</h3><div class="settings-list">${items}</div><div class="sheet-actions"><button class="secondary-button" data-action="close-sheet">取消</button><button class="primary-button" data-action="close-sheet">完成</button></div>`, true);
}

function accountForm(edit = false, accountType = state.accountFormType || 'asset') {
  state.accountFormType = accountType;
  const isCredit = accountType === 'credit';
  const typeLabel = isCredit ? '信贷账户' : '资金账户';
  const balanceLabel = edit ? (isCredit ? '当前欠款' : '当前余额') : (isCredit ? '期初欠款' : '期初余额');
  const typeChoice = edit ? `<div class="field-row"><span class="field-label">账户归属</span><span class="field-value">${typeLabel}（不可直接切换）</span></div>` : `<div class="account-type-choice"><button class="${isCredit ? '' : 'active'}" data-action="toggle-account-type" data-account-type="asset">资金账户</button><button class="${isCredit ? 'active credit-choice' : ''}" data-action="toggle-account-type" data-account-type="credit">信贷账户</button></div>`;
  openSheet(`<h3 class="sheet-title">${edit ? '编辑账户' : '新增资产账户'}</h3><div class="settings-list"><div class="field-row"><span class="field-label">账户名称</span><span class="field-value">${edit ? (isCredit ? '花呗' : '微信') : (isCredit ? '例如：花呗' : '例如：微信')}</span></div>${typeChoice}<div class="field-row"><span class="field-label">${balanceLabel}</span><span class="field-value">¥${edit ? (isCredit ? '2,520.00' : '4,280.00') : '0.00'}</span></div><div class="field-row"><span class="field-label">计入净资产</span><span class="field-value" style="color:var(--primary-dark)">${isCredit ? '●' : '●'}</span></div></div><p class="confirm-copy">${isCredit ? '信贷账户用当前欠款表达。' : '资金账户用当前余额表达。'} 余额由期初余额与账单流水计算得出；编辑态只读，请通过记账或转账调整。</p><div class="sheet-actions"><button class="secondary-button" data-action="close-sheet">取消</button><button class="primary-button" data-action="save-account">保存账户</button></div>`);
}

function recurringSheet() {
  openSheet(`<h3 class="sheet-title">周期记账</h3><div class="settings-list"><button class="setting-item"><span class="setting-icon">↻</span><span><b>房租</b><small style="display:block;color:var(--text-secondary);margin-top:4px">支出 · 每月 1 日 · ¥3,200</small></span><span class="setting-badge">启用中</span><span class="setting-arrow">›</span></button><button class="setting-item"><span class="setting-icon">↻</span><span><b>会员订阅</b><small style="display:block;color:var(--text-secondary);margin-top:4px">支出 · 每月 15 日 · ¥25</small></span><span class="setting-badge" style="background:#f3f3f3;color:#9ca4a1">已暂停</span><span class="setting-arrow">›</span></button></div><p class="confirm-copy">到期后生成待确认记录，确认后才会影响账户余额与统计。</p><div class="sheet-actions"><button class="secondary-button" data-action="close-sheet">关闭</button><button class="primary-button" data-action="show-toast">＋ 新增规则</button></div>`);
}

document.addEventListener('click', (event) => {
  const navButton = event.target.closest('[data-nav]');
  if (navButton) { state.view = navButton.dataset.nav; state.editMode = false; render(); return; }
  const typeButton = event.target.closest('[data-type]');
  if (typeButton) { state.entryType = typeButton.dataset.type; state.expression = ''; state.formError = ''; state.calculatorFirst = null; state.calculatorOperator = ''; state.calculatorWaiting = false; render(); showToast(`已切换为${typeButton.textContent}`); return; }
  const dayButton = event.target.closest('[data-day]');
  if (dayButton && dayButton.dataset.day) { state.selectedDay = Number(dayButton.dataset.day); render(); return; }
  const keyButton = event.target.closest('[data-key]');
  if (keyButton) { handleKey(keyButton.dataset.key); return; }
  const categoryButton = event.target.closest('[data-pick-category]');
  if (categoryButton) { if (state.entryType === 'income') state.incomeCategory = categoryButton.dataset.pickCategory; else state.category = categoryButton.dataset.pickCategory; closeSheet(); render(); return; }
  const accountButton = event.target.closest('[data-pick-account]');
  if (accountButton) {
    if (state.pickerRole === 'from') state.transferFrom = accountButton.dataset.pickAccount;
    else if (state.pickerRole === 'to') state.transferTo = accountButton.dataset.pickAccount;
    else state.account = accountButton.dataset.pickAccount;
    closeSheet(); render(); return;
  }
  const monthButton = event.target.closest('[data-month]');
  if (monthButton) { state.calendarMonth = Number(monthButton.dataset.month); closeSheet(); render(); return; }

  const actionElement = event.target.closest('[data-action]');
  const action = actionElement?.dataset.action;
  if (!action) return;
  activeTrigger = actionElement;
  if (action === 'new-entry') { state.view = 'entry'; state.editMode = false; state.entryType = 'expense'; state.receiptState = 'empty'; state.transferFrom = '微信'; state.transferTo = '银行卡'; resetCalculator(); render(); }
  if (action === 'edit-entry') { state.view = 'entry'; state.editMode = true; state.amount = '32.00'; state.category = '餐饮'; state.receiptState = 'ready'; state.expression = ''; state.formError = ''; state.calculatorFirst = null; state.calculatorOperator = ''; state.calculatorWaiting = false; render(); }
  if (action === 'back') { if (state.editMode) openSheet(`<h3 class="sheet-title">放弃这次修改？</h3><p class="confirm-copy">当前内容还没有保存，离开后修改会丢失。</p><div class="sheet-actions"><button class="secondary-button" data-action="close-sheet">继续编辑</button><button class="primary-button" data-action="discard-entry">放弃更改</button></div>`); else { state.view = 'home'; render(); } }
  if (action === 'discard-entry') { closeSheet(); state.view = 'home'; render(); }
  if (action === 'show-category-picker') categoryPicker();
  if (action === 'show-account-picker') { state.pickerRole = event.target.closest('[data-account-role]')?.dataset.accountRole || 'account'; accountPicker(); }
  if (action === 'show-date-picker') openSheet(`<h3 class="sheet-title">日期与时间</h3><div class="settings-list"><div class="field-row"><span class="field-label">记账日期</span><span class="field-value">2026年09月15日　›</span></div><div class="field-row"><span class="field-label">记账时间</span><span class="field-value">12:20　›</span></div></div><div class="sheet-actions"><button class="secondary-button" data-action="close-sheet">取消</button><button class="primary-button" data-action="close-sheet">确定</button></div>`);
  if (action === 'show-note') openSheet(`<h3 class="sheet-title">添加备注</h3><textarea class="note-input" maxlength="100" placeholder="记录一点上下文，例如：和朋友聚餐"></textarea><div class="note-count">0 / 100</div><div class="sheet-actions"><button class="secondary-button" data-action="close-sheet">取消</button><button class="primary-button" data-action="close-sheet">完成</button></div>`);
  if (action === 'save-entry') {
    if (!state.amount || Number(state.amount) <= 0) { state.formError = '请输入大于 0 的金额'; render(); showToast('请先输入记账金额'); return; }
    if (Number(state.amount) > 999999999.99) { state.formError = '金额不能超过 ¥999,999,999.99'; render(); showToast('金额超过上限'); return; }
    if (state.entryType === 'transfer' && state.transferFrom === state.transferTo) { state.formError = '转出账户和转入账户不能相同'; render(); showToast('请更换一个账户'); return; }
    state.formError = ''; state.saving = true; render(); setTimeout(() => { state.saving = false; state.view = 'home'; state.editMode = false; render(); showToast('记账成功，账户余额已更新'); }, 850);
  }
  if (action === 'delete-entry') openSheet(`<h3 class="sheet-title">删除这笔账单？</h3><p class="confirm-copy">删除后会同步刷新首页、日历和相关账户余额。凭证会进入异步清理流程。</p><div class="sheet-actions"><button class="secondary-button" data-action="close-sheet">取消</button><button class="primary-button" style="background:#df8b77" data-action="confirm-delete">确认删除</button></div>`);
  if (action === 'confirm-delete') { closeSheet(); state.view = 'home'; render(); showToast('账单已删除，余额已重算'); }
  if (action === 'add-receipt') { openSheet(`<h3 class="sheet-title">添加账单凭证</h3><p class="confirm-copy">可选择相册或拍照。图片只作为附件保存，不会自动识别金额。</p><div class="sheet-actions"><button class="secondary-button" data-action="start-upload">从相册选择</button><button class="primary-button" data-action="start-upload">拍照上传</button></div>`); }
  if (action === 'start-upload') { closeSheet(); state.receiptState = 'uploading'; render(); setTimeout(() => { state.receiptState = 'failed'; render(); showToast('凭证上传失败，请点击重试'); }, 650); }
  if (action === 'retry-upload') { state.receiptState = 'uploading'; render(); setTimeout(() => { state.receiptState = 'ready'; render(); showToast('凭证已上传'); }, 650); }
  if (action === 'new-account') accountForm(false, 'asset');
  if (action === 'edit-account') accountForm(true, event.target.closest('[data-account-type]')?.dataset.accountType || 'asset');
  if (action === 'toggle-account-type') accountForm(false, event.target.closest('[data-account-type]')?.dataset.accountType || 'asset');
  if (action === 'save-account') { closeSheet(); showToast('账户已保存'); }
  if (action === 'recurring') recurringSheet();
  if (action === 'avatar-auth') { state.avatarAuthState = 'loading'; render(); setTimeout(() => { state.avatarAuthState = 'success'; render(); showToast('头像授权成功'); }, 650); }
  if (action === 'goto-assets') { state.view = 'assets'; render(); }
  if (action === 'show-search') openSheet(`<h3 class="sheet-title">搜索账单</h3><div class="field-row" style="border:1px solid var(--divider);border-radius:12px"><span class="field-icon">⌕</span><span class="field-value placeholder">输入账单名称或备注</span></div><p class="confirm-copy" style="text-align:center;margin-top:18px">支持按名称、备注搜索；无结果时会在这里展示空状态。</p>`);
  if (action === 'show-month-picker') openSheet(`<h3 class="sheet-title">选择月份</h3><div class="sheet-options"><button class="sheet-option ${state.calendarMonth === 8 ? 'selected' : ''}" data-month="8"><span class="category-icon">‹</span>2026年8月</button><button class="sheet-option ${state.calendarMonth === 9 ? 'selected' : ''}" data-month="9"><span class="category-icon">◷</span>2026年9月</button></div>`);
  if (action === 'prev-month') { state.calendarMonth = 8; render(); }
  if (action === 'next-month') { state.calendarMonth = 9; state.selectedDay = 15; render(); }
  if (action === 'show-spec') specPanel.hidden = false;
  if (action === 'close-spec') specPanel.hidden = true;
  if (action === 'preview-first-use') { specPanel.hidden = true; state.firstUse = true; render(); }
  if (action === 'close-sheet') closeSheet();
  if (action === 'show-toast') { closeSheet(); showToast('该入口已预留，第一版暂不开放'); }
  if (action === 'start-use' || action === 'skip-use') { state.firstUse = false; state.view = 'home'; render(); showToast(action === 'start-use' ? '欢迎开始记账' : '已跳过账户设置'); }
});

backdrop.addEventListener('click', (event) => { if (event.target === backdrop) closeSheet(); });
window.addEventListener('resize', () => positionSheet());

function evaluateExpression(expression) {
  const tokens = expression.trim().split(/\s+/);
  const values = [];
  const operators = [];
  const precedence = { '+': 1, '−': 1, '×': 2, '÷': 2 };
  const apply = () => {
    const operator = operators.pop();
    const second = values.pop();
    const first = values.pop();
    if (operator === '÷' && second === 0) throw new Error('DIVIDE_BY_ZERO');
    values.push(calculate(first, operator, second));
  };
  tokens.forEach((token) => {
    if (/^-?\d+(\.\d+)?$/.test(token)) values.push(Number(token));
    else if (precedence[token]) {
      while (operators.length && precedence[operators[operators.length - 1]] >= precedence[token]) apply();
      operators.push(token);
    }
  });
  while (operators.length) apply();
  return values.length === 1 && Number.isFinite(values[0]) ? values[0] : null;
}

function handleKey(key) {
  state.formError = '';
  if (key === 'C') { resetCalculator(); }
  else if (key === '⌫') {
    if (state.calculatorWaiting) {
      state.expression = state.expression.trim().replace(/\s[+−×÷]\s$/, '');
      state.calculatorWaiting = false;
    } else {
      state.amount = state.amount.slice(0, -1);
      state.expression = state.expression.replace(/\d*\.?\d*$/, state.amount);
    }
  } else if (key === '=') {
    const expression = state.expression || state.amount;
    if (expression && /\d\s[+−×÷]\s\d/.test(expression)) {
      try {
        const result = evaluateExpression(expression);
        if (result === null) throw new Error('INVALID');
        state.expression = `${expression} =`;
        state.amount = formatNumber(result);
        state.calculatorWaiting = true;
      } catch (error) {
        state.formError = error.message === 'DIVIDE_BY_ZERO' ? '除数不能为 0，请重新输入' : '算式有误，请检查后重试';
      }
    }
  } else if (['+', '−', '×', '÷'].includes(key)) {
    if (!state.amount && !state.expression) return;
    if (!state.expression) state.expression = state.amount;
    if (state.calculatorWaiting) state.expression = state.expression.replace(/\s=$/, '');
    if (!/\s[+−×÷]\s$/.test(state.expression)) {
      state.expression = `${state.expression} ${key} `;
      state.calculatorWaiting = true;
    }
  } else if (key === '.') {
    if (state.calculatorWaiting) { state.amount = '0.'; state.calculatorWaiting = false; }
    else if (!state.amount.includes('.')) state.amount += state.amount ? '.' : '0.';
    state.expression = state.expression.replace(/\d*\.?\d*$/, state.amount);
  } else {
    if (state.calculatorWaiting) { state.amount = ''; state.calculatorWaiting = false; }
    if (state.amount.length < 12 && (!state.amount.includes('.') || state.amount.split('.')[1].length < 2)) state.amount += key;
    state.expression = state.expression ? state.expression.replace(/\d*\.?\d*$/, state.amount) : state.amount;
  }
  render();
}

render();
