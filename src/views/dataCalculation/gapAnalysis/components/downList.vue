<template>
  <div>
    <!--引用表格-->
    <BasicTable @register="registerTable">
    </BasicTable>
  </div>
</template>

<script lang="ts" name="dataCalculation-fiveWhy-fiveWhyList" setup>
import { BasicTable } from '/@/components/Table';
import { useListPage } from '/@/hooks/system/useListPage'
import { downColumns } from '../gapAnalysis.data';
import { monthTracking } from '/@/views/dataCalculation/kpi/input/KpiTargetInput.api';
import { getColor} from '/@/utils/common/piColorUtils';

//注册table数据
const { tableContext } = useListPage({
  tableProps: {
    title: '',
    canResize: false,
    showTableSetting: false,
    showActionColumn: false,
    pagination: false,
    immediate: false,
  },
})

const [registerTable, {reload, setProps }] = tableContext;

//初始化表格配置
function initDownList(obj){
  console.log('obj :>> ', obj);
  //根据传入参数获取月份
  let yearMonth = obj?.queryParam?.month ?? obj.kpiParams.yearMonth;
  let month = yearMonth.split('-')[1];
  //调整月年累显示文字
  downColumns[14].title = parseInt(month)+"月年累";
  downColumns[14].dataIndex = yearMonth+"-yearAccumulate";
  //设置表格颜色和显示文字
  downColumns.forEach((item, index) => {
    if(index > 0){
      item.customRender = ({value}) => {
        return Object.prototype.toString.call(value) == '[object Object]' ? value.value : value;
      };
      item.customCell = (record, index, column) => { 
        const field = column?.dataIndex as string;
        const value = record[field];
        return {  
          style: {
            'background-color': Object.prototype.toString.call(value) == '[object Object]' ? getColor(value.colour) : value,
          }
        }
      };
    }
  })

  setProps({
    api: monthTracking,
    columns: downColumns,
    beforeFetch: (params) => {
      params.kpiCode = obj.kpiRowData.kpiName.id;
      params.month = obj?.queryParam?.month ?? obj.kpiParams.yearMonth;
    },
  })
  reload();
}

//暴露方法
defineExpose({
  initDownList
})
</script>

<style lang="less" scoped>
:deep(.ant-picker),
:deep(.ant-input-number) {
  width: 100%;
}

:deep(.ant-table-cell .ant-table-cell-ellipsis) {
  min-height: 100px !important;
}

.box {
  height: 100%;
  display: flex;
  align-items: center;

  span {
    cursor: pointer;
    margin-left: 4px;
  }
}
</style>