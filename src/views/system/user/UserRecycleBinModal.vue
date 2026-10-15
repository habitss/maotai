<template>
  <BasicModal v-bind="$attrs" @register="registerModal" :title="t('language.recycle_bin')" :showOkBtn="false" width="1000px" destroyOnClose>
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <!--插槽:table标题-->
      <template #tableTitle>
        <a-dropdown v-if="checkedKeys.length > 0">
          <template #overlay>
            <a-menu>
              <a-menu-item key="1" @click="batchHandleDelete">
                <Icon icon="ant-design:delete-outlined"></Icon>
                {{ t('language.delete_in_batches') }}
              </a-menu-item>
              <a-menu-item key="1" @click="batchHandleRevert">
                <Icon icon="ant-design:redo-outlined"></Icon>
                {{ t('language.Batch_reduction') }}
              </a-menu-item>
            </a-menu>
          </template>
          <a-button
            >{{ t('language.batchOperation') }}
            <Icon icon="ant-design:down-outlined"></Icon>
          </a-button>
        </a-dropdown>
      </template>
      <!--操作栏-->
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'action'">
          <TableAction :actions="getTableAction(record)" />
        </template>
      </template>
    </BasicTable>
  </BasicModal>
</template>
<script lang="ts" setup>
import { ref, toRaw, unref } from 'vue';
import { BasicModal, useModalInner } from '/@/components/Modal';
import { BasicTable, useTable, TableAction } from '/@/components/Table';
import { recycleColumns } from './user.data';
import { getRecycleBinList, putRecycleBin, deleteRecycleBin } from './user.api';
import { useMessage } from '/@/hooks/web/useMessage';
import { useI18n } from '/@/hooks/web/useI18n';
const { t } = useI18n();
const { createConfirm } = useMessage();
// 声明Emits
const emit = defineEmits(['success', 'register']);
const checkedKeys = ref<Array<string | number>>([]);
const [registerModal] = useModalInner(() => {
  checkedKeys.value = [];
});
//注册table数据
const [registerTable, { reload }] = useTable({
  api: getRecycleBinList,
  columns: recycleColumns,
  rowKey: 'id',
  striped: true,
  useSearchForm: false,
  showTableSetting: false,
  clickToRowSelect: false,
  bordered: true,
  showIndexColumn: false,
  pagination: true,
  tableSetting: { fullScreen: true },
  canResize: false,
  actionColumn: {
    width: 150,
    title: t('language.action'),
    dataIndex: 'action',
    // slots: { customRender: 'action' },
    fixed: undefined,
  },
});
/**
 * 选择列配置
 */
const rowSelection = {
  type: 'checkbox',
  columnWidth: 50,
  selectedRowKeys: checkedKeys,
  onChange: onSelectChange,
};
/**
 * 选择事件
 */
function onSelectChange(selectedRowKeys: (string | number)[]) {
  checkedKeys.value = selectedRowKeys;
}
/**
 * 还原事件
 */
async function handleRevert(record) {
  await putRecycleBin({ userIds: record.id }, reload);
  emit('success');
}
/**
 * 批量还原事件
 */
function batchHandleRevert() {
  handleRevert({ id: toRaw(unref(checkedKeys)).join(',') });
}
/**
 * 删除事件
 */
async function handleDelete(record) {
  await deleteRecycleBin({ userIds: record.id }, reload);
}
/**
 * 批量删除事件
 */
function batchHandleDelete() {
  createConfirm({
    iconType: 'warning',
    title: t('language.delText'),
    content: t('language.confirmDelNotice'),
    onOk: () => handleDelete({ id: toRaw(unref(checkedKeys)).join(',') }),
    onCancel() {},
  });
}
//获取操作栏事件
function getTableAction(record) {
  return [
    {
      label: t('language.getBack'),
      icon: 'ant-design:redo-outlined',
      popConfirm: {
        title: t('language.confirmBack'),
        confirm: handleRevert.bind(null, record),
      },
    },
    {
      label: t('language.permanentlyDelete'),
      icon: 'ant-design:scissor-outlined',
      popConfirm: {
        title: t('language.confirmDelete'),
        confirm: handleDelete.bind(null, record),
      },
    },
  ];
}
</script>
