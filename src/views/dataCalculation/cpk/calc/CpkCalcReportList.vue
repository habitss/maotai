<template>
  <div>
    <!--引用表格-->
    <BasicTable @register="registerTable">
      <template #boldTextSlots="{ record, text }">
        <div class="bold">{{ text }}</div>
      </template>
      <template #fiveLevelNameSlots="{ record, text }">
        <div :class="record.five_level_name === record.four_level_name ? 'bold' : 'normal'">{{ text }}</div>
      </template>
    </BasicTable>
  </div>
</template>

<script lang="ts" name="cpk-cpkReport" setup>
//ts语法
import { BasicTable } from '/@/components/Table';
import { useListPage } from '/@/hooks/system/useListPage'
import { codeList, reportColumns, reportSearchFormSchema } from './CpkCalc.data';
import { queryCpkCalcReport } from './CpkCalc.api';
import { cloneDeep } from 'lodash-es';
import { watch } from 'vue';
//注册table数据
const { tableContext } = useListPage({
  tableProps: {
    api: queryCpkCalcReport,
    title: 'CPK报表',
    columns: reportColumns,
    canResize: false,
    showActionColumn: false,
    showTableSetting: false,
    immediate: false,
    formConfig: {
      schemas: reportSearchFormSchema,
      autoSubmitOnEnter: true,
      showAdvancedButton: true,
      fieldMapToNumber: [
      ],
      fieldMapToTime: [
      ],
    },
    pagination: false,
    beforeFetch: (params) => {
      buildColumns(params);
      return params
    },
  },
})

const [registerTable, { setColumns, reload }] = tableContext

function buildColumns(params){
  let arr = params.months ? params.months.split(',').map(Number).sort((a, b) => a - b) : [1,2,3,4,5,6,7,8,9,10,11,12];
  let columnsArr = cloneDeep(reportColumns);
  columnsArr.push({
    title: '目标值',
    align: 'center',
    dataIndex: 'target_value',
    width: '120px',
  },
  {
    title: '年累',
    align: 'center',
    dataIndex: 'mean',
    width: '80px',
  });

  arr.forEach(item => {
    columnsArr.push({
      title: params.year+'-'+item,
      align: 'center',
      dataIndex: 'cpk_'+item,
      width: '120px',
    })
  });
  setColumns(columnsArr);
}
watch(codeList, (newVal, oldVal) => {
  if(oldVal.length == 0 && newVal.length > 0){
    reload();
  }
}, { deep: true })
</script>

<style lang="less" scoped>
:deep(.ant-picker),
:deep(.ant-input-number) {
  width: 100%;
}
.bold{
  text-align: left;
  font-weight: bold;
}
.normal{
  text-align: left;
  padding-left: 14px;
}
</style>
