<template>
  <div class="home-dash">
    <!-- 顶栏:标题 + 周期切换 + 导出(占位) + 实时时钟 -->
    <div class="page-header">
      <div>
        <h1>首页看板</h1>
        <div class="sub-title">设备运行 · 能耗介质 · 工艺报警总览</div>
      </div>
      <div class="ph-right">
        <div class="sub-nav">
          <a :class="{ active: curRange === 'today' }" @click="setRange('today')">当天</a>
          <a :class="{ active: curRange === 'week' }" @click="setRange('week')">当周</a>
          <a :class="{ active: curRange === 'month' }" @click="setRange('month')">当月</a>
        </div>
        <button class="btn-export" @click="onExport">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path><polyline points="7 10 12 15 17 10"></polyline><line x1="12" y1="15" x2="12" y2="3"></line></svg>
          导出数据
        </button>
        <div class="ph-clock"><span>{{ liveDate }}</span>&ensp;<b>{{ liveTime }}</b></div>
      </div>
    </div>

    <!-- ① 置顶 5 张 KPI 卡 -->
    <div class="stat-cards">
      <div class="kpi-card" v-for="k in kpis" :key="k.label">
        <div class="kc-label">{{ k.label }}</div>
        <div class="kc-value">{{ k.value }}<span v-if="k.unit" class="kc-unit">{{ k.unit }}</span></div>
      </div>
    </div>

    <!-- ② 能耗趋势三卡 + 车间能耗综合对比(静态数,待计量数据源接入) -->
    <div class="grid-a">
      <div class="card">
        <div class="card-header"><h3>综合电耗</h3><span class="hd-unit">kWh</span></div>
        <div class="card-body">
          <div class="chart-h" ref="energyTrendRef"></div>
          <div class="duo-legend">
            <span v-for="w in wsEn" :key="w.name" class="dl-row"><span class="dot" :style="{ background: w.color }"></span>{{ w.name }}</span>
          </div>
        </div>
      </div>
      <div class="card">
        <div class="card-header"><h3>综合水耗</h3><span class="hd-unit">m³</span></div>
        <div class="card-body">
          <div class="chart-h" ref="waterTrendRef"></div>
          <div class="duo-legend">
            <span v-for="w in wsEn" :key="w.name" class="dl-row"><span class="dot" :style="{ background: w.color }"></span>{{ w.name }}</span>
          </div>
        </div>
      </div>
      <div class="card">
        <div class="card-header"><h3>综合压缩空气</h3><span class="hd-unit">m³</span></div>
        <div class="card-body">
          <div class="chart-h" ref="airTrendRef"></div>
          <div class="duo-legend">
            <span v-for="w in wsEn" :key="w.name" class="dl-row"><span class="dot" :style="{ background: w.color }"></span>{{ w.name }}</span>
          </div>
        </div>
      </div>
      <div class="card">
        <div class="card-header"><h3>车间能耗综合对比</h3></div>
        <div class="card-body radar-body">
          <div class="radar-h" ref="energyRadarRef"></div>
          <div class="rp-legend">
            <span v-for="(w, i) in wsProfile" :key="w.name" class="rp-item" :class="{ active: selectedProfile === i }"
              :title="selectedProfile === i ? '点击恢复全部车间' : '点击只看该车间与其基准能耗'"
              @click="toggleProfile(i)"><span class="dot" :style="{ background: w.color }"></span>{{ w.name }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- ③ 状态 / 保养 / 报警概览(真实数据) -->
    <div class="grid-b">
      <div class="card">
        <div class="card-header"><h3>设备状态分布</h3></div>
        <div class="dn-flex">
          <div class="dn-box">
            <div class="dn-chart" ref="statusDonutRef"></div>
            <div class="dn-center"><div class="dc-val">{{ statusTotal }}</div><div class="dc-label">设备总数·台</div></div>
          </div>
          <div class="dn-legend">
            <div class="dl-row" v-for="s in statusLegend" :key="s.name">
              <span class="dot" :style="{ background: s.color }"></span>
              <span class="dl-name">{{ s.name }}</span>
              <span class="dl-val">{{ s.value }}<i>{{ s.unit }} · {{ s.pct }}%</i></span>
            </div>
          </div>
        </div>
      </div>
      <div class="card">
        <div class="card-header"><h3>保养执行</h3></div>
        <div class="dn-flex">
          <div class="dn-box">
            <div class="dn-chart" ref="mtDonutRef"></div>
            <div class="dn-center"><div class="dc-val">{{ maintTotal }}</div><div class="dc-label">{{ rangeLabel }}计划·项</div></div>
          </div>
          <div class="dn-legend">
            <div class="dl-row" v-for="s in maintLegend" :key="s.name">
              <span class="dot" :style="{ background: s.color }"></span>
              <span class="dl-name">{{ s.name }}</span>
              <span class="dl-val">{{ s.value }}<i>{{ s.unit }} · {{ s.pct }}%</i></span>
            </div>
          </div>
        </div>
      </div>
      <div class="card">
        <div class="card-header"><h3>报警概览</h3></div>
        <div class="dn-flex">
          <div class="dn-box">
            <div class="dn-chart" ref="alarmDonutRef"></div>
            <div class="dn-center"><div class="dc-val">{{ alarmTotal }}</div><div class="dc-label">{{ rangeLabel }}报警·条</div></div>
          </div>
          <div class="dn-legend">
            <div class="dl-row" v-for="s in alarmLegend" :key="s.name">
              <span class="dot" :style="{ background: s.color }"></span>
              <span class="dl-name">{{ s.name }}</span>
              <span class="dl-val">{{ s.value }}<i>{{ s.unit }} · {{ s.pct }}%</i></span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup name="maotai-home-dashboard">
  import { ref, computed, onMounted, onBeforeUnmount, nextTick } from 'vue';
  import * as echarts from 'echarts';
  import { genHourLabels, genDays7Labels, genDays30Labels, genHourData, genWeekData, genMonthData } from './energyMock';
  import * as XLSX from 'xlsx';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { fetchLedgerRows, fetchMaintRows, fetchAlarmRows, type MaintRow, type LedgerRow } from './HomeDashboard.api';
  import type { AlarmEventVO } from '../alarm/AlarmCenter.api';

  const { createMessage: msg } = useMessage();

  // ============ 周期 ============
  type Range = 'today' | 'week' | 'month';
  const curRange = ref<Range>('today'); // 默认当天(用户定稿)
  const rangeLabel = computed(() => ({ today: '当天', week: '当周', month: '当月' }[curRange.value]));
  function setRange(r: Range) {
    curRange.value = r;
    renderAll();
  }

  // 雷达图车间筛选:选中后只显示该车间的能耗指数与其专属基准能耗(黄色虚线),再点一次恢复全部
  const selectedProfile = ref<number | null>(null);
  function toggleProfile(i: number) {
    selectedProfile.value = selectedProfile.value === i ? null : i;
    renderRadar();
  }

  // ============ 时钟 ============
  const liveDate = ref('----');
  const liveTime = ref('--:--:--');
  let clockTimer: ReturnType<typeof setInterval> | undefined;
  function tick() {
    const d = new Date();
    const p = (n: number) => (n < 10 ? '0' : '') + n;
    liveTime.value = `${p(d.getHours())}:${p(d.getMinutes())}:${p(d.getSeconds())}`;
    liveDate.value = `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}`;
  }

  // ============ 能耗演示数据段(生成逻辑在 ./energyMock,计量数据源接入后整体替换) ============
  // 四车间能耗计量口径(WS05 包装厂房独立计量,高位酒罐区 WS04 无计量不计入)
  const wsEn = [
    { name: '中转酒罐车间', color: '#3B7BFF' },
    { name: '系列酒勾调厂', color: '#2FA10E' },
    { name: '成品酒罐房', color: '#722ED1' },
    { name: '包装厂房', color: '#DB2777' },
  ];
  const HOURS = genHourLabels();
  const DAYS7 = genDays7Labels();
  const DAYS30 = genDays30Labels();
  const hourData = genHourData();
  const weekData = genWeekData();
  const monthData = genMonthData();
  const hourE = hourData.e, hourW = hourData.w, hourA = hourData.a;
  const weekE = weekData.e, weekW = weekData.w, weekA = weekData.a;
  const monthE = monthData.e, monthW = monthData.w, monthA = monthData.a;

  const RANGES = computed(() => ({
    today: { label: '当天', e: hourE, w: hourW, a: hourA, x: HOURS, interval: 3 },
    week: { label: '当周', e: weekE, w: weekW, a: weekA, x: DAYS7, interval: 0 },
    month: { label: '当月', e: monthE, w: monthW, a: monthA, x: DAYS30, interval: 3 },
  })[curRange.value]);
  // 雷达「车间能耗综合对比」:五维指标(相对基准 100 的百分比) + 各车间专属基准
  const WS_PROFILE_DIMS = ['电', '水', '压缩空气', '待机电耗', '停机电耗'];
  const wsProfile = wsEn.map((w) => ({ name: w.name, color: w.color, fill: w.color + '26' }));
  const PROFILE_BY_RANGE = {
    today: {
      short: '当天', full: '当天能耗基准',
      idx: [[97.2, 99.5, 94.1, 101.3, 98.6], [102.8, 94.2, 99.7, 96.4, 103.1], [93.5, 101.8, 96.9, 99.2, 97.4], [99.6, 96.7, 102.3, 94.8, 101.9]],
      base: [[95.0, 98.0, 93.0, 100.0, 99.0], [97.0, 101.0, 95.0, 102.0, 104.0], [93.0, 99.0, 95.0, 103.0, 97.0], [96.0, 98.0, 92.0, 99.0, 101.0]],
    },
    week: {
      short: '当周', full: '当周能耗基准',
      idx: [[101.5, 96.8, 99.2, 94.7, 102.6], [96.4, 103.2, 94.8, 99.9, 97.2], [99.8, 95.4, 97.6, 102.1, 96.5], [94.2, 98.9, 100.4, 97.3, 99.8]],
      base: [[94.0, 96.0, 92.0, 99.0, 98.0], [96.0, 100.0, 94.0, 101.0, 103.0], [95.0, 98.0, 93.0, 102.0, 96.0], [97.0, 99.0, 95.0, 100.0, 102.0]],
    },
    month: {
      short: '当月', full: '当月能耗基准',
      idx: [[98.3, 97.6, 95.8, 100.2, 99.1], [100.7, 95.9, 98.4, 97.6, 102.2], [96.1, 99.3, 94.2, 98.8, 100.6], [97.4, 101.2, 96.7, 99.5, 98.3]],
      base: [[95.0, 97.0, 93.0, 100.0, 98.0], [96.0, 99.0, 95.0, 100.0, 102.0], [94.0, 98.0, 94.0, 101.0, 97.0], [96.0, 99.0, 94.0, 99.0, 100.0]],
    },
  };
  // ============ 能耗静态数结束 ============

  // ============ 真实数据:设备台账(在线率+状态分布) ============
  const ledgerRows = ref<LedgerRow[]>([]);
  const STATUS_COLOR: Record<number, string> = { 1: '#059669', 2: '#A8A29E', 3: '#D97706', 4: '#DC2626' };
  const STATUS_NAME: Record<number, string> = { 1: '在线', 2: '停机', 3: '维修', 4: '报警' };
  const statusCounts = computed(() => {
    const c: Record<number, number> = { 1: 0, 2: 0, 3: 0, 4: 0 };
    ledgerRows.value.forEach((r) => {
      if (r.runStatus && c[r.runStatus] !== undefined) c[r.runStatus] += 1;
    });
    return c;
  });
  const statusTotal = computed(() => ledgerRows.value.length);
  const onlineRate = computed(() => (statusTotal.value ? ((statusCounts.value[1] / statusTotal.value) * 100).toFixed(1) : '0'));
  const statusLegend = computed(() => {
    const t = statusTotal.value || 1;
    return [1, 2, 3, 4].map((k) => ({
      name: STATUS_NAME[k], color: STATUS_COLOR[k], value: statusCounts.value[k], unit: '台',
      pct: ((statusCounts.value[k] / t) * 100).toFixed(1),
    }));
  });

  // ============ 真实数据:保养工单(保养执行) ============
  const maintRows = ref<MaintRow[]>([]);
  /** 周期时间窗:当天=今天;当周=近7天(含今天);当月=近30天(含今天) */
  function inRange(dateStr?: string): boolean {
    if (!dateStr) return false;
    const d = new Date(dateStr.replace(/-/g, '/'));
    if (isNaN(d.getTime())) return false;
    const now = new Date();
    const today = new Date(now.getFullYear(), now.getMonth(), now.getDate()).getTime();
    const days = curRange.value === 'today' ? 1 : curRange.value === 'week' ? 7 : 30;
    const t = d.getTime();
    return t >= today - (days - 1) * 86400000 && t < today + 86400000;
  }
  const maintCounts = computed(() => {
    const c = { completed: 0, progress: 0, pending: 0, overdue: 0 };
    maintRows.value.forEach((r) => {
      if (!r.status) return;
      // 已完成按完成日期归期,其余按计划日期归期
      const dayHit = r.status === 'completed' ? inRange(r.completeDate) || inRange(r.planDate) : inRange(r.planDate);
      if (!dayHit) return;
      if (c[r.status as keyof typeof c] !== undefined) c[r.status as keyof typeof c] += 1;
    });
    return c;
  });
  const maintTotal = computed(() => Object.values(maintCounts.value).reduce((a, b) => a + b, 0));
  const maintLegend = computed(() => {
    const t = maintTotal.value || 1;
    const defs = [
      { key: 'completed', name: '已完成', color: '#059669' },
      { key: 'progress', name: '进行中', color: '#3B7BFF' },
      { key: 'pending', name: '待执行', color: '#98A2B3' },
      { key: 'overdue', name: '逾期', color: '#DC2626' },
    ] as const;
    return defs.map((d) => ({
      name: d.name, color: d.color, value: maintCounts.value[d.key], unit: '项',
      pct: ((maintCounts.value[d.key] / t) * 100).toFixed(1),
    }));
  });

  // ============ 真实数据:工艺报警(报警概览) ============
  const alarmRows = ref<AlarmEventVO[]>([]);
  const alarmCounts = computed(() => {
    const c = { critical: 0, warning: 0, info: 0 };
    alarmRows.value.forEach((r) => {
      if (!inRange(r.alarmTime)) return;
      if (r.alarmLevel === 3) c.critical += 1;
      else if (r.alarmLevel === 2) c.warning += 1;
      else c.info += 1;
    });
    return c;
  });
  const alarmTotal = computed(() => Object.values(alarmCounts.value).reduce((a, b) => a + b, 0));
  const alarmLegend = computed(() => {
    const t = alarmTotal.value || 1;
    const defs = [
      { key: 'critical', name: '紧急报警', color: '#DC2626' },
      { key: 'warning', name: '一般告警', color: '#D97706' },
      { key: 'info', name: '提示', color: '#0284C7' },
    ] as const;
    return defs.map((d) => ({
      name: d.name, color: d.color, value: alarmCounts.value[d.key], unit: '条',
      pct: ((alarmCounts.value[d.key] / t) * 100).toFixed(1),
    }));
  });

  // ============ KPI 五卡(真实+静态混合) ============
  const kpis = computed(() => {
    const R = RANGES.value;
    const eSum = R.e.flat().reduce((a, b) => a + b, 0);
    const wSum = R.w.flat().reduce((a, b) => a + b, 0);
    const aSum = R.a.flat().reduce((a, b) => a + b, 0);
    const fmt = (n: number) => (n >= 1000 ? Math.round(n).toLocaleString('en-US') : n);
    return [
      { v: onlineRate.value, u: '%', l: '设备在线率' },
      { v: fmt(Math.round(eSum)), u: 'kWh', l: `${R.label}总电耗` },
      { v: fmt(+wSum.toFixed(1)), u: 'm³', l: `${R.label}总水耗` },
      { v: fmt(Math.round(aSum)), u: 'm³', l: `${R.label}总压缩空气消耗` },
      { v: alarmTotal.value, u: '条', l: '总报警数' },
    ].map((k) => ({ label: k.l, value: k.v, unit: k.u }));
  });

  // ============ ECharts 实例与渲染 ============
  const energyTrendRef = ref<HTMLElement>();
  const waterTrendRef = ref<HTMLElement>();
  const airTrendRef = ref<HTMLElement>();
  const energyRadarRef = ref<HTMLElement>();
  const statusDonutRef = ref<HTMLElement>();
  const mtDonutRef = ref<HTMLElement>();
  const alarmDonutRef = ref<HTMLElement>();
  const charts: Record<string, echarts.ECharts | undefined> = {};
  const TT = { backgroundColor: '#fff', borderColor: '#D6D3D1', textStyle: { color: '#1C1917', fontSize: 12 } };

  function ensureChart(key: string, el?: HTMLElement) {
    if (!el) return undefined;
    if (!charts[key]) charts[key] = echarts.init(el);
    return charts[key];
  }

  function renderTrend(key: string, el: HTMLElement | undefined, data: number[][], unit: string) {
    const chart = ensureChart(key, el);
    if (!chart) return;
    const R = RANGES.value;
    // 折线图:4 车间各一条线,平滑曲线+小圆点,悬停看数值(tooltip 列出全部车间)
    const series = wsEn.map((w, i) => ({
      name: w.name, type: 'line' as const, smooth: true, symbol: 'circle', symbolSize: 4,
      itemStyle: { color: w.color, borderColor: '#fff', borderWidth: 1 },
      lineStyle: { width: 2 },
      emphasis: { focus: 'series' as const },
      data: data[i],
    }));
    chart.setOption({
      tooltip: { trigger: 'axis', axisPointer: { type: 'line', lineStyle: { color: '#D6D3D1' } }, ...TT, valueFormatter: (v: any) => v + ' ' + unit },
      grid: { left: 46, right: 12, top: 12, bottom: 24 },
      xAxis: { type: 'category', boundaryGap: false, data: R.x, axisLine: { lineStyle: { color: '#D6D3D1' } }, axisTick: { show: false }, axisLabel: { color: '#A8A29E', fontSize: 11, interval: R.interval } },
      yAxis: { type: 'value', min: 0, axisLabel: { formatter: (v: number) => (v >= 1000 ? v / 1000 + 'k' : v), color: '#A8A29E', fontSize: 11 }, splitLine: { lineStyle: { color: '#EFEEEC' } } },
      series,
    }, true);
  }

  function renderRadar() {
    const chart = ensureChart('radar', energyRadarRef.value);
    if (!chart) return;
    const P = PROFILE_BY_RANGE[curRange.value];
    const sel = selectedProfile.value;
    // 选中车间:只画该车间能耗 + 其专属基准(黄色虚线);未选中:全部车间 + 公共基准环(=100)
    const baseline = (i: number) => ({
      name: sel === null ? P.short : `${P.short}·${wsProfile[i].name}`,
      value: sel === null ? WS_PROFILE_DIMS.map(() => 100) : P.base[i],
      symbol: 'none', itemStyle: { color: '#F59E0B' }, lineStyle: { width: 1.5, type: 'dashed' },
      areaStyle: sel === null ? undefined : { color: 'rgba(245,158,11,.05)' },
    });
    const seriesData = sel === null
      ? [baseline(0), ...wsProfile.map((w, i) => ({
          name: w.name, value: P.idx[i], symbol: 'circle', symbolSize: 5,
          itemStyle: { color: w.color, borderColor: '#fff', borderWidth: 1.5 },
          lineStyle: { width: 2 }, areaStyle: { color: w.fill },
        }))]
      : [baseline(sel), {
          name: wsProfile[sel].name, value: P.idx[sel], symbol: 'circle', symbolSize: 6,
          itemStyle: { color: wsProfile[sel].color, borderColor: '#fff', borderWidth: 1.5 },
          lineStyle: { width: 2.5 }, areaStyle: { color: wsProfile[sel].fill },
        }];
    chart.setOption({
      tooltip: { trigger: 'item', ...TT, confine: true,
        formatter: (p: any) => {
          const isBase = typeof p.name === 'string' && p.name.startsWith(P.short);
          if (isBase) {
            return `<b>${p.name}（黄色虚线）</b><br>` + WS_PROFILE_DIMS.map((d, i) => `${d}：<b>${Number(p.value[i]).toFixed(1)}</b>`).join('<br>');
          }
          const baseVals = sel === null ? WS_PROFILE_DIMS.map(() => 100) : P.base[sel];
          return `<b>${p.name}</b> <span style="color:#A8A29E">vs ${P.full}</span><br>` + WS_PROFILE_DIMS.map((d, i) => {
            const v = p.value[i], dv = v - baseVals[i];
            return `${d}：<b>${v.toFixed(1)}</b>　<span style="color:${dv >= 0 ? '#B45309' : '#78716C'}">${dv >= 0 ? '超' : '省'} ${Math.abs(dv).toFixed(1)}</span>`;
          }).join('<br>');
        } },
      radar: {
        center: ['50%', '50%'], radius: '68%', splitNumber: 5,
        indicator: WS_PROFILE_DIMS.map((d) => ({ name: d, min: 85, max: 110 })),
        axisName: { color: '#57534E', fontSize: 11, fontWeight: 600 },
        axisNameGap: 9,
        axisLine: { lineStyle: { color: '#E7E5E4' } },
        splitLine: { lineStyle: { color: '#E7E5E4' } },
        splitArea: { show: false },
      },
      series: [{ type: 'radar', data: seriesData }],
    }, true);
  }

  function donut(key: string, el: HTMLElement | undefined, data: { name: string; value: number; itemStyle: { color: string } }[], unit: string) {
    const chart = ensureChart(key, el);
    if (!chart) return;
    const total = data.reduce((s, d) => s + d.value, 0);
    chart.setOption({
      tooltip: { trigger: 'item', ...TT, formatter: (p: any) => `${p.name}: <b>${p.value}</b>${unit} (${total ? (p.value / total * 100).toFixed(1) : 0}%)` },
      series: [{ type: 'pie', radius: ['56%', '74%'], center: ['50%', '45%'], startAngle: 90,
        itemStyle: { borderColor: '#fff', borderWidth: 2 },
        label: { show: false }, labelLine: { show: false }, emphasis: { scaleSize: 3 }, data }],
    });
  }

  function renderAll() {
    const R = RANGES.value;
    renderTrend('energy', energyTrendRef.value, R.e, 'kWh');
    renderTrend('water', waterTrendRef.value, R.w, 'm³');
    renderTrend('air', airTrendRef.value, R.a, 'm³');
    renderRadar();
    donut('status', statusDonutRef.value, statusLegend.value.map((s) => ({ name: s.name, value: s.value, itemStyle: { color: s.color } })), '台');
    donut('mt', mtDonutRef.value, maintLegend.value.map((s) => ({ name: s.name, value: s.value, itemStyle: { color: s.color } })), '项');
    donut('alarm', alarmDonutRef.value, alarmLegend.value.map((s) => ({ name: s.name, value: s.value, itemStyle: { color: s.color } })), '条');
  }

  // ============ 数据加载 + 5 分钟轮询 ============
  const POLL = 5 * 60 * 1000;
  let pollTimer: ReturnType<typeof setTimeout> | undefined;
  async function loadReal() {
    const [l, m, a] = await Promise.all([
      fetchLedgerRows().catch(() => null),
      fetchMaintRows().catch(() => null),
      fetchAlarmRows().catch(() => null),
    ]);
    if (l?.records) ledgerRows.value = l.records;
    if (m?.records) maintRows.value = m.records;
    if (a?.records) alarmRows.value = a.records;
    await nextTick();
    renderAll();
  }
  function schedulePoll() {
    if (pollTimer) clearTimeout(pollTimer);
    pollTimer = setTimeout(async () => { await loadReal(); schedulePoll(); }, POLL);
  }

  function onExport() {
    // 导出当前周期看板数据:KPI+能耗(趋势/车间对比)+三张分布概览+三源明细(xlsx 多 sheet)
    const MAINT_STATUS_TEXT: Record<string, string> = { pending: '待执行', progress: '进行中', completed: '已完成', overdue: '逾期' };
    const ALARM_LEVEL_TEXT: Record<number, string> = { 3: '紧急报警', 2: '一般告警', 1: '提示' };
    // 数值单元格统一去千分位逗号,写入原生数字(Excel 可直接计算)
    const num = (v: any) => {
      const n = Number(String(v ?? '').replace(/,/g, '').replace('%', ''));
      return isNaN(n) ? v : n;
    };
    const pad = (n: number) => String(n).padStart(2, '0');
    const now = new Date();
    const stamp = `${now.getFullYear()}${pad(now.getMonth() + 1)}${pad(now.getDate())}_${pad(now.getHours())}${pad(now.getMinutes())}`;
    const wb = XLSX.utils.book_new();
    const aoa = (rows: any[][], widths: number[]) => {
      const ws = XLSX.utils.aoa_to_sheet(rows);
      ws['!cols'] = widths.map((w) => ({ wch: w }));
      return ws;
    };
    try {
      XLSX.utils.book_append_sheet(wb, aoa([
        ['周期', rangeLabel.value], ['导出时间', now.toLocaleString('zh-CN')], [],
        ['指标', '数值', '单位'],
        ...kpis.value.map((k) => [k.label, num(k.value), k.unit]),
      ], [24, 14, 10]), 'KPI概览');
      // 能耗趋势明细(当前周期):时间 × 4 车间 + 合计
      const R = RANGES.value;
      const energySheet = (title: string, data: number[][], unit: string) => aoa([
        [`时间(${R.label})`, ...wsEn.map((w) => `${w.name}(${unit})`), `合计(${unit})`],
        ...R.x.map((t: string, i: number) => [t, ...data.map((row) => num(row[i])), num(data.reduce((s, row) => s + row[i], 0))]),
      ], [12, ...wsEn.map(() => 14), 12]);
      XLSX.utils.book_append_sheet(wb, energySheet('综合电耗', R.e, 'kWh'), '综合电耗');
      XLSX.utils.book_append_sheet(wb, energySheet('综合水耗', R.w, 'm³'), '综合水耗');
      XLSX.utils.book_append_sheet(wb, energySheet('综合压缩空气', R.a, 'm³'), '综合压缩空气');
      // 车间能耗对比(雷达数据):各车间五维指数 + 专属基准
      const P = PROFILE_BY_RANGE[curRange.value];
      XLSX.utils.book_append_sheet(wb, aoa([
        [`车间(vs ${P.full},基准=各车间专属)`],
        ['车间', ...WS_PROFILE_DIMS, ...WS_PROFILE_DIMS.map((d) => `基准·${d}`)],
        ...wsProfile.map((w, i) => [w.name, ...P.idx[i].map(num), ...P.base[i].map(num)]),
      ], [16, ...WS_PROFILE_DIMS.map(() => 10), ...WS_PROFILE_DIMS.map(() => 12)]), '车间能耗对比');
      XLSX.utils.book_append_sheet(wb, aoa([
        ['状态', '数量(台)', '占比(%)'],
        ...statusLegend.value.map((s) => [s.name, num(s.value), num(s.pct)]),
        ['合计', num(statusTotal.value), 100], ['设备在线率(%)', num(onlineRate.value), ''],
      ], [16, 12, 12]), '设备状态分布');
      XLSX.utils.book_append_sheet(wb, aoa([
        ['状态', '数量(项)', '占比(%)'],
        ...maintLegend.value.map((s) => [s.name, num(s.value), num(s.pct)]),
        ['合计', num(maintTotal.value), 100],
      ], [16, 12, 12]), '保养执行');
      XLSX.utils.book_append_sheet(wb, aoa([
        ['级别', '数量(条)', '占比(%)'],
        ...alarmLegend.value.map((s) => [s.name, num(s.value), num(s.pct)]),
        ['合计', num(alarmTotal.value), 100],
      ], [16, 12, 12]), '报警概览');
      XLSX.utils.book_append_sheet(wb, aoa([
        ['功能位置编码', '设备名称', '所属车间', '设备型号', '制造商', '安装日期', '运行状态', '最近保养'],
        ...ledgerRows.value.map((r: any) => [r.funlocCode, r.equipName, r.workshopCode_dictText, r.equipModel, r.manufacturer, r.installDate, STATUS_NAME[r.runStatus] || r.runStatus, r.lastMaintDate]),
      ], [16, 18, 14, 14, 16, 12, 10, 12]), '设备台账明细');
      XLSX.utils.book_append_sheet(wb, aoa([
        ['工单号', '设备名称', '保养类型', '计划日期', '完成日期', '执行人', '状态'],
        ...maintRows.value.map((r: any) => [r.orderNo, r.equipmentName, r.maintType_dictText, r.planDate, r.completeDate, r.executor, MAINT_STATUS_TEXT[r.status || ''] || r.status]),
      ], [20, 16, 12, 12, 12, 10, 10]), '保养工单明细');
      XLSX.utils.book_append_sheet(wb, aoa([
        ['报警时间', '报警点位', '报警描述', '级别', '设备编码', '车间'],
        ...alarmRows.value.map((r: any) => [r.alarmTime, r.alarmName, r.alarmDesc, ALARM_LEVEL_TEXT[r.alarmLevel] || r.alarmLevel, r.deviceCode, r.deptCode_dictText]),
      ], [18, 16, 30, 10, 14, 12]), '报警明细');
      XLSX.writeFile(wb, `首页看板_${rangeLabel.value}_${stamp}.xlsx`);
      msg.success({ content: '导出成功' });
    } catch (e) {
      console.error(e);
      msg.error({ content: '导出失败:' + (e as Error).message });
    }
  }

  function onResize() {
    Object.values(charts).forEach((c) => c && c.resize());
  }

  onMounted(async () => {
    tick();
    clockTimer = setInterval(tick, 1000);
    window.addEventListener('resize', onResize);
    await loadReal();
    schedulePoll();
  });
  onBeforeUnmount(() => {
    if (clockTimer) clearInterval(clockTimer);
    if (pollTimer) clearTimeout(pollTimer);
    window.removeEventListener('resize', onResize);
    Object.values(charts).forEach((c) => c && c.dispose());
  });
</script>

<style lang="less" scoped>
  /* 视觉体系移植自原型 common.css(变量内联为字面量) */
  .home-dash {
    --text: #1c1917;
    --text-secondary: #57534e;
    --text-muted: #a8a29e;
    --border-light: #e7e5e4;
    --bg-card: #ffffff;
    --bg-panel: #fafaf9;
    --accent: #d97706;
    --mono: consolas, menlo, monospace;
    padding: 12px 16px;
    color: var(--text);
    font-family: 'Microsoft YaHei', sans-serif;
  }

  /* 顶栏 */
  .page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; gap: 12px; flex-wrap: wrap; }
  .page-header h1 { font-size: 18px; margin: 0; }
  .sub-title { font-size: 12px; color: var(--text-muted); margin-top: 4px; }
  .ph-right { display: flex; align-items: center; gap: 16px; flex-wrap: wrap; }
  .sub-nav { display: flex; gap: 2px; padding: 3px; background: var(--bg-panel); border-radius: 8px; border: 1px solid var(--border-light); }
  .sub-nav a { padding: 6px 14px; border-radius: 6px; font-size: 12px; font-weight: 600; color: var(--text-secondary); cursor: pointer; transition: all .15s; }
  .sub-nav a:hover { background: var(--bg-card); }
  .sub-nav a.active { background: var(--accent); color: #fff; }
  .btn-export { display: inline-flex; align-items: center; gap: 7px; padding: 6px 14px; background: var(--bg-card); border: 1px solid var(--border-light); border-radius: 8px; font-size: 12px; font-weight: 600; color: var(--text-secondary); cursor: pointer; transition: all .15s; }
  .btn-export:hover { border-color: var(--accent); color: var(--accent); }
  .btn-export svg { width: 13px; height: 13px; }
  .ph-clock { font-size: 12px; color: var(--text-muted); font-family: var(--mono); }
  .ph-clock b { color: var(--text-secondary); font-weight: 600; }

  /* KPI 五卡 */
  .stat-cards { display: grid; grid-template-columns: repeat(5, 1fr); gap: 14px; margin-bottom: 14px; }
  .kpi-card { text-align: center; background: var(--bg-card); border: 1px solid var(--border-light); border-radius: 12px; box-shadow: 0 1px 1px rgba(0,0,0,.03), 0 2px 4px rgba(0,0,0,.04), 0 8px 16px -6px rgba(0,0,0,.08); padding: 22px 20px 20px; transition: all .15s; cursor: default; }
  .kpi-card:hover { transform: translateY(-2px); box-shadow: 0 1px 1px rgba(0,0,0,.03), 0 4px 8px rgba(0,0,0,.05), 0 14px 28px -8px rgba(0,0,0,.12); }
  .kc-label { font-size: 12px; font-weight: 600; color: var(--text-secondary); letter-spacing: 1px; margin-bottom: 12px; }
  .kc-value { font-family: var(--mono); font-size: 30px; font-weight: 600; color: var(--text); line-height: 1; }
  .kc-unit { font-size: 13px; font-weight: 500; color: var(--text-muted); margin-left: 6px; }

  /* 行布局:四卡/三卡 */
  .grid-a { display: grid; grid-template-columns: repeat(4, 1fr); gap: 14px; margin-bottom: 14px; }
  .grid-b { display: grid; grid-template-columns: repeat(3, 1fr); gap: 14px; margin-bottom: 14px; }
  .grid-a .card, .grid-b .card { display: flex; flex-direction: column; }

  /* 内容卡 */
  .card { background: var(--bg-card); border-radius: 12px; box-shadow: 0 1px 1px rgba(0,0,0,.03), 0 2px 4px rgba(0,0,0,.04), 0 8px 16px -6px rgba(0,0,0,.08); border: 1px solid var(--border-light); overflow: hidden; }
  .card-header { padding: 12px 20px; border-bottom: 1px solid var(--border-light); display: flex; justify-content: space-between; align-items: center; background: var(--bg-panel); }
  .card-header h3 { font-size: 13px; font-weight: 600; letter-spacing: .3px; color: var(--text-secondary); margin: 0; }
  .hd-unit { font-size: 11px; color: var(--text-muted); }
  .card-body { padding: 14px 16px; flex: 1; }

  /* 图表容器 */
  .chart-h { height: 218px; }
  .radar-h { height: 230px; }
  .radar-body { display: flex; flex-direction: column; }
  .duo-legend { display: flex; flex-wrap: wrap; justify-content: center; gap: 6px 14px; padding: 10px 0 2px; border-top: 1px solid var(--border-light); margin-top: 10px; font-size: 11px; color: var(--text-secondary); }
  .rp-legend { display: flex; flex-wrap: wrap; justify-content: center; gap: 6px 14px; padding: 10px 0 0; border-top: 1px solid var(--border-light); margin-top: 8px; font-size: 11px; color: var(--text-secondary); }
  .rp-item { cursor: pointer; padding: 2px 6px; border-radius: 4px; border: 1px solid transparent; transition: all .15s; }
  .rp-item:hover { color: var(--text); background: rgba(0, 0, 0, .04); }
  .rp-item.active { color: var(--text); font-weight: 600; border-color: currentColor; }
  .dot { display: inline-block; width: 8px; height: 8px; border-radius: 2px; margin-right: 6px; }

  /* 环形卡 */
  .dn-flex { display: flex; align-items: center; gap: 16px; padding: 10px 16px; }
  .dn-box { position: relative; width: 45%; height: 172px; flex-shrink: 0; }
  .dn-chart { height: 100%; }
  .dn-center { position: absolute; left: 50%; top: 45%; transform: translate(-50%, -50%); text-align: center; pointer-events: none; }
  .dc-val { font: 700 24px/1 var(--mono); color: var(--text); }
  .dc-label { font-size: 10px; color: var(--text-muted); margin-top: 4px; }
  .dn-legend { flex: 1; display: flex; flex-direction: column; gap: 8px; }
  /* 图例字号/字体与设备台账列表(antd 表格 14px 默认字体)统一 */
  .dl-row { display: flex; align-items: center; gap: 8px; font-size: 14px; color: var(--text-secondary); }
  .dl-name { flex: 1; color: var(--text); }
  .dl-val { font-weight: 600; color: var(--text); }
  .dl-val i { font-style: normal; font-weight: 400; color: var(--text-muted); margin-left: 4px; font-size: 12px; }

  /* 响应式 */
  @media (max-width: 1400px) { .stat-cards { grid-template-columns: repeat(3, 1fr); } .grid-a { grid-template-columns: 1fr 1fr; } }
  @media (max-width: 1200px) { .grid-b { grid-template-columns: 1fr 1fr; } .grid-b .card:last-child { grid-column: 1 / -1; } }
  @media (max-width: 1000px) { .grid-a, .grid-b { grid-template-columns: 1fr; } .grid-b .card:last-child { grid-column: auto; } }
  @media (max-width: 900px) { .stat-cards { grid-template-columns: 1fr; } }
</style>
