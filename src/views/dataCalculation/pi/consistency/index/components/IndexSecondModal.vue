<template>
  <BasicModal v-bind="$attrs" @register="registerModal" destroyOnClose title="详情" width="80vw" :showCancelBtn="false"
    :showOkBtn=false>
    <BasicTable @register="registerTable">
    </BasicTable>
  </BasicModal>
</template>

<script lang="ts" setup>
import { reactive } from 'vue';
import { BasicModal, useModalInner } from '/@/components/Modal';
import { BasicTable } from '/@/components/Table';
import { mxColumns } from '../index.data';
import { detailList } from '../index.api';
import { useListPage } from '/@/hooks/system/useListPage';
const queryParam = reactive<any>({});
//表单赋值
const [registerModal] = useModalInner(async (data) => {
  queryParam.zbId = data.zbId;
  queryParam.timeBegin = data.timeBegin;
  queryParam.timeEnd = data.timeEnd;
  queryParam.column = "sort";
  queryParam.order = "asc";
});

// 列表页面公共参数、方法
const { tableContext } = useListPage({
  tableProps: {
    api: detailList,
    beforeFetch: (params) => {
      return Object.assign(params, queryParam);
    },
    scroll: { y: 500 },
    columns: mxColumns,
    expandRowByClick: true,
    showActionColumn: false,
    showTableSetting: false,
    pagination: false,
    rowKey: 'id',
    useSearchForm: false
  },
});

// BasicTable绑定注册
const [registerTable, { setProps }] = tableContext;
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