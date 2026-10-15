<template>
  <div class="trace-page">
    <div class="section">
      <button type="button" class="trace-close" title="关闭，返回工单任务列表（Esc）" @click="backToList">✕</button>
      <div class="section-title">{{ pageTitle }}</div>

      <!-- 工具条:追溯方式 | 检索 | 追溯方向 -->
      <div class="trace-toolbar">
        <div class="trace-mode">
          <button type="button" class="tm-tab" :class="{ active: state.mode === 'order' }" @click="setMode('order')">按工单号</button>
          <button type="button" class="tm-tab" :class="{ active: state.mode === 'batch' }" @click="setMode('batch')">按批次号</button>
        </div>
        <div class="trace-search">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="7" /><line x1="21" y1="21" x2="16.65" y2="16.65" /></svg>
          <input
            v-model="keyword"
            type="text"
            :placeholder="state.mode === 'batch' ? '输入批次号检索，如 XC-2609-01' : '输入工单号 / 罐号检索，如 WO-XC2609-1001'"
            autocomplete="off"
            @input="onSuggest"
            @keydown="onSearchKey"
            @blur="hideSuggestLater"
          />
          <div v-show="suggestVisible" class="trace-suggest">
            <template v-if="suggestItems.length">
              <div v-for="it in suggestItems" :key="it.key" class="ts-item" @mousedown.prevent="selectSuggest(it)">
                <span class="ts-id">{{ it.head }}</span>
                <span class="ts-stage" :style="{ background: it.color }">{{ it.stage }}</span>
                <span class="ts-info">{{ it.info }}</span>
              </div>
            </template>
            <div v-else class="ts-empty">未找到匹配{{ state.mode === 'batch' ? '批次' : '工单' }}</div>
          </div>
        </div>
        <div class="trace-dir">
          <button type="button" class="td-tab" :class="{ active: state.dir === 'up' }" @click="setDir('up')">↑ 向上追源（酒液来自哪）</button>
          <button type="button" class="td-tab" :class="{ active: state.dir === 'down' }" @click="setDir('down')">↓ 向下追踪（流向了哪些成品）</button>
        </div>
      </div>

      <!-- 图例 -->
      <div class="trace-legend">
        <span class="tl-item"><i style="background: #2e7d32"></i>卸车</span>
        <span class="tl-item"><i style="background: #1e5bb8"></i>中转倒罐</span>
        <span class="tl-item"><i style="background: #8e70b5"></i>勾调</span>
        <span class="tl-item"><i style="background: #c88a2b"></i>成品倒罐</span>
        <span class="tl-item"><i style="background: #c0584f"></i>灌装</span>
      </div>

      <!-- 谱系画布 -->
      <div class="trace-canvas-wrap" ref="wrapRef">
        <div v-if="!hasQuery" class="trace-empty">
          {{ state.mode === 'batch' ? '请先在上方输入批次号，查看该批次全部工单的酒液生命图谱' : '请先在上方输入工单号（支持工单号 / 罐号 / 工艺模糊检索），查看该工单的酒液生命图谱' }}
        </div>
        <div v-else-if="!graph" class="trace-empty">未找到匹配工单</div>
        <div v-else class="trace-canvas" :style="{ width: layout.w + 'px', height: layout.h + 'px' }">
          <svg class="trace-svg" :width="layout.w" :height="layout.h">
            <defs>
              <marker id="tarrow" markerWidth="8" markerHeight="8" refX="6" refY="3" orient="auto">
                <path d="M0,0 L6,3 L0,6 Z" fill="#9DB2C6" />
              </marker>
            </defs>
            <path v-for="(e, i) in layout.edges" :key="i" :d="e" stroke="#9DB2C6" stroke-width="1.5" fill="none" marker-end="url(#tarrow)" />
          </svg>
          <div
            v-for="n in layout.nodes"
            :key="n.key"
            class="tnode"
            :class="{ start: n.kind === 'start' }"
            :style="{ left: n.x + 'px', top: n.y + 'px', '--sc': n.color }"
            :title="n.key + ' · 点击查看工单详情'"
            @click="openDetail(n.order)"
          >
            <div class="tn-top">
              <span class="tn-id">{{ state.mode === 'batch' ? n.order.batchNo || n.key : n.key }}</span>
              <span class="tn-stage">{{ n.stageLabel }}</span>
            </div>
            <div class="tn-proc">{{ n.order.processName }} · {{ workshopName(n.order.workshopCode) }}</div>
            <div class="tn-flow">
              <span :class="{ 'tn-tank': !!n.order.sourceTank }">{{ n.order.sourceTank || '槽车' }}</span>
              →
              <span :class="{ 'tn-tank': !!n.order.targetTank }">{{ n.order.targetTank || '槽车' }}</span>
            </div>
            <div class="tn-meta">输送量 {{ n.order.amount ?? '-' }} 吨 · {{ n.order.planDate || '' }}</div>
            <div v-if="n.order.processName === '卸车工艺'" class="tn-extra">
              {{ n.order.plateNo || '外部槽车' }}{{ n.order.alcohol ? ' · ' + n.order.alcohol + '%vol' : '' }}
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 工单详情弹窗(复用列表页同款,只读) -->
    <FormModal @register="registerDetailModal" :width="960" />
  </div>
</template>

<script lang="ts" setup name="maotai-work-order-trace">
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { useModal } from '/@/components/Modal';
import { getTraceOrders, type WorkOrderItem } from './WorkOrder.api';
import { workshopNameMap } from './WorkOrder.data';
import FormModal from './components/WorkOrderForm.vue';
import { buildPools, stageOf, findOrder, buildTraceGraph as buildTraceGraphFn, buildBatchGraph as buildBatchGraphFn, layoutGraph } from './traceGraph';

const LIST_ROUTE = '/maotai/workOrderList';

const router = useRouter();
const orders = ref<WorkOrderItem[]>([]);
const keyword = ref('');
const suggestVisible = ref(false);
const wrapRef = ref<HTMLDivElement | null>(null);

// 追溯状态:order=按工单号 batch=按批次号;dir=up向上追源/down向下追踪;lastOrderId 供批次页签切回
const state = ref({ mode: 'order', orderId: '', batchNo: '', dir: 'up', lastOrderId: '' });
const hasQuery = computed(() => (state.value.mode === 'batch' ? !!state.value.batchNo : !!state.value.orderId));
const pageTitle = computed(() => {
  let t = '批次追溯';
  if (state.value.mode === 'order' && state.value.orderId) t += ' - ' + state.value.orderId;
  else if (state.value.mode === 'batch' && state.value.batchNo) t += ' - ' + state.value.batchNo;
  return t;
});

// ===== 工单池(口径见 traceGraph.buildPools):贡献方只看已完成,消费方含执行中,不含已废弃 =====
const pools = computed(() => buildPools(orders.value));
const doneOrders = computed(() => pools.value.done);
const liveOrders = computed(() => pools.value.live);
const activeOrders = computed(() => pools.value.active);

function workshopName(code?: string) {
  return workshopNameMap[code || ''] || code || '—';
}

// ===== 布局(口径见 traceGraph.layoutGraph):追源行序倒置——图谱恒「卸车在上 → 灌装在下」 =====
const graph = computed(() => {
  if (!hasQuery.value) return null;
  return state.value.mode === 'batch'
    ? buildBatchGraphFn(pools.value, state.value.batchNo, state.value.dir)
    : buildTraceGraphFn(pools.value, orders.value, state.value.orderId, state.value.dir);
});
const layout = computed(() => layoutGraph(graph.value, state.value.dir));

// 查询对象定位:追源时查询单在底部、追踪时在顶部
async function scrollToFocus() {
  await nextTick();
  const wrap = wrapRef.value;
  if (wrap) wrap.scrollTop = state.value.dir === 'up' ? wrap.scrollHeight : 0;
}

// ===== 检索联想 =====
const suggestItems = ref<{ key: string; head: string; stage: string; color: string; info: string; type: 'order' | 'batch' }[]>([]);
function onSuggest() {
  const kw = keyword.value.trim().toLowerCase();
  if (!kw) {
    suggestVisible.value = false;
    suggestItems.value = [];
    return;
  }
  if (state.value.mode === 'batch') {
    const map: Record<string, number> = {};
    activeOrders.value.forEach((o) => {
      if (o.batchNo && o.batchNo.toLowerCase().includes(kw)) map[o.batchNo] = (map[o.batchNo] || 0) + 1;
    });
    // 批次按工艺阶段划分(卸车/中转/勾调/成品/灌装),一种批次=同阶段多张工单,联想带出阶段身份
    suggestItems.value = Object.keys(map).slice(0, 8).map((bn) => {
      const first = activeOrders.value.find((o) => o.batchNo === bn);
      const st = first ? stageOf(first) : null;
      return {
        key: bn, head: bn, stage: st ? st.label + '批次' : '批次', color: st?.color || '#0E7490',
        info: `关联 ${map[bn]} 张工单`, type: 'batch',
      };
    });
  } else {
    const hits = orders.value
      .filter((o) => {
        const hay = [o.orderNo, o.processName, workshopName(o.workshopCode), o.sourceTank, o.targetTank]
          .map((v) => String(v || '').toLowerCase())
          .join(' ');
        return hay.includes(kw);
      })
      .slice(0, 8);
    suggestItems.value = hits.map((o) => {
      const st = stageOf(o);
      return {
        key: o.orderNo!, head: o.orderNo!, stage: st.label, color: st.color,
        info: `${o.processName} · ${o.sourceTank || '槽车'} → ${o.targetTank || '槽车'} · ${o.planDate || ''}`, type: 'order',
      };
    });
  }
  suggestVisible.value = true;
}
function onSearchKey(e: KeyboardEvent) {
  if (e.key !== 'Enter') return;
  const kw = keyword.value.trim().toLowerCase();
  if (!kw) return;
  if (state.value.mode === 'batch') {
    const exact = [...new Set(activeOrders.value.filter((o) => (o.batchNo || '').toLowerCase() === kw).map((o) => o.batchNo!))];
    if (exact.length === 1) {
      selectBatch(exact[0]);
      return;
    }
    if (suggestItems.value.length) selectSuggest(suggestItems.value[0]);
    return;
  }
  const exact = orders.value.find((o) => (o.orderNo || '').toLowerCase() === kw);
  if (exact) {
    selectOrder(exact.orderNo!);
    return;
  }
  if (suggestItems.value.length) selectSuggest(suggestItems.value[0]);
}
function hideSuggestLater() {
  setTimeout(() => (suggestVisible.value = false), 200);
}
function selectSuggest(it: { key: string; type: 'order' | 'batch' }) {
  if (it.type === 'batch') selectBatch(it.key);
  else selectOrder(it.key);
}
function selectOrder(id: string) {
  suggestVisible.value = false;
  state.value.lastOrderId = id;
  state.value.mode = 'order';
  state.value.orderId = id;
  state.value.batchNo = '';
  keyword.value = id;
  scrollToFocus();
}
function selectBatch(bn: string) {
  suggestVisible.value = false;
  state.value.mode = 'batch';
  state.value.batchNo = bn;
  state.value.orderId = '';
  keyword.value = bn;
  scrollToFocus();
}

// ===== 页签切换:来回切不丢内容(工单→批次自动带批次;批次→工单恢复最近工单) =====
function setMode(m: 'order' | 'batch') {
  if (m === state.value.mode) return;
  suggestVisible.value = false;
  keyword.value = '';
  if (m === 'order' && state.value.lastOrderId && findOrder(orders.value, state.value.lastOrderId)) {
    selectOrder(state.value.lastOrderId);
    return;
  }
  let carry = '';
  if (m === 'batch' && state.value.orderId) {
    const f = findOrder(orders.value, state.value.orderId);
    const bn = f?.batchNo;
    if (bn && activeOrders.value.some((o) => o.batchNo === bn)) carry = bn;
  }
  state.value.mode = m;
  state.value.orderId = '';
  state.value.batchNo = '';
  if (carry) {
    selectBatch(carry);
    return;
  }
  scrollToFocus();
}
function setDir(dir: 'up' | 'down') {
  state.value.dir = dir;
  scrollToFocus();
}
function backToList() {
  router.push(LIST_ROUTE);
}
function onEsc(e: KeyboardEvent) {
  if (e.key === 'Escape') backToList();
}

// ===== 工单详情弹窗(与列表页同款,只读) =====
const [registerDetailModal, { openModal }] = useModal();
function openDetail(order: WorkOrderItem) {
  openModal(true, { action: 'detail', record: order });
}

onMounted(async () => {
  document.addEventListener('keydown', onEsc);
  try {
    orders.value = (await getTraceOrders()) || [];
  } catch (e) {
    orders.value = [];
  }
  // 支持列表页「追溯」按钮带工单号进入(按工单号+向上追源)
  const q = router.currentRoute.value.query;
  const qOrder = typeof q.orderNo === 'string' ? q.orderNo : '';
  const qBatch = typeof q.batchNo === 'string' ? q.batchNo : '';
  if (qOrder && findOrder(orders.value, qOrder)) selectOrder(qOrder);
  else if (qBatch) selectBatch(qBatch);
});
onBeforeUnmount(() => {
  document.removeEventListener('keydown', onEsc);
});
</script>

<style lang="less" scoped>
/** 原型批次追溯页样式移植(Brewmaxx 覆盖层):深蓝 #003366、扁平小圆角 */
.trace-page {
  padding: 12px;
  background: #fff;
  border: 1px solid #ccc;
  border-radius: 4px;
}
.section {
  position: relative;
  display: flex;
  flex-direction: column;
  height: calc(100vh - 210px);
  min-height: 460px;
}
.section-title {
  font-size: 15px;
  font-weight: 700;
  color: #003366;
  margin-bottom: 10px;
  flex: none;
}
.trace-close {
  position: absolute;
  top: 0;
  right: 0;
  width: 28px;
  height: 28px;
  line-height: 26px;
  padding: 0;
  text-align: center;
  background: #fff;
  border: 1px solid #ccc;
  border-radius: 3px;
  color: #666;
  font-size: 14px;
  cursor: pointer;
}
.trace-close:hover {
  color: #003366;
  border-color: #003366;
  background: #f5f8fc;
}

/* 工具条 */
.trace-toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
  flex: none;
  flex-wrap: wrap;
}
.trace-mode,
.trace-dir {
  display: flex;
  gap: 2px;
}
.trace-dir {
  margin-left: auto;
}
.tm-tab,
.td-tab {
  padding: 6px 16px;
  font-size: 12px;
  font-family: Arial, sans-serif;
  background: #e0e0e0;
  color: #333;
  border: 1px solid #ccc;
  border-radius: 3px;
  cursor: pointer;
}
.tm-tab:hover,
.td-tab:hover {
  background: #e6f2ff;
  color: #003366;
}
.tm-tab.active,
.td-tab.active {
  background: #003366;
  color: #fff;
  border-color: #003366;
  font-weight: 600;
}

/* 检索框 + 联想下拉 */
.trace-search {
  position: relative;
  width: 340px;
}
.trace-search > svg {
  position: absolute;
  left: 9px;
  top: 50%;
  transform: translateY(-50%);
  width: 14px;
  height: 14px;
  color: #888;
  pointer-events: none;
  z-index: 21;
}
.trace-search input {
  width: 100%;
  padding: 6px 10px 6px 30px;
  border: 1px solid #ccc;
  border-radius: 3px;
  font-size: 12px;
  font-family: 'Consolas', 'Courier New', monospace;
  color: #222;
  background: #fff;
}
.trace-search input:focus {
  outline: none;
  border-color: #66afe9;
  box-shadow: 0 0 0 2px rgba(102, 175, 233, 0.25);
}
.trace-suggest {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  margin-top: 2px;
  background: #fff;
  border: 1px solid #ccc;
  border-radius: 3px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
  z-index: 20;
  max-height: 264px;
  overflow-y: auto;
}
.ts-item {
  padding: 7px 10px;
  cursor: pointer;
  border-bottom: 1px solid #eee;
}
.ts-item:last-child {
  border-bottom: none;
}
.ts-item:hover {
  background: #e6f2ff;
}
.ts-id {
  font-family: 'Consolas', 'Courier New', monospace;
  font-weight: 700;
  font-size: 12px;
  color: #003366;
  margin-right: 6px;
}
.ts-stage {
  font-size: 10px;
  font-weight: 600;
  color: #fff;
  background: #4a6580;
  border-radius: 2px;
  padding: 1px 5px;
}
.ts-info {
  display: block;
  font-size: 11px;
  color: #666;
  margin-top: 2px;
}
.ts-empty {
  padding: 10px;
  font-size: 12px;
  color: #888;
  text-align: center;
}

/* 图例 */
.trace-legend {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 11px;
  color: #555;
  margin-bottom: 8px;
  flex: none;
  flex-wrap: wrap;
}
.tl-item {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}
.tl-item i {
  width: 10px;
  height: 10px;
  border-radius: 2px;
  display: inline-block;
}

/* 画布 */
.trace-canvas-wrap {
  flex: 1;
  overflow: auto;
  border: 1px solid #ccc;
  background: #fafafa;
  border-radius: 3px;
}
.trace-empty {
  padding: 70px 0;
  text-align: center;
  color: #888;
  font-size: 13px;
}
.trace-canvas {
  position: relative;
  margin: 0 auto;
}
.trace-svg {
  position: absolute;
  left: 0;
  top: 0;
}
.tnode {
  position: absolute;
  width: 210px;
  height: 104px;
  background: #fff;
  border: 1px solid #ddd;
  border-left: 3px solid var(--sc, #4a6580);
  border-radius: 3px;
  padding: 8px 10px;
  cursor: pointer;
  overflow: hidden;
}
.tnode:hover {
  background: #e6f2ff;
  border-color: #66afe9;
}
.tnode.start {
  border: 2px solid #c88a2b;
  border-left-width: 4px;
  background: #fff7e6;
}
.tn-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 3px;
}
.tn-id {
  font-family: 'Consolas', 'Courier New', monospace;
  font-weight: 700;
  font-size: 12px;
  color: #003366;
}
.tn-stage {
  font-size: 10px;
  font-weight: 600;
  padding: 1px 6px;
  border-radius: 2px;
  color: #fff;
  background: var(--sc, #4a6580);
}
.tn-proc,
.tn-flow,
.tn-meta,
.tn-extra {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.tn-proc {
  font-size: 11px;
  color: #333;
}
.tn-flow {
  font-family: 'Consolas', 'Courier New', monospace;
  font-size: 11px;
  color: #222;
  margin: 2px 0;
}
/* 罐名加粗(槽车不是罐,不加粗) */
.tn-tank {
  font-weight: 700;
}
.tn-meta {
  font-size: 10px;
  color: #666;
}
.tn-extra {
  font-size: 10px;
  color: #2e7d32;
  margin-top: 2px;
}
</style>
