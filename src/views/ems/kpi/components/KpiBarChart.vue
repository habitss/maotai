<template>
  <div ref="chartRef" style="min-width: 100px; min-height: 100px; width: 100%; height: 100%"></div>
</template>

<script lang="ts" setup>
  import { onMounted, onBeforeUnmount, ref } from 'vue';
  import * as echarts from 'echarts';

  const chartRef = ref<HTMLElement | null>(null);
  let chartInstance: echarts.ECharts | null = null;
  let resizeObserver: ResizeObserver | null = null;

  const option: echarts.EChartsOption = {
    title: {
      text: 'YTD',
      left: 'center',
      top: 0,
      textStyle: {
        fontSize: 12,
      },
    },
    tooltip: {
      show: false,
    },
    legend: {
      data: ['BU', 'AC'],
      top: 20,
      left: 'center',
      itemWidth: 20,
      itemHeight: 10,
      textStyle: {
        fontSize: 10,
        fontWeight: 'bold',
      },
    },
    xAxis: {
      type: 'category',
      data: ['2025-05-29'],
      axisLabel: {
        fontSize: 14,
      },
    },
    yAxis: {
      show: false,
    },
    grid: {
      top: 65,
      bottom: 20,
      left: 5,
      right: 5,
    },
    series: [
      {
        name: 'BU',
        type: 'bar',
        data: [49.58],
        barGap: 0.5,
        itemStyle: {
          color: '#00BFFF',
        },
        label: {
          show: true,
          position: 'top',
          fontSize: 14,
        },
      },
      {
        name: 'AC',
        type: 'bar',
        data: [48.59],
        itemStyle: {
          color: '#FFFF00',
        },
        label: {
          show: true,
          position: 'top',
          fontSize: 14,
        },
      },
    ],
  };

  const initChart = () => {
    if (chartRef.value) {
      chartInstance = echarts.init(chartRef.value);
      chartInstance.setOption(option);

      // 用 ResizeObserver 监听 DOM 尺寸变化
      resizeObserver = new ResizeObserver(() => {
        chartInstance?.resize();
      });
      resizeObserver.observe(chartRef.value);
    }
  };

  onMounted(() => {
    initChart();
  });

  onBeforeUnmount(() => {
    chartInstance?.dispose();
    resizeObserver?.disconnect();
  });
</script>

<style scoped>
  /* 你也可以根据实际需求修改宽高 */
</style>
