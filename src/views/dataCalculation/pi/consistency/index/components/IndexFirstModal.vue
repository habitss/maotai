<template>
  <BasicModal v-bind="$attrs" @register="registerModal" destroyOnClose title="详情" width="80vw" :showCancelBtn="false"
    :showOkBtn=false>
    <BasicTable @register="registerTable">
      <!--操作栏-->
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)" />
      </template>
    </BasicTable>
    <!-- 详情区域 -->
    <IndexSecondModal @register="registerDetailModal"></IndexSecondModal>
  </BasicModal>
</template>

<script lang="ts" setup>
import { reactive } from 'vue';
import { BasicModal, useModalInner } from '/@/components/Modal';
import { BasicTable, TableAction } from '/@/components/Table';
import { useModal } from '/@/components/Modal';
import { mxColumns } from '../index.data';
import { detailList } from '../index.api';
import { useListPage } from '/@/hooks/system/useListPage';
import IndexSecondModal from './IndexSecondModal.vue'
const queryParam = reactive<any>({});
//注册model
const [registerDetailModal, { openModal }] = useModal();
//表单赋值
const [registerModal] = useModalInner(async (data) => {
  queryParam.zbId = data.zbId;
  queryParam.timeBegin = data.timeBegin;
  queryParam.timeEnd = data.timeEnd;
  queryParam.column = "sort";
  queryParam.order = "asc";
  setProps({
    showActionColumn: data.consistencyType == '1'
  });
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
    showActionColumn: true,
    showTableSetting: false,
    pagination: false,
    actionColumn: {
      width: 80,
    },
    rowKey: 'id',
    useSearchForm: false
  },
});

// BasicTable绑定注册
const [registerTable, { setProps }] = tableContext;

//操作栏
function getTableAction(record) {
  return [
    {
      label: '详情',
      onClick: handleDetail.bind(null, record),
    },
  ]
}

//详情
function handleDetail(record){
  openModal(true, {
    zbId: record.piCode,
    consistencyType: 2,
    timeBegin: queryParam.timeBegin,
    timeEnd: queryParam.timeEnd,
  });
}
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