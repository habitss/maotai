<template>
  <div>
    <!--引用表格-->
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <!--插槽:table标题-->
      <template #tableTitle>
        <a-button type="primary" v-auth="'post_check:mes3_post_check_record_config_main:add'" @click="handleAdd"
          preIcon="ant-design:plus-outlined"> 新增</a-button>
        <a-button type="primary" v-auth="'post_check:mes3_post_check_record_config_main:exportXls'"
          preIcon="ant-design:export-outlined" @click="onExportXls"> 导出</a-button>
        <j-upload-button type="primary" v-auth="'post_check:mes3_post_check_record_config_main:importExcel'"
          preIcon="ant-design:import-outlined" @click="onImportXls">导入</j-upload-button>
        <a-dropdown v-if="selectedRowKeys.length > 0">
          <template #overlay>
            <a-menu>
              <a-menu-item key="1" @click="batchHandleDelete">
                <Icon icon="ant-design:delete-outlined"></Icon>
                删除
              </a-menu-item>
            </a-menu>
          </template>
          <a-button v-auth="'post_check:mes3_post_check_record_config_main:deletionBatch'">批量操作
            <Icon icon="mdi:chevron-down"></Icon>
          </a-button>
        </a-dropdown>
      </template>
      <!--操作栏-->
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)" />
      </template>

      <!--  deptCode部门查询插槽    -->
      <template #form-deptCode="{ model, field }">
        <kt-depart-select v-model:value="model[field]" select-type="depart" placeholder="请选择部门"
          @change="handleDeptChange" />
      </template>

      <!--  lineCode产线查询插槽    -->
      <template #form-lineCode="{ model, field }">
        <kt-depart-select v-model:value="model[field]" select-type="line" placeholder="请选择产线"
          :parentKey="model.deptCode" />
      </template>
    </BasicTable>
    <!-- 表单区域 -->
    <PostCheckRecordConfigMainModal @register="registerModal" @success="handleSuccess"></PostCheckRecordConfigMainModal>
    <!-- 配置区域 -->
    <PostCheckRecordConfigProjectModal :propsReportType=reportType @register="registerConfigModal" @success="handleSuccess"></PostCheckRecordConfigProjectModal>
  </div>
</template>

<script lang="ts" name="post_check-postCheckRecordConfigMain" setup>
import { reactive } from 'vue';
import { BasicTable, TableAction } from '/@/components/Table';
import { useModal } from '/@/components/Modal';
import { useListPage } from '/@/hooks/system/useListPage'
import PostCheckRecordConfigMainModal from './components/PostCheckRecordConfigMainModal.vue'
import PostCheckRecordConfigProjectModal from './components/PostCheckRecordConfigProjectModal.vue'
import { columns, searchFormSchema } from './PostCheckRecordConfigMain.data';
import { list, deleteOne, batchDelete, getImportUrl, getExportUrl } from './PostCheckRecordConfigMain.api';
import KtDepartSelect from '/@/components/KtDepartSelect/KtDepart.vue';
import { useRoute } from 'vue-router';
//获取路由
const route = useRoute();
//获取路由中的code参数
const reportType = route.params.code;
const queryParam = reactive<any>({ reportType });
//注册表单model
const [registerModal, { openModal }] = useModal();
//注册配置model
const [registerConfigModal, { openModal : openConfigModal }] = useModal();
//注册table数据
const { tableContext, onExportXls, onImportXls } = useListPage({
  tableProps: {
    title: '',
    api: list,
    columns,
    canResize: false,
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
      width: 160,
      fixed: 'right'
    },
    defSort: {
      column: 'sort',
      order: 'asc',
    },
    beforeFetch: (params) => {
      params.reportType = reportType;
      params.reportName = params?.reportName ? `*${params.reportName}*` : null;
      return Object.assign(params, queryParam);
    },
  },
  exportConfig: {
    name: reportType == '1' ? "岗位记录配置主表" : "岗位检查配置主表",
    url: getExportUrl,
    params: queryParam,
  },
  importConfig: {
    url: getImportUrl,
    success: handleSuccess
  },
})
const [registerTable, { reload, getForm }, { rowSelection, selectedRowKeys }] = tableContext

/**
 * 新增事件
 */
function handleAdd() {
  openModal(true, {
    reportType: reportType,
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
 * 配置事件
*/
function handleConfig(record: Recordable) {
  openConfigModal(true, {
    record
  });
}
/**
 * 删除事件
 */
async function handleDelete(record) {
  await deleteOne({ id: record.id }, handleSuccess);
}
/**
 * 批量删除事件
 */
async function batchHandleDelete() {
  await batchDelete({ ids: selectedRowKeys.value }, handleSuccess);
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
      auth: 'post_check:mes3_post_check_record_config_main:edit'
    }, 
    {
      label: '删除',
      popConfirm: {
        title: '是否确认删除',
        confirm: handleDelete.bind(null, record),
        placement: 'topLeft',
      },
      auth: 'post_check:mes3_post_check_record_config_main:delete'
    },
    {
      label: '配置',
      onClick: handleConfig.bind(null, record),
      auth: 'post_check:mes3_post_check_record_config_main:config'
    }, 
  ]
}

//部门改变事件
async function handleDeptChange(value) {
  //清空产线
  await getForm().setFieldsValue({
    lineCode: '',
  });
}
</script>

<style lang="less" scoped>
:deep(.ant-picker),
:deep(.ant-input-number) {
  width: 100%;
}
</style>