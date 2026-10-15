<template>
  <BasicModal @register="registerModal" v-bind="$attrs" title="请选择需要导出数据" cancelText="取消" :defaultFullscreen=false
    :canFullscreen=false :showOkBtn="true" :showCancelBtn="true" @ok="handleOk">
    <BasicTable @register="registerTable" :rowSelection="rowSelection" :scroll="{ x: 1100, y: 500 }"></BasicTable>
  </BasicModal>
</template>
<script lang="ts" setup>
import { ref } from 'vue';
import { BasicTable, TableAction } from '/@/components/Table';
import { useListPage } from '/@/hooks/system/useListPage';
import { BasicModal, useModalInner } from '/@/components/Modal';
import { list } from '/@/views/dataCalculation/pi/allocationTable/QueryTable.api';
const emit = defineEmits(['success']);
const queryParam = ref<any>({});
const tableHeight = ref(500);
// 列表页面公共参数、方法
const { tableContext, } = useListPage({
  tableProps: {
    title: '',
    api: list,
    columns: [],
    canResize: true,
    useSearchForm: false,
    showTableSetting: false,
    showActionColumn: false,
    pagination: {
      pageSizeOptions: ['10', '20']
    },
    beforeFetch: (params) => {
      return Object.assign(params, queryParam.value);
    },
  }
});

// BasicTable绑定注册
const [registerTable, { reload,setProps }, { rowSelection, selectedRowKeys }] = tableContext;

// //注册modal
const [registerModal, { closeModal,changeLoading }] = useModalInner(async (data: any) => {
  changeLoading(false);
  //设置查询条件
  queryParam.value = data.queryParam;
  //设置表格列和数据
  setProps({
    columns: data.piColumns,
  });
  //设置高度
  // calculateHeight();
  handleSuccess();
});

function handleSuccess() {
  (selectedRowKeys.value = []) && reload();
}
function handleChoose(record) {
  emit('success', record);
  closeModal();
}
//确认导出
function handleOk(){
  console.log(rowSelection.selectedRows);
  if(rowSelection.selectedRows && rowSelection.selectedRows.length > 0){
    changeLoading(true);
  }
  emit('success', rowSelection.selectedRows);
}

</script>
