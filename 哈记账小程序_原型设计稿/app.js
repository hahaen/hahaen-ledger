const root = document.getElementById('screen-root');
const backdrop = document.getElementById('modal-backdrop');
const sheet = document.getElementById('bottom-sheet');
const toast = document.getElementById('toast');
const specPanel = document.getElementById('spec-panel');
let datePickerDraft = null;
let autoCalculateTimer = null;
let pendingFundEdit = null;

function getCurrentDateTime() {
  const now = new Date();
  const pad = value => String(value).padStart(2, '0');
  return `${now.getFullYear()}年${pad(now.getMonth() + 1)}月${pad(now.getDate())}日 ${pad(now.getHours())}:${pad(now.getMinutes())}`;
}

const state = {
  view: 'login',
  platform: 'h5',
  authenticated: false,
  wechatLoggingIn: false,
  authMode: 'login',
  authSubmitting: false,
  authStatus: 'idle',
  captchaCode: 'K7P2',
  captchaMessage: '',
  authError: '',
  authForm: { account: '', password: '', code: '' },
  entryType: 'expense',
  amount: '128.00',
  expression: '',
  account: '微信',
  lastAccount: '微信',
  entryDateTime: getCurrentDateTime(),
  transferFrom: '微信',
  transferTo: '银行卡',
  calendarYear: 2026,
  calendarMonth: 9,
  selectedDay: 15,
  editMode: false,
  saving: false,
  firstUse: false,
  formError: '',
  avatarAuthState: 'idle',
  accountFormType: 'asset',
  accountFormEdit: false,
  accountIncludeNetAsset: true,
  accountSections: { credit: true, asset: true },
  openFaq: 'quick-start',
  calculatorFirst: null,
  calculatorOperator: '',
  calculatorWaiting: false,
  selectedEntryId: null,
  detailReturnView: 'home',
  editReturnView: 'home',
  deletedEntryIds: {},
  refundRecords: { 'expense-6': [{ id: 'refund-expense-6-1', amount: '6.00', time: '2026年09月15日 20:18' }] },
  pendingRefundId: null,
  refundAmountDraft: '',
  refundError: '',
  note: ''
};

const settingIcons = {
  help: '<span class="setting-icon setting-icon-help" aria-hidden="true"><svg viewBox="0 0 40 40" focusable="false"><path class="icon-fill" d="M5.5 11c5.5-2.2 10-1.5 14.5 2v20c-4.5-2.8-9-3.1-14.5-.7z"></path><path class="icon-fill" d="M34.5 11c-5.5-2.2-10-1.5-14.5 2v20c4.5-2.8 9-3.1 14.5-.7z"></path><path d="M5.5 11c5.5-2.2 10-1.5 14.5 2v20c-4.5-2.8-9-3.1-14.5-.7zM34.5 11c-5.5-2.2-10-1.5-14.5 2v20c4.5-2.8 9-3.1 14.5-.7zM20 13v20"></path><circle class="icon-badge" cx="29" cy="10.5" r="6.5"></circle><path d="M27.2 9a2 2 0 1 1 3 1.7c-.8.5-1.2.8-1.2 1.9M29 15.2h.01"></path></svg></span>'
};

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
  return `<section class="app-screen${active === 'home' ? ' home-screen' : ''}"><div class="screen-scroll">${content}</div>${add}${nav(active)}</section>`;
}

const demoEntries = [
  { id: 'income-8500', type: 'income', amount: 8500, date: '2026年09月14日', time: '09:00', account: '银行卡', note: '9月工资', category: '工资' },
  { id: 'expense-8990', type: 'expense', amount: 89.9, date: '2026年09月14日', time: '18:26', account: '支付宝', note: '晚餐', category: '餐饮' },
  { id: 'expense-32', type: 'expense', amount: 32, date: '2026年09月15日', time: '12:20', account: '微信', note: '午餐', category: '餐饮' },
  { id: 'expense-6', type: 'expense', amount: 6, date: '2026年09月15日', time: '08:41', account: '交通卡', note: '地铁出行', category: '交通' },
  { id: 'transfer-100', type: 'transfer', amount: 100, date: '2026年09月15日', time: '15:30', from: '微信', to: '银行卡', note: '日常资金转入', category: '转账' }
];

const entryTypes = {
  expense: { label: '支出', image: 'expense-coin.png', sign: '− ' },
  income: { label: '收入', image: 'income-piggy-bank.png', sign: '＋ ' },
  repayment: { label: '还款', image: 'repayment-card.png', sign: '' },
  transfer: { label: '转账', image: 'transfer-card.png', sign: '' }
};

function getEntry(id = state.selectedEntryId) {
  return demoEntries.find(entry => entry.id === id);
}

function money(value) {
  return Number(value).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}

function entryNote(entry) {
  return ['transfer', 'repayment'].includes(entry.type)
    ? `${entry.time} · ${entry.from} → ${entry.to}`
    : `${entry.time} · ${entry.account}`;
}

function transactionItem(entry) {
  const type = entryTypes[entry.type];
  const label = entry.balanceAdjustment ? '账户余额补齐' : type.label;
  const refunded = getRefundRecords(entry.id).length > 0;
  const refundMark = refunded ? '<em class="refund-mark" aria-label="已退款">退</em>' : '';
  return `<button class="transaction-item ${entry.type === 'transfer' ? 'transfer-row' : ''}" data-action="view-entry" data-entry-id="${entry.id}" aria-label="查看${label} ${money(entry.amount)} 元详情"><span class="category-icon type-icon ${entry.type}" aria-hidden="true"><img src="assets/${type.image}" alt="" /></span><span class="transaction-copy"><span class="transaction-title-row"><span class="transaction-title type-label ${entry.type}-label">${label}</span>${refundMark}</span><span class="transaction-note">${escapeAttribute(entryNote(entry))}</span></span><span class="transaction-amount ${entry.type}">${type.sign}¥${money(entry.amount)}</span><span class="arrow">›</span></button>`;
}

function activeEntryAmount(entry) {
  return Math.max(0, entry.amount - getRefundTotal(entry.id));
}

function getRefundRecords(entryId) {
  return state.refundRecords[entryId] || [];
}

function getRefundTotal(entryId) {
  return getRefundRecords(entryId).reduce((sum, record) => sum + Number(record.amount), 0);
}

function getRemainingRefundAmount(entry) {
  return Math.max(0, Number((entry.amount - getRefundTotal(entry.id)).toFixed(2)));
}

function escapeAttribute(value) {
  return String(value).replace(/[&<>"']/g, character => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[character]));
}

function refundSheet(entry) {
  const remaining = getRemainingRefundAmount(entry);
  const amount = state.refundAmountDraft || formatNumber(remaining);
  const actionText = entry.type === 'income' ? '冲销这笔收入' : entry.type === 'transfer' ? '撤销这笔转账' : '退回这笔支出';
  return `<h3 class="sheet-title">填写退款金额</h3><p class="confirm-copy">${actionText}，原账单金额为 <b>¥${money(entry.amount)}</b>，本次最多可退款 <b>¥${money(remaining)}</b>。</p><label class="refund-input-label" for="refund-amount">本次退款金额</label><div class="refund-input-wrap"><span>¥</span><input id="refund-amount" class="refund-input" data-refund-input inputmode="decimal" type="number" min="0.01" max="${remaining}" step="0.01" value="${escapeAttribute(amount)}" aria-describedby="refund-error" /></div>${state.refundError ? `<div class="refund-error" id="refund-error">${state.refundError}</div>` : ''}<div class="sheet-actions"><button class="secondary-button" data-action="close-sheet">取消</button><button class="primary-button" data-action="confirm-refund">确认退款</button></div>`;
}

function homeView() {
  const entries = demoEntries.filter(entry => !state.deletedEntryIds[entry.id]);
  const dates = [...new Set(entries.map(entry => entry.date))].sort().reverse();
  const groups = dates.map(date => {
    const rows = entries.filter(entry => entry.date === date);
    const total = type => rows.filter(entry => entry.type === type).reduce((sum, entry) => sum + activeEntryAmount(entry), 0);
    return `<div class="date-group"><div class="date-heading"><div class="date-heading-copy"><b>${date}</b></div><div class="date-flow"><span class="date-income">收 ¥${money(total('income'))}</span><span class="date-expense">支 ¥${money(total('expense'))}</span></div></div>${rows.sort((a,b) => b.time.localeCompare(a.time)).map(transactionItem).join('')}</div>`;
  }).join('');
  const content = `
    <header class="page-top home-page-top"><div><div class="brand-lockup home-brand-icon"><img src="../哈记账.png" alt="" /></div><p class="page-subtitle">记录每一笔，让生活更清晰</p></div></header>
    <section class="summary-card" aria-label="本月收支概览">
      <div class="summary-kicker">2026 年 9 月 · 日均消费</div>
      <div class="summary-amount"><small>¥</small>86.40</div>
      <div class="summary-foot"><div>本月支出<strong>¥2,592.00</strong></div><div>本月收入<strong class="income-text">¥8,500.00</strong></div></div>
    </section>
    <div class="section-row"><h3 class="section-title">最近记账</h3></div>
    <div class="home-recent-list transaction-list">${groups || '<div class="list-empty">暂无记账记录</div>'}</div>`;
  return appScreen(content, 'home', '<button class="circle-add" data-action="new-entry" aria-label="新增记账">＋</button>');
}

function calendarView() {
  const year = state.calendarYear;
  const monthNumber = state.calendarMonth;
  const month = `${monthNumber}月`;
  const firstDay = new Date(year, monthNumber - 1, 1).getDay();
  const daysInMonth = new Date(year, monthNumber, 0).getDate();
  state.selectedDay = Math.min(state.selectedDay, daysInMonth);
  const previousMonthDays = new Date(year, monthNumber - 1, 0).getDate();
  const records = {};
  demoEntries.filter(entry => !state.deletedEntryIds[entry.id]).forEach(entry => {
    const parts = entry.date.match(/(\d+)年(\d+)月(\d+)日/);
    if (parts && Number(parts[1]) === year && Number(parts[2]) === monthNumber) (records[Number(parts[3])] ||= []).push(entry);
  });
  const types = entryTypes;
  const calendarTypeOrder = ['expense', 'income', 'transfer', 'repayment'];
  const entries = records[state.selectedDay] || [];
  const income = entries.filter(entry => entry.type === 'income').reduce((sum, entry) => sum + activeEntryAmount(entry), 0);
  const expense = entries.filter(entry => entry.type === 'expense').reduce((sum, entry) => sum + activeEntryAmount(entry), 0);
  const money = value => value.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
  const rows = entries.map(transactionItem).join('');
  const cells = Array.from({ length: Math.ceil((firstDay + daysInMonth) / 7) * 7 }, (_, i) => {
    const dayIndex = i - firstDay + 1;
    const muted = dayIndex < 1 || dayIndex > daysInMonth;
    const day = muted ? (dayIndex < 1 ? previousMonthDays + dayIndex : dayIndex - daysInMonth) : dayIndex;
    const dayText = String(day);
    const selected = !muted && dayText === String(state.selectedDay);
    const now = new Date();
    const today = year === now.getFullYear() && monthNumber === now.getMonth() + 1 && day === now.getDate() && !muted;
    const dayTypes = muted ? [] : calendarTypeOrder.filter(type => (records[day] || []).some(entry => entry.type === type));
    return `<button class="calendar-cell ${muted ? 'muted' : ''} ${selected ? 'selected' : ''} ${today ? 'today' : ''}" data-day="${muted ? '' : dayText}" ${muted ? 'disabled' : `aria-pressed="${selected}" aria-label="${month}${day}日${today ? '，今天' : ''}，${dayTypes.length ? dayTypes.map(type => types[type].label).join('、') : '暂无记账'}"`}><span class="day-num">${dayText}</span><span class="dots" aria-hidden="true">${dayTypes.map(type => `<i class="calendar-dot ${type}-dot" data-entry-type="${type}"></i>`).join('')}</span></button>`;
  }).join('');
  const content = `
    <header class="page-top home-page-top"><div><div class="brand-lockup home-brand-icon"><img src="../哈记账.png" alt="" /></div><p class="page-subtitle">按日期回看每一笔生活</p></div></header>
    <section class="calendar-card" aria-label="记账日历"><div class="calendar-toolbar"><button class="calendar-month" data-action="show-month-picker" aria-label="选择月份">${year} 年 ${monthNumber} 月</button><div class="calendar-controls"><button data-action="prev-month" aria-label="上个月" ${monthNumber === 8 ? 'disabled' : ''}>‹</button><button class="calendar-today" data-action="next-month">今天</button></div></div>
      <div class="week-row"><span>日</span><span>一</span><span>二</span><span>三</span><span>四</span><span>五</span><span>六</span></div><div class="calendar-grid">${cells}</div>
      <div class="calendar-legend" aria-label="记账类型"><span><i class="expense-dot"></i>支出</span><span><i class="income-dot"></i>收入</span><span><i class="transfer-dot"></i>转账</span><span><i class="repayment-dot"></i>还款</span></div>
      <div class="day-summary" aria-label="所选日期收支"><div><span>当日支出</span><strong class="green">¥${money(expense)}</strong></div><div><span>当日收入</span><strong class="red">¥${money(income)}</strong></div><div><span>当日结余</span><strong>¥${money(income - expense)}</strong></div></div>
    </section>
    <div class="date-heading"><div class="date-heading-copy"><b>${month}${state.selectedDay}日</b><span>${new Date(year, monthNumber - 1, state.selectedDay).toLocaleDateString('zh-CN', { weekday: 'short' })}</span></div><span class="section-meta">${entries.length} 笔</span></div>
    <div class="transaction-list">${rows || '<div class="list-empty">这一天还没有记账记录</div>'}</div>`;
  return appScreen(content, 'calendar').replace('class="app-screen"', 'class="app-screen calendar-screen"');
}

const fundAccounts = [
  { id: 'bank', name: '银行卡', balance: 8600, included: true },
  { id: 'wechat', name: '微信', balance: 4280, included: true },
  { id: 'cash', name: '现金', balance: 2320, included: true }
];

const creditAccounts = [
  { id: 'huabei', name: '花呗', limit: 10000, debt: 2520, included: true, repayments: [] }
];

function selectedCredit() {
  return creditAccounts.find(account => account.id === state.selectedCreditId && !account.deleted);
}

function creditDetailView() {
  const account = selectedCredit();
  if (!account) return assetsView();
  const records = demoEntries.filter(entry => !state.deletedEntryIds[entry.id] && (entry.account === account.name || entry.from === account.name || entry.to === account.name));
  const filtered = records.filter(entry => !state.creditFilter || entry.type === state.creditFilter);
  const dates = [...new Set(filtered.map(entry => entry.date))].sort().reverse();
  const groups = dates.map(date => `<div class="date-group"><div class="date-heading"><b>${date.replace('2026年', '')}</b><span class="section-meta">${filtered.filter(entry => entry.date === date).length} 笔</span></div><div class="transaction-list">${filtered.filter(entry => entry.date === date).sort((a, b) => b.time.localeCompare(a.time)).map(transactionItem).join('')}</div></div>`).join('');
  return `<section class="detail-screen"><div class="detail-scroll"><header class="screen-nav detail-nav"><div class="nav-side"><button class="back-button" data-action="fund-back" aria-label="返回资产页">‹</button></div><h2>账户详情</h2><div class="nav-side"></div></header>
    <section class="detail-hero fund-hero credit-hero"><div class="fund-heading"><div><h3>${escapeAttribute(account.name)}</h3><span>信贷账户</span></div><span class="fund-included">${account.included ? '计入净资产' : '不计入净资产'}</span></div><div class="fund-balance-label">当前欠款</div><div class="detail-amount"><small>¥</small>${money(account.debt)}</div><div class="credit-metrics"><div><span>总额度</span><strong>¥${money(account.limit)}</strong></div><div><span>可用额度</span><strong>¥${money(account.limit - account.debt)}</strong></div></div></section>
    <section class="detail-section"><div class="detail-section-heading"><h3>账户流水</h3><span class="section-meta">${filtered.length} 笔记录</span></div><div class="fund-filters" aria-label="流水类型">${[['', '全部'], ['expense', '支出'], ['income', '收入'], ['transfer', '转账'], ['repayment', '还款']].map(([value, label]) => `<button data-action="filter-credit" data-filter="${value}" class="${(state.creditFilter || '') === value ? 'active' : ''}" aria-pressed="${(state.creditFilter || '') === value}">${label}</button>`).join('')}</div>${groups || '<div class="fund-empty"><span>◷</span><b>暂无相关记录</b><p>这个账户的收支、转账和还款会在这里呈现</p></div>'}</section></div>
    <div class="detail-actions" aria-label="账户操作"><button class="detail-action edit" data-action="edit-credit">编辑账户</button><button class="detail-action refund ${account.debt === 0 ? 'disabled' : ''}" data-action="repay-credit" ${account.debt === 0 ? 'disabled' : ''}>${account.debt === 0 ? '已还清' : '还款'}</button><button class="detail-action delete" data-action="delete-credit">删除账户</button></div></section>`;
}

function repaymentEditView() {
  const entry = getEntry();
  const fund = fundAccounts.find(item => item.id === state.repayFundId);
  return `<section class="detail-screen"><div class="detail-scroll"><header class="screen-nav"><button class="back-button" data-action="cancel-repayment-edit">‹</button><h2>编辑还款</h2></header><section class="detail-section"><div class="settings-list"><button class="field-row" data-action="choose-repay-fund"><span class="field-label">还款账户</span><span class="field-value">${escapeAttribute(fund?.name || '')}</span><span class="arrow">›</span></button><div class="field-row"><span class="field-label">信贷账户</span><span class="field-value">${escapeAttribute(entry.to)}</span></div><label class="field-row"><span class="field-label">还款金额 · ¥</span><input id="repay-amount" class="fund-input" type="number" min="0.01" step="0.01" value="${escapeAttribute(state.repayAmountDraft)}" /></label></div></section></div><div class="detail-actions two-actions"><button class="detail-action" data-action="cancel-repayment-edit">取消</button><button class="detail-action edit" data-action="save-repayment-edit">保存修改</button></div></section>`;
}

// 修改和删除先撤回原还款，再应用新金额；全部使用分计算。
function updateRepayment(entry, amount, fundId, deleting = false) {
  if (!entry || state.deletedEntryIds[entry.id]) return false;
  const credit = creditAccounts.find(item => item.id === entry.creditId);
  const oldFund = fundAccounts.find(item => item.id === entry.fundId);
  const fund = fundAccounts.find(item => item.id === fundId);
  if (!credit || !oldFund || (!deleting && (!fund || fund.deleted || credit.deleted))) { showToast('相关账户不可用'); return false; }
  const cents = Math.round(amount * 100), old = Math.round(entry.amount * 100);
  const debt = Math.round(credit.debt * 100) + old;
  if (!deleting && (!Number.isFinite(amount) || cents <= 0 || Math.abs(amount * 100 - cents) > 0.00001 || cents > debt)) { showToast('请输入有效金额，不能超过可还欠款'); return false; }
  const available = fund ? Math.round(fund.balance * 100) + (fund === oldFund ? old : 0) : 0;
  if (!deleting && cents > available) { showToast('还款账户余额不足'); return false; }
  oldFund.balance = (Math.round(oldFund.balance * 100) + old) / 100;
  credit.debt = (debt - (deleting ? 0 : cents)) / 100;
  if (deleting) state.deletedEntryIds[entry.id] = true;
  else { fund.balance = (Math.round(fund.balance * 100) - cents) / 100; Object.assign(entry, { amount, fundId, from: fund.name }); }
  return true;
}

function repaymentSheet() {
  const account = selectedCredit();
  if (!account) return;
  const fund = fundAccounts.find(item => item.id === state.repayFundId && !item.deleted);
  openSheet(`<h3 class="sheet-title">还款 · ${escapeAttribute(account.name)}</h3><p class="confirm-copy">当前欠款 ¥${money(account.debt)}，还款后恢复相应可用额度。</p><div class="settings-list"><button class="field-row repay-account-trigger" data-action="choose-repay-fund"><span class="account-icon"><img src="assets/funds-account.png" alt="" /></span><span class="repay-account-copy"><small>还款账户</small><strong>${fund ? escapeAttribute(fund.name) : '请选择账户'}</strong></span><span class="repay-account-balance">${fund ? `余额 ¥${money(fund.balance)}` : ''}</span><span class="arrow">›</span></button><label class="field-row"><span class="field-label">还款金额 · ¥</span><input class="fund-input" id="repay-amount" type="number" min="0.01" max="${account.debt}" step="0.01" value="${escapeAttribute(state.repayAmountDraft)}" /></label></div><div class="sheet-actions"><button class="secondary-button" data-action="close-sheet">取消</button><button class="primary-button" data-action="confirm-repay-credit">确认还款</button></div>`);
}

function creditForm(edit = false) {
  const account = edit ? selectedCredit() : { name: '', limit: '', debt: 0, included: true };
  if (!account) return;
  state.creditFormEdit = edit;
  openSheet(`<h3 class="sheet-title">${edit ? '编辑信贷账户' : '新增信贷账户'}</h3><div class="settings-list"><label class="field-row"><span class="field-label">账户名称</span><input class="fund-input" id="credit-name" maxlength="20" placeholder="例如：花呗" value="${escapeAttribute(account.name)}" /></label><label class="field-row"><span class="field-label">总额度 · ¥</span><input class="fund-input" id="credit-limit" type="number" min="0" max="999999999.99" step="0.01" value="${account.limit}" /></label><label class="field-row"><span class="field-label">当前欠款 · ¥</span><input class="fund-input" id="credit-debt" type="number" min="0" max="999999999.99" step="0.01" value="${account.debt}" /></label><label class="field-row"><span class="field-label">计入净资产</span><input id="credit-included" type="checkbox" ${account.included ? 'checked' : ''} /></label></div><div class="sheet-actions"><button class="secondary-button" data-action="close-sheet">取消</button><button class="primary-button" data-action="save-credit">保存账户</button></div>`);
}

function selectedFund() {
  return fundAccounts.find(account => account.id === state.selectedFundId);
}

function saveFundEdit(addRecord = false) {
  const draft = pendingFundEdit;
  if (!draft) return;
  const account = fundAccounts.find(item => item.id === draft.id && !item.deleted);
  if (!account) { pendingFundEdit = null; return; }
  const difference = Math.round(draft.balance * 100) - Math.round(account.balance * 100);
  demoEntries.forEach(entry => ['account', 'from', 'to'].forEach(key => { if (entry[key] === account.name) entry[key] = draft.name; }));
  ['account', 'lastAccount', 'transferFrom', 'transferTo'].forEach(key => { if (state[key] === account.name) state[key] = draft.name; });
  if (addRecord && difference !== 0) {
    const [date, time] = getCurrentDateTime().split(' ');
    demoEntries.push({ id: `balance-${Date.now()}-${demoEntries.length}`, type: difference > 0 ? 'income' : 'expense', amount: Math.abs(difference) / 100, date, time, account: draft.name, category: '账户余额补齐', balanceAdjustment: true, note: `账户余额补齐：¥${money(account.balance)} → ¥${money(draft.balance)}` });
    state.fundFilter = '';
  }
  Object.assign(account, { name: draft.name, balance: draft.balance, included: draft.included });
  pendingFundEdit = null;
  closeSheet(); render(); showToast(addRecord && difference !== 0 ? '账户已保存，补齐记录已加入流水' : '账户已保存');
}

function fundDetailView() {
  const account = selectedFund();
  if (!account || account.deleted) return assetsView();
  const records = demoEntries.filter(entry => !state.deletedEntryIds[entry.id] && (entry.account === account.name || entry.from === account.name || entry.to === account.name));
  const filtered = records.filter(entry => !state.fundFilter || entry.type === state.fundFilter);
  const dates = [...new Set(filtered.map(entry => entry.date))].sort().reverse();
  const groups = dates.map(date => `<div class="date-group"><div class="date-heading"><b>${date.replace('2026年', '')}</b><span class="section-meta">${filtered.filter(entry => entry.date === date).length} 笔</span></div><div class="transaction-list">${filtered.filter(entry => entry.date === date).sort((a, b) => b.time.localeCompare(a.time)).map(transactionItem).join('')}</div></div>`).join('');
  return `<section class="detail-screen fund-detail-screen"><div class="detail-scroll"><header class="screen-nav detail-nav"><div class="nav-side"><button class="back-button" data-action="fund-back" aria-label="返回资产页">‹</button></div><h2>账户详情</h2><div class="nav-side"></div></header>
    <section class="detail-hero fund-hero"><div class="fund-heading"><div><h3>${escapeAttribute(account.name)}</h3><span>资金账户</span></div><span class="fund-included">${account.included ? '计入净资产' : '不计入净资产'}</span></div><div class="fund-balance-label">当前余额</div><div class="detail-amount"><small>¥</small>${money(account.balance)}</div><div class="detail-orbit orbit-one"></div></section>
    <section class="detail-section"><div class="detail-section-heading"><h3>账户流水</h3><span class="section-meta">${filtered.length} 笔记录</span></div><div class="fund-filters" aria-label="流水类型">${[['', '全部'], ['expense', '支出'], ['income', '收入'], ['transfer', '转账'], ['repayment', '还款']].map(([value, label]) => `<button data-action="filter-fund" data-filter="${value}" class="${(state.fundFilter || '') === value ? 'active' : ''}" aria-pressed="${(state.fundFilter || '') === value}">${label}</button>`).join('')}</div>${groups || '<div class="fund-empty"><span>◷</span><b>暂无相关记录</b><p>这个账户的收支会在这里呈现</p></div>'}</section>
    </div><div class="detail-actions fund-actions" aria-label="账户操作"><button class="detail-action edit" data-action="edit-fund">编辑账户</button><button class="detail-action delete" data-action="delete-fund">删除账户</button></div></section>`;
}

function assetsView() {
  const accountSection = (type, title, items) => {
    const expanded = state.accountSections[type];
    return `<section class="account-section ${expanded ? 'expanded' : 'collapsed'}">
      <button class="account-heading" data-action="toggle-account-section" data-account-section="${type}" aria-expanded="${expanded}" aria-controls="${type}-accounts">
        <h3>${title}</h3><span class="account-toggle"><span>${expanded ? '收起' : '展开'}</span><i aria-hidden="true"></i></span>
      </button>
      <div class="account-list" id="${type}-accounts" ${expanded ? '' : 'hidden'}>${items}</div>
    </section>`;
  };
  const fundsIcon = '<span class="account-icon"><img src="assets/funds-account.png" alt="" /></span>';
  const creditIcon = '<span class="account-icon credit"><img src="assets/credit-account.png" alt="" /></span>';
  const funds = fundAccounts.filter(account => !account.deleted).map(account => `<button class="account-item" data-action="view-fund" data-fund-id="${account.id}">${fundsIcon}<span class="account-main"><span class="account-name">${escapeAttribute(account.name)}</span></span><span class="account-balance">¥${money(account.balance)}</span><span class="arrow">›</span></button>`).join('') || '<div class="list-empty">暂无资金账户</div>';
  const assetTotal = fundAccounts.filter(account => !account.deleted && account.included).reduce((sum, account) => sum + account.balance, 0);
  const debtTotal = creditAccounts.filter(account => !account.deleted && account.included).reduce((sum, account) => sum + account.debt, 0);
  const credit = creditAccounts.filter(account => !account.deleted).map(account => `<button class="account-item" data-action="view-credit" data-credit-id="${account.id}">${creditIcon}<span class="account-main"><span class="account-name">${escapeAttribute(account.name)}</span><span class="credit-available">可用 ¥${money(account.limit - account.debt)}</span></span><span class="account-balance credit"><small>欠款</small>¥${money(account.debt)}</span><span class="arrow">›</span></button>`).join('') || '<div class="list-empty">暂无信贷账户</div>';
  const content = `
    <header class="page-top home-page-top"><div><div class="brand-lockup home-brand-icon"><img src="../哈记账.png" alt="" /></div><p class="page-subtitle">记录每一笔，让生活更清晰</p></div></header>
    <section class="asset-hero"><div class="asset-label">净资产</div><div class="asset-net"><small>¥</small>${money(assetTotal - debtTotal)}</div><div class="asset-breakdown"><div><span>总资产</span><b>¥${money(assetTotal)}</b></div><div><span>总负债</span><b class="liability">¥${money(debtTotal)}</b></div></div></section>
    ${accountSection('credit', '信贷账户', credit)}
    ${accountSection('asset', '资金账户', funds)}`;
  return appScreen(content, 'assets', '<button class="circle-add" data-action="new-account" aria-label="新增资产账户">＋</button>').replace('class="app-screen"', 'class="app-screen assets-screen"');
}

function mineView() {
  const content = `
    <header class="page-top home-page-top"><div><div class="brand-lockup home-brand-icon"><img src="../哈记账.png" alt="" /></div><p class="page-subtitle">记录每一笔，让生活更清晰</p></div></header>
    <section class="profile-card"><div class="avatar ${state.avatarAuthState === 'success' ? 'authorized' : ''}" aria-label="${state.avatarAuthState === 'success' ? '已授权头像' : '默认头像'}"><img src="../哈记账.png" alt="" /></div><div class="profile-copy"><div class="profile-name">账本主人</div><div class="profile-status">${state.avatarAuthState === 'success' ? '头像已授权 · 数据随时可用' : state.avatarAuthState === 'loading' ? '正在获取微信头像…' : '头像未授权 · 可继续使用'}</div></div><button class="text-button" data-action="avatar-auth" ${state.avatarAuthState === 'loading' ? 'disabled' : ''}>${state.avatarAuthState === 'success' ? '更换头像' : state.avatarAuthState === 'loading' ? '授权中' : '去授权'}</button></section>
    <div class="days-card"><span>坚持记录，正在变成习惯</span><strong>累计记账 28 天</strong></div>
    <section class="settings-group"><p class="settings-label">更多</p><div class="settings-list"><button class="setting-item" data-action="open-about">${settingIcons.help}<span>关于与帮助</span><span class="setting-arrow">›</span></button><button class="setting-item logout-item" data-action="logout"><span class="setting-icon logout-icon" aria-hidden="true">↪</span><span>退出登录</span><span class="setting-arrow">›</span></button></div></section>`;
  return appScreen(content, 'mine');
}

function aboutView() {
  const faqs = [
    ['quick-start', '如何开始记一笔？', '点击首页右下角的“＋”，选择支出、收入或转账，输入金额后选择账户并确认日期时间，最后点击保存即可。常用账户会优先为你保留。'],
    ['transfer', '转账为什么不算收支？', '转账只是资金在不同账户之间移动，不会改变你的总资产，也不会进入本月收入或支出统计。'],
    ['balance', '账户余额是怎么计算的？', '余额会根据期初余额和每一笔已保存的流水自动计算。需要调整余额时，请通过记账或转账完成，避免流水对不上。'],
    ['account', '记账账户会自动选择吗？', '会。新增记账默认使用上一次保存记账时使用的账户，也可以在当前页面手动切换。']
  ];
  const faqItems = faqs.map(([id, title, answer]) => {
    const opened = state.openFaq === id;
    return `<div class="faq-item ${opened ? 'open' : ''}"><button class="faq-question" data-faq="${id}" aria-expanded="${opened}"><span>${title}</span><i aria-hidden="true"></i></button><div class="faq-answer" ${opened ? '' : 'hidden'}>${answer}</div></div>`;
  }).join('');
  return `<section class="about-screen">
    <div class="about-scroll">
      <header class="screen-nav about-nav"><div class="nav-side"><button class="back-button" data-action="back-to-mine" aria-label="返回我的">‹</button></div><h2>关于与帮助</h2><div class="nav-side"></div></header>
      <section class="about-hero"><div class="about-brand"><img src="../哈记账.png" alt="哈记账" /><span>哈记账</span></div><span class="about-eyebrow">A LITTLE BOOKKEEPER</span><h1>记账，从简单开始</h1><p>记录每一笔，让生活更清晰。<br>这里整理了常用功能和使用说明。</p><div class="about-hero-orbit orbit-one"></div><div class="about-hero-orbit orbit-two"></div></section>
      <section class="about-section"><div class="about-section-heading"><div><span class="section-kicker">QUICK START</span><h3>三步，开始看懂生活</h3></div><span class="about-step-count">01 / 03</span></div><div class="help-cards"><div class="help-card"><span class="help-card-icon mint">＋</span><b>记下一笔</b><small>3 秒完成记录</small></div><div class="help-card"><span class="help-card-icon peach">◷</span><b>看懂变化</b><small>日历查看趋势</small></div><div class="help-card"><span class="help-card-icon lavender">▣</span><b>管理账户</b><small>资产清晰可见</small></div></div></section>
      <section class="about-section faq-section"><div class="about-section-heading"><div><span class="section-kicker">FAQ</span><h3>常见问题</h3></div><span class="section-meta">点击查看</span></div><div class="faq-list">${faqItems}</div></section>
      <section class="about-section"><div class="about-section-heading"><div><span class="section-kicker">YOUR DATA</span><h3>数据与隐私</h3></div></div><div class="about-settings"><button class="about-setting" data-action="show-privacy"><span class="about-setting-icon">♡</span><span class="about-setting-copy"><b>隐私说明</b><small>只为记账服务，数据去向清晰可见</small></span><span class="setting-arrow">›</span></button><div class="about-setting"><span class="about-setting-icon">↗</span><span class="about-setting-copy"><b>数据导出 / 删除</b><small>首版暂未开放，不会生成不可用操作</small></span><span class="about-status">暂未开放</span></div></div></section>
      <section class="about-note"><span class="about-note-icon">✦</span><div><b>需要反馈？</b><p>告诉我们哪里还可以更好，帮助哈记账变得更顺手。</p></div><button class="about-feedback" data-action="show-feedback">反馈建议</button></section>
      <footer class="about-footer"><img src="../哈记账.png" alt="" /><span>哈记账 · 原型版本 v1.0</span><span>简单记账，安心生活</span></footer>
    </div>
  </section>`;
}

function detailView() {
  const entry = getEntry();
  if (!entry) { state.view = state.detailReturnView || 'home'; return ''; }
  const type = entryTypes[entry.type];
  const refundRecords = getRefundRecords(entry.id);
  const refundTotal = getRefundTotal(entry.id);
  const refunded = refundRecords.length > 0;
  const isTransfer = ['transfer', 'repayment'].includes(entry.type);
  const amountPrefix = entry.type === 'expense' ? '−' : entry.type === 'income' ? '＋' : '';
  const detailAmount = entry.amount;
  const fullyRefunded = refundTotal >= entry.amount;
  const statusLabel = refunded ? `${fullyRefunded ? '已退款' : '部分退款'} · ¥${money(refundTotal)}` : '已完成';
  const refundAction = isTransfer ? '' : `<button class="detail-action refund ${fullyRefunded ? 'disabled' : ''}" data-action="refund-entry" ${fullyRefunded ? 'disabled' : ''}>${fullyRefunded ? '已退款' : '退款'}</button>`;
  const refundRecordRows = refundRecords.map((record, index) => `<div class="refund-record-item"><span class="refund-record-icon">↩</span><div class="refund-record-copy"><b>第 ${index + 1} 笔退款</b><small>${record.time}</small></div><strong>¥${money(record.amount)}</strong><button class="refund-record-delete" data-action="delete-refund-entry" data-refund-id="${record.id}" aria-label="删除第 ${index + 1} 笔退款记录">删除</button></div>`).join('');
  const refundRecord = refunded ? `<section class="detail-section refund-record-section"><div class="detail-section-heading"><div><span class="section-kicker">REFUND RECORD</span><h3>退款记录</h3></div><span class="refund-record-count">${refundRecords.length} 笔</span></div><div class="refund-record-card"><div class="refund-record-total"><span>累计退款</span><strong>¥${money(refundTotal)}</strong></div>${refundRecordRows}</div></section>` : '';
  const accountRows = isTransfer
    ? `<div class="detail-info-row"><span class="detail-info-icon">↗</span><span class="detail-info-label">${entry.type === 'repayment' ? '还款账户' : '转出账户'}</span><strong>${escapeAttribute(entry.from)}</strong></div><div class="detail-info-row"><span class="detail-info-icon">↘</span><span class="detail-info-label">${entry.type === 'repayment' ? '信贷账户' : '转入账户'}</span><strong>${escapeAttribute(entry.to)}</strong></div>`
    : `<div class="detail-info-row"><span class="detail-info-icon">◉</span><span class="detail-info-label">资金账户</span><strong>${escapeAttribute(entry.account)}</strong></div>`;
  return `<section class="detail-screen"><div class="detail-scroll"><header class="screen-nav detail-nav"><div class="nav-side"><button class="back-button" data-action="back" aria-label="返回">‹</button></div><h2>账单详情</h2><div class="nav-side"></div></header>
    <section class="detail-hero ${entry.type}"><div class="detail-hero-heading"><span class="category-icon type-icon ${entry.type}" aria-hidden="true"><img src="assets/${type.image}" alt="" /></span><span class="detail-type-badge ${entry.type}">${type.label}</span></div><div class="detail-amount"><small>${amountPrefix}${amountPrefix ? ' ' : ''}¥</small>${money(detailAmount)}</div><div class="detail-status ${refunded ? 'refunded' : ''}"><i></i>${statusLabel}</div><div class="detail-orbit orbit-one"></div><div class="detail-orbit orbit-two"></div></section>
    <section class="detail-section"><div class="detail-section-heading"><span class="section-kicker">TRANSACTION INFO</span><h3>账单信息</h3></div><div class="detail-info-card">${accountRows}<div class="detail-info-row"><span class="detail-info-icon">◷</span><span class="detail-info-label">记账时间</span><strong>${entry.date} ${entry.time}</strong></div><div class="detail-info-row"><span class="detail-info-icon">⌁</span><span class="detail-info-label">备注</span><strong>${entry.note}</strong></div></div></section>
    <section class="detail-section detail-meta-section"><div class="detail-section-heading"><span class="section-kicker">RECORD</span><h3>记录来源</h3></div><div class="detail-meta-card"><div><span>账单类型</span><b>${entry.category}</b></div><div><span>记录编号</span><b>${entry.id.toUpperCase()}</b></div></div></section>${refundRecord}
    </div><div class="detail-actions ${isTransfer ? 'two-actions' : ''}" aria-label="账单操作"><button class="detail-action edit" data-action="edit-entry">编辑</button>${refundAction}<button class="detail-action delete" data-action="delete-entry">删除</button></div></section>`;
}

function entryView() {
  const isIncome = state.entryType === 'income';
  const isTransfer = state.entryType === 'transfer';
  const title = state.editMode ? '编辑记账' : '新增记账';
  const accountRows = isTransfer ? `<button class="field-row" data-action="show-account-picker" data-account-role="from"><span class="field-icon">↗</span><span class="field-label">转出账户</span><span class="field-value">${state.transferFrom}</span><span class="arrow">›</span></button><button class="field-row" data-action="show-account-picker" data-account-role="to"><span class="field-icon">↘</span><span class="field-label">转入账户</span><span class="field-value">${state.transferTo}</span><span class="arrow">›</span></button>` : `<button class="field-row" data-action="show-account-picker" data-account-role="account"><span class="field-icon">◉</span><span class="field-label">资金账户</span><span class="field-value">${state.account || state.lastAccount || '请选择'}</span><span class="arrow">›</span></button>`;
  const [entryDate, entryTime] = (state.entryDateTime || getCurrentDateTime()).split(' ');
  const hasAmount = Boolean(state.amount);
  const keypadConfirmAction = hasAmount ? 'save-entry' : 'back';
  const keypadConfirmLabel = hasAmount ? '确定' : '取消';
  return `<section class="entry-screen"><div class="entry-content"><header class="screen-nav"><div class="nav-side"><button class="back-button" data-action="back" aria-label="返回">‹</button></div><h2>${title}</h2><div class="nav-side"></div></header>
    <div class="entry-type"><button class="${state.entryType === 'expense' ? 'active' : ''}" data-type="expense">支出</button><button class="${isIncome ? 'active income-active' : ''}" data-type="income">收入</button><button class="${isTransfer ? 'active' : ''}" data-type="transfer">转账</button></div>
    <section class="amount-panel"><div class="amount-label">${isTransfer ? '转账金额' : '记账金额'}</div><div class="amount-display"><small>¥</small><strong class="${state.amount ? '' : 'amount-placeholder'}">${state.amount || '输入金额'}</strong></div><div class="calculation-line">${state.expression || '支持 +  −  ×  ÷  连续计算'}</div>${state.formError ? `<div class="inline-error">${state.formError}</div>` : ''}</section>
    <div class="fields-card">${accountRows}<button class="field-row" data-action="show-date-picker"><span class="field-icon">◷</span><span class="field-label">日期与时间</span><span class="field-value">${entryDate} ${entryTime}</span><span class="arrow">›</span></button><button class="field-row" data-action="show-note"><span class="field-icon">⌁</span><span class="field-label">备注</span><span class="field-value ${state.note ? '' : 'placeholder'}">${state.note || '写点说明...'}</span></button></div>
    </div>
    <div class="keypad" aria-label="金额键盘"><div class="keypad-cell"><button class="key" data-key="1">1</button></div><div class="keypad-cell"><button class="key" data-key="2">2</button></div><div class="keypad-cell"><button class="key" data-key="3">3</button></div><div class="keypad-cell"><button class="key key-backspace" data-key="⌫" aria-label="退格">⌫</button></div><div class="keypad-cell"><button class="key" data-key="4">4</button></div><div class="keypad-cell"><button class="key" data-key="5">5</button></div><div class="keypad-cell"><button class="key" data-key="6">6</button></div><div class="keypad-cell keypad-pair"><button class="key" data-key="+" aria-label="加">＋</button><button class="key" data-key="−" aria-label="减">−</button></div><div class="keypad-cell"><button class="key" data-key="7">7</button></div><div class="keypad-cell"><button class="key" data-key="8">8</button></div><div class="keypad-cell"><button class="key" data-key="9">9</button></div><div class="keypad-cell keypad-pair"><button class="key" data-key="×" aria-label="乘">×</button><button class="key" data-key="÷" aria-label="除">÷</button></div><div class="keypad-cell"><button class="key key-text" data-key="C">再记</button></div><div class="keypad-cell"><button class="key" data-key="0">0</button></div><div class="keypad-cell"><button class="key" data-key=".">.</button></div><div class="keypad-cell"><button class="key key-cancel ${hasAmount ? 'key-confirm' : ''}" data-action="${keypadConfirmAction}">${keypadConfirmLabel}</button></div></div>
  </section>`;
}

function firstUseView() {
  return `<section class="first-use"><div class="welcome-brand"><img src="../哈记账.png" alt="哈记账" /><span>哈记账</span></div><span class="eyebrow">A LITTLE BOOKKEEPER</span><div class="first-illustration"><span class="leaf"></span></div><h2>记录每一笔，<br><span style="color:var(--primary-dark)">让生活更清晰</span></h2><p>用 3 秒记下一笔，<br>用 10 秒看懂这个月。</p><button class="primary-button" data-action="start-use">开始记账</button><button class="skip-link" data-action="skip-use">稍后设置账户</button></section>`;
}

function resetAuthForm() {
  state.authForm = { account: '', password: '', code: '' };
  state.authMode = 'login';
  state.authSubmitting = false;
  state.authStatus = 'idle';
  state.captchaCode = createCaptchaCode();
  state.captchaMessage = '';
  state.authError = '';
}

function createCaptchaCode() {
  const alphabet = 'ABCDEFGHJKLMNPQRSTUVWXYZ23456789';
  return Array.from({ length: 4 }, () => alphabet[Math.floor(Math.random() * alphabet.length)]).join('');
}

function authView(mode = state.authMode) {
  const isLogin = mode === 'login';
  const account = escapeAttribute(state.authForm.account);
  const password = escapeAttribute(state.authForm.password);
  const code = escapeAttribute(state.authForm.code);
  const codeStatus = state.captchaMessage || '请输入右侧图形验证码';
  const canSubmit = Boolean(state.authForm.account.trim() && state.authForm.password && state.authForm.code.trim()) && !state.authSubmitting;
  return `<section class="auth-screen">
    <div class="auth-scroll">
      <header class="auth-header">
        <div class="auth-brand"><img src="../哈记账.png" alt="哈记账" /><span>哈记账</span></div>
        <span class="eyebrow">${isLogin ? 'WELCOME BACK' : 'A LITTLE BOOKKEEPER'}</span>
        <div class="auth-context">H5 · 账号${isLogin ? '登录' : '注册'}</div>
        <h2>${isLogin ? '欢迎回来' : '创建你的账号'}</h2>
        <p>${isLogin ? '登录后继续记录每一笔生活' : '用一个轻量账号，开启你的记账空间'}</p>
      </header>
      <form class="auth-card" data-auth-form novalidate>
        <label class="auth-field ${state.authForm.account ? 'has-value' : ''}">
          <span class="auth-field-icon" aria-hidden="true">◎</span>
          <span class="auth-field-copy"><small>账号</small><input data-auth-field="account" autocomplete="username" value="${account}" placeholder="输入账号" required /></span>
        </label>
        <label class="auth-field ${state.authForm.password ? 'has-value' : ''}">
          <span class="auth-field-icon" aria-hidden="true">⌁</span>
          <span class="auth-field-copy"><small>密码</small><input data-auth-field="password" type="password" autocomplete="${isLogin ? 'current-password' : 'new-password'}" value="${password}" placeholder="请输入密码" minlength="6" required /></span>
        </label>
        <div class="auth-code-row">
          <label class="auth-field auth-code-field ${state.authForm.code ? 'has-value' : ''}">
            <span class="auth-field-icon" aria-hidden="true">◇</span>
            <span class="auth-field-copy"><small>图形验证码</small><input data-auth-field="code" autocapitalize="characters" maxlength="4" value="${code}" placeholder="输入图形验证码" required /></span>
          </label>
          <button class="captcha-refresh" type="button" data-action="refresh-captcha" aria-label="刷新图形验证码"><span class="captcha-art" aria-hidden="true">${state.captchaCode}</span></button>
        </div>
        <div class="auth-code-status ${state.captchaMessage ? 'sent' : ''}"><span class="auth-status-dot"></span>${codeStatus}</div>
        ${state.authError ? `<div class="auth-error" role="alert">${state.authError}</div>` : ''}
        <button class="primary-button auth-submit ${state.authSubmitting ? 'loading' : ''}" type="submit" data-action="auth-submit" ${canSubmit ? '' : 'disabled'}>${state.authSubmitting ? (isLogin ? '登录中…' : '注册中…') : (isLogin ? '登录' : '注册')}</button>
        <button class="auth-switch" type="button" data-action="${isLogin ? 'goto-register' : 'goto-login'}">${isLogin ? '还没有账号？去注册' : '已有账号？返回登录'}</button>
      </form>
      <p class="auth-footnote">${isLogin ? '微信小程序会自动完成微信登录，无需账号密码。' : '注册仅需要账号、密码和验证码，不收集昵称或头像。'}</p>
    </div>
  </section>`;
}

function wechatAutoLoginView() {
  return `<section class="wechat-auto-screen"><div class="wechat-auto-card"><div class="auth-brand"><img src="../哈记账.png" alt="哈记账" /><span>哈记账</span></div><span class="eyebrow">WECHAT MINI PROGRAM</span><div class="wechat-spinner" aria-hidden="true"></div><h2>正在自动登录</h2><p>正在确认微信登录状态，马上进入首页</p><span class="wechat-auto-note">无需填写账号和密码</span></div></section>`;
}

function render() {
  if (state.wechatLoggingIn) { root.innerHTML = wechatAutoLoginView(); return; }
  if (!state.authenticated && state.platform === 'h5') { root.innerHTML = authView(); return; }
  if (state.firstUse) { root.innerHTML = firstUseView(); return; }
  if (state.view === 'repayment-edit') root.innerHTML = repaymentEditView();
  else if (state.view === 'entry') root.innerHTML = entryView();
  else if (state.view === 'detail') root.innerHTML = detailView();
  else if (state.view === 'credit-detail') root.innerHTML = creditDetailView();
  else if (state.view === 'fund-detail') root.innerHTML = fundDetailView();
  else if (state.view === 'calendar') root.innerHTML = calendarView();
  else if (state.view === 'assets') root.innerHTML = assetsView();
  else if (state.view === 'mine') root.innerHTML = mineView();
  else if (state.view === 'about') root.innerHTML = aboutView();
  else root.innerHTML = homeView();
}

function showToast(message) {
  toast.textContent = message; toast.classList.add('show');
  clearTimeout(showToast.timer); showToast.timer = setTimeout(() => toast.classList.remove('show'), 2200);
}

function positionSheet() {
  if (backdrop.hidden) return;
  // 居中由 .modal-backdrop 的 flex 布局负责，清理旧版本可能留下的坐标。
  sheet.style.left = '';
  sheet.style.top = '';
}

function openSheet(html, wide = false, centered = false) {
  sheet.className = `bottom-sheet${wide ? ' sheet-wide' : ''}${centered ? ' centered-sheet' : ''}`;
  sheet.innerHTML = `<div class="sheet-handle"></div>${html}`;
  if (html.includes('repay-account-trigger')) sheet.classList.add('repayment-sheet');
  if (html.includes('repay-account-options')) sheet.classList.add('repay-picker-sheet');
  backdrop.hidden = false;
  requestAnimationFrame(() => positionSheet());
}

function closeSheet() { backdrop.hidden = true; sheet.innerHTML = ''; datePickerDraft = null; pendingFundEdit = null; }

function accountPicker() {
  const role = state.pickerRole || 'account';
  const selected = role === 'from' ? state.transferFrom : role === 'to' ? state.transferTo : state.account;
  const title = role === 'from' ? '选择转出账户' : role === 'to' ? '选择转入账户' : '选择资金账户';
  const items = [['◉', '微信', '¥4,280.00'], ['◈', '银行卡', '¥8,600.00'], ['▱', '现金', '¥2,320.00']].map(([icon, name, balance]) => `<button class="field-row ${selected === name ? 'selected-row' : ''}" data-pick-account="${name}"><span class="field-icon">${icon}</span><span class="field-label">${name}</span><span class="field-value">${balance}</span><span class="arrow">${selected === name ? '✓' : '›'}</span></button>`).join('');
  openSheet(`<h3 class="sheet-title">${title}</h3><div class="settings-list">${items}</div><div class="sheet-actions"><button class="secondary-button" data-action="close-sheet">取消</button><button class="primary-button" data-action="close-sheet">完成</button></div>`, true);
}

function datePicker() {
  datePickerDraft = datePickerDraft || { year: state.calendarYear, month: state.calendarMonth };
  const years = [2024, 2025, 2026, 2027, 2028];
  const months = Array.from({ length: 12 }, (_, index) => index + 1);
  const yearOptions = years.map(year => `<button class="date-picker-option ${datePickerDraft.year === year ? 'selected' : ''}" data-calendar-year="${year}">${year}年</button>`).join('');
  const monthOptions = months.map(month => `<button class="date-picker-option ${datePickerDraft.month === month ? 'selected' : ''}" data-calendar-month="${month}">${month}月</button>`).join('');
  openSheet(`<h3 class="sheet-title date-picker-title">日期</h3><div class="date-picker-columns"><div class="date-picker-column"><div class="date-picker-label">年份</div><div class="date-picker-scroll" aria-label="选择年份">${yearOptions}</div></div><div class="date-picker-column"><div class="date-picker-label">月份</div><div class="date-picker-scroll" aria-label="选择月份">${monthOptions}</div></div></div><div class="sheet-actions"><button class="secondary-button" data-action="close-sheet">取消</button><button class="primary-button" data-action="confirm-date-picker">确定</button></div>`, false, true);
  requestAnimationFrame(() => {
    sheet.querySelectorAll('.date-picker-option.selected').forEach(option => option.scrollIntoView({ block: 'center' }));
  });
}

function accountForm(edit = false, accountType = state.accountFormType || 'asset') {
  state.accountFormType = accountType;
  state.accountFormEdit = edit;
  if (accountType === 'credit') { creditForm(edit); return; }
  const isCredit = accountType === 'credit';
  const typeLabel = isCredit ? '信贷账户' : '资金账户';
  const balanceLabel = isCredit ? '欠款' : '余额';
  const typeChoice = edit ? `<div class="field-row"><span class="field-label">账户归属</span><span class="field-value">${typeLabel}（不可直接切换）</span></div>` : `<div class="account-type-choice"><button class="${isCredit ? '' : 'active'}" data-action="toggle-account-type" data-account-type="asset">资金账户</button><button class="${isCredit ? 'active credit-choice' : ''}" data-action="toggle-account-type" data-account-type="credit">信贷账户</button></div>`;
  openSheet(`<h3 class="sheet-title">${edit ? '编辑账户' : '新增资产账户'}</h3><div class="settings-list">${typeChoice}<div class="field-row"><span class="field-label">账户名称</span><span class="field-value">${edit ? (isCredit ? '花呗' : '微信') : (isCredit ? '例如：花呗' : '例如：微信')}</span></div><div class="field-row"><span class="field-label">${balanceLabel}</span><span class="field-value">¥${edit ? (isCredit ? '2,520.00' : '4,280.00') : '0.00'}</span></div><div class="field-row"><span class="field-label">计入净资产</span><button class="asset-switch ${state.accountIncludeNetAsset ? 'active' : ''}" data-action="toggle-net-asset" role="switch" aria-checked="${state.accountIncludeNetAsset}" aria-label="计入净资产"></button></div></div><div class="sheet-actions"><button class="secondary-button" data-action="close-sheet">取消</button><button class="primary-button" data-action="save-account">保存账户</button></div>`, false, !edit);
}

function refreshCaptcha() {
  state.captchaCode = createCaptchaCode();
  state.authForm.code = '';
  state.captchaMessage = '图形验证码已更新，请重新输入';
  state.authError = '';
  render();
}

function authSubmit() {
  const { account, password, code } = state.authForm;
  if (state.authSubmitting) return;
  if (!account.trim() || !password || !code.trim()) {
    state.authError = '请填写完整的账号、密码和验证码';
    render();
    return;
  }
  if (password.length < 6) {
    state.authError = '密码至少需要 6 位';
    render();
    return;
  }
  if (code.trim().toUpperCase() !== state.captchaCode) {
    state.authError = '图形验证码不正确，请重新输入';
    render();
    return;
  }
  state.authError = '';
  state.authSubmitting = true;
  render();
  setTimeout(() => {
    if (state.authMode === 'login' && account.trim().toLowerCase() === 'fail') {
      state.authSubmitting = false;
      state.authStatus = 'error';
      state.authError = '账号或密码错误，请检查后重试';
      render();
      return;
    }
    if (state.authMode === 'register') {
      state.authSubmitting = false;
      state.authMode = 'login';
      state.authStatus = 'idle';
      state.authForm = { account: account.trim(), password: '', code: '' };
      state.captchaMessage = '';
      state.authError = '';
      render();
      showToast('注册成功，请登录');
      return;
    }
    state.authSubmitting = false;
    state.authStatus = 'idle';
    state.authError = '';
    state.authenticated = true;
    state.platform = 'h5';
    state.view = 'home';
    render();
    showToast('登录成功，欢迎回来');
  }, 650);
}

function startWechatAutoLogin() {
  state.platform = 'mini';
  state.authenticated = false;
  state.wechatLoggingIn = true;
  state.firstUse = false;
  render();
  setTimeout(() => {
    state.wechatLoggingIn = false;
    state.authenticated = true;
    state.view = 'home';
    render();
    showToast('微信登录成功，已进入首页');
  }, 850);
}

function logout() {
  state.authenticated = false;
  state.platform = 'h5';
  state.firstUse = false;
  state.view = 'login';
  resetAuthForm();
  render();
  showToast('已退出登录');
}

document.addEventListener('input', (event) => {
  const refundInput = event.target.closest('[data-refund-input]');
  if (refundInput) { state.refundAmountDraft = refundInput.value; state.refundError = ''; }
  const authInput = event.target.closest('[data-auth-field]');
  if (authInput) {
    state.authForm[authInput.dataset.authField] = authInput.value;
    if (state.authError) {
      state.authError = '';
      document.querySelector('.auth-error')?.remove();
    }
    const submit = document.querySelector('[data-action="auth-submit"]');
    if (submit) submit.disabled = !(state.authForm.account.trim() && state.authForm.password && state.authForm.code.trim()) || state.authSubmitting;
  }
});

document.addEventListener('submit', (event) => {
  if (event.target.closest('[data-auth-form]')) {
    event.preventDefault();
    authSubmit();
  }
});

document.addEventListener('click', (event) => {
  const navButton = event.target.closest('[data-nav]');
  if (navButton) { state.view = navButton.dataset.nav; state.editMode = false; render(); return; }
  const typeButton = event.target.closest('[data-type]');
  if (typeButton) { state.entryType = typeButton.dataset.type; state.expression = ''; state.formError = ''; state.calculatorFirst = null; state.calculatorOperator = ''; state.calculatorWaiting = false; render(); showToast(`已切换为${typeButton.textContent}`); return; }
  const dayButton = event.target.closest('[data-day]');
  if (dayButton && dayButton.dataset.day) { state.selectedDay = Number(dayButton.dataset.day); render(); return; }
  const faqButton = event.target.closest('[data-faq]');
  if (faqButton) { state.openFaq = state.openFaq === faqButton.dataset.faq ? '' : faqButton.dataset.faq; render(); return; }
  const keyButton = event.target.closest('[data-key]');
  if (keyButton) { handleKey(keyButton.dataset.key); return; }
  const accountButton = event.target.closest('[data-pick-account]');
  if (accountButton) {
    if (state.pickerRole === 'from') state.transferFrom = accountButton.dataset.pickAccount;
    else if (state.pickerRole === 'to') state.transferTo = accountButton.dataset.pickAccount;
    else state.account = accountButton.dataset.pickAccount;
    closeSheet(); render(); return;
  }
  const yearButton = event.target.closest('[data-calendar-year]');
  if (yearButton) { datePickerDraft = datePickerDraft || { year: state.calendarYear, month: state.calendarMonth }; datePickerDraft.year = Number(yearButton.dataset.calendarYear); datePicker(); return; }
  const monthButton = event.target.closest('[data-calendar-month]');
  if (monthButton) { datePickerDraft = datePickerDraft || { year: state.calendarYear, month: state.calendarMonth }; datePickerDraft.month = Number(monthButton.dataset.calendarMonth); datePicker(); return; }
  const actionElement = event.target.closest('[data-action]');
  const action = actionElement?.dataset.action;
  if (!action) return;
  if (action === 'goto-register') { state.authMode = 'register'; state.authStatus = 'idle'; state.authError = ''; state.authForm = { account: '', password: '', code: '' }; render(); return; }
  if (action === 'goto-login') { state.authMode = 'login'; state.authStatus = 'idle'; state.authError = ''; state.authForm = { account: state.authForm.account || '', password: '', code: '' }; render(); return; }
  if (action === 'refresh-captcha') { refreshCaptcha(); return; }
  if (action === 'logout') { logout(); return; }
  if (action === 'preview-login') { specPanel.hidden = true; state.platform = 'h5'; state.authenticated = false; state.wechatLoggingIn = false; state.firstUse = false; resetAuthForm(); render(); return; }
  if (action === 'preview-register') { specPanel.hidden = true; state.platform = 'h5'; state.authenticated = false; state.wechatLoggingIn = false; state.firstUse = false; resetAuthForm(); state.authMode = 'register'; render(); return; }
  if (action === 'preview-login-error') { specPanel.hidden = true; state.platform = 'h5'; state.authenticated = false; state.wechatLoggingIn = false; state.firstUse = false; resetAuthForm(); state.authForm = { account: 'fail', password: 'error123', code: state.captchaCode }; state.captchaMessage = '图形验证码已显示，请输入'; state.authError = '账号或密码错误，请检查后重试'; render(); return; }
  if (action === 'preview-wechat-auto') { specPanel.hidden = true; startWechatAutoLogin(); return; }
  if (action === 'view-credit') { state.selectedCreditId = actionElement.dataset.creditId; state.creditFilter = ''; state.view = 'credit-detail'; render(); }
  if (action === 'filter-credit') { state.creditFilter = actionElement.dataset.filter; render(); }
  if (action === 'choose-repay-fund') {
    state.repayAmountDraft = document.getElementById('repay-amount').value;
    openSheet(`<h3 class="sheet-title">选择还款账户</h3><div class="repay-account-options">${fundAccounts.filter(item => !item.deleted).map(item => `<button class="repay-account-option ${item.id === state.repayFundId ? 'selected' : ''}" data-action="pick-repay-fund" data-fund-id="${item.id}" aria-pressed="${item.id === state.repayFundId}"><span class="account-icon"><img src="assets/funds-account.png" alt="" /></span><span class="repay-account-copy"><strong>${escapeAttribute(item.name)}</strong><small>可用余额 ¥${money(item.balance)}</small></span><span class="repay-check" aria-hidden="true">${item.id === state.repayFundId ? '✓' : ''}</span></button>`).join('')}</div><div class="sheet-actions"><button class="secondary-button" data-action="back-to-repayment">返回还款</button></div>`);
  }
  if (action === 'pick-repay-fund') { state.repayFundId = actionElement.dataset.fundId; if (state.view === 'repayment-edit') { closeSheet(); render(); } else repaymentSheet(); }
  if (action === 'back-to-repayment') { if (state.view === 'repayment-edit') closeSheet(); else repaymentSheet(); }
  if (action === 'edit-credit') creditForm(true);
  if (action === 'save-credit') {
    const account = state.creditFormEdit ? selectedCredit() : null;
    const name = document.getElementById('credit-name').value.trim();
    const limitInput = document.getElementById('credit-limit');
    const debtInput = document.getElementById('credit-debt');
    const limit = Number(limitInput.value), debt = Number(debtInput.value);
    if (!name || !limitInput.value || !debtInput.value || !Number.isFinite(limit) || !Number.isFinite(debt) || !limitInput.checkValidity() || !debtInput.checkValidity()) { showToast('请输入名称和有效的两位小数金额'); return; }
    if (debt > limit) { showToast('欠款不能超过总额度'); return; }
    if ([...creditAccounts, ...fundAccounts].some(item => item !== account && !item.deleted && item.name === name)) { showToast('账户名称已存在'); return; }
    const values = { name, limit, debt, included: document.getElementById('credit-included').checked };
    if (account) {
      demoEntries.forEach(entry => ['account', 'from', 'to'].forEach(key => { if (entry[key] === account.name) entry[key] = name; }));
      Object.assign(account, values);
    }
    else creditAccounts.push({ id: `credit-${Date.now()}`, ...values, repayments: [] });
    closeSheet(); render(); showToast('信贷账户已保存');
  }
  if (action === 'delete-credit') {
    const account = selectedCredit();
    if (!account) return;
    openSheet(`<h3 class="sheet-title">删除这个账户？</h3><p class="confirm-copy">删除“${escapeAttribute(account.name)}”后，该账户及欠款将从资产汇总移除，历史还款记录保留。</p><div class="sheet-actions"><button class="secondary-button" data-action="close-sheet">取消</button><button class="primary-button fund-delete-confirm" data-action="confirm-delete-credit">确认删除</button></div>`);
  }
  if (action === 'confirm-delete-credit') { const account = selectedCredit(); if (!account) return; account.deleted = true; closeSheet(); state.view = 'assets'; render(); showToast('账户已删除'); }
  if (action === 'repay-credit') {
    const account = selectedCredit();
    if (!account || account.debt <= 0) return;
    const funds = fundAccounts.filter(item => !item.deleted);
    if (!funds.length) { showToast('请先添加用于还款的资金账户'); return; }
    state.repayFundId = funds[0].id;
    state.repayAmountDraft = String(account.debt);
    repaymentSheet();
  }
  if (action === 'confirm-repay-credit') {
    const account = selectedCredit();
    const input = document.getElementById('repay-amount');
    if (!account || !input) return;
    const amount = Number(input.value);
    const fund = fundAccounts.find(item => item.id === state.repayFundId && !item.deleted);
    if (!input.value || !Number.isFinite(amount) || amount <= 0 || amount > account.debt || !input.checkValidity()) { showToast('还款金额须大于 0、不超过欠款，最多两位小数'); return; }
    if (!fund || Math.round(fund.balance * 100) < Math.round(amount * 100)) { showToast('还款账户余额不足，请更换账户或调整金额'); return; }
    fund.balance = (Math.round(fund.balance * 100) - Math.round(amount * 100)) / 100;
    account.debt = (Math.round(account.debt * 100) - Math.round(amount * 100)) / 100;
    const [date, time] = getCurrentDateTime().split(' ');
    demoEntries.push({ id: `repayment-${Date.now()}-${demoEntries.length}`, type: 'repayment', amount, date, time, from: fund.name, to: account.name, fundId: fund.id, creditId: account.id, category: '还款', note: '信贷账户还款' });
    state.creditFilter = '';
    closeSheet(); render(); showToast('还款成功，可用额度已恢复');
  }
  if (action === 'view-fund') { state.selectedFundId = actionElement.dataset.fundId; state.fundFilter = ''; state.view = 'fund-detail'; render(); }
  if (action === 'fund-back') { state.view = 'assets'; render(); }
  if (action === 'filter-fund') { state.fundFilter = actionElement.dataset.filter; render(); }
  if (action === 'edit-fund') {
    pendingFundEdit = null;
    const account = selectedFund();
    openSheet(`<h3 class="sheet-title">编辑账户</h3><div class="settings-list"><label class="field-row"><span class="field-label">账户名称</span><input class="fund-input" id="fund-name" maxlength="20" value="${escapeAttribute(account.name)}" /></label><label class="field-row"><span class="field-label">余额 · ¥</span><input class="fund-input" id="fund-balance" type="number" step="0.01" value="${account.balance}" /></label><label class="field-row"><span class="field-label">计入净资产</span><input id="fund-included" type="checkbox" ${account.included ? 'checked' : ''} /></label></div><div class="sheet-actions"><button class="secondary-button" data-action="close-sheet">取消</button><button class="primary-button" data-action="save-fund">保存账户</button></div>`);
  }
  if (action === 'save-fund') {
    const account = selectedFund();
    const name = document.getElementById('fund-name').value.trim();
    const raw = document.getElementById('fund-balance').value;
    const balance = Number(raw);
    if (!name || !raw || !Number.isFinite(balance) || Math.abs(balance) > 999999999 || !document.getElementById('fund-balance').checkValidity()) { showToast('请输入账户名称和有效的两位小数余额'); return; }
    if (fundAccounts.some(item => item.id !== account.id && item.name === name)) { showToast('账户名称已存在，请换一个名称'); return; }
    pendingFundEdit = { id: account.id, name, balance, included: document.getElementById('fund-included').checked };
    const difference = Math.round(balance * 100) - Math.round(account.balance * 100);
    if (difference !== 0) {
      openSheet(`<h3 class="sheet-title">余额变动是否加入流水？</h3><p class="confirm-copy">账户余额由 <b>¥${money(account.balance)}</b> 调整为 <b>¥${money(balance)}</b>。</p><p class="confirm-copy">加入流水将生成一笔「账户余额补齐」，记为${difference > 0 ? '收入' : '支出'} <b>¥${money(Math.abs(difference) / 100)}</b>。不加入则仅更新账户余额。</p><div class="sheet-actions"><button class="secondary-button" data-action="save-fund-only">不加入流水</button><button class="primary-button" data-action="save-fund-record">加入流水</button></div><button class="skip-link" data-action="close-sheet">取消本次修改</button>`);
    } else saveFundEdit();
  }
  if (action === 'save-fund-only') saveFundEdit();
  if (action === 'save-fund-record') saveFundEdit(true);
  if (action === 'delete-fund') openSheet(`<h3 class="sheet-title">删除这个账户？</h3><p class="confirm-copy">删除“${escapeAttribute(selectedFund().name)}”后，该账户将从资产列表移除，其余额不再计入净资产。历史账单仍会保留。</p><div class="sheet-actions"><button class="secondary-button" data-action="close-sheet">取消</button><button class="primary-button fund-delete-confirm" data-action="confirm-delete-fund">确认删除</button></div>`);
  if (action === 'confirm-delete-fund') { selectedFund().deleted = true; closeSheet(); state.view = 'assets'; render(); showToast('账户已删除，历史账单已保留'); }
  if (action === 'new-entry') { state.view = 'entry'; state.editMode = false; state.selectedEntryId = null; state.entryType = 'expense'; state.account = state.lastAccount || state.account; state.entryDateTime = getCurrentDateTime(); state.transferFrom = state.lastAccount || '微信'; state.transferTo = state.transferFrom === '微信' ? '银行卡' : '微信'; state.note = ''; resetCalculator(); render(); }
  if (action === 'view-entry') { state.selectedEntryId = actionElement.dataset.entryId; state.detailReturnView = state.view; state.view = 'detail'; state.editMode = false; render(); }
  if (action === 'cancel-repayment-edit') { state.view = 'detail'; render(); }
  if (action === 'save-repayment-edit') {
    const input = document.getElementById('repay-amount');
    if (!input.checkValidity() || !input.value) { showToast('请输入有效还款金额'); return; }
    if (updateRepayment(getEntry(), Number(input.value), state.repayFundId)) { state.view = 'detail'; render(); showToast('还款已修改，余额已更新'); }
  }
  if (action === 'edit-entry') {
    const entry = getEntry();
    if (entry?.type === 'repayment') { state.repayFundId = entry.fundId; state.repayAmountDraft = String(entry.amount); state.view = 'repayment-edit'; render(); return; }
    state.editReturnView = state.view === 'detail' ? 'detail' : 'home';
    state.view = 'entry'; state.editMode = true; state.entryType = entry?.type || 'expense'; state.amount = entry ? formatNumber(entry.amount) : '32.00'; state.account = entry?.account || '微信'; state.entryDateTime = entry ? `${entry.date} ${entry.time}` : '2026年09月15日 12:20'; state.transferFrom = entry?.from || '微信'; state.transferTo = entry?.to || '银行卡'; state.note = entry?.note || ''; state.expression = ''; state.formError = ''; state.calculatorFirst = null; state.calculatorOperator = ''; state.calculatorWaiting = false; render();
  }
  if (action === 'back') {
    if (state.view === 'detail') { state.view = state.detailReturnView || 'home'; render(); }
    else if (state.editMode) openSheet(`<h3 class="sheet-title">放弃这次修改？</h3><p class="confirm-copy">当前内容还没有保存，离开后修改会丢失。</p><div class="sheet-actions"><button class="secondary-button" data-action="close-sheet">继续编辑</button><button class="primary-button" data-action="discard-entry">放弃更改</button></div>`);
    else { state.view = 'home'; render(); }
  }
  if (action === 'back-to-mine') { state.view = 'mine'; render(); }
  if (action === 'discard-entry') { closeSheet(); state.view = state.editReturnView || state.detailReturnView || 'home'; render(); }
  if (action === 'show-account-picker') { state.pickerRole = event.target.closest('[data-account-role]')?.dataset.accountRole || 'account'; accountPicker(); }
  if (action === 'show-date-picker') { const [entryDate, entryTime] = (state.entryDateTime || getCurrentDateTime()).split(' '); openSheet(`<h3 class="sheet-title">日期与时间</h3><div class="settings-list"><div class="field-row"><span class="field-label">记账日期</span><span class="field-value">${entryDate}　›</span></div><div class="field-row"><span class="field-label">记账时间</span><span class="field-value">${entryTime}　›</span></div></div><div class="sheet-actions"><button class="secondary-button" data-action="close-sheet">取消</button><button class="primary-button" data-action="close-sheet">确定</button></div>`); }
  if (action === 'show-note') openSheet(`<h3 class="sheet-title">添加备注</h3><textarea class="note-input" maxlength="100" placeholder="记录一点上下文，例如：和朋友聚餐"></textarea><div class="note-count">0 / 100</div><div class="sheet-actions"><button class="secondary-button" data-action="close-sheet">取消</button><button class="primary-button" data-action="close-sheet">完成</button></div>`);
  if (action === 'save-entry') {
    if (!state.amount || Number(state.amount) <= 0) { state.formError = '请输入大于 0 的金额'; render(); showToast('请先输入记账金额'); return; }
    if (Number(state.amount) > 999999999.99) { state.formError = '金额不能超过 ¥999,999,999.99'; render(); showToast('金额超过上限'); return; }
    if (isCompleteExpression(state.expression)) {
      try {
        const result = evaluateExpression(state.expression);
        if (result === null) throw new Error('INVALID');
        state.amount = formatNumber(result);
      } catch (error) {
        state.formError = error.message === 'DIVIDE_BY_ZERO' ? '除数不能为 0，请重新输入' : '算式有误，请检查后重试';
        render();
        return;
      }
    }
    if (state.entryType === 'transfer' && state.transferFrom === state.transferTo) { state.formError = '转出账户和转入账户不能相同'; render(); showToast('请更换一个账户'); return; }
    state.formError = ''; if (state.entryType !== 'transfer' && state.account) state.lastAccount = state.account; state.saving = true; render(); setTimeout(() => { state.saving = false; state.view = state.editReturnView === 'detail' ? 'detail' : 'home'; state.editMode = false; render(); showToast('记账成功，账户余额已更新'); }, 850);
  }
  if (action === 'refund-entry') { const entry = getEntry(); if (entry && !['transfer', 'repayment'].includes(entry.type) && getRemainingRefundAmount(entry) > 0) { state.refundAmountDraft = formatNumber(getRemainingRefundAmount(entry)); state.refundError = ''; openSheet(refundSheet(entry)); } }
  if (action === 'confirm-refund') {
    const entry = getEntry();
    const refundAmount = Number(state.refundAmountDraft);
    if (!entry || !Number.isFinite(refundAmount) || refundAmount <= 0) { state.refundError = '退款金额必须大于 0'; if (entry) openSheet(refundSheet(entry)); return; }
    const remainingRefundAmount = getRemainingRefundAmount(entry);
    if (refundAmount > remainingRefundAmount) { state.refundError = `本次退款不能超过剩余可退金额 ¥${money(remainingRefundAmount)}`; openSheet(refundSheet(entry)); return; }
    const refundId = `refund-${state.selectedEntryId}-${Date.now()}-${getRefundRecords(state.selectedEntryId).length + 1}`;
    state.refundRecords[state.selectedEntryId] = [...getRefundRecords(state.selectedEntryId), { id: refundId, amount: formatNumber(refundAmount), time: getCurrentDateTime() }]; state.refundAmountDraft = ''; state.refundError = ''; closeSheet(); render(); showToast('已退款，账单状态已更新');
  }
  if (action === 'delete-refund-entry') { state.pendingRefundId = actionElement.dataset.refundId; openSheet(`<h3 class="sheet-title">删除退款记录？</h3><p class="confirm-copy">只会删除当前选中的退款记录，其他退款记录和原账单都会保留。</p><div class="sheet-actions"><button class="secondary-button" data-action="close-sheet">取消</button><button class="primary-button" style="background:#df8b77" data-action="confirm-delete-refund">确认删除</button></div>`); }
  if (action === 'confirm-delete-refund') { state.refundRecords[state.selectedEntryId] = getRefundRecords(state.selectedEntryId).filter(record => record.id !== state.pendingRefundId); state.pendingRefundId = null; closeSheet(); render(); showToast('退款记录已删除'); }
  if (action === 'delete-entry') openSheet(`<h3 class="sheet-title">删除这笔账单？</h3><p class="confirm-copy">删除后会同步刷新首页、日历和相关账户余额。此操作无法撤销。</p><div class="sheet-actions"><button class="secondary-button" data-action="close-sheet">取消</button><button class="primary-button" style="background:#df8b77" data-action="confirm-delete">确认删除</button></div>`);
  if (action === 'confirm-delete') { const entry = getEntry(); if (entry?.type === 'repayment' && !updateRepayment(entry, 0, entry.fundId, true)) return; state.deletedEntryIds[state.selectedEntryId] = true; closeSheet(); state.view = state.detailReturnView || 'home'; render(); showToast('账单已删除，余额已重算'); }
  if (action === 'new-account') { state.accountIncludeNetAsset = true; accountForm(false, 'asset'); }
  if (action === 'edit-account') accountForm(true, event.target.closest('[data-account-type]')?.dataset.accountType || 'asset');
  if (action === 'toggle-account-type') accountForm(false, event.target.closest('[data-account-type]')?.dataset.accountType || 'asset');
  if (action === 'toggle-net-asset') { state.accountIncludeNetAsset = !state.accountIncludeNetAsset; accountForm(state.accountFormEdit, state.accountFormType); }
  if (action === 'toggle-account-section') {
    const section = actionElement.dataset.accountSection;
    state.accountSections[section] = !state.accountSections[section];
    render();
  }
  if (action === 'save-account') { closeSheet(); showToast('账户已保存'); }
  if (action === 'avatar-auth') { state.avatarAuthState = 'loading'; render(); setTimeout(() => { state.avatarAuthState = 'success'; render(); showToast('头像授权成功'); }, 650); }
  if (action === 'open-about') { state.view = 'about'; render(); }
  if (action === 'show-privacy') openSheet(`<h3 class="sheet-title">隐私说明</h3><p class="confirm-copy">哈记账只收集完成记账和账户管理所需的信息。账单内容用于生成你的收支和资产数据，不会用于广告推荐。</p><p class="confirm-copy">头像授权为可选操作；拒绝授权不影响基础记账功能。数据导出与删除能力将在后续版本开放。</p><div class="sheet-actions"><button class="primary-button" data-action="close-sheet">我知道了</button></div>`, false, true);
  if (action === 'show-feedback') { closeSheet(); showToast('反馈入口已预留，感谢你的建议'); }
  if (action === 'goto-assets') { state.view = 'assets'; render(); }
  if (action === 'show-month-picker') datePicker();
  if (action === 'confirm-date-picker') { const draft = datePickerDraft || { year: state.calendarYear, month: state.calendarMonth }; state.calendarYear = draft.year; state.calendarMonth = draft.month; closeSheet(); render(); }
  if (action === 'prev-month') { const previous = new Date(state.calendarYear, state.calendarMonth - 2, 1); state.calendarYear = previous.getFullYear(); state.calendarMonth = previous.getMonth() + 1; render(); }
  if (action === 'next-month') { const now = new Date(); state.calendarYear = now.getFullYear(); state.calendarMonth = now.getMonth() + 1; state.selectedDay = now.getDate(); render(); }
  if (action === 'show-spec') specPanel.hidden = false;
  if (action === 'close-spec') specPanel.hidden = true;
  if (action === 'preview-first-use') { specPanel.hidden = true; state.platform = 'h5'; state.authenticated = true; state.wechatLoggingIn = false; state.firstUse = true; render(); }
  if (action === 'close-sheet') closeSheet();
  if (action === 'show-toast') { closeSheet(); showToast('该入口已预留，第一版暂不开放'); }
  if (action === 'start-use' || action === 'skip-use') { state.firstUse = false; state.view = 'home'; render(); showToast(action === 'start-use' ? '欢迎开始记账' : '已跳过账户设置'); }
});

backdrop.addEventListener('click', (event) => { if (event.target === backdrop) closeSheet(); });
window.addEventListener('resize', positionSheet);

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

function isCompleteExpression(expression) {
  return /^\d+(?:\.\d+)?(?:\s[+−×÷]\s\d+(?:\.\d+)?)+$/.test(expression.trim());
}

function scheduleAutoCalculate() {
  clearTimeout(autoCalculateTimer);
  if (!state.expression || !isCompleteExpression(state.expression)) return;
  autoCalculateTimer = setTimeout(() => {
    try {
      const result = evaluateExpression(state.expression);
      if (result === null) throw new Error('INVALID');
      state.amount = formatNumber(result);
      state.calculatorWaiting = true;
      render();
    } catch (error) {
      state.formError = error.message === 'DIVIDE_BY_ZERO' ? '除数不能为 0，请重新输入' : '算式有误，请检查后重试';
      render();
    }
  }, 450);
}

function handleKey(key) {
  clearTimeout(autoCalculateTimer);
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
    if (state.calculatorWaiting && isCompleteExpression(state.expression)) state.expression = '';
    if (state.calculatorWaiting) { state.amount = '0.'; state.calculatorWaiting = false; }
    else if (!state.amount.includes('.')) state.amount += state.amount ? '.' : '0.';
    state.expression = state.expression.replace(/\d*\.?\d*$/, state.amount);
  } else {
    if (state.calculatorWaiting) {
      if (isCompleteExpression(state.expression)) state.expression = '';
      state.amount = '';
      state.calculatorWaiting = false;
    }
    if (state.amount.length < 12 && (!state.amount.includes('.') || state.amount.split('.')[1].length < 2)) state.amount += key;
    state.expression = state.expression ? state.expression.replace(/\d*\.?\d*$/, state.amount) : state.amount;
  }
  scheduleAutoCalculate();
  render();
}

render();
