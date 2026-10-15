<template>
  <BasicModal v-bind="$attrs" @register="registerModal" destroyOnClose title="pi基础数据" width="80vw" :min-height="600"
    :showCancelBtn="false" :showOkBtn=false :loading="modalLoading">
    <BasicTable @register="registerTable" @row-dbClick="handleEdit">
      <!-- 头部操作按钮插槽 -->
      <template #tableTitle>
        <a-button type="primary" preIcon="ep:plus" @click="handleAdd" :disabled="'' != currentEditKeyRef">新增</a-button>
      </template>

      <!-- 头部右侧插槽 -->
      <template #toolbar>
        <span>共{{ tableTotal }}条数据</span>
      </template>

      <!-- 操作列插槽 -->
      <template #action="{ record, column }">
        <TableAction :actions="createActions(record, column)" />
      </template>
    </BasicTable>
  </BasicModal>
</template>

<script lang="ts" setup>
import { ref } from 'vue';
import { BasicModal, useModalInner } from '/@/components/Modal';
import { ActionItem, BasicTable, BasicColumn, TableAction, EditRecordRow } from '/@/components/Table';
import { piList, piCustomizeList, piCustomizeFieldList, piSaveOrUpdate, piDeleteOne } from '../ConsistencyConfigMx.api';
import { useListPage } from '/@/hooks/system/useListPage';
import { useMessage } from '/@/hooks/web/useMessage';
import cloneDeep from 'lodash-es/cloneDeep';
import { buildUUID } from '/@/utils/uuid';
const modalLoading = ref(true);
//消息提示
const { createMessage: msg } = useMessage();
//明细数据
const mxData = ref({} as any);
// Emits声明
const emit = defineEmits(['register', 'success']);
//基础数据表数据
const customizeList = ref([] as any);
const customizeOptions = ref([] as any);
//当前编辑行
const currentEditKeyRef = ref('none');
//总数
const tableTotal = ref(0);

//表单赋值
const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
  //重置编辑状态
  currentEditKeyRef.value = 'none';
  modalLoading.value = true;
  mxData.value = data.record;
  //获取基础数据表
  const { records: customizeRecords } = await piCustomizeList({ page: 1, pageSize: 999 });
  customizeList.value = customizeRecords;
  //获取列表数据
  reloadTable();
  //重置编辑状态
  currentEditKeyRef.value = '';
  modalLoading.value = false;
});

//列表数据
const piColumns: BasicColumn[] = [
  {
    title: '基础数据表',
    align: "center",
    dataIndex: 'tableCode',
    editComponent: 'Select',
    editComponentProps: {
      options: customizeOptions,
      fieldNames: { label: 'name', value: 'tableName' },
      getPopupContainer: () => document.body,
      showSearch: true,
      filterOption: (input, option) => handleFilterOption(input, option, 'name'),
      onChange: async function (value, option) {
        await processingFieldCodeOptions(value);
      }
    },
    editRow: true,
    editRule: true,
    editValueMap: (value) => processingEditValue(value, 'tableCode'),
  },
  {
    title: '字段名称',
    align: "center",
    dataIndex: 'fieldCodeId',
    editComponent: 'Select',
    editComponentProps: {
      getPopupContainer: () => document.body,
      fieldNames: { label: 'name', value: 'id' },
      showSearch: true,
      filterOption: (input, option) => handleFilterOption(input, option, 'name'),
      onChange: function (value, option) {
        getDataSource().find((item) => item.id == currentEditKeyRef.value).editValueRefs.fieldCode = option.fieldName;
      }
    },
    editRow: true,
    editRule: true,
    editValueMap: (value) => processingEditValue(value, 'fieldCodeId'),
  },
  {
    title: '查询字段名称',
    align: "center",
    dataIndex: 'queryFieldId',
    editComponent: 'Select',
    editComponentProps: {
      getPopupContainer: () => document.body,
      fieldNames: { label: 'name', value: 'id' },
      showSearch: true,
      allowClear: true,
      filterOption: (input, option) => handleFilterOption(input, option, 'name'),
      onChange: function (value, option) {
        getDataSource().find((item) => item.id == currentEditKeyRef.value).editValueRefs.queryField = value ? option.fieldName : null;
      }
    },
    editRow: true,
    editRule: true,
    editValueMap: (value) => processingEditValue(value, 'queryFieldId'),
  },
  {
    title: '排序',
    align: "center",
    dataIndex: 'sort',
    editComponent: 'InputNumber',
    editRow: true,
  },
  {
    title: '备注',
    align: "center",
    dataIndex: 'remark',
    editComponent: 'Input',
    editRow: true,
  },
  {
    title: '字段名称列名',
    dataIndex: 'queryField',
    ifShow: false
  },
  {
    title: '查询字段名称列名',
    dataIndex: 'queryField',
    ifShow: false
  },
];

// 列表页面公共参数、方法
const { tableContext } = useListPage({
  tableProps: {
    beforeFetch: function (params) {
      params.column = 'sort';
      params.order = 'asc';
    },
    scroll: { y: 500 },
    columns: piColumns,
    expandRowByClick: true,
    showActionColumn: true,
    showTableSetting: false,
    pagination: false,
    actionColumn: {
      width: 120,
    },
    rowKey: 'id',
    useSearchForm: false
  },
});

// BasicTable绑定注册
const [registerTable, { setTableData, insertTableDataRecord, getDataSource, setColumns, getColumns, deleteTableDataRecord }, { rowSelection, selectedRowKeys }] = tableContext;

//创建操作列
function createActions(record: EditRecordRow, column: BasicColumn): ActionItem[] {
  if (!record.editable) {
    return [
      {
        label: '编辑',
        disabled: currentEditKeyRef.value ? currentEditKeyRef.value !== record.id : false,
        onClick: handleEdit.bind(null, record),
      },
      {
        label: '删除',
        popConfirm: {
          title: '是否删除',
          confirm: handleDelete.bind(null, record),
          getPopupContainer: () => document.body,
        },
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
        title: '是否取消操作',
        confirm: handleCancel.bind(null, record),
        getPopupContainer: () => document.body,
      },
    },
  ];
}

//开启行编辑
async function handleEdit(record: EditRecordRow) {
  await processingTableCodeOptions(record.tableCode);
  await processingFieldCodeOptions(record.tableCode);
  currentEditKeyRef.value = record.id;
  record.onEdit?.(true);
}

//取消行编辑
function handleCancel(record: EditRecordRow) {
  if (record.id.startsWith('temp_')) {
    deleteTableDataRecord(record.id);
  }
  currentEditKeyRef.value = '';
  record.onEdit?.(false, false);
}

//添加事件
async function handleAdd() {
  await processingTableCodeOptions(null);
  await processingFieldCodeOptions('none');
  //在第一行插入一条新数据
  await insertTableDataRecord({
    id: 'temp_' + buildUUID(),
  }, 0)
  const record = getDataSource()[0];
  currentEditKeyRef.value = record.id;
  record.onEdit?.(true);
}

//删除操作
async function handleDelete(record) {
  try {
    if (!record.id.startsWith('temp_')) {
      await piDeleteOne(record, handleSuccess);
    }
    deleteTableDataRecord(record.id);
  } catch (error) {
    console.log('error :>> ', error);
  }
}

//保存数据
async function handleSave(record: EditRecordRow) {
  // 校验
  msg.loading({ content: '正在保存...', duration: 0, key: 'saving' });
  const valid = await record.onValid?.();
  if (valid) {
    try {
      //处理数据
      const submitData = cloneDeep(record.editValueRefs) ?? {};
      //判断是否为新增数据
      if (record.id && !record.id.startsWith('temp_')) {
        submitData.id = record.id;
      } else {
        submitData.mxId = mxData.value.id
      }

      //调用接口提交数据
      await piSaveOrUpdate(submitData, submitData.id ? true : false);
      // //修改表格编辑状态
      // const pass = await record.onEdit?.(false, true);
      // if (pass) {
        currentEditKeyRef.value = '';
      // }
      customizeOptions.value = customizeOptions.value.filter(item => item.tableCode !== record.tableCode);
      emit('success');
      //刷新表格
      handleSuccess();
      msg.loading({ content: '正在保存...', duration: -1, key: 'saving' });
    } catch (error) {
      console.log('error :>> ', error);
      msg.loading({ content: '正在保存...', duration: -1, key: 'saving' });
    }
  } else {
    msg.error({ content: '请填写正确的数据', key: 'saving' });
  }
}

//下拉框搜索 
function handleFilterOption(input, option, searchField) {
  return option[searchField].toLowerCase().indexOf(input.toLowerCase()) >= 0;
}
/**
 * 成功回调
 */
function handleSuccess() {
  (selectedRowKeys.value = []) && reloadTable();
}

//刷新表格
async function reloadTable() {
  //获取表格数据
  const { records, total } = await piList({ page: 1, pageSize: 999, mxId: mxData.value.id });
  setTableData(records);
  //设置表格总数
  tableTotal.value = total;
  await processingTableCodeOptions(null);
}

//处理选项数据，排除已选择的基础数据表
function processingTableCodeOptions(_tableCode) {
  //获取表格数据
  // const records = getDataSource();
  //排除列表中已选择的基础数据表
  let columns = getColumns();
  // TODO 临时取消过滤，恢复时改回 filtered 写法
  columns[0].editComponentProps.options = customizeList.value;
  // columns[0].editComponentProps.options = customizeList.value.filter(customize => {
  //   return !records.some(item => item.tableCode === customize.tableName && item.tableCode !== tableCode);
  // });
  setColumns(columns);
}

//处理字段名称、查询字段名称下拉数据
async function processingFieldCodeOptions(tableName) {
  //获取字段列表数据
  const { records } = await piCustomizeFieldList({ tableName, page: 1, pageSize: 999 });
  //设置字段名称、查询字段名称可选下拉数据
  let columns = getColumns();
  columns[1].editComponentProps.options = records.filter(item => item.isStatistics == 1);
  columns[2].editComponentProps.options = records.filter(item => item.type == 6);
  setColumns(columns);
  //基础数据表改变时清空字段名称、查询字段名称
  for (const item of getDataSource()) {
    if (item.id == currentEditKeyRef.value) {
      item.editValueRefs.fieldCodeId = null;
      item.editValueRefs.fieldCode = null;
      item.editValueRefs.queryFieldId = null;
      item.editValueRefs.queryField = null;
      break;
    }
  }
}

//处理开启编辑行时的返回枚举
function processingEditValue(value, field) {
  let str = value;
  for (const item of getDataSource()) {
    if (item[field] == value) {
      str = item[field + '_dictText'];
      break;
    }
  }
  return str;
}
</script>

<style lang="less" scoped>
/** 时间和数字输入框样式 */
:deep(.ant-input-number) {
  width: 100%;
}

:deep(.ant-calendar-picker) {
  width: 100%;
}
</style>