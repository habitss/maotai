<template>
  <Workshop3D :indicators="realtimeIndicators" :ws-index="wsIndex" :ws-color="wsColor" />
</template>

<script lang="ts" name="maotai-workshop-tank-3d" setup>
  import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue';
  import Workshop3D from './WorkshopTankScene.vue';
  import { getTankRealtime, type TankRealtimeItem } from './WorkshopCockpit.api';

  const props = defineProps<{
    indicators: any[];
    wsIndex: number;
    wsColor?: string;
  }>();

  const emit = defineEmits<{
    alertSummary: [summary: { highLevel: number; temperature: number; pendingInspection: number }];
  }>();

  const records = ref<TankRealtimeItem[]>([]);
  let pollTimer: ReturnType<typeof setTimeout> | undefined;
  let requestSeq = 0;
  let running = false;
  let resumeWhenVisible = false;
  let consecutiveFailures = 0;
  let currentInterval = 10000;
  const pollInterval = 10000;
  const maxBackoff = 60000;

  const realtimeIndicators = computed(() => {
    // 匹配键优先位置编码(台账改名不断链),无 funlocCode 的旧数据回退罐名匹配
    const recordMap = new Map(records.value.map((record) => [record.funlocCode || normalizeTankId(record.tankId), record]));
    return (props.indicators || []).map((source) => {
      const record = recordMap.get((source as any).funlocCode || normalizeTankId(source.title || source.tankId));
      if (!record) return source;
      const indicator = { ...source, params: (source.params || []).map((item: any) => ({ ...item })) };
      indicator.status = normalizeStatus(record.status || source.status);
      // 报警字段集合(中集报警点位属于哪列测点):只有命中字段的参数行标红,其余行保持正常色
      const alarmFields = new Set(((record.alarmList as any[]) || []).map((a: any) => a.field).filter(Boolean));
      const rowState = (field: string) => (alarmFields.has(field) ? 'alarm' : 'normal');
      if (record.levelPct !== undefined) indicator.liveValue = `${formatOneDecimal(record.levelPct)}%`;
      if (record.capacityM3 !== undefined) indicator.totalCap = record.capacityM3;
      setParam(indicator.params, '液位', `${formatOneDecimal(record.levelPct)}%`, rowState('level'));
      setParam(indicator.params, '实际液位', record.volumeM3 === undefined ? '-' : `${formatRounded(record.volumeM3)}m³`, rowState('level'));
      setParam(indicator.params, '温度', `${formatOneDecimal(record.temperatureC)}°C`, rowState('temp'));
      setParam(indicator.params, '酒精度', record.alcoholPct === undefined ? '-' : `${formatOneDecimal(record.alcoholPct)}%vol`, rowState('alcohol'));
      setParam(indicator.params, '容量', record.capacityM3 === undefined ? '-' : `${formatRounded(record.capacityM3)}m³`, 'normal');
      setParam(indicator.params, '位置编码', record.funlocCode || '-', 'normal');
      indicator.realtime = record;
      return indicator;
    });
  });

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
  function emitAlertSummary(nextRecords: TankRealtimeItem[]) {
    emit('alertSummary', {
      highLevel: nextRecords.filter((record) => Number(record.levelPct) > 85).length,
      temperature: nextRecords.filter((record) => Number(record.temperatureC) > 26).length,
      pendingInspection: nextRecords.filter((record) => normalizeStatus(record.status) === 'standby').length,
    });
  }
  function clearPollTimer() {
    if (pollTimer) clearTimeout(pollTimer);
    pollTimer = undefined;
  }
  function scheduleNext() {
    clearPollTimer();
    if (running && !document.hidden) pollTimer = setTimeout(pollOnce, currentInterval);
  }
  async function pollOnce() {
    if (!running || document.hidden) return;
    const seq = ++requestSeq;
    const workshopIndex = props.wsIndex;
    try {
      const response = await getTankRealtime(workshopIndex);
      if (!running || seq !== requestSeq || workshopIndex !== props.wsIndex) return;
      records.value = Array.isArray(response?.list) ? response.list : [];
      emitAlertSummary(records.value);
      consecutiveFailures = 0;
      currentInterval = pollInterval;
    } catch {
      if (!running || seq !== requestSeq) return;
      consecutiveFailures++;
      currentInterval = Math.min(pollInterval * 2 ** consecutiveFailures, maxBackoff);
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
    clearPollTimer();
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

  watch(
    () => props.wsIndex,
    () => {
      records.value = [];
      emitAlertSummary([]);
      if (running) startPolling();
    }
  );

  onMounted(() => {
    document.addEventListener('visibilitychange', onVisibilityChange);
    startPolling();
  });
  onBeforeUnmount(() => {
    stopPolling();
    document.removeEventListener('visibilitychange', onVisibilityChange);
  });
</script>
