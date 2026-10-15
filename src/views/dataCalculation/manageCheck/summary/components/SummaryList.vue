<template>
  <!--引用表格-->
  <BasicTable @register="registerTable">
    <template #tableTitle>
      <div class="border-style">
        <a-divider class="divider" type="vertical" />{{listTitle}}
        <a-button type="primary" style="float:right" @click="handleExport">导出</a-button>
      </div>
    </template>
  </BasicTable>
</template>

<script lang="ts" setup>
import { BasicTable } from '/@/components/Table';
import { useListPage } from '/@/hooks/system/useListPage'
import { columns } from '../ManageCheckSummary.data';
import { onMounted, ref } from 'vue';
import { cloneDeep } from 'lodash-es';
import { exportExcel } from '/@/utils/common/exportToExcelUtils';
// Emits声明
const emit = defineEmits(['success']);
//父组件传值
const props = defineProps({
  type: { type: Number, default: 1 },
});
//表格标题信息
const listTitle = ref('汇总分析');
//动态列名
const dynamicText = ref('');

//注册table数据
const { tableContext } = useListPage({
  tableProps: {
    title: listTitle,
    ellipsis: false,
    maxColumnWidth: 100,
    showActionColumn: false,
    showTableSetting: false,
    pagination: false,
    maxHeight: 400
  },
})
const [registerTable, { getColumns, setColumns, setTableData, setLoading, getDataSource }] = tableContext;

//刷新数据
async function reloadData(data, monthRange){
  setColumn(monthRange);
  if(data && data.length > 0){
    data[0].rowSpan = data.length;
    data[0].dynamicDate = '汇总';
  }
  //重新设置第一列的合并行数
  setTableData(data);
  setLoading(false);
}
/**
 * 设置列信息
 * @param monthRange 
 */
function setColumn(monthRange){
  const newColumns = cloneDeep(columns);
  newColumns[0].title = monthRange[0] + ' 至 ' + monthRange[1];
  newColumns[1].title = dynamicText.value;
  setColumns(newColumns);
}

//导出数据
function handleExport() {
  const columnMap = {} as any;
  for(const item of getColumns()){
    columnMap[item.dataIndex as string] = item.title;
  }
  let newArr = [] as any;
  const tableData = cloneDeep(getDataSource());
  for(const item of tableData){
    let obj = {} as any;
    for(const key in columnMap){
      if(key != 'id'){
        obj[columnMap[key]] = item[key];
      }
    }
    newArr.push(obj);
  }
  exportExcel('分层检查汇总-'+listTitle.value, newArr, null);
}


onMounted(() => {
  //根据类型设置表格标题
  let text = '';
  if(1 == props.type){ 
    text = '问题发现人';
  } else if(2 == props.type){
    text = '责任班组';
  } else if(3 == props.type){
    text = '责任岗位';
  } else if(4 == props.type){
    text = '责任人';
  }
  listTitle.value = text+'汇总分析';
  dynamicText.value = text;
})

//暴露方法
defineExpose({
  reloadData,
  setLoading
})
</script>

<style lang="less" scoped>
:deep(.ant-picker),
:deep(.ant-input-number) {
  width: 100%;
}

:deep(.jeecg-basic-table-header__toolbar) {
  display: none;
}

.border-style {
  width: 100%;
  font-size: 20px;
  font-weight: bold;

  .divider {
    width: 0.3rem !important;
    top: -5px;
    background-color: red;
    height: 20px;
    margin: 14px 10px 5px 0;
  }
}
</style>