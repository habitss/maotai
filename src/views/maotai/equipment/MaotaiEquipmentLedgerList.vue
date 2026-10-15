<template>
  <div class="p-1">
    <BasicTable @register="registerTable">
      <!-- 工具栏 -->
      <template #tableTitle>
        <a-button type="primary" preIcon="ant-design:plus-outlined" @click="handleAdd">新增</a-button>
        <a-button type="primary" preIcon="ant-design:export-outlined" @click="onExportXls">导出</a-button>
        <j-upload-button type="primary" preIcon="ant-design:import-outlined" @click="onImportXls">导入</j-upload-button>
        <a-dropdown v-if="selectedRowKeys.length > 0">
          <template #overlay>
            <a-menu>
              <a-menu-item key="1" @click="batchHandleDelete">
                <Icon icon="ant-design:delete-outlined" />
                批量删除
              </a-menu-item>
            </a-menu>
          </template>
          <a-button>批量操作 <Icon icon="mdi:chevron-down" /></a-button>
        </a-dropdown>
      </template>
      <!-- 操作栏 -->
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)" :dropDownActions="getDropDownAction(record)" />
      </template>
    </BasicTable>

    <!-- 新增、编辑、详情modal -->
    <FormModal @register="registerActionModal" :width="880" @success="reload" />
  </div>
</template>
<script lang="ts" name="maotai-equipment-ledger" setup>
import { BasicTable, TableAction, EditRecordRow } from '/@/components/Table';
import { Icon } from '/@/components/Icon';
import { list, deleteOne, batchDelete, getImportUrl, getExportUrl } from './MaotaiEquipmentLedger.api';
import { columns, searchFormSchema } from './MaotaiEquipmentLedger.data';
import { useListPage } from '/@/hooks/system/useListPage';
import { useMessage } from '/@/hooks/web/useMessage';
import { useModal } from '/@/components/Modal';
import { useRouter } from 'vue-router';
import { nextTick } from 'vue';
import FormModal from './components/MaotaiEquipmentLedgerForm.vue';

// 路由(详情页跳转)
const router = useRouter();

// 新增、编辑、详情modal
const [registerActionModal, { openModal }] = useModal();

// 列表页面公共参数、方法
const { tableContext, onExportXls, onImportXls } = useListPage({
  tableProps: {
    title: '设备台账',
    api: list,
    columns,
    showActionColumn: true,
    actionColumn: { width: 120 },
    rowSelection: { columnWidth: 20 },
    formConfig: {
      schemas: searchFormSchema,
      labelCol: { style: { width: '96px' } },
      showAdvancedButton: false,
      // 覆盖 useListPage 默认的自适应 xs/sm/md/lg/xl/xxl(那套会让 1280 宽下 4 条件换行)
      baseColProps: { span: 5 },
      // 按钮列同样显式钉死各档位 span=4(框架会把自适应键混进 col class,不钉死会掉到第二行)
      actionColOptions: { span: 4, xs: 4, sm: 4, md: 4, lg: 4, xl: 4, xxl: 4 },
      fieldMapToTime: [['createTime', ['createTime_begin', 'createTime_end'], 'YYYY-MM-DD']],
    },
  },
  exportConfig: { name: '设备台账', url: getExportUrl },
  importConfig: { url: getImportUrl, success: handleSuccess },
});

// BasicTable绑定注册
const [registerTable, { reload }, { selectedRowKeys }] = tableContext;

// 消息提示
const { createMessage: msg } = useMessage();

/** 操作栏(主操作) */
function getTableAction(record) {
  return [{ label: '编辑', onClick: handleEdit.bind(null, record) }];
}

/** 下拉操作栏 */
function getDropDownAction(record) {
  return [
    { label: '详情', onClick: handleDetail.bind(null, record) },
    {
      label: '删除',
      popConfirm: { title: '确认删除该设备?', confirm: handleDelete.bind(null, record), placement: 'left' },
    },
  ];
}

/** 新增 */
function handleAdd() {
  nextTick(() => openModal(true, { action: 'add' }));
}

/** 编辑 */
function handleEdit(record: EditRecordRow) {
  openModal(true, { action: 'edit', record });
}

/** 详情(跳转独立详情页) */
function handleDetail(record: EditRecordRow) {
  router.push({ path: '/maotai/equipmentLedger/detail', query: { id: record.id } });
}

/** 删除 */
async function handleDelete(record: EditRecordRow) {
  try {
    await deleteOne({ id: record.id }, handleSuccess);
    reload();
  } catch (error) {
    msg.error({ content: '操作失败' });
  }
}

/** 批量删除 */
async function batchHandleDelete() {
  await batchDelete({ ids: selectedRowKeys.value }, handleSuccess);
}

/** 成功回调 */
function handleSuccess() {
  (selectedRowKeys.value = []) && reload();
}
</script>

<style scoped>
:deep(.ant-input-number) {
  width: 100%;
}
:deep(.ant-picker) {
  width: 100%;
}
:deep(.ant-select-selection-placeholder) {
  color: #bebebe !important;
}
</style>
