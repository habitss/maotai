<template>
  <div>
    <!--引用表格-->
    <BasicTable @register="registerTable">
    </BasicTable>
  </div>
</template>

<script lang="ts" name="dataCalculation-fiveWhy-fiveWhyList" setup>
import { BasicTable } from '/@/components/Table';
import { useListPage } from '/@/hooks/system/useListPage'
import { upColumns } from '../gapAnalysis.data';
import { listKpi } from '../gapAnalysis.api';

//注册table数据
const { tableContext } = useListPage({
  tableProps: {
    canResize: false,
    showTableSetting: false,
    showActionColumn: false,
    pagination: false,
    immediate: false
  },
})

const [registerTable, { reload, setProps }] = tableContext;

function initUpList(obj){
  let yearMonth = obj?.queryParam?.month ?? obj.kpiParams.yearMonth;
  let month = yearMonth.split('-')[1];

  upColumns[1].children[0].title = obj.kpiRowData.kpiName.value;
  upColumns[3].title = parseInt(month)+"月年累";

  setProps({
    title: obj.kpiRowData.kpiName.value+'&差距分析',
    api: listKpi,
    beforeFetch: (params) => {
      params.kpiCode = obj.kpiParams.kpiCode;
      params.month = yearMonth;
    },
    columns: upColumns,
  })
  reload();
}

//暴露方法
defineExpose({
  initUpList
})
</script>

<style lang="less" scoped>
:deep(.ant-picker),
:deep(.ant-input-number) {
  width: 100%;
}

:deep(.ant-table-cell .ant-table-cell-ellipsis) {
  min-height: 100px !important;
}

.box {
  height: 100%;
  display: flex;
  align-items: center;

  span {
    cursor: pointer;
    margin-left: 4px;
  }
}
</style>