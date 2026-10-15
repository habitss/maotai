<template>
  <div>
    <!--引用表格-->
    <BasicTable @register="registerTable">
      <!--插槽:table标题-->
      <template #tableTitle>
        <a-button type="primary" preIcon="ant-design:export-outlined"
          @click="onExportXls"> 导出</a-button>
      </template>
      <!--字段回显插槽-->
      <template v-slot:bodyCell="{ column, record, index, text }">
        <template v-if="column.dataIndex === 'action'">
          <a-button type="link" @click="handleDetail(record)">详情</a-button>
        </template>
      </template>
    </BasicTable>
    <!-- 异常分析弹框 -->
    <AbnormalList ref="abnormalRef" @success="handleSuccess"/>
  </div>
</template>

<script lang="ts" name="fivewhy-fiveWhyAnalyse" setup>
import { ref, reactive, computed } from 'vue';
import { BasicTable } from '/@/components/Table';
import { useListPage } from '/@/hooks/system/useListPage'
import { columns as baseColumns, searchFormSchema, validSelectValue } from './FiveWhyAnalyse.data';
import { list } from './FiveWhyAnalyse.api';
import AbnormalList from '/@/views/dataCalculation/abnormal/abnormal.vue';
import { exportExcel } from '/@/utils/common/exportToExcelUtils';

const queryParam = reactive<any>({});
// 异常分析弹框引用
const abnormalRef = ref();

// 添加操作列
const columns = computed(() => {
  const cols = [...baseColumns];
  cols.push({
    title: '操作',
    dataIndex: 'action',
    align: 'center',
    width: 80,
    fixed: 'right' as const,
  });
  return cols;
});

// 详情按钮点击事件
const handleDetail = (record) => {
  // 打开异常分析弹框
  const obj = {
    allocationTableId: record.tableCode,
    tableId: record.tableId,
    tableCode: record.tableCode,
    tableName: record.tableName,
    fieldCode: record.fieldCode,
    fieldName: record.fieldName,
    dataId: record.dataId,
    standardValue: record.standardValue,
    actualValue: record.actualValue,
    color: record.color,
    type: '1',
  };
  abnormalRef.value?.openAbnormal(obj);
}

// 成功回调
function handleSuccess() {
  reload();
}

// 注册table数据
const { tableContext } = useListPage({
  tableProps: {
    showActionColumn: false,
    title: '5Why分析查询',
    api: (params) => {
      // 处理设置过labelInValue的值
      if (params.tableCode) {
        const tableCodeValue = validSelectValue(params.tableCode);
        if (tableCodeValue && tableCodeValue !== '') {
          params.tableCode = tableCodeValue;
        } else {
          delete params.tableCode;
        }
      }
      if (params.fieldCode) {
        const fieldCodeValue = validSelectValue(params.fieldCode);
        if (fieldCodeValue && fieldCodeValue !== '') {
          params.fieldCode = fieldCodeValue;
        } else {
          delete params.fieldCode;
        }
      }
      return list(params);
    },
    columns,
    canResize: false,
    formConfig: {
      schemas: searchFormSchema,
      autoSubmitOnEnter: true,
      showAdvancedButton: false,
      fieldMapToNumber: [],
      fieldMapToTime: [
        ['createTime', ['createTime_begin', 'createTime_end'], 'YYYY-MM-DD HH:mm:ss'],
      ],
      resetFunc: () => {
        // 重置时清空queryParam
        Object.keys(queryParam).forEach(key => {
          delete queryParam[key];
        });
      },
    },
    beforeFetch: (params) => {
      // 只合并有值的参数
      const filteredQueryParam = {};
      Object.keys(queryParam).forEach(key => {
        if (queryParam[key] !== null && queryParam[key] !== undefined && queryParam[key] !== '') {
          filteredQueryParam[key] = queryParam[key];
        }
      });
      // 清除params中的空值
      Object.keys(params).forEach(key => {
        if (params[key] === null || params[key] === undefined || params[key] === '') {
          delete params[key];
        }
      });
      return Object.assign(params, filteredQueryParam);
    },
  },
})

const [registerTable, { reload, getDataSource }] = tableContext

// 导出当前表格数据
async function onExportXls() {
  const columnMap = {} as any;
  for (const item of baseColumns) {
    columnMap[item.dataIndex] = item.title;
  }
  const exportData = [] as any
  const tableData = getDataSource()
  for (const item of tableData) {
    const obj = {} as any
    for (const key in columnMap) {
      obj[columnMap[key]] = item[key];
    }
    exportData.push(obj);
  }
  exportExcel('5Why分析数据', exportData, null);
}
</script>

<style lang="less" scoped>
:deep(.ant-picker),
:deep(.ant-input-number) {
  width: 100%;
}
</style>
