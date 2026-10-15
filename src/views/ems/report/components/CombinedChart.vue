<template>
  <div :class="className" :style="{ height, width }" ref="chartRef"></div>
</template>

<script setup>
  import { ref, watch, onMounted, onBeforeUnmount, nextTick } from 'vue';
  import * as echarts from 'echarts/core';
  import { TitleComponent, TooltipComponent, LegendComponent, GridComponent } from 'echarts/components';
  import { BarChart, LineChart } from 'echarts/charts';
  import { CanvasRenderer } from 'echarts/renderers';

  echarts.use([TitleComponent, TooltipComponent, LegendComponent, GridComponent, BarChart, LineChart, CanvasRenderer]);

  defineProps({
    className: { type: String, default: 'chart' },
    width: { type: String, default: '100%' },
    height: { type: String, default: '400px' },
    xTitle: { type: String, default: '' },
    xDatas: { type: Array, required: true },
    yaxisDatas: { type: Array, required: true },
    serieDatas: { type: Array, required: true },
    legendDatas: { type: Array, required: true },
  });

  const chartRef = ref(null);
  let chartInstance = null;

  const renderChart = () => {
    if (!chartRef.value) return;
    if (!chartInstance) chartInstance = echarts.init(chartRef.value, 'dark');

    chartInstance.setOption({
      title: {
        text: xTitle,
        left: 'center',
        top: '20',
        textStyle: { fontSize: 24, color: '#ffffff' },
      },
      tooltip: { trigger: 'axis' },
      legend: {
        data: legendDatas,
        orient: 'vertical',
        left: 'left',
        top: 'middle',
      },
      grid: {
        left: 120,
        right: 30,
        bottom: 30,
        containLabel: true,
      },
      xAxis: { type: 'category', data: xDatas },
      yAxis: yaxisDatas,
      color: ['#e6281f', '#f3f712', '#6feb0d', '#129ff0', '#f2a40d', '#ca16e8', '#3a52d4', '#73b9bc', '#7289ab', '#91ca8c', '#26d1e8'],
      series: serieDatas,
    });
  };

  const resizeHandler = () => {
    chartInstance?.resize();
  };

  watch(
    () => serieDatas,
    (newVal) => {
      if (newVal.length > 0) renderChart();
      else chartInstance?.clear();
    },
    { deep: true }
  );

  onMounted(() => {
    nextTick(() => {
      renderChart();
      window.addEventListener('resize', resizeHandler);
    });
  });

  onBeforeUnmount(() => {
    window.removeEventListener('resize', resizeHandler);
    chartInstance?.dispose();
    chartInstance = null;
  });
</script>

<style scoped>
  .chart {
    width: 100%;
    height: 400px;
  }
</style>
