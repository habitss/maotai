<template>
  <!--引用表格-->
  <BasicTable @register="registerTable">
    <template #tableTitle>
      <a-button type="link" @click="handleAddRow">新增</a-button>
    </template>
    <template #action="{ record }">
      <TableAction :actions="getTableAction(record)" />
    </template>

    <template #bodyCell="{ column, record }">
      <a-select v-if="column.dataIndex === 'conditionType'" v-model:value="record[column.dataIndex]"
        :options="selectOption"></a-select>
        <a-select v-else-if="column.dataIndex === 'defaultVal' && (record.conditionType == '2' || record.conditionType == '3')" 
        v-model:value="record[column.dataIndex]"
        :options="defaultValOption"></a-select>
      <a-input v-else v-model:value="record[column.dataIndex]" />
    </template>
  </BasicTable>
</template>

<script lang="ts" name="summary-template-query-file" setup>
import { reactive, ref } from 'vue';
import { BasicTable, TableAction } from '/@/components/Table';
import { useListPage } from '/@/hooks/system/useListPage';
import { useCopyToClipboard } from '/@/hooks/web/useCopyToClipboard';
import dayjs from 'dayjs';
//查询参数
const queryParam = reactive<any>({});
//pi报表字段
const selectOption = ref<any[]>([
  {
    label: '文本',
    value: '1',
  }, {
    label: '日期',
    value: '2',
  }, {
    label: '时间',
    value: '3',
  }
]);
//默认字段下拉
const defaultValOption = ref<any[]>([
  {
    label: '本周',
    value: '1',
  }, {
    label: '本月',
    value: '2',
  }, {
    label: '近7天',
    value: '3',
  }, {
    label: '近30天',
    value: '4',
  }, {
    label: '近60天',
    value: '5',
  }
]);

//注册table数据
const { tableContext } = useListPage({
  tableProps: {
    immediate: false,
    pagination: false,
    showActionColumn: true,
    actionColumn: {
      width: 60,
      fixed: 'right'
    },
    rowKey: 'conditionField',
    scroll: { y: 400 },
    ellipsis: false,
    showTableSetting: false,
    columns: [
      {
        title: '查询别名',
        align: "center",
        dataIndex: 'conditionField',
        width: 80,
      },
      {
        title: '显示文本',
        align: "center",
        dataIndex: 'conditionText',
        width: 80,
      },
      {
        title: '字段类型',
        align: "center",
        dataIndex: 'conditionType',
        width: 80,
      },
      {
        title: '默认值',
        align: "center",
        dataIndex: 'defaultVal',
        width: 80,
      },
    ],
    beforeFetch: (params) => {
      return Object.assign(params, queryParam);
    },
  },
})

const [registerTable, { setTableData, getDataSource, setLoading, insertTableDataRecord, deleteTableDataRecord }, { }] = tableContext;

/**
 * 操作栏
 */
function getTableAction(record) {
  return [
    {
      label: '复制条件',
      onClick: handleCopyField.bind(null, record),
    },
    {
      label: '删除',
      popConfirm: {
        title: '是否删除？',
        confirm: handleDeleteRow.bind(null, record),
        placement: 'left',
      },
    },
  ];
}
//新增一行数据
async function handleAddRow() {
  await insertTableDataRecord({ conditionField: dayjs().valueOf().toString(36) })
}
//删除一行数据
function handleDeleteRow(record) {
  deleteTableDataRecord(record.conditionField)
}
function handleCopyField(record) {
  useCopyToClipboard(` @#&&${record.conditionField}#@ `);
}
//重新加载数据
async function reloadData(records) {
  setLoading(true);
  if (Array.isArray(records)) {
    setTableData(records);
  }else{
    setTableData([]);
  }
  setLoading(false);
  return true;
}
//暴露方法
defineExpose({
  reloadData,
  getDataSource,
})
</script>

<style lang="less" scoped>
:deep(.ant-picker),
:deep(.ant-input-number) {
  width: 100%;
}

:deep(.ant-table-wrapper .ant-table.ant-table-small) {
  .ant-table-footer {
    padding: 8px 0px !important;
  }
}
</style>