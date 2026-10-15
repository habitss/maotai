<template>
  <div>
    <!--引用表格-->
    <BasicTable @register="registerTable">
      <!-- 查询区域插槽 -->
      <!--  deptCode部门查询插槽    -->
      <template #form-deptCode="{ model, field }">
        <kt-depart-select
          v-model:value="model[field]"
          select-type="depart"
          :placeholder="t('language.selectDepartment')"
          @change="handleDeptChange"
        />
      </template>
      <!--  lineCode产线查询插槽    -->
      <template #form-lineCode="{ model, field }">
        <kt-depart-select
          v-model:value="model[field]"
          select-type="line"
          :placeholder="t('language.selectProductLine')"
          :parentKey="model.deptCode"
        />
      </template>
    </BasicTable>
  </div>
</template>

<script lang="ts" name="plan-planOrderResult-total" setup>
import { BasicTable } from '/@/components/Table';
import { useListPage } from '/@/hooks/system/useListPage';
import {resultColumns, searchResultFormSchema, calculatePercentage} from './PlanOrderTotal.data';
import {resultList} from './PlanOrderTotal.api';
import KtDepartSelect from '/@/components/KtDepartSelect/KtDepart.vue';
import { useI18n } from '/@/hooks/web/useI18n';
import { mapTableTotalSummary } from "@/utils/common/compUtils";
const { t } = useI18n();

//注册table数据
const { tableContext } = useListPage({
  tableProps: {
    title: '',
    api: resultList,
    columns:resultColumns,
    showTableSetting: false,
    showActionColumn: false,
    pagination: false,
    formConfig: {
      schemas: searchResultFormSchema,
      autoSubmitOnEnter: true,
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

//部门改变事件
async function handleDeptChange(value){
  //清空产线
  await getForm().setFieldsValue({
    lineCode: '',
  });
}

/**
 * 合计
 * @param tableData 表格数据
 * @returns
 */
function onSummary(tableData: Recordable[]){
  const totals = mapTableTotalSummary(tableData, [
    'itemCount',
    'completeCount',
    'orderResultCompleteCount',
    'orderResultNoCompleteCount',
    'noCompleteCount'
  ]);
  //计算完成率
  totals.completeRate = calculatePercentage(totals.completeCount, totals.itemCount);
  //计算合格率
  totals.orderResultRate = calculatePercentage(totals.orderResultCompleteCount, totals.itemCount);
  //未完成检查项数
  totals.noCompleteCount = totals.itemCount - totals.completeCount;
  totals.itemName = '合计';
  return[totals]
}
</script>

<style lang="less" scoped>
:deep(.ant-picker),
:deep(.ant-input-number) {
  width: 100%;
}
</style>
