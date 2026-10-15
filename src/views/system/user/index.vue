<template>
  <div>
    <!--引用表格-->
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <!--插槽:table标题-->
      <template #tableTitle>
        <a-button type="primary" preIcon="ant-design:plus-outlined" @click="handleCreate"> {{ t('language.add') }}</a-button>
        <a-button type="primary" v-auth="'system:user:export'" preIcon="ant-design:export-outlined" @click="onExportXls">
          {{ t('language.export') }}</a-button
        >
        <j-upload-button type="primary" v-auth="'system:user:import'" preIcon="ant-design:import-outlined" @click="onImportXls">
          {{ t('language.import') }}</j-upload-button
        >
        <a-button type="primary" @click="openModal(true, {})" preIcon="ant-design:hdd-outlined"> {{ t('language.recycle_bin') }}</a-button>
        <!--        <JThirdAppButton biz-type="user" :selected-row-keys="selectedRowKeys" syncToApp syncToLocal @sync-finally="onSyncFinally" />-->
        <a-dropdown v-if="selectedRowKeys.length > 0">
          <template #overlay>
            <a-menu>
              <a-menu-item key="1" @click="batchHandleDelete">
                <Icon icon="ant-design:delete-outlined"></Icon>
                {{ t('language.delText') }}
              </a-menu-item>
              <a-menu-item key="2" @click="batchFrozen(2)">
                <Icon icon="ant-design:lock-outlined"></Icon>
                {{ t('language.freeze') }}
              </a-menu-item>
              <a-menu-item key="3" @click="batchFrozen(1)">
                <Icon icon="ant-design:unlock-outlined"></Icon>
                {{ t('language.thaw') }}
              </a-menu-item>
            </a-menu>
          </template>
          <a-button
            >{{ t('language.batchOperation') }}
            <Icon icon="mdi:chevron-down"></Icon>
          </a-button>
        </a-dropdown>
      </template>
      <!--操作栏-->
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)" :dropDownActions="getDropDownAction(record)" />
      </template>
    </BasicTable>
    <!--用户抽屉-->
    <UserDrawer @register="registerDrawer" @success="handleSuccess" />
    <!--修改密码-->
    <PasswordModal @register="registerPasswordModal" @success="reload" />
    <!--用户代理-->
    <UserAgentModal @register="registerAgentModal" @success="reload" />
    <!--回收站-->
    <UserRecycleBinModal @register="registerModal" @success="reload" />
    <!-- 离职受理人弹窗 -->
    <UserQuitAgentModal @register="registerQuitAgentModal" @success="reload" />
    <!-- 离职人员列弹窗 -->
    <UserQuitModal @register="registerQuitModal" @success="reload" />
  </div>
</template>

<script lang="ts" name="system-user" setup>
//ts语法
import { ref, computed, unref } from 'vue';
import { BasicTable, TableAction, ActionItem } from '/@/components/Table';
import UserDrawer from './UserDrawer.vue';
import UserRecycleBinModal from './UserRecycleBinModal.vue';
import PasswordModal from './PasswordModal.vue';
import UserAgentModal from './UserAgentModal.vue';
import JThirdAppButton from '/@/components/jeecg/thirdApp/JThirdAppButton.vue';
import UserQuitAgentModal from './UserQuitAgentModal.vue';
import UserQuitModal from './UserQuitModal.vue';
import { useDrawer } from '/@/components/Drawer';
import { useListPage } from '/@/hooks/system/useListPage';
import { useModal } from '/@/components/Modal';
import { useMessage } from '/@/hooks/web/useMessage';
import { columns, searchFormSchema } from './user.data';
import { listNoCareTenant, deleteUser, batchDeleteUser, getImportUrl, getExportUrl, frozenBatch } from './user.api';
import { usePermission } from '/@/hooks/web/usePermission';
import { useI18n } from '/@/hooks/web/useI18n';
const { t } = useI18n();

const { createMessage, createConfirm } = useMessage();
const { isDisabledAuth } = usePermission();
//注册drawer
const [registerDrawer, { openDrawer }] = useDrawer();
//回收站model
const [registerModal, { openModal }] = useModal();
//密码model
const [registerPasswordModal, { openModal: openPasswordModal }] = useModal();
//代理人model
const [registerAgentModal, { openModal: openAgentModal }] = useModal();
//离职代理人model
const [registerQuitAgentModal, { openModal: openQuitAgentModal }] = useModal();
//离职用户列表model
const [registerQuitModal, { openModal: openQuitModal }] = useModal();

// 列表页面公共参数、方法
const { prefixCls, tableContext, onExportXls, onImportXls } = useListPage({
  designScope: 'user-list',
  tableProps: {
    title: t('language.userList'),
    api: listNoCareTenant,
    columns: columns,
    size: 'small',
    formConfig: {
      // labelWidth: 200,
      schemas: searchFormSchema,
    },
    actionColumn: {
      width: 120,
    },
    beforeFetch: (params) => {
      return Object.assign({ column: 'createTime', order: 'desc' }, params);
    },
  },
  exportConfig: {
    name: t('language.userList'),
    url: getExportUrl,
  },
  importConfig: {
    url: getImportUrl,
  },
});

//注册table数据
const [registerTable, { reload, updateTableDataRecord }, { rowSelection, selectedRows, selectedRowKeys }] = tableContext;

/**
 * 新增事件
 */
function handleCreate() {
  openDrawer(true, {
    isUpdate: false,
    showFooter: true,
    tenantSaas: false,
  });
}
/**
 * 编辑事件
 */
async function handleEdit(record: Recordable) {
  openDrawer(true, {
    record,
    isUpdate: true,
    showFooter: true,
    tenantSaas: false,
  });
}
/**
 * 详情
 */
async function handleDetail(record: Recordable) {
  openDrawer(true, {
    record,
    isUpdate: true,
    showFooter: false,
    tenantSaas: false,
  });
}
/**
 * 删除事件
 */
async function handleDelete(record) {
  if ('admin' == record.username) {
    createMessage.warning(t('language.unoperationNotice'));
    return;
  }
  await deleteUser({ id: record.id }, reload);
}
/**
 * 批量删除事件
 */
async function batchHandleDelete() {
  let hasAdmin = unref(selectedRows).filter((item) => item.username == 'admin');
  if (unref(hasAdmin).length > 0) {
    createMessage.warning(t('language.unoperationNotice'));
    return;
  }
  await batchDeleteUser({ ids: selectedRowKeys.value }, () => {
    selectedRowKeys.value = [];
    reload();
  });
}
/**
 * 成功回调
 */
function handleSuccess() {
  reload();
}

/**
 * 打开修改密码弹窗
 */
function handleChangePassword(username) {
  openPasswordModal(true, { username });
}
/**
 * 打开代理人弹窗
 */
function handleAgentSettings(userName) {
  openAgentModal(true, { userName });
}
/**
 * 冻结解冻
 */
async function handleFrozen(record, status) {
  if ('admin' == record.username) {
    createMessage.warning(t('language.unoperationNotice'));
    return;
  }
  await frozenBatch({ ids: record.id, status: status }, reload);
}
/**
 * 批量冻结解冻
 */
function batchFrozen(status) {
  let hasAdmin = selectedRows.value.filter((item) => item.username == 'admin');
  if (unref(hasAdmin).length > 0) {
    createMessage.warning(t('language.unoperationNotice'));
    return;
  }
  createConfirm({
    iconType: 'warning',
    title: t('language.confirmTitle'),
    content: status == 1 ? t('language.isUnfreeze') : t('language.isFreeze'),
    onOk: async () => {
      await frozenBatch({ ids: unref(selectedRowKeys).join(','), status: status }, reload);
    },
  });
}

/**
 *同步钉钉和微信回调
 */
function onSyncFinally({ isToLocal }) {
  // 同步到本地时刷新下数据
  if (isToLocal) {
    reload();
  }
}

/**
 * 操作栏
 */
function getTableAction(record): ActionItem[] {
  return [
    {
      label: t('language.edit'),
      onClick: handleEdit.bind(null, record),
      // ifShow: () => hasPermission('system:user:edit'),
    },
  ];
}
/**
 * 下拉操作栏
 */
function getDropDownAction(record): ActionItem[] {
  return [
    {
      label: t('language.detail'),
      onClick: handleDetail.bind(null, record),
    },
    {
      label: t('language.password'),
      //auth: 'user:changepwd',
      onClick: handleChangePassword.bind(null, record.username),
    },
    {
      label: t('language.delText'),
      popConfirm: {
        title: t('language.confirmDelete'),
        confirm: handleDelete.bind(null, record),
      },
    },
    {
      label: t('language.freeze'),
      ifShow: record.status == 1,
      popConfirm: {
        title: t('language.sureFreeze'),
        confirm: handleFrozen.bind(null, record, 2),
      },
    },
    {
      label: t('language.thaw'),
      ifShow: record.status == 2,
      popConfirm: {
        title: t('language.sureUnfreeze'),
        confirm: handleFrozen.bind(null, record, 1),
      },
    },
    {
      label: t('language.mandatory'),
      onClick: handleAgentSettings.bind(null, record.username),
    },
  ];
}

/**
 * 离职
 * @param userName
 */
function handleQuit(userName) {
  //打开离职代理人弹窗
  openQuitAgentModal(true, { userName });
}
</script>

<style scoped></style>
