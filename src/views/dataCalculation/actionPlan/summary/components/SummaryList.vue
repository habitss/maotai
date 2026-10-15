<template>
  <div>
    <!--引用表格-->
    <BasicTable @register="registerTable">
      <template #totalSlots="{ record, text }">
        <a-button type="link" @click="delayFinishClick(record)">{{ text }}</a-button>
      </template>
    </BasicTable>
  </div>
</template>

<script lang="ts" name="action.plan-summaryaList" setup>
import { BasicTable } from '/@/components/Table';
import { useListPage } from '/@/hooks/system/useListPage'
import { columns } from '../Summary.data';
  import { router } from '/@/router';

//注册table数据
const { tableContext } = useListPage({
  tableProps: {
    columns,
    canResize: false,
    showActionColumn: false,
    showTableSetting: false,
    pagination: false,
  },
})
const [registerTable, {setTableData}] = tableContext;

//设置表格数据
function reloadData(tableData){
  setTableData([tableData]);
}

//延迟完成次数点击事件
function delayFinishClick(record){
  //计划
  router.push({ path: "/dataCalculation/actionPlan/track/actionPlanTrackList"})
}

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