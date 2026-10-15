<template>
  <div class="wo-page">
    <!-- 车间筛选(带计数) -->
    <div class="workshop-tabs">
      <button
        v-for="ws in workshopList"
        :key="ws.code"
        class="ws-tab"
        :class="{ active: currentWorkshop === ws.code }"
        @click="switchWorkshop(ws.code)"
      >
        {{ ws.name }} <span class="ws-count">{{ statistics?.workshopCount?.[ws.code] ?? 0 }}</span>
      </button>
    </div>

    <!-- 状态tab + 排序/创建 -->
    <div class="toolbar">
      <div class="tabs">
        <button
          v-for="s in STATUS_TABS"
          :key="s.key"
          class="tab"
          :class="[tabClass(s.key), { active: currentStatus === s.key }]"
          @click="switchStatus(s.key)"
        >
          {{ s.text }} ({{ statusCount(s.key) }})
        </button>
      </div>
      <div class="toolbar-right">
        <button class="btn-create" @click="handleAdd">＋ 创建工单</button>
        <button class="btn-create" @click="handleBatchAdd">批量创建</button>
        <button class="btn-sort" :class="{ 'sort-active': sortField === 'orderNo' }" @click="toggleSort('orderNo')">工单号 {{ arrow('orderNo') }}</button>
        <button class="btn-sort" :class="{ 'sort-active': sortField === 'planDate' }" @click="toggleSort('planDate')">日期 {{ arrow('planDate') }}</button>
      </div>
    </div>

    <!-- 卡片列表(原型:交替行+左色条+固定列宽对齐) -->
    <div class="card-list">
      <div v-if="orders.length === 0" class="empty">暂无数据</div>
      <div v-for="o in orders" :key="o.id" class="card" :class="`card-${o.status}`">
        <div class="card-main">
          <div class="col c-id"><span class="col-label">工单号</span><span class="col-value bold">{{ o.orderNo }}</span></div>
          <div class="col c-proc"><span class="col-label">工艺名称</span><span class="col-value name">{{ o.processName }}</span></div>
          <div class="col c-ws"><span class="col-label">车间</span><span class="col-value">{{ workshopName(o.workshopCode) }}</span></div>
          <div class="col c-date"><span class="col-label">日期</span><span class="col-value">{{ o.planDate }}</span></div>
          <template v-if="o.status !== 'pending'">
            <div class="col c-op"><span class="col-label">操作人</span><span class="col-value">{{ o.operator || '—' }}</span></div>
            <div class="col c-time"><span class="col-label">开始时间</span><span class="col-value">{{ fmtTime(o.startTime) || '—' }}</span></div>
            <div class="col c-end"><span class="col-label">{{ o.status === 'completed' || o.status === 'abandoned' ? '完成时间' : '预计完成' }}</span><span class="col-value">{{ fmtTime(o.endTime) || o.planEndTime || '—' }}</span></div>
          </template>
          <div class="col c-src"><span class="col-label">源酒罐</span><span class="col-value" :title="o.sourceTank">{{ o.sourceTank || '—' }}</span></div>
          <div class="col c-tgt"><span class="col-label">目标酒罐</span><span class="col-value" :title="o.targetTank">{{ o.targetTank || '—' }}</span></div>
          <div class="col c-path"><span class="col-label">转酒路径</span><span class="col-value" :title="o.transferPathName || o.transferPath">{{ o.transferPathName || o.transferPath || '—' }}</span></div>
          <div class="col c-amt"><span class="col-label">输送量(吨)</span><span class="col-value">{{ o.amount ?? 0 }}</span></div>
        </div>
        <div class="card-actions">
          <span v-if="o.status === 'paused'" class="badge badge-paused">已暂停</span>
          <span v-else-if="o.status === 'completed'" class="badge badge-completed">已完成</span>
          <span v-else-if="o.status === 'abandoned'" class="badge badge-abandoned">{{ o.abandonType === '停止' ? '已停止' : '已删除' }}</span>
          <button class="btn btn-trace" @click="handleTrace(o)">追溯</button>
          <button class="btn btn-detail" @click="handleDetail(o)">详情</button>
          <button v-if="o.status === 'pending'" class="btn btn-edit" @click="handleEdit(o)">编辑</button>
          <button v-if="o.status === 'pending'" class="btn btn-transfer" @click="doTransition(o, 'start')">启动</button>
          <button v-if="o.status === 'running'" class="btn btn-pause" @click="doTransition(o, 'pause')">暂停</button>
          <button v-if="o.status === 'paused'" class="btn btn-resume" @click="doTransition(o, 'resume')">恢复</button>
          <button v-if="o.status === 'running' || o.status === 'paused'" class="btn btn-stop" @click="doTransition(o, 'stop')">停止</button>
          <button v-if="o.status !== 'abandoned' && o.status !== 'completed'" class="btn btn-delete" @click="doTransition(o, 'delete')">删除</button>
        </div>
      </div>
    </div>

    <div class="pager">
      <Pagination
        v-model:current="page"
        :total="total"
        :page-size="PAGE_SIZE"
        size="small"
        :show-total="(t) => `共 ${t} 条`"
        @change="loadOrders"
      />
    </div>

    <!-- 新增、编辑、详情modal -->
    <FormModal @register="registerActionModal" :width="960" @success="refreshAll" />
    <!-- 批量新增modal -->
    <BatchFormModal @register="registerBatchModal" @success="refreshAll" />
  </div>
</template>
<script lang="ts" setup name="maotai-work-order">
import { createVNode, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { Pagination } from 'ant-design-vue';
import { Modal } from 'ant-design-vue';
import { ExclamationCircleOutlined } from '@ant-design/icons-vue';
import { useModal } from '/@/components/Modal';
import { getDictItems } from '/@/utils/dict/JDictSelectUtil';
import {
  getOrderPage, getOrderStatistics, transitionOrder,
  type WorkOrderItem, type WorkOrderStatistics,
} from './WorkOrder.api';
import { workshopNameMap } from './WorkOrder.data';
import FormModal from './components/WorkOrderForm.vue';
import BatchFormModal from './components/WorkOrderBatchForm.vue';

const PAGE_SIZE = 12;
// 状态 tab(原型4态;执行中含已暂停——后端 status 支持逗号多值);顺序:待执行 → 执行中 → 已完成 → 已废弃
const STATUS_TABS = [
  { key: 'pending', text: '待执行' },
  { key: 'running,paused', text: '执行中' },
  { key: 'completed', text: '已完成' },
  { key: 'abandoned', text: '已废弃' },
];

const workshopList = ref<{ code: string; name: string }[]>([
  { code: 'ZZG', name: '中转酒罐车间' }, { code: 'XLG', name: '系列酒勾调厂房' },
  { code: 'CPG', name: '成品酒罐房' }, { code: 'GWG', name: '包装厂房' },
]);

const currentWorkshop = ref('ZZG');
const currentStatus = ref('running,paused');
const orders = ref<WorkOrderItem[]>([]);
const statistics = ref<WorkOrderStatistics | null>(null);
const page = ref(1);
const total = ref(0);
const sortField = ref('planDate');
const sortDir = ref('desc');

// 新增、编辑、详情modal
const [registerActionModal, { openModal }] = useModal();
// 批量新增modal
const [registerBatchModal, { openModal: openBatchModal }] = useModal();

function workshopName(code?: string) {
  return workshopList.value.find((w) => w.code === code)?.name || code || '—';
}
function fmtTime(t?: string) {
  return t ? String(t).replace('T', ' ').slice(0, 16) : '';
}
function arrow(f: string) {
  return sortField.value === f ? (sortDir.value === 'asc' ? '↑' : '↓') : '↕';
}
function statusCount(tabKey: string) {
  const c = statistics.value?.statusCount || {};
  return tabKey.split(',').reduce((sum, k) => sum + (c[k] ?? 0), 0);
}
function tabClass(tabKey: string) {
  const base = tabKey.split(',')[0];
  return { running: 'tab-run', pending: 'tab-pen', completed: 'tab-com', abandoned: 'tab-abd' }[base] || 'tab-run';
}

async function loadOrders() {
  const res = await getOrderPage({
    workshopCode: currentWorkshop.value,
    status: currentStatus.value,
    column: sortField.value,
    order: sortDir.value,
    pageNo: page.value,
    pageSize: PAGE_SIZE,
  });
  orders.value = res?.records || [];
  total.value = res?.total || 0;
}
// 口径:车间 tab=该车间 4 状态总和(恒定);状态 tab=当前车间内各状态数(总和=车间总数,自洽)
async function loadStats() {
  statistics.value = await getOrderStatistics({ workshopCode: currentWorkshop.value || undefined });
}
function refreshAll() {
  loadOrders();
  loadStats();
}
function switchWorkshop(ws: string) {
  currentWorkshop.value = ws;
  page.value = 1;
  loadOrders();
  // 换车间:状态 tab 计数换成新车间的分布
  loadStats();
}
function switchStatus(s: string) {
  currentStatus.value = s;
  page.value = 1;
  loadOrders();
}
function toggleSort(f: string) {
  if (sortField.value === f) sortDir.value = sortDir.value === 'asc' ? 'desc' : 'asc';
  else {
    sortField.value = f;
    sortDir.value = 'desc';
  }
  page.value = 1;
  loadOrders();
}

// 状态流转确认文案(与原型一致)
const TRANSITION_TIPS: Record<string, (o: WorkOrderItem) => string> = {
  start: (o) => `确认启动工单 ${o.orderNo}（${o.processName}）？工单将由【待执行】变更为【执行中】。`,
  pause: (o) => `确认暂停工单 ${o.orderNo}（${o.processName}）？暂停后工单将保持在执行中页面。`,
  resume: (o) => `确认恢复工单 ${o.orderNo}（${o.processName}）？工单将恢复正常执行状态。`,
  stop: (o) => `确认停止工单 ${o.orderNo}（${o.processName}）？停止后工单将移入【已废弃】，此操作不可恢复。`,
  delete: (o) => `确认删除工单 ${o.orderNo}（${o.processName}）？删除后工单将移入【已废弃】，此操作不可恢复。`,
};

function doTransition(o: WorkOrderItem, action: string) {
  const tip = TRANSITION_TIPS[action]?.(o) || '确认执行该操作？';
  Modal.confirm({
    title: tip,
    icon: createVNode(ExclamationCircleOutlined, { style: { color: action === 'stop' || action === 'delete' ? '#ef4444' : '#f59e0b' } }),
    okText: '确认',
    okType: action === 'stop' || action === 'delete' ? 'danger' : 'primary',
    cancelText: '取消',
    onOk: async () => {
      try {
        await transitionOrder(o.id!, action);
        Modal.success({ title: `工单 ${o.orderNo} 操作成功` });
        refreshAll();
      } catch (e) {
        // 失败(必填拦截/中集拒绝)静默返回关闭确认框,错误信息 defHttp 已 toast;工单状态未变
      }
    },
  });
}

function handleAdd() {
  openModal(true, { action: 'add' });
}
function handleBatchAdd() {
  // 传空 data 触发 useModalInner 回调重置明细为初始一行
  openBatchModal(true, {});
}
function handleEdit(o: WorkOrderItem) {
  openModal(true, { action: 'edit', record: o });
}
function handleDetail(o: WorkOrderItem) {
  openModal(true, { action: 'detail', record: o });
}
// 跳批次追溯页,带工单号定位(向上追源)
const router = useRouter();
function handleTrace(o: WorkOrderItem) {
  router.push({ path: '/maotai/workOrderTrace', query: { orderNo: o.orderNo || '' } });
}

// 车间名取字典口径(静态兜底值与字典不一致时以字典为准)
onMounted(async () => {
  try {
    const items: any = await getDictItems("mes3_mdm_dict where group_code='MES3_WORKSHOP' and del_flag=0", 'item_val,item_key');
    const byKey: Record<string, string> = {};
    (items || []).forEach((it: any) => {
      if (it.value) {
        byKey[it.value] = it.text;
        workshopNameMap[it.value] = it.text;
      }
    });
    const order = ['ZZG', 'XLG', 'CPG', 'GWG'];
    const names = order.map((k) => byKey[k]).filter(Boolean);
    if (names.length === 4) workshopList.value = order.map((k, i) => ({ code: k, name: names[i] }));
  } catch (e) {
    console.error('车间字典加载失败', e);
  }
  refreshAll();
});
</script>

<style lang="less" scoped>
/** 原型(Brewmaxx 覆盖层)样式:深蓝 #003366、扁平小圆角、卡片表格行风 */
@wo-blue: #003366;

.wo-page {
  padding: 12px;
  background: #fff;
  border: 1px solid #ccc;
  border-radius: 4px;
}

/* 车间 tab */
.workshop-tabs {
  display: flex;
  gap: 4px;
  margin-bottom: 10px;
}
.ws-tab {
  flex: 1;
  padding: 7px 10px;
  border-radius: 3px;
  font-size: 13px;
  border: 1px solid #ccc;
  background: #e0e0e0;
  color: #333;
  cursor: pointer;
  transition: all 0.2s;
  text-align: center;
}
.ws-tab:hover {
  background: #e6f2ff;
  color: @wo-blue;
  border-color: #66afe9;
}
.ws-tab.active {
  background: @wo-blue;
  color: #fff;
  border-color: @wo-blue;
}
.ws-count {
  font-weight: 700;
  margin-left: 4px;
}

/* 状态 tab + 工具栏 */
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #ccc;
  padding-bottom: 8px;
  margin-bottom: 10px;
}
.tabs {
  display: flex;
  gap: 2px;
}
.tab {
  border-radius: 3px 3px 0 0;
  min-width: 100px;
  padding: 6px 18px;
  font-size: 12px;
  border: 1px solid #ccc;
  background: #e0e0e0;
  color: #333;
  cursor: pointer;
}
.tab:hover {
  background: #e6f2ff;
  color: @wo-blue;
}
.tab.active.tab-run { background: #2e7d32; color: #fff; border-color: #2e7d32; font-weight: 600; }
.tab.active.tab-pen { background: #c88a2b; color: #fff; border-color: #c88a2b; font-weight: 600; }
.tab.active.tab-com { background: #1e5bb8; color: #fff; border-color: #1e5bb8; font-weight: 600; }
.tab.active.tab-abd { background: #6b7280; color: #fff; border-color: #6b7280; font-weight: 600; }

.toolbar-right {
  display: flex;
  gap: 8px;
  align-items: center;
}
.btn-create {
  background: @wo-blue;
  color: #fff;
  border: none;
  border-radius: 3px;
  padding: 5px 14px;
  font-size: 12px;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}
.btn-create:hover {
  background: #00264d;
}
.btn-sort {
  background: #fff;
  border: 1px solid #ccc;
  border-radius: 3px;
  padding: 5px 12px;
  font-size: 12px;
  color: #333;
  cursor: pointer;
}
.btn-sort:hover {
  border-color: #66afe9;
  color: @wo-blue;
}
.sort-active {
  background: @wo-blue;
  color: #fff;
  border-color: @wo-blue;
}

/* 卡片列表:交替行 + 左色条 + 固定列宽对齐 */
.card-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-height: 120px;
}
.card {
  display: flex;
  align-items: center;
  background: #fff;
  border: 1px solid #ddd;
  border-left: 3px solid @wo-blue;
  border-radius: 2px;
  padding: 6px 12px;
  gap: 14px;
}
.card:nth-child(even) {
  background: #f5f5f5;
}
.card:hover {
  background: #e6f2ff;
  border-color: #66afe9;
}
.card-paused {
  border-left-color: #8e70b5;
}
.card-completed {
  border-left-color: #1e5bb8;
}
.card-abandoned {
  border-left-color: #6b7280;
  opacity: 0.75;
}
.card-main {
  display: flex;
  flex: 1;
  gap: 24px;
  align-items: center;
}
.col {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
  flex: 0 0 auto;
}
.col-label {
  font-size: 10px;
  color: #666;
  white-space: nowrap;
}
.col-value {
  font-size: 12px;
  color: #222;
  font-family: 'Consolas', 'Courier New', monospace;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.col-value.bold,
.col-value.name {
  font-family: Arial, sans-serif;
  font-weight: 700;
}
/* 固定列宽:跨行严格对齐 */
.c-id { width: 104px; }
.c-proc { width: 82px; }
.c-ws { width: 130px; }
.c-date { width: 90px; }
.c-op { width: 80px; }
.c-time, .c-end { width: 118px; }
.c-src, .c-tgt { width: 96px; }
.c-path { width: 124px; }
.c-amt { width: 84px; }

/* 操作按钮:原型配色 */
.card-actions {
  display: flex;
  align-items: center;
  gap: 6px;
}
.btn {
  border: none;
  border-radius: 3px;
  font-size: 12px;
  padding: 5px 10px;
  color: #fff;
  cursor: pointer;
}
.btn-detail { background: @wo-blue; }
.btn-detail:hover { background: #00264d; }
.btn-trace { background: #0e7490; }
.btn-trace:hover { background: #0b5c73; }
.btn-edit { background: #5b7a99; }
.btn-edit:hover { background: #4a6580; }
.btn-transfer { background: #c88a2b; }
.btn-transfer:hover { background: #a97422; }
.btn-pause { background: #8e70b5; }
.btn-pause:hover { background: #76599a; }
.btn-resume { background: #3f9d6b; }
.btn-resume:hover { background: #348059; }
.btn-delete, .btn-stop { background: #c0584f; }
.btn-delete:hover, .btn-stop:hover { background: #a0463f; }

/* 徽章 */
.badge {
  padding: 2px 8px;
  border-radius: 2px;
  color: #fff;
  font-size: 11px;
  font-weight: 600;
}
.badge-paused {
  background: #fef3c7;
  color: #d97706;
  animation: blink 1.2s infinite;
}
.badge-completed {
  background: #10b981;
}
.badge-abandoned {
  background: #6b7280;
}
@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.4; }
}

.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 10px;
}
.empty {
  text-align: center;
  padding: 40px 0;
  color: #9ca3af;
}
</style>
