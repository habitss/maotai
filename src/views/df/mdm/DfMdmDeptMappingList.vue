<template>
  <div>
    <!--引用表格-->
    <BasicTable @register="registerTable">
      <!--插槽:table标题-->
      <template #tableTitle>
        <a-button type="primary" v-auth="'df.mdm:mes3_mdm_df_dept_mapping:add'" @click="handleAdd"
          preIcon="ant-design:plus-outlined"> 新增</a-button>
        <a-button type="primary" v-auth="'df.mdm:mes3_mdm_df_dept_mapping:exportXls'"
          preIcon="ant-design:export-outlined" @click="onExportXls"> 导出</a-button>
        <j-upload-button type="primary" v-auth="'df.mdm:mes3_mdm_df_dept_mapping:importExcel'"
          preIcon="ant-design:import-outlined" @click="onImportXls">导入</j-upload-button>
      </template>
      <!--操作栏-->
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)" />
      </template>
      <!--字段回显插槽-->
      <template v-slot:bodyCell="">
      </template>
    </BasicTable>
    <!-- 表单区域 -->
    <DfMdmDeptMappingModal @register="registerModal" @success="handleSuccess"></DfMdmDeptMappingModal>
  </div>
</template>

<script lang="ts" name="df.mdm-dfMdmDeptMapping" setup>
import { reactive } from 'vue';
import { BasicTable, TableAction } from '/@/components/Table';
import { useModal } from '/@/components/Modal';
import { useListPage } from '/@/hooks/system/useListPage'
import DfMdmDeptMappingModal from './components/DfMdmDeptMappingModal.vue'
import { columns, searchFormSchema } from './DfMdmDeptMapping.data';
import { list, getImportUrl, getExportUrl } from './DfMdmDeptMapping.api';
const queryParam = reactive<any>({});
//注册model
const [registerModal, { openModal }] = useModal();
//注册table数据
const { tableContext, onExportXls, onImportXls } = useListPage({
  tableProps: {
    title: 'MDM部门与系统部门映射关系表',
    api: list,
    columns,
    formConfig: {
      schemas: searchFormSchema,
      autoSubmitOnEnter: true,
      showAdvancedButton: true,
      fieldMapToNumber: [
      ],
      fieldMapToTime: [
      ],
    },
    defSort:{
      column: 'mdmFullCode',
      order: 'asc'
    },
    actionColumn: {
      width: 120,
      fixed: 'right'
    },
    beforeFetch: (params) => {
      return Object.assign(params, queryParam);
    },
  },
  exportConfig: {
    name: "MDM部门与系统部门映射关系表",
    url: getExportUrl,
    params: queryParam,
  },
  importConfig: {
    url: getImportUrl,
    success: handleSuccess
  },
})

const [registerTable, { reload }, { rowSelection, selectedRowKeys }] = tableContext

/**
 * 新增事件
 */
function handleAdd() {
  openModal(true, {
    isUpdate: false,
    showFooter: true,
  });
}
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
      label: '配置映射',
      onClick: handleEdit.bind(null, record),
      auth: 'df.mdm:mes3_mdm_df_dept_mapping:edit'
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