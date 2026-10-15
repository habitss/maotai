<template>
  <div>
    <!--引用表格-->
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <!--插槽:table标题-->
      <template #tableTitle>
        <a-button type="primary" v-auth="'practice:mes3_best_practice_type:add'" @click="handleAdd"
          preIcon="ant-design:plus-outlined"> 新增</a-button>
        <a-dropdown v-if="selectedRowKeys.length > 0">
          <template #overlay>
            <a-menu>
              <a-menu-item key="1" @click="batchHandleDelete">
                <Icon icon="ant-design:delete-outlined"></Icon>
                删除
              </a-menu-item>
            </a-menu>
          </template>
          <a-button v-auth="'practice:mes3_best_practice_type:deletionBatch'">批量操作
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
    <BestPracticeTypeModal @register="registerModal" @success="handleSuccess"></BestPracticeTypeModal>
    <!-- 配置modal -->
    <BestPracticeConfigModal @register="registerConfigModal" @success="handleSuccess"></BestPracticeConfigModal>
  </div>
</template>

<script lang="ts" name="practice-bestPracticeType" setup>
import { reactive } from 'vue';
import { BasicTable, TableAction } from '/@/components/Table';
import { useModal } from '/@/components/Modal';
import { useListPage } from '/@/hooks/system/useListPage'
import BestPracticeTypeModal from './components/BestPracticeTypeModal.vue'
import BestPracticeConfigModal from './components/BestPracticeConfigModal.vue'
import { columns, searchFormSchema } from './BestPracticeType.data';
import { list, deleteOne, batchDelete, getImportUrl, getExportUrl } from './BestPracticeType.api';
const queryParam = reactive<any>({});
//注册model
const [registerModal, { openModal }] = useModal();
//注册ConfigModel
const [registerConfigModal, { openModal: openConfigModal }] = useModal();
//注册table数据
const { tableContext } = useListPage({
  tableProps: {
    title: '最佳实践类型',
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
    beforeFetch: (params) => {
      params.typeName = params.typeName ? `*${params.typeName}*` : null;
      params.column = 'sort';
      params.order = 'asc';
      return Object.assign(params, queryParam);
    },
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
 * 配置事件
 */
function handleConfig(record: Recordable) {
  openConfigModal(true, {
    record
  });
}
/**
 * 详情
*/
function handleDetail(record: Recordable) {
  openModal(true, {
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
      auth: 'practice:mes3_best_practice_type:edit'
    },
    {
      label: '配置',
      onClick: handleConfig.bind(null, record),
      auth: 'practice:mes3_best_practice_type:config'
    }
  ]
}
/**
   * 下拉操作栏
   */
function getDropDownAction(record) {
  return [
    {
      label: '详情',
      onClick: handleDetail.bind(null, record),
    }, {
      label: '删除',
      popConfirm: {
        title: '是否确认删除',
        confirm: handleDelete.bind(null, record),
        placement: 'topLeft',
      },
      auth: 'practice:mes3_best_practice_type:delete'
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