<template>
  <a-modal
    :maskClosable="false"
    class="dig-dataDetail dig-chart"
    :width="dialogWidth"
    :visible="modelValue"
    :footer="null"
    @cancel="handleCancel"
    @update:visible="updateVisible"
  >
    <!-- 自定义标题区域 -->
    <template #title>
      <div>
        <div class="dialog-header">
          <span>{{ tagLineTitle }}</span>
          <span class="sub-title">{{ tagLineSubTitle }}</span>
        </div>

        <div class="dialog-controls">
          <a-range-picker v-model:value="tagLineDates" :value-format="dateFormat" :picker="pickerType" :placeholder="['开始日期', '结束日期']" />
          <a-button class="btn-search" shape="circle" @click="handleSearch">
            <Icon icon="ant-design:search-outlined" />
          </a-button>
        </div>
      </div>
    </template>
    <!-- 图表内容 -->
    <a-spin :spinning="chartLoading">
      <div class="chart-container" v-if="modelValue">
        <xdate-bar-chart
          class-name="xdate-bar-chart"
          :y-data="yLineData"
          :x-data="xLineData"
          @handler-line-click="showHourLineChart"
          @handler-chart-loading="handleChartLoading"
        />
      </div>
    </a-spin>
  </a-modal>
</template>

<script lang="ts" setup>
  import { ref, computed, watch, PropType } from 'vue';
  import XdateBarChart from './XdateBarChart.vue';
  import dayjs from 'dayjs';

  // 定义props类型
  interface ChartDataItem {
    value: number;
    redUpperLimit: number;
    yellowLowerLimit: number;
    flag?: string;
    valueDate?: string;
    itemStyle?: Record<string, any>;
  }

  const chartLoading = ref(false);
  const handleChartLoading = (loading: boolean) => {
    chartLoading.value = loading;
  };

  // 定义Props
  const props = defineProps({
    modelValue: {
      type: Boolean,
      default: false,
    },
    tagLineTitle: {
      type: String,
      default: '',
    },
    tagLineSubTitle: {
      type: String,
      default: '',
    },
    pickerType: {
      type: String,
      default: 'date',
    },
    yLineData: {
      type: Array as () => ChartDataItem[],
      required: true,
    },
    xLineData: {
      type: Array as () => string[],
      required: true,
    },
    initialDateRange: {
      type: Array as unknown as PropType<[string, string]>,
      default: () => [dayjs().subtract(7, 'days').format('YYYY-MM-DD'), dayjs().format('YYYY-MM-DD')],
    },
  });

  // 定义Emits
  const emit = defineEmits(['update:modelValue', 'close', 'search', 'handlerLineClick']);

  // 响应式数据
  const tagLineDates = ref<[string, string]>(props.initialDateRange);
  const dateFormat = computed(() => {
    if (props.pickerType === 'date') {
      return 'YYYY-MM-DD';
    } else if (props.pickerType === 'month') {
      return 'YYYY-MM';
    } else if (props.pickerType === 'year') {
      return 'YYYY';
    } else {
      return 'YYYY-MM-DD';
    }
  });
  const dialogWidth = computed(() => {
    return window.innerWidth > 1200 ? '80%' : '95%';
  });

  // 监听日期范围变化
  watch(
    () => props.initialDateRange,
    (newVal) => {
      tagLineDates.value = newVal;
    },
    { immediate: true }
  );

  // 处理搜索按钮点击
  const handleSearch = () => {
    chartLoading.value = true;
    emit('search', tagLineDates.value);
  };

  // 处理弹窗取消/关闭
  const handleCancel = () => {
    emit('close');
  };

  // 更新弹窗显示状态
  const updateVisible = (visible: boolean) => {
    emit('update:modelValue', visible);
  };

  // 图表点击事件转发
  const showHourLineChart = (params: any) => {
    emit('handlerLineClick', params);
  };
</script>

<style lang="less" scoped>
  .dig-dataDetail.dig-chart {
    :deep(.ant-modal-body) {
      padding: 16px;
    }
  }

  .dialog-header {
    display: flex;
    flex-direction: row;
    margin-bottom: 12px;
    align-items: center;
    span {
      font-size: 18px;
      font-weight: 600;
    }

    .sub-title {
      font-size: 14px;
      color: #666;
      margin-top: 2px;
      margin-left: 15px;
    }
  }

  .dialog-controls {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-top: 8px;
  }

  .btn-search {
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .chart-container {
    height: 70vh;
    min-height: 400px;
    padding: 10px;
    .xdate-bar-chart {
      width: 100%;
      height: 100%;
    }
  }
</style>
