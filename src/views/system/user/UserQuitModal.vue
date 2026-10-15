<template>
  <BasicModal
    v-bind="$attrs"
    @register="registerModal"
    :title="t('language.resignationPersonnelInfo')"
    :showOkBtn="false"
    width="1000px"
    destroyOnClose
  >
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <!--插槽:table标题-->
      <template #tableTitle>
        <a-dropdown v-if="selectedRowKeys.length > 0">
          <template #overlay>
            <a-menu>
              <a-menu-item key="1" @click="batchHandleRevert">
                <Icon icon="ant-design:redo-outlined"></Icon>
                {{ t('language.batchCancel') }}
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
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)" />
      </template>
    </BasicTable>
  </BasicModal>
</template>

<script lang="ts" setup name="user-quit-modal">
import { ref, toRaw, unref } from 'vue';
import { BasicModal, useModalInner } from '/@/components/Modal';
import { BasicTable, useTable, TableAction } from '/@/components/Table';
import { recycleColumns } from './user.data';
import { getQuitList, putCancelQuit, deleteRecycleBin } from './user.api';
import { useMessage } from '/@/hooks/web/useMessage';
import { useListPage } from '/@/hooks/system/useListPage';
import { Modal } from 'ant-design-vue';
import { defHttp } from '/@/utils/http/axios';
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
const { prefixCls, tableContext } = useListPage({
  tableProps: {
    api: getQuitList,
    columns: recycleColumns,
    rowKey: 'id',
    canResize: false,
    useSearchForm: false,
    actionColumn: {
      width: 120,
    },
  },
});
//注册table数据
const [registerTable, { reload }, { rowSelection, selectedRowKeys, selectedRows }] = tableContext;

/**
 * 取消离职事件
 * @param record
 */
async function handleCancelQuit(record) {
  await putCancelQuit({ userIds: record.id, usernames: record.username }, reload);
  emit('success');
}
/**
 * 批量取消离职事件
 */
function batchHandleRevert() {
  Modal.confirm({
    title: t('language.cancelResignation'),
    content: t('language.cancelResignationNotice'),
    okText: t('language.okText'),
    cancelText: t('language.cancel'),
    onOk: () => {
      let rowValue = selectedRows.value;
      let rowData: any = [];
      for (const value of rowValue) {
        rowData.push(value.username);
      }
      handleCancelQuit({ id: toRaw(unref(selectedRowKeys)).join(','), username: rowData.join(',') });
    },
  });
}

//获取操作栏事件
function getTableAction(record) {
  return [
    {
      label: t('language.cancelResignation'),
      icon: 'ant-design:redo-outlined',
      popConfirm: {
        title: t('language.confirmCancelResignation'),
        confirm: handleCancelQuit.bind(null, record),
      },
    },
  ];
}
</script>

<style scoped lang="less">
:deep(.ant-popover-inner-content) {
  width: 185px !important;
}
</style>
