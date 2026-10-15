<template>
   <a-card :bordered="false">
    <a-form-model ref="formRef" :model="tableForm" class="myForm">
      <a-table size="small" bordered rowKey="id" :columns="columns" :scroll="{ x: 1100, y: '100vh' }"
        :data-source="tableForm.tableData" :pagination="false">
      </a-table>
    </a-form-model>

   </a-card>
</template>

<script lang="ts" setup>
  import { ref, nextTick, reactive,defineExpose, unref } from 'vue';
  import { queryKpiReportHeader,listKpiReportData } from '../kpiWeekAndMonReports.api';
  import { kpiControlConfiguration } from '/@/views/dataCalculation/kpi/info/KpiInfo.api';
  import { useUserStore } from '/@/store/modules/user';
  // 用户信息
  const userStore = useUserStore();

  const tableForm = ref({
    tableData: []
  });
  const columns = ref([])


  async function init(params){
    getColumns(params);
    getData(params);
  }
  //获取表头
  function getColumns(params){
    let year = params.kpiParams.year;
    let editArr = []
    // 定义月份数组
    let newDays = [year + '年', "1月", "2月", "3月", "4月", "5月", "6月", "7月", "8月", "9月", "10月", "11月", "12月"]
    let newDaysVal = ['yearlyTotal', "one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten", "eleven", "twelve"]

    if (newDays && newDays.length > 0) {
      newDays.forEach((item, index) => {
        editArr.push({
          title: item,
          children: [
            { title: '计划频次', dataIndex: `${newDaysVal[index]}|plannedFrequency`, key: `${newDaysVal[index]}|plannedFrequency`, width: '80px', align: "center" },
            { title: '计划量', dataIndex: `${newDaysVal[index]}|amountOfPlan`, key: `${newDaysVal[index]}|amountOfPlan`, width: '80px', align: "center" },
            { title: '实际频次', dataIndex: `${newDaysVal[index]}|actualFrequency`, key: `${newDaysVal[index]}|actualFrequency`, width: '80px', align: "center" },
            { title: '实际量', dataIndex: `${newDaysVal[index]}|actualQuantity`, key: `${newDaysVal[index]}|actualQuantity`, width: '80px', align: "center" },
          ]
        });
      });
    }

    columns.value = [
      {
        title: 'KPI名称', dataIndex: 'kpiName', key: 'kpiName', align: "center", width: '100px',
        customRender: (value, row, index) => {
          // const obj = {
          //   children: value !== null ? value : "",
          //   attrs: {},
          // };
          // obj.attrs.rowSpan = mergeCells(value, tableForm.value.tableData, "kpiName", index);//(当前内容,原数组,key值,下标)
          return value.value;
        },
      },{
        title: '相关部门/车间', dataIndex: 'parentName', key: 'parentName', align: "center", width: '90px',
        customRender: (value, row, index) => {
          // const obj = {
          //   children: value !== null ? value : "",
          //   attrs: {},
          // };
          // obj.attrs.rowSpan = mergeCells(value, tableForm.value.tableData, "parentName", index);//(当前内容,原数组,key值,下标)
          return value.value;
        },
      },{
        title: '相关流程/区域', dataIndex: 'platoOneName', key: 'platoOneName', width: '90px',
        customRender: (value, row, index) => {
          // const obj = {
          //   children: value !== null ? value : "",
          //   attrs: {},
          // };
          // obj.attrs.rowSpan = mergeCells(value, tableForm.value.tableData, "platoOneName", index);//(当前内容,原数组,key值,下标)
          return value.value;
        },
      },{
        title: '相关控制点/设备', dataIndex: 'platoTwoName', key: 'platoTwoName', align: "center", width: '90px',
        customRender: (value, row, index) => {
          // const obj = {
          //   children: value !== null ? value : "",
          //   attrs: {},
          // };
          // obj.attrs.rowSpan = mergeCells(value, tableForm.value.tableData, "platoTwoName", index);//(当前内容,原数组,key值,下标)
          return value.value;
        },
      },
      { title: 'PI名称', dataIndex: 'platoThreeName', key: 'platoThreeName', width: '90px', },
      {
        title: '控制活动',
        dataIndex: 'controlActivity',
        key: 'controlActivity', width: '250px',
        scopedSlots: { customRender: 'htmlSlot' },
      },
      { title: '标准', dataIndex: 'standard', align: "center", key: 'standard', width: '90px', },
      { title: '单位', dataIndex: 'unit', align: "center", key: 'unit', width: '90px', },
      ...editArr
    ]
  }

  
  function mergeCells(text, data, key, index) {
    // 上一行该列数据是否一样
    if (index !== 0 && text === data[index - 1][key]) {
      return 0;
    }
    let rowSpan = 1;
    // 判断下一行是否相等
    for (let i = index + 1; i < data.length; i++) {
      if (text !== data[i][key]) {
        break;
      }
      rowSpan++;
    }
    return rowSpan;
  }

  //获取数据
  function getData(params){
    let factoryCode = userStore.factoryCode
    let paramsObj = { kpiCode: params.kpiParams.kpiCode, year: params.kpiParams.year, type: 1,typeBool:true,factoryCode:factoryCode }
    kpiControlConfiguration(paramsObj).then((res) => {
      if (!res.success) return this.$message.error(res.message);
      res.result && res.result.length > 0 && res.result.forEach(element => {
        if (!element.yearDataVos || element.yearDataVos.length == 0) return;
        
        for (let index = 0; index < element.yearDataVos.length; index++) {
          let item = element.yearDataVos[index]
          Object.keys(item).forEach(elm => {
            let key = elm + "|" + item.planName
            element[key] = item[elm]
          })
        }
      });
      console.log(res.result);
      tableForm.value.tableData = res.result
    })
  }




  defineExpose({
    init,
  });
</script>

<style lang="less">
  /**隐藏样式-modal确定按钮 */
  .jee-hidden {
    display: none !important;
  }

  // :deep(.mycard_tab .ant-card-body) {
  //     padding: 0;
  // }

  // :deep(.myCard .ant-form-item-control) {
  //     line-height: 1;
  // }

  // :deep(.myCard .ant-table-thead th) {
  //     /* display: none; */
  //     text-align: center;
  // }

  // :deep(.myCard .ant-form-item) {
  //     margin-bottom: 0;
  // }

  // :deep(.myCard .ant-table-tbody) {
  //     /* tr:first-child {
  //         background: #EBF1FB;
  //     } */
  //     text-align: center;
  // }
</style>
<style lang="less" scoped></style>
