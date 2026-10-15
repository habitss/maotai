<template>
  <BasicModal v-bind="$attrs" @register="registerModal" destroyOnClose :title width="80vw" :height="600"
    :maskClosable="false" :showOkBtn="false">
    <!--引用表格-->
    <BasicTable @register="registerTable">
      <!--操作栏-->
      <template #fiveWhyDataSlot="{ text }">
        <a-button type="link" @click="handleSelect(text)">选择</a-button>
      </template>
    </BasicTable>
  </BasicModal>
</template>

<script lang="ts" setup>
import { reactive, ref } from 'vue';
import { BasicTable } from '/@/components/Table';
import { useListPage } from '/@/hooks/system/useListPage'
import { BasicModal, useModalInner } from '/@/components/Modal';
import { queryKpiReportHeader, listKpiReportData } from '/@/views/dataCalculation/kpi/reports/kpiWeekAndMonReports.api';
import { cloneDeep } from 'lodash-es';
//注册emit
const emit = defineEmits(['success']);
//查询参数
const queryParam = reactive<any>({});
//上级页面所传数据
const parentData = ref();
//标题
const title = ref('');
//注册table数据
const { tableContext } = useListPage({
  tableProps: {
    title: '',
    canResize: true,
    showTableSetting: false,
    showActionColumn: false,
    immediate: false,
    pagination: false,
    beforeFetch: (params) => {
      return Object.assign(params, queryParam);
    },
    actionColumn: {
      width: 260,
      fixed: 'right'
    },
  },
})
const [registerTable, { setProps }] = tableContext;

//表单赋值
const [registerModal, { closeModal }] = useModalInner(async (data) => {
  parentData.value = data.params;
  title.value = `5WHY分析重复引用（${data.params.kpiParams.kpiName}）`;
  const yearMonth = data.params.kpiParams.yearMonth.startsWith('w') ? data.params.kpiRowData.year+'-'+data.params.kpiRowData.month.padStart(2, '0') : data.params.kpiParams.yearMonth;
  //设置头部查询条件
  const queryParam = {
    months: [yearMonth],
    showChildrenKpi: true,
    showWeek: true,
    typeId: data.params.kpiRowData.typeId.value,
    year: data.params.kpiParams.year,
    citeKpiCode: data.params.kpiParams.kpiCode
  };
  //获取表头数据
  const headerRes = await queryKpiReportHeader(queryParam);
  let columns = [] as any;
  headerRes.result.forEach((item, index) => {
    if(index < 4 || index == 6){
      if(index < 4){
        //设置需要合并的行
        item.children[0].customCell = (record, index, column) => {
          if (index % 2 == 1) {
            return { rowSpan: 0 };
          } else {
            return { rowSpan: 2 };
          }
        };
      }
      item.children[0].dataIndex = item.children[0].key;      
      columns.push(item.children[0]);
    }
    //判断选择的是否为月累
    if(!data.params.kpiRowData.colKey.startsWith('w') && item.title === '月累'){
      item.children[0].dataIndex = item.children[0].key
      columns.push(item);
    } else if (data.params.kpiRowData.colKey.startsWith('w')){
      const colKeyArr = data.params.kpiRowData.colKey.split('_');
      if(item.title === `第${colKeyArr[2]}周`){
        item.children[0].dataIndex = item.children[0].key
        columns.push(item);
      }
    }
  })
  
  //获取列表数据
  const listRes = await listKpiReportData(queryParam);
  let dataSource = [] as any;
  listRes.result.forEach((item, itemIndex) => {
    //排除引用自身数据
    if(item.kpiName.id != data.params.kpiParams.kpiCode){
      let row = {} as any;
      //循环设置对应数据
      columns.forEach((column, index) => {
        let key = column.key;
        if(index >= 5){
          key = column.children[0]?.key;
        }
        row[key] = item[key]?.value;
      });
      //获取实际值是否有5w，
      if(itemIndex % 2 == 0){
        row.fiveWhyData = listRes.result[itemIndex+1][data.params.kpiRowData.colKey].fiveWhyData
      } else {
        row.fiveWhyData = item[data.params.kpiRowData.colKey].fiveWhyData
      }
      //只添加有5why的数据
      if(row.fiveWhyData){
        dataSource.push(row)
      }
    }
  })
  columns.push({
    title: '操作',
    dataIndex: 'fiveWhyData',
    width: 100,
    slots: { customRender: 'fiveWhyDataSlot' },
    customCell : (record, index, column) => {
      if (index % 2 == 1) {
        return { rowSpan: 0 };
      } else {
        return { rowSpan: 2 };
      }
    }
  })
  await setProps({ columns, dataSource });
});
function handleSelect(data) {
  let fiveWhyData = cloneDeep(data);
  fiveWhyData.kpiCode = parentData.value.kpiParams.kpiCode;
  parentData.value.problemDesc = parentData.value.kpiRowData.text;
  emit('success', parentData.value, cloneDeep(fiveWhyData));
  closeModal();
}
</script>