<template>
  <div>
    <!--引用表格-->
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <!--插槽:table标题-->
      <template #tableTitle>
        <a-button type="primary" v-auth="'plan:mes3_plan:add'" @click="handleAdd" preIcon="ant-design:plus-outlined">
          新增</a-button>
        <a-button type="primary" v-auth="'plan:mes3_action_plan_problem_type:exportXls'"
          preIcon="ant-design:export-outlined" @click="onExportXls"> 导出</a-button>
        <j-upload-button type="primary" v-auth="'plan:mes3_action_plan_problem_type:importExcel'"
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
          <a-button v-auth="'plan:mes3_plan:deletionBatch'">批量操作
            <Icon icon="mdi:chevron-down"></Icon>
          </a-button>
        </a-dropdown>
      </template>
      <!--操作栏-->
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)" :dropDownActions="getDropDownAction(record)" />
      </template>
      <!--字段回显插槽-->
      <template v-slot:bodyCell="">
      </template>
    </BasicTable>
    <!-- 表单区域 -->
    <PlanModal @register="registerModal" @success="handleSuccess"></PlanModal>
    <!-- 任务明细 -->
    <PlanTaskDetailModal @register="registerTaskModal" @success="handleSuccess"></PlanTaskDetailModal>
    <!-- 暂停记录 -->
    <PlanPauseModal @register="registerPauseModal"></PlanPauseModal>
  </div>
</template>

<script lang="ts" name="mes3-plan-plan" setup>
import { reactive, ref } from 'vue';
import { BasicTable, TableAction } from '/@/components/Table';
import { useModal } from '/@/components/Modal';
import { useListPage } from '/@/hooks/system/useListPage';
import PlanModal from './components/PlanModal.vue';
import PlanTaskDetailModal from './components/PlanTaskDetailModal.vue';
import PlanPauseModal from './components/PlanPauseModal.vue';
import { columns, searchFormSchema } from './Plan.data';
import { list, deleteOne, batchDelete, getImportUrl, getExportUrl } from './Plan.api';
const queryParam = reactive<any>({});
const taskItemName = ref('');
//注册model
const [registerModal, { openModal }] = useModal();
//注册model
const [registerTaskModal, { openModal : openTaskModal }] = useModal();
//注册model
const [registerPauseModal, { openModal : openPauseModal}] = useModal();
//注册table数据
const { tableContext, onExportXls, onImportXls } = useListPage({
  tableProps: {
    title: '工单管理',
    api: list,
    columns,
    canResize: true,
    formConfig: {
      schemas: searchFormSchema,
      autoSubmitOnEnter: true,
      showAdvancedButton: true,
      fieldMapToTime: [
        ['ymd', ['ymd_begin', 'ymd_end'], 'YYYY-MM-DD'],
      ],
    },
    actionColumn: {
      width: 140,
      fixed: 'right'
    },
    beforeFetch: (params) => {
      params.orderName = params.orderName ? `*${params.orderName}*` : null;
      params.planName = params.planName ? `*${params.planName}*` : null;
      taskItemName.value = params.taskItemName;
      return Object.assign(params, queryParam);
    },
    defSort: { 
      column: 'ymd', 
      order: 'desc' 
    },
  },
  exportConfig: {
    name: "工单管理",
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
 * 详情
*/
function handleDetail(record: Recordable) {
  openTaskModal(true, {
    record,
    isUpdate: true,
    showFooter: false,
    taskItemName: taskItemName.value
  });
}
/**
 * 暂停记录
*/
function handlePause(record: Recordable) {
  openPauseModal(true, {
    record,
    isUpdate: true,
    showFooter: false,
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
      auth: 'plan:mes3_plan:edit'
    },
  ]
}

/**
 * 下拉操作栏
 */
function getDropDownAction(record) {
  return [
    {
      label: '任务明细',
      onClick: handleDetail.bind(null, record),
    }, 
    {
      label: '暂停记录',
      onClick: handlePause.bind(null, record),
    }, 
    {
      label: '删除',
      popConfirm: {
        title: '是否确认删除',
        confirm: handleDelete.bind(null, record),
        placement: 'topLeft',
      },
      auth: 'plan:mes3_plan:delete'
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