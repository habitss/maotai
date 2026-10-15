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
</template>
<script lang="ts" name="mes3-mdm-bottle-type-manage" setup>
import { BasicTable, TableAction, EditRecordRow } from '/@/components/Table';
import { list, deleteOne, batchDelete, getImportUrl, getExportUrl } from './MdmBottleType.api';
import { columns, searchFormSchema } from './MdmBottleType.data';
import { useListPage } from '/@/hooks/system/useListPage';
import { useMessage } from '/@/hooks/web/useMessage';
import { useModal } from '/@/components/Modal';
import { useI18n } from '/@/hooks/web/useI18n';
import KtDepartSelect from '/@/components/KtDepartSelect/KtDepart.vue';
import Modal from './components/MdmBottleTypeForm.vue';
import { nextTick } from 'vue';

//国际化
const { t } = useI18n();

//新增、编辑、详情modal
const [registerActionModal, { openModal }] = useModal();

// 列表页面公共参数、方法
const { tableContext, onExportXls, onImportXls } = useListPage({
  tableProps: {
    title: '瓶型管理',
    api: list,
    columns: columns,
    expandRowByClick: true,
    showActionColumn: true,
    actionColumn: {
      width: 100,
    },
    rowSelection: {
      columnWidth: 20,
    },
    formConfig: {
      schemas: searchFormSchema,
      fieldMapToTime: [['createTime', ['createTime_begin', 'createTime_end'], 'YYYY-MM-DD']],
    },
  },
  exportConfig: {
    name: t('language.bottle'),
    url: getExportUrl,
  },
  importConfig: {
    url: getImportUrl,
    success: handleSuccess,
  },
});

// BasicTable绑定注册
const [registerTable, { reload, getForm }, { selectedRowKeys }] = tableContext;

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
  nextTick(() => {
    openModal(true, {'action': 'add'});
  });
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
  await batchDelete({ ids: selectedRowKeys.value }, handleSuccess);
}

//部门改变事件
async function handleDeptChange(value){
  //清空产线
  await getForm().setFieldsValue({ 
    lineCode: '',
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
