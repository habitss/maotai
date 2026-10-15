<template>
  <div :class="className" :style="{ height, width }" ref="chartRef"></div>
</template>

<script lang="ts" setup>
  import { ref, watch, onBeforeUnmount, Ref, watchEffect, reactive } from 'vue';
  import { useECharts } from '/@/hooks/web/useECharts';
  import { useDebounceFn } from '@vueuse/core';
  import type { EChartsOption } from 'echarts';

  // 定义props类型
  interface ChartDataItem {
    value: number;
    redUpperLimit?: number;
    yellowUpperLimit?: number;
    yellowLowerLimit?: number;
    redLowerLimit?: number;
    valueDate?: string;
    itemStyle?: Record<string, any>;
  }

  const props = defineProps({
    className: {
      type: String,
      default: 'chart',
    },
    width: {
      type: String,
      default: 'none',
    },
    height: {
      type: String,
      default: 'none',
    },
    autoResize: {
      type: Boolean,
      default: true,
    },
    titleName: {
      type: String,
      default: '',
    },
    yData: {
      type: Array as () => ChartDataItem[],
      required: true,
    },
    xData: {
      type: Array as () => string[],
      required: true,
    },
  });

  // 组件状态
  const chartRef = ref<HTMLDivElement | null>(null);
  const { setOptions, getInstance } = useECharts(chartRef as Ref<HTMLDivElement>);
  const resizeObserver = ref<ResizeObserver | null>(null);
  // 初始化图表
  const initChart = () => {
    if (!chartRef.value) return;

    // 绑定点击事件
    getInstance()?.on('click', handlerClick);

    // 初始调整图表尺寸
    getInstance()?.resize();

    // 设置响应式监听
    setupResponsive();
  };
  watchEffect(() => {
    initChart();
  });

  // 设置响应式监听
  const setupResponsive = () => {
    if (!props.autoResize || !getInstance()) return;

    // 使用ResizeObserver监听容器大小变化
    if ('ResizeObserver' in window) {
      resizeObserver.value = new ResizeObserver(handleResize);
      chartRef.value && resizeObserver.value.observe(chartRef.value);
    }

    // 同时监听窗口大小变化
    window.addEventListener('resize', handleResize);
  };

  // 处理大小变化
  const handleResize = useDebounceFn(() => {
    getInstance()?.resize();
  }, 200);

  // 设置图表选项
  const setChartOptions = (xdata: string[], ydata: ChartDataItem[]) => {
    const ydatav = ydata.map((item) => ({
      value: Number(item.value),
      redUpperLimit: item.redUpperLimit,
      yellowUpperLimit: item.yellowUpperLimit,
      yellowLowerLimit: item.yellowLowerLimit,
      redLowerLimit: item.redLowerLimit,
      valueDate: item.valueDate,
      itemStyle: item.itemStyle,
    }));

    console.log('ydatav', ydatav);

    const markLineData: any[] = [];
    if (ydata.length > 0) {
      const item = ydata[0];
      if (item.redUpperLimit !== undefined) {
        markLineData.push({
          silent: false,
          symbolSize: 6,
          lineStyle: { color: 'red' },
          yAxis: item.redUpperLimit,
          name: '红色上限',
          label: { distance: ['5', '10'], color: 'red' },
        });
      }
      if (item.yellowUpperLimit !== undefined) {
        markLineData.push({
          silent: false,
          symbolSize: 6,
          lineStyle: { color: '#CCCC00' },
          yAxis: item.yellowUpperLimit,
          name: '黄色上限',
          label: { distance: ['5', '30'], color: '#CCCC00' },
        });
      }
      if (item.yellowLowerLimit !== undefined) {
        markLineData.push({
          silent: false,
          symbolSize: 6,
          lineStyle: { color: '#CCCC00' },
          yAxis: item.yellowLowerLimit,
          name: '黄色下限',
          label: { distance: ['5', '30'], color: '#CCCC00' },
        });
      }
      if (item.redLowerLimit !== undefined) {
        markLineData.push({
          silent: false,
          symbolSize: 6,
          lineStyle: { color: 'red' },
          yAxis: item.redLowerLimit,
          name: '红色下限',
          label: { distance: ['5', '30'], color: 'red' },
        });
      }
    }

    const chartOption = reactive({
      xAxis: {
        type: 'category',
        axisLabel: {
          color: '#333333', // 文本颜色改为深色
          show: true,
        },
        axisTick: {
          show: false,
        },
        axisLine: {
          lineStyle: {
            color: '#e0e0e0', // 轴线颜色改为浅色
            type: 'dashed',
          },
        },
        data: xdata,
      },
      tooltip: {
        trigger: 'axis',
        axisPointer: { type: 'shadow' },
        backgroundColor: 'rgba(255, 255, 255, 0.9)', // 提示框背景改为白色
        borderColor: '#ddd',
        borderWidth: 1,
        textStyle: {
          fontWeight: 'normal',
          fontFamily: 'Arial',
          fontSize: 16,
          color: '#333333', // 提示框文本颜色改为深色
        },
        formatter: function (param) {
          const data = param[0].data || {};
          let result = `${param[0].name}<br>用量: ${param[0].value}`;
          if (data.redUpperLimit !== undefined) {
            result += `<br>红色上限: ${data.redUpperLimit}`;
          }
          if (data.yellowUpperLimit !== undefined) {
            result += `<br>黄色上限: ${data.yellowUpperLimit}`;
          }
          if (data.redLowerLimit !== undefined) {
            result += `<br>红色下限: ${data.redLowerLimit}`;
          }
          if (data.yellowLowerLimit !== undefined) {
            result += `<br>黄色下限: ${data.yellowLowerLimit}`;
          }
          return result;
        },
      },
      yAxis: {
        type: 'value',
        axisLabel: {
          color: '#333333', // 文本颜色改为深色
          show: true,
        },
        axisTick: {
          show: false,
        },
        axisLine: {
          show: false,
          lineStyle: {
            color: '#333333',
          },
        },
        splitLine: {
          show: true,
          lineStyle: {
            color: '#e0e0e0', // 网格线颜色改为浅色
            type: 'dashed',
          },
        },
      },
      grid: {
        top: 25,
        bottom: 40,
        left: 60,
        right: 70,
      },
      series: [
        {
          type: 'bar',
          data: ydatav,
          // markLine: {
          //   tooltip: {
          //     formatter: function (param) {
          //       return `${param.name}: ${param.value}`;
          //     },
          //   },
          //   label: {
          //     fontSize: 14,
          //     color: '#333333', // 标记线标签颜色改为深色
          //     textShadowColor: 'rgba(0, 0, 0, 0.1)',
          //     textShadowBlur: 0.1,
          //     textShadowOffsetX: 0.3,
          //     textShadowOffsetY: 0.3,
          //     formatter: function (params) {
          //       console.log(params, 'params');
          //       return params.value;
          //     },
          //   },
          //   data: markLineData,
          // },
          label: {
            show: true,
            color: '#333333', // 数据标签颜色改为深色
            position: 'top',
            formatter: function (param) {
              return param.value;
            },
          },
        },
      ],
    });
    setOptions(chartOption as EChartsOption);
  };

  // 处理图表点击事件
  const handlerClick = (param: any) => {
    if (param.componentType === 'series') {
      emits('handlerLineClick', param);
    }
  };

  // 清理资源
  const cleanup = () => {
    // 清理ResizeObserver
    if (resizeObserver.value) {
      resizeObserver.value?.disconnect();
      resizeObserver.value = null;
    }

    // 清理ECharts实例
    if (!getInstance()) {
      return;
    }
    getInstance()?.dispose();

    // 移除窗口监听
    window.removeEventListener('resize', handleResize);
  };

  // 监听yData变化
  watch(
    () => props.yData,
    (val) => {
      if (val.length > 0) {
        setChartOptions(props.xData, val);
        emits('handlerChartLoading', false);
      } else if (getInstance()) {
        getInstance()?.clear();
      }
    },
    { deep: true }
  );

  onBeforeUnmount(() => {
    cleanup();
  });

  const emits = defineEmits(['handlerLineClick', 'handlerChartLoading']);
</script>

<style scoped>
  .chart {
    width: 100%;
    height: 100%;
  }
</style>
