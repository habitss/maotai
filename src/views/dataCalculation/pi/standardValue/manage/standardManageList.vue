<template>
  <BasicTable @register="registerTable" :rowSelection="'piChart' === configData.inputType ? rowSelection : null">
    <!-- 头部操作按钮插槽 -->
    <template #tableTitle >
      <a-button v-if="'piChart' === configData.inputType" type="primary" @click="handleStandSelect">确定</a-button>
      <template v-else>
        <a-button type="primary" preIcon="ep:plus" @click="onAddRow">新增</a-button>
        <a-button type="primary" preIcon="ant-design:export-outlined" @click="onExportXls"> 导出</a-button>
        <j-upload-button type="primary" preIcon="ant-design:import-outlined" @click="onImportXls">导入</j-upload-button>
      </template>
    </template>

    <!-- 操作列插槽 -->
    <template #action="{ record, column }">
      <TableAction :actions="createActions(record, column)"  :dropDownActions="createDropDownActions(record, column)"/>
    </template>
  </BasicTable>
  <Modal @register="registerAddModal" :width="800" @success="reload" />
  <MaintenanceModal @register="registerMaintenanceModal" :width="1400" @success="reload" />
</template>
<script lang="ts" name="dataCalculation-standard-manage-list" setup>
import { ActionItem, BasicTable, BasicColumn, TableAction, EditRecordRow } from '/@/components/Table';
import { getList, deleteData, getImportUrl, getExportUrl, getFieldList,standardCopy } from './standardManage.api';
import { columns, searchFormSchemas } from './standardManage.data';
import { useListPage } from '/@/hooks/system/useListPage';
import { useMessage } from '/@/hooks/web/useMessage';
import { useModal } from '/@/components/Modal';
import Modal from './components/manageActionModal.vue';
import MaintenanceModal from './components/maintenanceModal.vue';
import { nextTick, ref, watchEffect } from 'vue';
//emit组件
const emit = defineEmits(['closeSelect']);

//消息提示
const { createMessage: msg } = useMessage();

//新增/编辑modal
const [registerAddModal, { openModal }] = useModal();

//维护modal
const [registerMaintenanceModal, { openModal: openMaintenanceModal }] = useModal();

//父组件传值
const props = defineProps({
  configData: { type: Object, default: {} },
});
/**导入导出使用参数 */
const tableName = ref();


const fieldList = ref([] as any);
// 列表页面公共参数、方法
const { tableContext, onExportXls, onImportXls } = useListPage({
  tableProps: {
    title: 'PI标准管理',
    api: getList,
    formConfig: {
      schemas: searchFormSchemas,
      autoSubmitOnEnter: true,
      showAdvancedButton: true,
    },
    beforeFetch: async function(params){
      params.tableName = props.configData.tableName;
      params.fieldName = props.configData.fields;
      params.column = 'sort';
      params.order = 'asc';
      params.standardName = params.standardName ? `*${params.standardName}*` : null;
      if(fieldList.value.length == 0){
        fieldList.value = await getFieldList({pageSize: 9999, tableId: props.configData.id});
      }
    },
    afterFetch: async function(data){
      if(fieldList.value.length == 0){
        fieldList.value = await getFieldList({pageSize: 9999, tableId: props.configData.id});
      }

      for(const item of data) {
        for(const field of fieldList.value.records ?? []) {
          if(item.calcTimeField == field.fieldName){
            item.calcTimeField_dictText = field.name;
          }
        }
      }
    },
    columns: columns,
    expandRowByClick: true,
    showActionColumn: true,
    actionColumn: {
      width: 270,
    },
  },
  exportConfig: {
    name: 'PI标准管理',
    url: getExportUrl,
    params: {
      column: 'sort',
      order: 'asc',
      tableName: tableName
    },
  },
  importConfig: {
    url: getImportUrl,
    success: handleSuccess,
  },
});

// BasicTable绑定注册
const [registerTable, { reload, getDataSource, setProps, getSelectRows }, { rowSelection, selectedRowKeys }] = tableContext;

/**
 * 创建操作列
 */
function createActions(record: EditRecordRow, column: BasicColumn): ActionItem[] {
  return [
    {
      label: '编辑',
      onClick: handleEditRow.bind(null, record),
    },
    {
      label: 'PI标准维护',
      onClick: handleOpenModal.bind(null, record),
    },
    {
      label: 'PI标准复制',
      //onClick: handleOpenModal.bind(null, record),
      popConfirm: {
        title: '是否复制PI标准',
        confirm: copy.bind(null, record, column),
      },
    },
  ];
}

/**更多操作列 */
function createDropDownActions(record: EditRecordRow, column: BasicColumn): ActionItem[] {
  return [
    {
      label: '详情',
      onClick: handleDetail.bind(null, record),
    },
    {
      label: '删除',
      popConfirm: {
        title: '是否删除',
        confirm: handleDelete.bind(null, record, column),
      },
    },
  ];
}

/**
 * 打开标准维护
 */
function handleOpenModal(record: EditRecordRow) {
  record.tableText = props.configData.name;
  openMaintenanceModal(true, {record})
}

/**
 * 打开新增modal
 */
function onAddRow(){
  const calcTimeField = getDataSource().length > 0 ? getDataSource()[0].calcTimeField : '';
  openModal(true, {'isAdd': true, configData: props.configData, calcTimeField})
}

/**
 * 打开编辑modal
 */
function handleEditRow(record){
  openModal(true, {'isAdd': false, configData: props.configData, record})
}

/**
 * 打开详情modal
 */
function handleDetail(record){
  nextTick(function () {
    openModal(true, {'isAdd': false, 'isDetail': true, configData: props.configData, record})
  })
}

/**
 * 删除操作
 */
async function handleDelete(record){
  try {
    await deleteData(record.id);
    handleSuccess();
    msg.success({ content: '删除成功'});
  } catch (error) {
    msg.error({ content: '删除失败'});
  } 
}
/**
 * 复制PI标准
 */
async function copy(record){
  try {
    await standardCopy(record.id);
    handleSuccess();
    msg.success({ content: '复制成功'});
  } catch (error) {
    msg.error({ content: '复制失败'});
  } 
}
//数据选择事件
function handleStandSelect(){
  if('piChart' === props.configData.inputType){
    emit('closeSelect', getSelectRows());
  }
}
/**
 * 成功回调
 */
function handleSuccess() {
  (selectedRowKeys.value = []) && reload();
}

watchEffect(async () => {
  tableName.value = props.configData.tableName;
});

//暴露方法
defineExpose({
  setProps,
})
</script>