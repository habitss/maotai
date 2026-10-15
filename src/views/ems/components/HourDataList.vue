<template>
  <div class="hour-data-list">
    <a-table :dataSource="tableData" :columns="columns" size="small" :pagination="false" bordered>
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'action'">
          <span>{{ record.label }}</span>
        </template>
        <template v-else-if="record.label === '用量'">
          <a-button type="link" size="small" @click="showMinuteChart(column.key)">
            {{ record.data[column.key] }}
          </a-button>
        </template>
        <template v-else>
          {{ record.data[column.key] }}
        </template>
      </template>
    </a-table>
  </div>
</template>

<script lang="ts" setup>
  import { ref, watch } from 'vue';
  import { queryTagHourData } from '../report/ReportData.api';

  interface HourData {
    tagId: string;
    tagKind: number;
    usage: string;
    baseVal: string;
    utime: string;
  }

  const props = defineProps<{
    tagUsageObj: {
      tagId: string;
      tagName: string;
      tagKindId: number;
      collectPeriod: number;
      stime: string;
      etime: string;
    };
  }>();

  const emit = defineEmits(['showMinuteChart']);

  const hourData = ref<HourData[]>([]);
  const loading = ref(false);
  const tableData = ref<Array<{ label: string; data: Record<string, any> }>>([]);

  // 生成小时列配置
  const columns = ref([
    {
      title: '数据',
      key: 'action',
      align: 'center',
      width: '60px',
    },
  ]);

  // 将小时数据转换为表格数据格式
  const prepareTableData = () => {
    const usageRow: Record<string, any> = {};
    const baseValRow: Record<string, any> = {};

    hourData.value.forEach((data) => {
      const hourKey = `hour_${data.utime}`;
      usageRow[hourKey] = data.usage;
      baseValRow[hourKey] = data.baseVal;
    });

    tableData.value = [
      { label: '用量', data: usageRow },
      { label: '底数', data: baseValRow },
    ];
  };

  const fetchHourData = async () => {
    loading.value = true;
    try {
      const res = await queryTagHourData(props.tagUsageObj);
      hourData.value = res;

      // 清空旧的列，只保留操作列
      columns.value = [
        {
          title: '数据',
          key: 'action',
          align: 'center',
          width: '60px',
        },
      ];

      // 添加时间列
      hourData.value.forEach((data) => {
        columns.value.push({
          title: `${data.utime}点`,
          key: `hour_${data.utime}`,
          align: 'center',
          width: '60px',
        });
      });

      // 准备表格数据
      prepareTableData();
    } finally {
      loading.value = false;
    }
  };

  const showMinuteChart = (hourData: string) => {
    if (hourData) {
      emit(
        'showMinuteChart',
        props.tagUsageObj.stime.split(' ')[0] + ' ' + hourData.split('_')[1] + ':00:00',
        props.tagUsageObj.tagId,
        props.tagUsageObj.tagName
      );
    }
  };

  // 添加 watch
  watch(
    () => props.tagUsageObj,
    (newVal) => {
      if (newVal.tagId) {
        fetchHourData();
      }
    },
    { deep: true, immediate: true }
  );
</script>
<style scoped>
  .hour-data-list {
    overflow-x: auto;
    margin-top: 1px;
  }

  :deep(.ant-table) {
    font-size: 12px;
  }

  :deep(.ant-table-thead > tr > th),
  :deep(.ant-table-tbody > tr > td) {
    padding: 8px 4px;
  }

  :deep(.ant-table-cell) {
    white-space: nowrap;
  }
</style>
