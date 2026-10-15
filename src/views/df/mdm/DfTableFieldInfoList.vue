<template>
  <div>
    <!--引用表格-->
    <BasicTable @register="registerTable">
      <!--插槽:table标题-->
      <template #tableTitle>
        <a-button type="primary" v-auth="'df.mdm:mes3_mdm_df_table_info:add'" @click="handleSync"
          preIcon="ant-design:plus-outlined">字段同步</a-button>
      </template>
      <!--操作栏-->
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)" />
      </template>
    </BasicTable>
    <!-- 表单区域 -->
    <DfTableFieldInfoModal @register="registerModal" @success="handleSuccess"></DfTableFieldInfoModal>
  </div>
</template>

<script lang="ts" name="df.mdm-dfTableFieldInfo" setup>
import { reactive } from 'vue';
import { BasicTable, TableAction } from '/@/components/Table';
import { useModal } from '/@/components/Modal';
import { useListPage } from '/@/hooks/system/useListPage'
import DfTableFieldInfoModal from './components/DfTableFieldInfoModal.vue'
import { columns, searchFormSchema } from './DfTableFieldInfo.data';
import { list, syncDfTableField } from './DfTableFieldInfo.api';
import { cloneDeep } from 'lodash-es';
const queryParam = reactive<any>({});
//父组件传递参数对象
const props = defineProps({
  parentRecord: { type: Object, default: () => ({}) },
});
//注册model
const [registerModal, { openModal }] = useModal();
//注册table数据
const { tableContext } = useListPage({
  tableProps: {
    title: 'DF数据库表字段信息',
    api: list,
    columns,
    canResize: true,
    pagination: {
      pageSizeOptions: ['50', '100', '300'],
      pageSize: 50
    },
    formConfig: {
      //labelWidth: 120,
      schemas: searchFormSchema,
      autoSubmitOnEnter: true,
      showAdvancedButton: true,
      fieldMapToNumber: [
      ],
      fieldMapToTime: [
      ],
    },
    actionColumn: {
      width: 120,
      fixed: 'right'
    },
    beforeFetch: (params) => {
      queryParam.dfTableId = props.parentRecord.id;
      return Object.assign(params, queryParam);
    },
  },
})

const [registerTable, { reload, setLoading }, { selectedRowKeys }] = tableContext

/**
 * 编辑事件
 */
function handleEdit(record: Recordable) {
  openModal(true, {
    record,
    isUpdate: true,
    showFooter: true,
  });
}
/**
 * 字段同步
 */
async function handleSync(){
  try {
    setLoading(true);
    let params = cloneDeep(props.parentRecord);
    params.dfTableId = params.id;
    await syncDfTableField(params);
    handleSuccess();
  } catch (error) {
    console.log(error);
  } finally {
    setLoading(false);
  }
}
/**
 * 成功回调
 */
function handleSuccess() {
  (selectedRowKeys.value = []) && reload();
}
/**
   * 操作栏
   */
function getTableAction(record) {
  return [
    {
      label: '编辑',
      onClick: handleEdit.bind(null, record),
      auth: 'df.mdm:mes3_mdm_df_table_field_info:edit'
    }
  ]
}
</script>

<style lang="less" scoped>
:deep(.ant-picker),
:deep(.ant-input-number) {
  width: 100%;
}
</style>