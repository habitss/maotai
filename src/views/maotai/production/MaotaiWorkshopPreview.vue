<template>
  <div class="workshop-preview" :class="{ fullscreen: isFullscreen }">
    <div class="ws-tabs" v-show="!isFullscreen">
      <div v-for="(w, i) in list" :key="i" class="ws-tab" :class="{ active: activeIdx === i }" @click="switchWorkshop(i)">
        <span class="tab-dot" :style="{ background: activeIdx === i ? w.color : '#ddd' }"></span>{{ w.name }}
      </div>
    </div>
    <a-spin :spinning="loading">
      <!-- v-if=ready:静态骨架与实时数值同时首绘,不出现"先静态绿罐后覆盖"的中间态 -->
      <div class="scene-container" v-if="current && realtimeReady">
        <Workshop3D ref="workshop3dRef" :indicators="realtimeIndicators" :wsIndex="activeIdx" :wsColor="current.color" />
        <div class="cockpit-title" v-if="isFullscreen">{{ current.name }}</div>
        <!-- 全屏车间切换器 -->
        <div class="ws-switcher" v-if="isFullscreen">
          <div v-for="(w, i) in list" :key="i" class="ws-sw-tab" :class="{ active: activeIdx === i }" @click="switchWorkshop(i)">{{ w.name }}</div>
        </div>
        <div class="scene-badge"><span class="dot"></span>实时运行中</div>
        <!-- 全屏驾驶舱入口:悬浮画布右上,与左上"实时运行中"徽标对称 -->
        <!-- 全屏驾驶舱入口:样式与"实时运行中"徽标一致(固定色不随主题变) -->
        <div v-show="!isFullscreen" class="cockpit-btn" @click="openCockpit">全屏驾驶舱</div>
        <button v-if="isFullscreen" class="exit-btn" @click="toggleFullscreen">✕ 退出</button>
      </div>
    </a-spin>
  </div>
</template>

<script lang="ts" name="maotai-workshop-preview" setup>
import { ref, computed, onMounted, onBeforeUnmount, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { getWorkshopList } from './MaotaiWorkshopPreview.api';
import { getTankRealtime } from '../workshopCockpit/WorkshopCockpit.api';
import Workshop3D from './components/Workshop3D.vue';

const workshop3dRef = ref<InstanceType<typeof Workshop3D> | null>(null);

const route = useRoute();
const router = useRouter();
const list = ref<any[]>([]);
const activeIdx = ref(normalizeWorkshopIndex(route.query.workshopIndex));
const loading = ref(true); // 进页面即转圈,首轮实时数据到达才展示罐体
const isFullscreen = ref(false);
const current = computed(() => list.value[activeIdx.value]);

function normalizeWorkshopIndex(value: unknown) {
  const raw = Array.isArray(value) ? value[0] : value;
  const index = Number(raw);
  return Number.isInteger(index) && index >= 0 && index <= 3 ? index : 0;
}

async function loadData() {
  // loading 保持到首轮实时数据合并完成(pollOnce 内解除)——进页面直接见最终罐色,无"先绿后红"跳变
  try { list.value = (await getWorkshopList()) || []; } catch (e) { loading.value = false; }
}
function toggleFullscreen() { isFullscreen.value = !isFullscreen.value; }

// ===== 实时数据轮询(与驾驶舱同源同频:tankRealtime 10s,失败退避,页面隐藏暂停) =====
const realtimeRecords = ref<any[]>([]);
// 首轮实时数据是否已合并(与 loading 配合:未就绪不渲染 3D 场景)
const realtimeReady = ref(false);
function switchWorkshop(i: number) {
  if (activeIdx.value === i) return;
  activeIdx.value = i;
  realtimeReady.value = false;
  realtimeRecords.value = [];
  // 立即拉新车间的首轮数据(不能等下一轮 10s:在途旧车间请求会被守卫丢弃,期间新车间无数据导致场景空白)
  if (running && !document.hidden) {
    requestSeq++;
    if (pollTimer) clearTimeout(pollTimer);
    void pollOnce();
  }
}
let pollTimer: ReturnType<typeof setTimeout> | undefined;
let requestSeq = 0;
let running = false;
let resumeWhenVisible = false;
let consecutiveFailures = 0;
let currentInterval = 10000;
const pollInterval = 10000;
const maxBackoff = 60000;

function normalizeTankId(value: unknown) {
  return String(value || '').trim().toUpperCase().replace(/储罐$/, '');
}
function normalizeStatus(value: unknown) {
  const status = String(value || 'normal').toLowerCase();
  return ['normal', 'standby', 'warning', 'alarm'].includes(status) ? status : 'normal';
}
function formatOneDecimal(value: unknown) {
  const number = Number(value);
  return Number.isFinite(number) ? number.toFixed(1) : '-';
}
function formatRounded(value: unknown) {
  const number = Number(value);
  return Number.isFinite(number) ? String(Math.round(number)) : '-';
}
function setParam(params: any[], label: string, value: string, state: string) {
  if (value.startsWith('-')) return;
  const current = params.find((item) => item.label === label);
  if (current) {
    current.value = value;
    current.s = state;
  } else {
    params.push({ label, value, s: state });
  }
}

// 合并逻辑与驾驶舱 WorkshopTank3D.realtimeIndicators 完全同款,另加:罐顶标签/品牌标签覆盖为台账罐名
const realtimeIndicators = computed(() => {
  const recordMap = new Map(realtimeRecords.value.map((record) => [record.funlocCode || normalizeTankId(record.tankId), record]));
  const currentWs = list.value[activeIdx.value];
  return ((currentWs?.indicators as any[]) || []).map((source) => {
    const record = recordMap.get((source as any).funlocCode || normalizeTankId(source.title || source.tankId));
    if (!record) return source;
    const indicator = { ...source, params: (source.params || []).map((item: any) => ({ ...item })) };
    indicator.status = normalizeStatus(record.status || source.status);
    // 报警字段集合(中集报警点位属于哪列测点):只有命中字段的参数行标红,其余行保持正常色
    const alarmFields = new Set(((record.alarmList as any[]) || []).map((a: any) => a.field).filter(Boolean));
    const rowState = (field: string) => (alarmFields.has(field) ? 'alarm' : 'normal');
    if (record.levelPct !== undefined) indicator.liveValue = `${formatOneDecimal(record.levelPct)}%`;
    setParam(indicator.params, '液位', `${formatOneDecimal(record.levelPct)}%`, rowState('level'));
      setParam(indicator.params, '实际液位', record.volumeM3 === undefined ? '-' : `${formatRounded(record.volumeM3)}m³`, rowState('level'));
      setParam(indicator.params, '容量', record.capacityM3 === undefined ? '-' : `${formatRounded(record.capacityM3)}m³`, 'normal');
    setParam(indicator.params, '温度', `${formatOneDecimal(record.temperatureC)}°C`, rowState('temp'));
    setParam(indicator.params, '酒精度', record.alcoholPct === undefined ? '-' : `${formatOneDecimal(record.alcoholPct)}%vol`, rowState('alcohol'));
    setParam(indicator.params, '位置编码', record.funlocCode || '-', 'normal');
    // 罐顶标签显示品牌名(与驾驶舱一致,后端已按字典翻译);浮层标题仍为台账罐名
    indicator.brand = record.brand || record.tankId;
    indicator.realtime = record;
    return indicator;
  });
});

function scheduleNext() {
  if (pollTimer) clearTimeout(pollTimer);
  if (running && !document.hidden) pollTimer = setTimeout(pollOnce, currentInterval);
}
async function pollOnce() {
  if (!running || document.hidden) return;
  const seq = ++requestSeq;
  const workshopIndex = activeIdx.value;
  try {
    const response = await getTankRealtime(workshopIndex);
    if (!running || seq !== requestSeq || workshopIndex !== activeIdx.value) return;
    realtimeRecords.value = Array.isArray(response?.list) ? response.list : [];
    consecutiveFailures = 0;
    currentInterval = pollInterval;
    loading.value = false;
    realtimeReady.value = true; // 首轮就绪:骨架+数值一次性首绘
  } catch {
    if (!running || seq !== requestSeq) return;
    consecutiveFailures++;
    currentInterval = Math.min(pollInterval * 2 ** consecutiveFailures, maxBackoff);
    if (loading.value) { loading.value = false; realtimeReady.value = true; } // 失败兜底渲染静态,避免白屏
  }
  scheduleNext();
}
function startPolling() {
  stopPolling();
  running = true;
  consecutiveFailures = 0;
  currentInterval = pollInterval;
  void pollOnce();
}
function stopPolling() {
  running = false;
  requestSeq++;
  if (pollTimer) clearTimeout(pollTimer);
  pollTimer = undefined;
}
function onVisibilityChange() {
  if (document.hidden) {
    resumeWhenVisible = running;
    if (running) stopPolling();
  } else if (resumeWhenVisible) {
    resumeWhenVisible = false;
    startPolling();
  }
}

function openCockpit() {
  router.push({ path: '/maotai/workshopCockpit', query: { workshopIndex: activeIdx.value } });
}
watch(() => route.query.workshopIndex, (value) => switchWorkshop(normalizeWorkshopIndex(value)));
onMounted(() => {
  loadData().then(() => startPolling());
  document.addEventListener('visibilitychange', onVisibilityChange);
});
onBeforeUnmount(() => {
  stopPolling();
  document.removeEventListener('visibilitychange', onVisibilityChange);
});
</script>

<style lang="less" scoped>
.workshop-preview { padding: 12px; }
.workshop-preview.fullscreen { padding: 0; }
.ws-tabs { display:flex; background:#fff; border-radius:4px; overflow:hidden; margin-bottom:16px; border:1px solid #e7e5e4; }
.ws-tab { flex:1; padding:14px; display:flex; align-items:center; justify-content:center; gap:8px; cursor:pointer; border-bottom:3px solid transparent; font-size:14px; font-weight:600; color:#666; }
.ws-tab.active { background:#fffbeb; border-bottom-color:#d97706; color:#d97706; }
.tab-dot { width:8px; height:8px; border-radius:50%; }
.scene-container { position:relative; width:100%; height:78vh; min-height:460px; border-radius:4px; overflow:hidden; background:#0a0e1a; }
.cockpit-btn { position:absolute; top:14px; right:18px; background:rgba(0,0,0,.5); color:#fff; padding:5px 12px; border-radius:4px; font-size:11px; z-index:5; cursor:pointer; }
.workshop-preview.fullscreen .scene-container { height:100vh; border-radius:0; }
.cockpit-title { position:absolute; top:16px; left:50%; transform:translateX(-50%); color:#cfe4ff; font-size:22px; font-weight:700; letter-spacing:6px; text-shadow:0 0 16px rgba(80,180,255,.5); z-index:5; }
.scene-badge { position:absolute; top:14px; left:18px; background:rgba(0,0,0,.5); color:#fff; padding:5px 12px; border-radius:4px; font-size:11px; display:flex; align-items:center; gap:6px; z-index:5; }
.scene-badge .dot { width:7px; height:7px; border-radius:50%; background:#10b981; animation:blink 2s infinite; }
@keyframes blink { 0%,100%{opacity:1} 50%{opacity:.3} }
.exit-btn { position:absolute; top:14px; right:18px; background:rgba(0,0,0,.5); color:#fff; border:1px solid rgba(255,255,255,.2); border-radius:4px; padding:6px 14px; cursor:pointer; font-size:12px; z-index:5; }
.ws-switcher { position:absolute; top:50px; left:50%; transform:translateX(-50%); z-index:15; display:flex; background:rgba(8,24,48,.82); border-radius:8px; overflow:hidden; border:1px solid rgba(60,130,220,.15); }
.ws-sw-tab { padding:12px 32px; font-size:16px; font-weight:500; color:rgba(140,180,230,.4); cursor:pointer; transition:all .3s; }
.ws-sw-tab:hover { color:rgba(160,200,255,.7); background:rgba(40,80,150,.1); }
.ws-sw-tab.active { color:#C8E0FF; font-weight:700; background:rgba(25,65,140,.15); }
</style>
