<template>
  <div>
    <!--引用表格-->
    <BasicTable @register="registerTable">
    </BasicTable>
  </div>
</template>

<script lang="ts" name="plan-planOrder-total" setup>
import { BasicTable } from '/@/components/Table';
import { useListPage } from '/@/hooks/system/useListPage';
import { columns, searchFormSchema, calculatePercentage } from './PlanOrderTotal.data';
import { list } from './PlanOrderTotal.api';
import { mapTableTotalSummary } from "@/utils/common/compUtils";

//注册table数据
const { tableContext } = useListPage({
  tableProps: {
    title: '',
    api: list,
    columns,
    showActionColumn: false,
    pagination: false,
    formConfig: {
      schemas: searchFormSchema,
      showAdvancedButton: true,
      autoAdvancedCol: 3,
      fieldMapToTime: [
        ['realStartTime', ['realStartTime_begin', 'realStartTime_end'], 'YYYY-MM-DD'],
      ],
    },
    beforeFetch: (params) => {
      params.pageSize = 50;
      return params;
    },
    summaryFunc: onSummary,
  },
})

const [registerTable, { getForm }] = tableContext

/**
 * 合计
 * @param tableData 表格数据
 * @returns
 */
function onSummary(tableData: Recordable[]){
  const totals = mapTableTotalSummary(tableData, [
    'taskCount',
    'taskCompleteCount',
    'taskNoCompleteCount',
    'orderResultCount',
    'orderResultCompleteCount',
    'orderResultNoCompleteCount',
  ]);
  //计算完成率
  totals.taskCompleteRate = calculatePercentage(totals.taskCompleteCount, totals.taskCount);
  //计算合格率
  totals.orderResultRate = calculatePercentage(totals.orderResultCompleteCount, totals.orderResultCount);
  totals.taskName = '合计';
  return[totals]
}
</script>

<style lang="less" scoped>
:deep(.ant-picker),
:deep(.ant-input-number) {
  width: 100%;
}
</style>
