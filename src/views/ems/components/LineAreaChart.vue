<template>
  <div ref="chartRef" :class="className" :style="{ height, width }"></div>
</template>

<script lang="ts" setup>
  import * as echarts from 'echarts';
  import dayjs from 'dayjs';
  import { ref, watch, onMounted, onBeforeUnmount, shallowRef } from 'vue';
  import type { ECharts, EChartsOption } from 'echarts';

  // 定义组件Props
  interface Props {
    className?: string;
    width?: string;
    height?: string;
    xShow?: boolean;
    titleName?: string;
    yData: any[];
    xData: string[];
    type?: 'Day' | 'Hour' | 'Second' | 'Other' | '';
    legendSeled?: Record<string, boolean>;
    limitObj?: { tagId: string; limitval: number };
    yMax?: string;
    yMin?: string;
  }

  const props = withDefaults(defineProps<Props>(), {
    className: 'chart',
    width: '100%',
    height: 'none',
    xShow: true,
    titleName: '',
    type: 'Day',
    yMax: '0',
    yMin: '0',
  });

  // 定义Emit事件
  const emit = defineEmits(['handlerLineDatazoom', 'handlerLineRestore', 'handlerLegendChg', 'changeYechats']);

  const chartRef = ref<HTMLDivElement | null>(null);
  const chartInstance = shallowRef<ECharts | null>(null);

  // 日期格式化函数（使用Day.js替代Moment.js）
  const formatDate = (value: string, type: string) => {
    switch (type) {
      case 'Day':
        return dayjs(value).format('MM/DD');
      case 'Hour':
        return dayjs(value).format('MM/DD HH:mm');
      case 'Second':
        return dayjs(value).format('HH:mm');
      case 'Other':
        return dayjs(value).format('HH:mm:ss');
      default:
        return value;
    }
  };

  // 初始化图表
  const initChart = () => {
    if (!chartRef.value) return;

    // 如果已经存在实例，则先销毁
    if (chartInstance.value) {
      chartInstance.value.dispose();
      chartInstance.value = null;
    }

    chartInstance.value = echarts.init(chartRef.value);
    if (props.yData.length > 0) {
      setOptions(props.xData, props.yData, props.type);
    }
  };

  // 设置图表选项
  const setOptions = (xdata: string[], ydata: any[], type: string) => {
    if (!chartInstance.value) return;

    // 清除之前的图表状态
    chartInstance.value.clear();

    const ydataX = ydata.map((item) => {
      // 确保数据有效
      const validData = item.data.map(
        (elem: any) => Number(elem) || 0 // 将 undefined/null 转为 0
      );
      const series: any = {
        ...item,
        data: validData, // 使用处理后的数据
        type: 'line',
        showSymbol: false,
      };

      if (props.limitObj && props.limitObj.tagId === item.tagid && props.limitObj.limitval) {
        series.markLine = {
          data: [{ yAxis: props.limitObj.limitval }],
        };
      }
      return series;
    });

    const legendArr = ydata.map((item) => ({
      name: item.name,
      icon: 'circle',
      textStyle: {
        textShadowColor: '#505050',
        textShadowOffsetX: 1,
        textShadowOffsetY: 1,
      },
    }));

    const allDatas: number[] = [];
    ydata.forEach((item) => {
      allDatas.push(...item.data.map((elem: any) => Number(elem)));
      if (props.limitObj && props.limitObj.tagId === item.tagid && props.limitObj.limitval) {
        allDatas.push(Number(props.limitObj.limitval));
      }
    });

    const maxVal = Math.max(...allDatas);
    const minVal = Math.min(...allDatas);
    const yMax = parseFloat(props.yMax);
    const yMin = parseFloat(props.yMin);

    const options: EChartsOption = {
      animation: false, // 关闭所有动画
      title: {
        left: 'center',
        text: props.titleName,
        textStyle: {
          fontSize: 14,
          color: '#505050',
        },
      },
      legend: [
        {
          left: '5%',
          bottom: 20,
          textStyle: { color: '#505050', fontSize: 14 },
          data: legendArr.slice(0, 3),
          pageButtonPosition: 'start',
          pageIconColor: '#505050',
          pageIconInactiveColor: '#2f4554',
          pageTextStyle: { color: '#505050' },
          inactiveColor: '#666',
          selected: props.legendSeled,
        },
        {
          left: '5%',
          bottom: 20,
          textStyle: { color: '#505050', fontSize: 14 },
          data: legendArr.slice(3, 6),
          pageButtonPosition: 'start',
          pageIconColor: '#505050',
          pageIconInactiveColor: '#2f4554',
          pageTextStyle: { color: '#505050' },
          inactiveColor: '#666',
          selected: props.legendSeled,
        },
        {
          left: '5%',
          bottom: 0,
          width: '85%',
          type: 'scroll',
          textStyle: { color: '#505050', fontSize: 14 },
          data: legendArr.slice(6),
          pageIconColor: '#505050',
          pageIconInactiveColor: '#2f4554',
          pageTextStyle: { color: '#505050' },
          inactiveColor: '#666',
          selected: props.legendSeled,
        },
      ],
      tooltip: {
        trigger: 'axis',
        textStyle: {
          fontWeight: 'normal',
          fontFamily: 'Arial',
          fontSize: 12,
        },
      },
      toolbox: {
        feature: {
          dataZoom: {
            iconStyle: { borderColor: '#505050' },
            yAxisIndex: 'none',
          },
          dataView: { readOnly: false },
          restore: { iconStyle: { borderColor: '#505050' } },
          saveAsImage: { iconStyle: { borderColor: '#505050' } },
        },
        show: props.xShow,
      },
      calculable: true,
      xAxis: {
        type: 'category',
        boundaryGap: false,
        axisLine: {
          onZero: false,
          lineStyle: { color: '#c7c9ce' },
        },
        axisTick: { show: true },
        axisLabel: {
          fontSize: 12,
          formatter: (value: string) => formatDate(value, type),
        },
        data: xdata,
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
      grid: {
        top: 40,
        bottom: 80,
        left: 90,
        right: 40,
      },
      series: ydataX,
    };

    // 添加事件监听
    chartInstance.value.off('datazoom');
    chartInstance.value.off('restore');
    chartInstance.value.off('legendselectchanged');

    chartInstance.value.on('datazoom', handleDatazoom);
    chartInstance.value.on('restore', handleRestore);
    chartInstance.value.on('legendselectchanged', handleLegendChg);

    chartInstance.value.setOption(options, true);
  };

  // 事件处理函数
  const handleDatazoom = (param: any) => {
    if (param.batch?.length > 0) {
      const dateRange = [props.xData[param.batch[0].startValue], props.xData[param.batch[0].endValue]];
      emit('handlerLineDatazoom', dateRange);
    }
  };

  const handleRestore = () => {
    emit('handlerLineRestore');
  };

  const handleLegendChg = (param: any) => {
    emit('handlerLegendChg', param.selected);
  };
  /**暂时不用
  const handleClick = (param: any) => {
    const allDatas: number[] = [];
    props.yData.forEach((item) => {
      if (param.selected[item.name]) {
        allDatas.push(...item.data.map((elem: any) => Number(elem)));
      }
    });

    if (allDatas.length > 0) {
      const maxVal = Math.max(...allDatas);
      const minVal = Math.min(...allDatas);
      emit('changeYechats', maxVal, minVal);
    }
  };*/

  // 响应式调整大小
  const handleResize = () => {
    if (chartInstance.value) {
      chartInstance.value.resize();
    }
  };

  // 监听props变化
  watch(
    () => props.yData,
    (newVal) => {
      try {
        if (newVal.length > 0) {
          setOptions(props.xData, newVal, props.type ?? 'Other');
        } else if (chartInstance.value) {
          chartInstance.value.clear();
        }
      } catch (e) {
        console.error('Error in yData watcher:', e);
      }
    },
    { deep: true }
  );

  watch(
    () => [props.yMax, props.yMin],
    () => {
      if (chartInstance.value) {
        setOptions(props.xData, props.yData, props.type);
      }
    },
    { deep: true }
  );

  // 生命周期钩子
  onMounted(() => {
    setTimeout(initChart, 100);
    window.addEventListener('resize', handleResize);
  });

  onBeforeUnmount(() => {
    if (chartInstance.value) {
      chartInstance.value.dispose();
      chartInstance.value = null;
    }
    window.removeEventListener('resize', handleResize);
  });

  // 提供公共方法
  const getChart = () => {
    return chartInstance.value;
  };

  defineExpose({
    getChart,
    handleResize,
  });
</script>
