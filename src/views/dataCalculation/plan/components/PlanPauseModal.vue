<template>
  <BasicModal v-bind="$attrs" @register="registerModal" destroyOnClose title="暂停记录" :width="800" :minHeight="500" @ok="handleSubmit"
    :maskClosable="false">
    
    <!--引用表格-->
    <BasicTable @register="registerTable" :rowSelection="rowSelection"></BasicTable>
  </BasicModal>
</template>

<script lang="ts" setup>
import { reactive } from 'vue';
import { BasicModal, useModalInner } from '/@/components/Modal';
import { BasicTable } from '/@/components/Table';
import { useListPage } from '/@/hooks/system/useListPage';
import { pauseColumns } from '../Plan.data';
import { listPause } from '../Plan.api';
const queryParam = reactive<any>({});
//注册table数据
const { tableContext } = useListPage({
  tableProps: {
    api: listPause,
    columns: pauseColumns,
    canResize: true,
    immediate: false,
    showTableSetting: false,
    showActionColumn: false,
    beforeFetch: (params) => {
      return Object.assign(params, queryParam);
    },
  },
})

const [registerTable, { reload }] = tableContext;

//表单赋值
const [registerModal, { setModalProps }] = useModalInner(async (data) => {
  setModalProps({ confirmLoading: false, showCancelBtn: !!data?.showFooter, showOkBtn: !!data?.showFooter });
  queryParam.planId = data.record.id;
  reload();
});
</script>

<style lang="less" scoped>
/** 时间和数字输入框样式 */
:deep(.ant-input-number) {
  width: 100%;
}

:deep(.ant-calendar-picker) {
  width: 100%;
}
</style>