<template>
  <div class="energy-dashboard">
    <!-- 页头 + 周期切换 -->
    <div class="page-bar">
      <div>
        <h2>能源看板</h2>
        <div class="breadcrumb">能源管理 / 能源看板</div>
      </div>
      <div class="period-tabs">
        <a-button v-for="p in PERIODS" :key="p.value" :type="period === p.value ? 'primary' : 'default'" size="small" @click="switchPeriod(p.value)">{{ p.label }}</a-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stat-cards">
      <div class="stat-card" v-for="s in stats" :key="s.key" :style="{ borderLeftColor: s.color }">
        <div class="stat-icon">{{ s.icon }}</div>
        <div class="stat-body">
          <div class="stat-label">{{ s.label }}</div>
          <div class="stat-value">{{ fmt(s.value) }}<span class="stat-unit">{{ s.unit }}</span></div>
          <div class="stat-desc">{{ s.desc }}</div>
        </div>
      </div>
    </div>

    <!-- 能耗趋势 -->
    <a-card class="block" :bordered="false">
      <template #title>
        <div class="card-head">
          <span>能耗趋势</span>
          <div class="energy-tabs">
            <a-button v-for="et in ENERGY_TYPES" :key="et.key" :type="currentEnergy === et.key ? 'primary' : 'default'" size="small" @click="switchEnergy(et.key)">{{ et.icon }} {{ et.label }}</a-button>
          </div>
        </div>
      </template>
      <div ref="trendRef" class="chart-box" style="height: 320px"></div>
    </a-card>

    <!-- 车间能耗占比 -->
    <a-card class="block" title="车间能耗占比" :bordered="false">
      <div ref="ratioRef" class="chart-box" style="height: 280px"></div>
    </a-card>

    <!-- 分车间趋势对比 -->
    <a-card class="block" title="分车间趋势对比" :bordered="false">
      <div class="ws-grid">
        <div class="ws-card" v-for="(w, i) in workshops" :key="i">
          <div class="ws-head"><span class="ws-dot" :style="{ background: w.color }"></span>{{ w.name }}</div>
          <div class="energy-row" v-for="et in ENERGY_TYPES" :key="et.key">
            <span class="er-icon">{{ et.icon }}</span>
            <span class="er-label">{{ et.label }}</span>
            <span class="er-val">{{ fmt(w[et.key].total) }}<span class="er-unit">{{ w[et.key].unit }}</span></span>
            <div class="er-bar"><div class="fill" :style="{ width: w[et.key].pct + '%', background: et.color }"></div></div>
          </div>
          <div :ref="(el) => (sparkRefs[i] = el)" class="spark-box"></div>
        </div>
      </div>
    </a-card>

    <!-- 能耗异常提醒 -->
    <a-card class="block" title="能耗异常提醒" :bordered="false">
      <a-table :columns="alarmColumns" :dataSource="alarms" :pagination="false" size="small" rowKey="time">
        <template #bodyCell="{ column, record }">
          <template v-if="column.dataIndex === 'energyLabel'">
            {{ record.energyIcon }} {{ record.energyLabel }}
          </template>
          <template v-else-if="column.dataIndex === 'levelLabel'">
            <span class="level-pill" :style="levelStyle(record.levelKey)">{{ record.levelLabel }}</span>
          </template>
          <template v-else-if="column.dataIndex === 'statusLabel'">
            <span class="status-pill" :style="statusStyle(record.statusKey)">{{ record.statusLabel }}</span>
          </template>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script lang="ts" name="maotai-energy-dashboard" setup>
import { ref, onMounted, nextTick } from 'vue';
import echarts from '/@/utils/lib/echarts';
import { useECharts } from '/@/hooks/web/useECharts';
import { getDashboard } from './MaotaiEnergyDashboard.api';

const ENERGY_TYPES = [
  { key: 'electric', label: '电耗', icon: '⚡', color: '#6366F1' },
  { key: 'water', label: '水耗', icon: '💧', color: '#0EA5E9' },
  { key: 'air', label: '压缩空气', icon: '💨', color: '#F59E0B' },
];
const PERIODS = [
  { value: 'today', label: '今日' },
  { value: 'week', label: '本周' },
  { value: 'month', label: '本月' },
  { value: 'quarter', label: '本季度' },
  { value: 'year', label: '本年' },
];
function switchPeriod(v: string) {
  period.value = v;
  loadData();
}
function switchEnergy(k: string) {
  currentEnergy.value = k;
  renderTrend();
  renderSparkles();
}

const period = ref('month');
const currentEnergy = ref('electric');
const stats = ref<any[]>([]);
const trendLabels = ref<string[]>([]);
const trendSeries = ref<any>({});
const workshops = ref<any[]>([]);
const alarms = ref<any[]>([]);

const trendRef = ref<HTMLDivElement>();
const ratioRef = ref<HTMLDivElement>();
const sparkRefs = ref<any[]>([]);
const { setOptions: setTrend } = useECharts(trendRef);
const { setOptions: setRatio } = useECharts(ratioRef);

async function loadData() {
  const res: any = await getDashboard({ period: period.value });
  stats.value = res?.stats || [];
  trendLabels.value = res?.trend?.labels || [];
  trendSeries.value = res?.trend?.series || {};
  workshops.value = res?.workshops || [];
  alarms.value = res?.alarms || [];
  await nextTick();
  renderTrend();
  renderRatio();
  renderSparkles();
}

function renderTrend() {
  const s = trendSeries.value[currentEnergy.value];
  if (!s || !s.workshops) return;
  setTrend({
    tooltip: { trigger: 'axis' },
    legend: { data: s.workshops.map((w: any) => w.name), bottom: 0 },
    grid: { left: 55, right: 20, top: 20, bottom: 40 },
    xAxis: { type: 'category', data: trendLabels.value, boundaryGap: false },
    yAxis: { type: 'value' },
    series: s.workshops.map((w: any) => ({
      name: w.name,
      type: 'line',
      smooth: true,
      showSymbol: false,
      data: w.data,
      itemStyle: { color: w.color },
      lineStyle: { color: w.color, width: 2.5 },
      areaStyle: { color: w.color, opacity: 0.08 },
    })),
  });
}

function renderRatio() {
  const wsList = workshops.value;
  if (!wsList.length) return;
  setRatio({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    legend: { data: wsList.map((w) => w.name), bottom: 0 },
    grid: { left: 55, right: 20, top: 20, bottom: 40 },
    xAxis: { type: 'category', data: ['电耗', '水耗', '压缩空气'] },
    yAxis: { type: 'value' },
    series: wsList.map((w) => ({
      name: w.name,
      type: 'bar',
      barWidth: 18,
      itemStyle: { color: w.color, borderRadius: [4, 4, 0, 0] },
      data: [w.electric.total, w.water.total, w.air.total],
    })),
  });
}

// sparkline 运行时按 DOM ref 初始化(模板动态 ref)
const sparkChartList: any[] = [];
function renderSparkles() {
  const s = trendSeries.value[currentEnergy.value];
  if (!s || !s.workshops) return;
  // 销毁旧实例
  sparkChartList.forEach((c) => c && c.dispose && c.dispose());
  sparkChartList.length = 0;
  sparkRefs.value.forEach((el: any, i: number) => {
    if (!el) return;
    const inst = echarts.init(el);
    sparkChartList.push(inst);
    const w = s.workshops[i];
    inst.setOption({
      grid: { left: 0, right: 0, top: 4, bottom: 0 },
      xAxis: { type: 'category', show: false, data: trendLabels.value },
      yAxis: { show: false, type: 'value', scale: true },
      series: [
        {
          type: 'line',
          smooth: true,
          showSymbol: false,
          data: w.data,
          lineStyle: { color: w.color, width: 2 },
          areaStyle: { color: w.color, opacity: 0.15 },
        },
      ],
    });
  });
}

function fmt(v: any) {
  if (v === null || v === undefined) return '—';
  const n = Number(v);
  return n.toLocaleString('zh-CN', { maximumFractionDigits: 1 });
}

const alarmColumns = [
  { title: '时间', dataIndex: 'time', width: 150 },
  { title: '车间', dataIndex: 'workshop', width: 180 },
  { title: '能源类型', dataIndex: 'energyLabel', width: 120 },
  { title: '异常详情', dataIndex: 'detail' },
  { title: '级别', dataIndex: 'levelLabel', width: 80 },
  { title: '状态', dataIndex: 'statusLabel', width: 90 },
];

const LEVEL_STYLE: Record<string, any> = {
  critical: { color: '#fff', background: '#DC2626' },
  warning: { color: '#92400E', background: '#FDE68A' },
  info: { color: '#1E40AF', background: '#BFDBFE' },
};
const STATUS_STYLE: Record<string, any> = {
  alarm: { color: '#DC2626', background: '#FEF2F2' },
  online: { color: '#10B981', background: '#ECFDF5' },
};
function levelStyle(k: string) {
  return LEVEL_STYLE[k] || {};
}
function statusStyle(k: string) {
  return STATUS_STYLE[k] || {};
}

onMounted(() => {
  loadData();
});
</script>

<style lang="less" scoped>
.energy-dashboard { padding: 12px; }
.page-bar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px;
  h2 { margin: 0 0 4px; font-size: 20px; } .breadcrumb { font-size: 12px; color: #a8a29e; }
}
.stat-cards { display: grid; grid-template-columns: repeat(4, 1fr); gap: 12px; margin-bottom: 12px; }
.stat-card { display: flex; align-items: center; gap: 14px; padding: 16px 18px; background: #fff; border: 1px solid #f0f0f0; border-radius: 8px; border-left: 4px solid #f59e0b; }
.stat-icon { font-size: 28px; }
.stat-label { font-size: 12px; color: #a8a29e; font-weight: 600; }
.stat-value { font-size: 24px; font-weight: 800; line-height: 1.2; }
.stat-unit { font-size: 12px; font-weight: 400; color: #a8a29e; margin-left: 2px; }
.stat-desc { font-size: 11px; color: #a8a29e; margin-top: 2px; }

.block { margin-bottom: 16px; border-radius: 8px; }
.card-head { display: flex; align-items: center; justify-content: space-between; }
.period-tabs { display: flex; gap: 8px; }
.energy-tabs { display: flex; gap: 8px; }
.chart-box { width: 100%; }

.ws-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 12px; }
.ws-card { background: #fafafa; border: 1px solid #f0f0f0; border-radius: 6px; padding: 14px; }
.ws-head { display: flex; align-items: center; gap: 8px; font-weight: 600; padding-bottom: 10px; border-bottom: 1px solid #f0f0f0; margin-bottom: 8px; }
.ws-dot { width: 8px; height: 8px; border-radius: 50%; }
.energy-row { display: flex; align-items: center; gap: 6px; padding: 6px 0; font-size: 12px; }
.er-icon { font-size: 14px; }
.er-label { color: #666; width: 64px; }
.er-val { font-weight: 700; margin-left: auto; }
.er-unit { font-size: 10px; color: #a8a29e; margin-left: 2px; font-weight: 400; }
.er-bar { width: 60px; height: 4px; background: #e7e5e4; border-radius: 2px; overflow: hidden; }
.er-bar .fill { height: 100%; border-radius: 2px; }
.spark-box { width: 100%; height: 50px; margin-top: 6px; }

.level-pill, .status-pill { display: inline-block; padding: 2px 10px; border-radius: 12px; font-size: 12px; font-weight: 600; }
</style>
