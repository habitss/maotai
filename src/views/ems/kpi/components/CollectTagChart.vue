<template>  
  <div class="minute-chart-container">
    <a-spin :spinning="chartLoading">
      <div v-show="isMounted" ref="chartRef" class="chart"></div>
    </a-spin>
  </div>
</template>

<script lang="ts" setup>
  import { ref, onMounted, watch, nextTick, onBeforeUnmount, Ref, watchEffect } from 'vue';
  import { useECharts } from '/@/hooks/web/useECharts';
  import type { EChartsOption } from 'echarts';
  import dayjs from 'dayjs';
  import { getHistoryCurveData } from '/@/views/ems/analysis/BaseTagHistory.api';

  const props = defineProps<{
    hourData: string | null;
    tagId: string | null;
    tagName: string | null;
  }>();

  const chartRef = ref<HTMLElement>();
  const { setOptions, getInstance } = useECharts(chartRef as Ref<HTMLDivElement>);
  const startTime = ref<string>();
  const endTime = ref<string>();
  const resizeObserver = ref<ResizeObserver>();
  const isMounted = ref(false);
  const pickDates = ref<string[]>([]);
  const chartLoading = ref(false);

  // 初始化时间范围
  const initTimeRange = () => {
    const now = dayjs(props.hourData);
    pickDates.value = [now.format('YYYY-MM-DD HH:mm'), now.add(1, 'hour').format('YYYY-MM-DD HH:mm')];
    startTime.value = pickDates.value[0] + ':00';
    endTime.value = pickDates.value[1] + ':00';
  };

  // 获取数据
  const fetchData = async () => {
    chartLoading.value = true;
    try {
      if (!startTime.value || !endTime.value) return;

      const paraObj = {
        frequency: 1000,
        stime: startTime.value,
        etime: endTime.value,
        tagIds: props.tagId,
      };
      const response = await getHistoryCurveData(paraObj);
      const dataArr = response.result;
      if (dataArr && dataArr.length > 0) {
        // 更新图表
        const lineDatas = dataArr[0].lineDatas.map((item) => {
          return {
            time: item.time,
            value: item.value,
          };
        });
        if (lineDatas && lineDatas.length > 0) {
          updateChart(lineDatas);
        } else {
          console.warn('没有获取到数据');
        }
      } else {
        console.warn('没有获取到数据');
      }
    } catch (error) {
      console.error('获取数据失败:', error);
    } finally {
      chartLoading.value = false;
    }
  };

  // 更新图表
  const updateChart = (data: any[]) => {
    if (!getInstance() || !data || data.length === 0) {
      console.warn('Chart instance or data not ready');
      return;
    }

    const allDatas: number[] = data.map((item) => item.value);
    const maxVal = Math.max(...allDatas);
    const minVal = Math.min(...allDatas);
    const yMax = 0;
    const yMin = 0;
    try {
      const option: EChartsOption = {
        title: {
          text: props.tagName || '',
          left: 'center',
          textStyle: { color: '#2c3e50', fontSize: 15 },
        },
        animation: false, // 禁用动画避免初始化问题
        tooltip: {
          trigger: 'axis',
          formatter: (params: any[]) => {
            const param = params[0];
            return `${param.axisValue} <br> ${props.tagName} : ${param.value}`;
          },
        },
        grid: {
          top: 40,
          bottom: 30,
          left: 60,
          right: 40,
          containLabel: true,
        },
        xAxis: {
          type: 'category',
          boundaryGap: false,
          data: data.map((item) => item.time),
        },
        yAxis: {
          axisLine: {
            show: true,
            lineStyle: { color: '#c7c9ce' },
          },
          axisTick: { show: true },
          axisLabel: { fontSize: 12 },
          splitLine: {
            lineStyle: { color: ['#ccc'], opacity: 0.3 },
          },
          type: 'value',
          max: (value: any) => {
            const val = value.max > maxVal ? value.max : maxVal;
            if (yMax === 0) {
              if (val < 10) return Math.round((val + 0.05) * 100) / 100;
              if (val < 30) return val + 1;
              if (val < 50) return val + 2;
              if (val < 100) return val + 5;
              return Math.round(val * 11.0) / 10.0;
            }
            return yMax;
          },
          min: (value: any) => {
            const val = value.min > minVal ? minVal : value.min;
            if (yMin === 0) {
              if (val < 10) return Math.ceil((val - 0.05) * 100) / 100;
              if (val < 30) return val - 1;
              if (val < 50) return val - 2;
              if (val < 100) return val - 5;
              return Math.ceil(val * 0.9);
            }
            return yMin;
          },
        },
        series: [
          {
            name: '值',
            data: data.map((item) => item.value),
            type: 'line',
            showSymbol: false,
            smooth: true,
            lineStyle: {
              width: 1,
              type: 'solid', // 明确指定线型
            },
            itemStyle: {
              borderWidth: 1,
              borderType: 'solid',
            },
          },
        ],
      };

      setOptions(option);

      // 延迟确保渲染完成
      setTimeout(() => {
        getInstance()?.resize();
      }, 100);
    } catch (error) {
      console.error('Update chart error:', error);
    }
  };

  // 初始化图表
  const initChart = () => {
    if (!chartRef.value) {
      console.warn('Chart container not found');
      return;
    }

    try {
      // 确保DOM完全加载
      const container = chartRef.value;
      if (container.clientWidth === 0 || container.clientHeight === 0) {
        console.warn('Chart container has zero dimension');
        // 延迟重试
        setTimeout(initChart, 100);
        return;
      }
      // 添加resize观察器
      resizeObserver.value = new ResizeObserver(() => {
        if (getInstance() && container.clientWidth > 0 && container.clientHeight > 0) {
          getInstance()?.resize();
        }
      });
      resizeObserver.value.observe(container);
    } catch (error) {
      console.error('Init chart error:', error);
    }
  };

  watchEffect(() => {
    initChart();
    fetchData();
  });

  // 组件挂载
  onMounted(() => {
    isMounted.value = true;
    initTimeRange();
  });

  // 组件卸载
  onBeforeUnmount(() => {
    if (resizeObserver.value) {
      resizeObserver.value.disconnect();
      resizeObserver.value = undefined;
    }

    // 清理ECharts实例
    if (!getInstance()) {
      return;
    }
    getInstance()?.dispose();
  });

  // 监听props变化
  watch(
    () => props.hourData,
    () => {
      nextTick(() => {
        fetchData();
      });
    },
    { immediate: true }
  );
</script>

<style scoped>
  .minute-chart-container {
    width: 100%;
    height: 100%;
    padding: 16px;
    position: relative;
  }

  .time-range-picker {
    margin-bottom: 16px;
    display: flex;
    justify-content: center;
  }

  .chart {
    width: 100%;
    height: 100%;
    min-height: 200px;
  }
</style>
