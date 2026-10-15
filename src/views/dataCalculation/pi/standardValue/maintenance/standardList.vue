<template>
  <BasicTable @register="registerTable">    
    <!-- 头部右侧插槽 -->
    <template #toolbar>
      <span>共{{ total }}条数据</span>
    </template>

    <!-- 操作列插槽 -->
    <template #action="{ record, column }">
      <TableAction :actions="createActions(record, column)" />
    </template>
  </BasicTable>
</template>
<script lang="ts" name="dataCalculation-standard-maintenance-list" setup>
import { ActionItem, BasicTable, BasicColumn, TableAction, EditRecordRow } from '/@/components/Table';
import { getList, addData, getExportUrl, deleteData, queryStandard, updateData, updateManageData} from './standardMaintenance.api';
import { getFieldList } from '../manage/standardManage.api';
import { piColumns } from './standardMaintenance.data';
import { useListPage } from '/@/hooks/system/useListPage';
import { useMessage } from '/@/hooks/web/useMessage';
import cloneDeep from 'lodash-es/cloneDeep';
import { ref } from 'vue';
import { buildUUID } from '/@/utils/uuid';

//消息提示
const { createMessage: msg } = useMessage();

//父组件传值
const manageData = ref({} as any);
/**导入导出使用参数 */
const manageId = ref();
const tableName = ref();

//pi名称下拉原始数据
const fieldArray = ref([] as any);

//p数据量
const total = ref(0);

const currentEditKeyRef = ref('');
// 列表页面公共参数、方法
const { tableContext, onExportXls, onImportXlsByParams } = useListPage({
  tableProps: {
    api: queryStandard,
    beforeFetch: function(params){
      params.tableName = manageData.value.tableName;
      params.fieldName = manageData.value.fieldName;
      params.dataId = manageData.value.id;
    },
    afterFetch: function(res){
      total.value = res.length;
    },
    columns: piColumns,
    expandRowByClick: false,
    showActionColumn: true,
    showTableSetting: false,
    pagination: false,
    canResize:true,
    actionColumn: {
      width: 80,
    },
    rowKey: 'id',
    useSearchForm: false
  },

  exportConfig: {
    name: 'PI标准维护',
    url: getExportUrl,
    params:{
      manageId : manageId,
      tableName :tableName,
    }
  },
  importConfigByParams: {
    url: '/pi/standardMaintenance/importExcel',
    params: {
      manageId : manageId,
      tableName :tableName,
    }
  },
});

// BasicTable绑定注册
const [registerTable, { reload, insertTableDataRecord, getDataSource, setColumns, getRawDataSource, deleteTableDataRecord }, { rowSelection, selectedRowKeys }] = tableContext;


/**
 * 初始化PI名称下拉数据
 */
async function initData(data){
  manageData.value = data;
}

/**
 * 创建操作列
 */
 function createActions(record: EditRecordRow, column: BasicColumn): ActionItem[] {
  if (!record.editable) {
    return [
      {
        label: '编辑',
        disabled: currentEditKeyRef.value ? currentEditKeyRef.value !== record.id : false,
        onClick: handleEdit.bind(null, record),
      },
    ];
  }
  return [
    {
      label: '保存',
      onClick: handleSave.bind(null, record),
    },
    {
      label: '取消',
      popConfirm: {
        title: '是否取消编辑',
        confirm: handleCancel.bind(null, record),
      },
    },
  ];
}

//开启行编辑
async function handleEdit(record: EditRecordRow) {
  currentEditKeyRef.value = record.id;
  record.onEdit?.(true);
}

//取消行编辑
function handleCancel(record: EditRecordRow) {
  currentEditKeyRef.value = '';
  record.onEdit?.(false, false);
}

//保存数据
async function handleSave(record: EditRecordRow) {
  // 校验
  msg.loading({ content: '正在保存...', duration: 0, key: 'saving' });
  const valid = await record.onValid?.();
  if (valid) {
    try {
      //处理数据
      const editData = cloneDeep(record.editValueRefs) ?? {};
      editData.id = record.id;

      //调用接口提交数据
      await updateData(editData);
      //修改表格编辑状态
      const pass = await record.onEdit?.(false, true);
      if (pass) {
        currentEditKeyRef.value = '';
      }
      msg.success({ content: '数据已保存', key: 'saving' });
    } catch (error) {
      msg.error({ content: '保存失败', key: 'saving' });
      console.log('error :>> ', error);
    }
  } else {
    msg.error({ content: '请填写正确的数据', key: 'saving' });
  }
}

// 公开方法，以便父组件可以访问
defineExpose({
  initData,
});
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

/** 操作列提示窗口样式 */
:deep(.ant-popover-buttons){
  display: flex;
}
</style>