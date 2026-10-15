<template>
  <BasicTable @register="registerTable">
    <!--插槽:table标题-->
    <template #tableTitle>
      <a-button type="primary" @click="handleAdd" preIcon="ant-design:plus-outlined">
        {{ t('language.add') }}</a-button
      >
      <a-button type="primary" preIcon="ant-design:export-outlined" @click="onExportXls">
        {{ t('language.export') }}</a-button
      >
      <j-upload-button type="primary" preIcon="ant-design:import-outlined" @click="onImportXls">{{
        t('language.import')
      }}</j-upload-button>
      <a-dropdown v-if="selectedRowKeys.length > 0">
        <template #overlay>
          <a-menu>
            <a-menu-item key="1" @click="batchHandleDelete">
              <Icon icon="ant-design:delete-outlined" />
              {{ t('language.delText') }}
            </a-menu-item>
          </a-menu>
        </template>
        <a-button 
          >{{ t('language.batchOperation') }}
          <Icon icon="mdi:chevron-down" />
        </a-button>
      </a-dropdown>
    </template>
    <!--操作栏-->
    <template #action="{ record }">
      <TableAction :actions="getTableAction(record)" :dropDownActions="getDropDownAction(record)" />
    </template>

    <!--  deptCode部门插槽    -->
    <template #form-deptCode="{ model, field }">
      <kt-depart-select
        v-model:value="model[field]"
        select-type="depart"
        :placeholder="t('language.selectDepartment')"
        @change="handleDeptChange"
      />
    </template>
    
    <!--  lineCode产线插槽    -->
    <template #form-lineCode="{ model, field }">
      <kt-depart-select
        v-model:value="model[field]"
        select-type="line"
        :placeholder="t('language.selectProductLine')"
        :parentKey="model.deptCode"
      />
    </template>
  </BasicTable>

  <!-- 新增、编辑、详情modal -->
  <Modal @register="registerActionModal" :width="600" @success="reload" />
  <!-- 任务管理抽屉 -->
  <MdmDeviceTaskDrawer @register="registerDrawer" @success="handleSuccess" />
</template>
<script lang="ts" name="mes3-mdm-device-manage" setup>
import { BasicTable, TableAction, EditRecordRow } from '/@/components/Table';
import { list, deleteOne, batchDelete, getImportUrl, getExportUrl, queryExistsPiBasePoint } from './MdmDevice.api';
import { columns, searchFormSchema } from './MdmDevice.data';
import { useListPage } from '/@/hooks/system/useListPage';
import { useMessage } from '/@/hooks/web/useMessage';
import { useModal } from '/@/components/Modal';
import { useDrawer } from '/@/components/Drawer';
import { useI18n } from '/@/hooks/web/useI18n';
import KtDepartSelect from '/@/components/KtDepartSelect/KtDepart.vue';
import Modal from './components/MdmDeviceForm.vue';
import MdmDeviceTaskDrawer from './components/MdmDeviceTaskDrawer.vue';

//国际化
const { t } = useI18n();

//新增、编辑、详情modal
const [registerActionModal, { openModal }] = useModal();
//注册任务管理抽屉
const [registerDrawer, { openDrawer }] = useDrawer();

// 列表页面公共参数、方法
const { tableContext, onExportXls, onImportXls } = useListPage({
  tableProps: {
    title: '设备管理',
    api: list,
    columns: columns,
    expandRowByClick: true,
    showActionColumn: true,
    ellipsis: false,
    actionColumn: {
      width: 120,
      fixed: 'right'
    },
    rowSelection: {
      columnWidth: 20,
    },
    formConfig: {
      schemas: searchFormSchema,
      fieldMapToTime: [['createTime', ['createTime_begin', 'createTime_end'], 'YYYY-MM-DD']],
    },
    defSort: {
      column: 'sort',
      order: 'asc',
    },
  },
  exportConfig: {
    name: t('language.deviceList'),
    url: getExportUrl,
    // params: unref(exportParams),
  },
  importConfig: {
    url: getImportUrl,
    success: handleSuccess,
  },
});

// BasicTable绑定注册
const [registerTable, { reload, getForm }, { rowSelection, selectedRowKeys }] = tableContext;

//消息提示
const { createMessage: msg } = useMessage();

/**
 * 操作栏
 */
function getTableAction(record) {
  return [
    {
      //编辑
      label: t('language.edit'),
      onClick: handleEdit.bind(null, record),
    },
  ];
}

/**
 * 下拉操作栏
 */
function getDropDownAction(record) {
  return [
    {
      //任务管理
      label: '过程质量任务',
      onClick: handleTaskDrawer.bind(null, record, 1, '过程质量任务'),
    },
    {
      label: '交接班任务',
      onClick: handleTaskDrawer.bind(null, record, 2, '交接班任务'),
    },
    {
      //详情
      label: t('language.detail'),
      onClick: handleDetail.bind(null, record),
    },
    {
      //删除
      label: t('language.delText'),
      popConfirm: {
        title: t('language.confirmDelete'),
        confirm: handleDelete.bind(null, record),
        placement: 'left',
      },
    },
  ];
}

/**
 * 新增操作
 */
function handleAdd() {
  openModal(true, {'action': 'add'});
}

/**
 * 编辑操作
 */
function handleEdit(record: EditRecordRow) {
  openModal(true, {'action': 'edit', 'record': record});
}

/**
 * 详情操作
 */
function handleDetail(record: EditRecordRow) {
  openModal(true, {'action': 'detail', 'record': record});
}

/**
 * 删除操作
 */
async function handleDelete(record: EditRecordRow) {
   try {
    if(!await getPiBasePoint(record.id)){
      return;
    }
    //调用后台修改接口, 删除
    await deleteOne({ id: record.id }, handleSuccess);
    reload();
  } catch (error) {
    msg.error({ content: '操作失败' });
  }
}

/**
 * 批量删除事件
 */
 async function batchHandleDelete() {
  if(!await getPiBasePoint(selectedRowKeys.value)){
    return;
  }
  await batchDelete({ ids: selectedRowKeys.value }, handleSuccess);
}

/**
 * 获取 PI基础点位
 */
async function getPiBasePoint(deviceId) {
  const res = await queryExistsPiBasePoint({ deviceId: deviceId });
  if(res && res.length > 0){
    let str = res
    .filter(item => item.deviceIdVal !== undefined && item.deviceIdVal !== null)
    .map(item => item.deviceIdVal)
    .join(',');
    msg.error({ content: '请先调整【'+str+'】设备下的所有基础点位！' });
    return false;
  }
  return true;
}

//部门改变事件
async function handleDeptChange(value){
  //清空产线、设备类型
  await getForm().setFieldsValue({ 
    lineCode: '',
  });
}

/**
 * 任务管理
 * @param record 设备信息
 */
function handleTaskDrawer(record, checkTaskType, title) {
  openDrawer(true, {
    record,
    checkTaskType,
    title,
  });
}

/**
 * 成功回调
 */
function handleSuccess() {
  (selectedRowKeys.value = []) && reload();
}
</script>
<style scoped>
  /** 时间和数字输入框样式 */
  :deep(.ant-input-number) {
    width: 100%;
  }

  :deep(.ant-picker) {
    width: 100%;
  }

  /** 下拉框默认文字颜色 */
  :deep(.ant-select-selection-placeholder) {
    color: #BEBEBE !important;
  }
</style>
