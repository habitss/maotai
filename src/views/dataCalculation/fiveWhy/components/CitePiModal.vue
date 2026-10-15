<template>
  <BasicModal v-bind="$attrs" @register="registerModal" destroyOnClose :title width="80vw" :height="600"
    :maskClosable="false" :showOkBtn="false">
    <!--引用表格-->
    <BasicTable @register="registerTable">
      <!--操作栏-->
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)" />
      </template>
    </BasicTable>
  </BasicModal>
</template>

<script lang="ts" setup>
import { reactive, ref } from 'vue';
import { BasicTable, TableAction } from '/@/components/Table';
import { BasicColumn } from '/@/components/Table';
import { useListPage } from '/@/hooks/system/useListPage'
import { BasicModal, useModalInner } from '/@/components/Modal';
import { list, head } from '/@/views/dataCalculation/pi/allocationTable/QueryTable.api';
import { cloneDeep } from 'lodash-es';
//注册emit
const emit = defineEmits(['success']);
//查询参数
const queryParam = reactive<any>({});
//上级页面所传数据
const parentData = ref();
//标题
const title = ref('');
//注册table数据
const { tableContext } = useListPage({
  tableProps: {
    title: '',
    api: list,
    canResize: true,
    showTableSetting: false,
    immediate: false,
    beforeFetch: (params) => {
      return Object.assign(params, queryParam);
    },
    actionColumn: {
      width: 260,
      fixed: 'right'
    },
  },
})
const [registerTable, { reload, setProps }] = tableContext;

//表单赋值
const [registerModal, { closeModal }] = useModalInner(async (data) => {
  parentData.value = data.params;
  //设置查询条件
  queryParam.allocationTableId = data.params.allocationTableId;
  queryParam.tableId = data.params.tableId;
  queryParam.tableName = data.params.tableCode;
  queryParam.citeField = data.params.fieldCode;
  title.value = `5WHY分析重复引用（${data.params.fieldName}）`;
  //获取表头数据
  const res = await head(queryParam);
  let columns: BasicColumn[] = [];
  if (res && res.length > 0) {
    for (const item of res) {
      columns.push({
        title: item.name,
        dataIndex: item.fieldName,
        align: 'center',
      })
    }
  }
  //将当前需要重复引用的pi字段添加到表头
  columns.push({
    title: data.params.fieldName,
    dataIndex: data.params.fieldCode,
    align: 'center',
  });
  //设置表头
  await setProps({ columns });
  //刷新列表
  reload();
});
/**
 * 操作栏
 */
function getTableAction(record) {
  return [
    {
      label: '选择',
      onClick: handleSelect.bind(null, record),
    }
  ]
}

function handleSelect(record) {
  let fiveWhyData = cloneDeep(record[queryParam.citeField+'_f']);
  emit('success', parentData.value, fiveWhyData);
  closeModal();
}
</script>