<template>
  <div>
    <!--引用表格-->
    <BasicTable @register="registerTable" @row-click="handleRowClick">
      <!--插槽:table标题-->
      <template #tableTitle>
        <a-button  type="primary"  preIcon="ant-design:export-outlined" @click="onExportXls"> 导出</a-button>
      </template>
    </BasicTable>
  </div>
</template>

<script lang="ts" name="action.period-totalList" setup>
import { BasicTable } from '/@/components/Table';
import { useListPage } from '/@/hooks/system/useListPage'
import { columns } from '../PeriodTotal.data';
import { reactive, watch} from 'vue';
import {addFactoryDept, mapTableTotalSummary} from "@/utils/common/compUtils";
import {getExportUrl} from "@/views/dataCalculation/periodCheck/periodTotal/PeriodTotal.api";
import {useUserStore} from "@/store/modules/user";

//父组件传值

const props = defineProps({
  formData: {
    type: Object,
    default: {}
  }
})
const userStore = useUserStore();
const queryParam = reactive<any>({});
//注册table数据
const { tableContext,onExportXls } = useListPage({
  tableProps: {
    title: '期间核查统计表',
    columns,
    canResize: false,
    emptyDataIsShowTable: true,
    showActionColumn: false,
    showTableSetting: false,
    pagination: false,
    scroll: { y: 300 },
    beforeFetch: (params) => {
      params.column = 'sort';
      params.order = 'asc';
      return Object.assign(addFactoryDept(params), queryParam);
    },
    summaryFunc: onSummary,
  },
  exportConfig: {
    name: "期间核查统计表",
    url: getExportUrl,
    params: queryParam
  },
})
const [registerTable, {setTableData}] = tableContext;

const emit = defineEmits(['custom-event']); // 声明事件名称和类型（可选）
const handleRowClick = (record, rowIndex) => {
  emit('custom-event', record); // 触发事件并传递数据
}
//设置表格数据
function reloadData(tableData){
  setTableData(tableData);
}
/**
 * 表格总结
 * @param tableData 表格数据
 * @returns
 */
function onSummary(tableData: Recordable[]){
  const totals = mapTableTotalSummary(tableData, ['noExecute', 'plan','unqualified','complete','delay','delayComplete','check','noExecutePlan','completeCount','total']);
  //计算完成率
  totals.completeRate = ((totals.complete+totals.delayComplete) / totals.completeCount* 100).toFixed(2) + '%'
  totals.name = '合计';
  return[totals]



}

watch(() => props.formData, (newVal) => {
  if (newVal) {
    Object.assign(queryParam, newVal);
  }
}, { deep: true, immediate: true });
//暴露方法
defineExpose({
  reloadData
})
</script>

<style lang="less" scoped>
:deep(.ant-picker),
:deep(.ant-input-number) {
  width: 100%;
}

:deep(.jeecg-basic-table-header__toolbar) {
  display: none;
}

.actionTitle{
  max-width: 100%;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  padding: 0;
  span{
    width: 100%;
    text-decoration: underline;
    color: #D92622;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }
}
</style>
