<template>
    <!--基础数据-->
   <a-card :bordered="false">
      <a-form-model ref="formRef" :model="tableForm" class="myForm">
        <a-table size="small" bordered rowKey="id" :loading="baseLoading" :columns="columns" :scroll="{ x: 1100, y: '100vh'}"
          :data-source="tableForm.tableData" :pagination="false">

          <!-- <template :slot='item' slot-scope="text,scope" v-for="item in parentKeys">
            <a-form-model-item>
              <a-input v-model="scope[item]" placeholder="请输入" clearable />
            </a-form-model-item>
          </template> -->
          
          <template v-for="col in parentKeys" :key="col" #[col]="{ text, record, index }">
            <a-form-model-item>
              <a-input v-model:value="record[col]" placeholder="请输入" clearable />
            </a-form-model-item>
          </template>

        </a-table>
      </a-form-model>
      <br>
      <a-button type="primary" @click="onClickSave()">保存</a-button>
    </a-card>
</template>

<script lang="ts" setup>
  import { ref, nextTick, reactive,defineExpose, unref } from 'vue';
  // import { queryKpiReportHeader,listKpiReportData } from '../kpiWeekAndMonReports.api';
  // import { listPlatoTree } from '/@/views/dataCalculation/kpi/plato/KpiPlatoOne.api';
  import { kpiControlConfiguration,kpiControlConfigAddEdit } from '/@/views/dataCalculation/kpi/info/KpiInfo.api';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { useUserStore } from '/@/store/modules/user';
  // 用户信息
  const userStore = useUserStore();
  const { createMessage } = useMessage();
  const reportObj = ref({})
  // const loading = ref(false)
  const baseLoading = ref(false)
  const columns = ref([])
  const parentKeys = ref([])
  
  const tableForm = ref({
    tableData: []
  });

  async function init(params){
    reportObj.value = params
    let yearMonthArr = params.queryParam.yearMonthArr;
    params.tian = yearMonthArr;
    // if(yearMonthArr && yearMonthArr.length > 0){
    getHead(yearMonthArr);
    // }
  }

  function getHead(days) {
      //let { tian: days } = newVal
      let editArr = []
      days && days.length > 0 && days.forEach(newDay => {
        editArr.push({
          title: newDay,
          children: [
            {
              title: '夜班',
              children: [
                { title: '计划频次', dataIndex: `${newDay}|ye|plannedFrequency`, key: `${newDay}|ye|plannedFrequency`, width: '80px', align: "center" },
                { title: '计划量', dataIndex: `${newDay}|ye|amountOfPlan`, key: `${newDay}|ye|amountOfPlan`, width: '80px', align: "center" },
                { title: '实际频次', dataIndex: `${newDay}|ye|actualFrequency`, key: `${newDay}|ye|actualFrequency`, width: '80px', align: "center" },
                { title: '实际量', dataIndex: `${newDay}|ye|actualQuantity`, key: `${newDay}|ye|actualQuantity`, width: '80px', align: "center" },
              ]
            },
            {
              title: '白班',
              children: [
                { title: '计划频次', dataIndex: `${newDay}|bai|plannedFrequency`, key: `${newDay}|bai|plannedFrequency`, width: '80px', align: "center" },
                { title: '计划量', dataIndex: `${newDay}|bai|amountOfPlan`, key: `${newDay}|bai|amountOfPlan`, width: '80px', align: "center" },
                { title: '实际频次', dataIndex: `${newDay}|bai|actualFrequency`, key: `${newDay}|bai|actualFrequency`, width: '80px', align: "center" },
                { title: '实际量', dataIndex: `${newDay}|bai|actualQuantity`, key: `${newDay}|bai|actualQuantity`, width: '80px', align: "center" },
              ]
            },
            {
              title: '中班',
              children: [
                { title: '计划频次', dataIndex: `${newDay}|zhong|plannedFrequency`, key: `${newDay}|zhong|plannedFrequency`, width: '80px', align: "center" },
                { title: '计划量', dataIndex: `${newDay}|zhong|amountOfPlan`, key: `${newDay}|zhong|amountOfPlan`, width: '80px', align: "center" },
                { title: '实际频次', dataIndex: `${newDay}|zhong|actualFrequency`, key: `${newDay}|zhong|actualFrequency`, width: '80px', align: "center" },
                { title: '实际量', dataIndex: `${newDay}|zhong|actualQuantity`, key: `${newDay}|zhong|actualQuantity`, width: '80px', align: "center" },
              ]
            },
          ]
        })
      });

      let columnsData = [
        {
          title: 'KPI名称', dataIndex: 'kpiName', key: 'kpiName', align: "center", width: '100px',
          customRender: (value, row, index) => {
            // const obj = {
            //   children: value !== null ? value : "",
            //   attrs: {},
            // };
            //obj.attrs.rowSpan = this.mergeCells(value, this.tableForm.tableData, "kpiName", index);//(当前内容,原数组,key值,下标)
            return value.value;
          },
        },
        {
          title: '相关部门/车间', dataIndex: 'parentName', key: 'parentName', align: "center", width: '120px',
          customRender: (value, row, index) => {
            // const obj = {
            //   children: value !== null ? value : "",
            //   attrs: {},
            // };
            //obj.attrs.rowSpan = this.mergeCells(value, this.tableForm.tableData, "parentName", index);//(当前内容,原数组,key值,下标)
            return value.value;
          },
        },
        {
          title: '相关流程/区域', dataIndex: 'platoOneName', key: 'platoOneName', width: '120px',
          customRender: (value, row, index) => {
            // const obj = {
            //   children: value !== null ? value : "",
            //   attrs: {},
            // };
            //obj.attrs.rowSpan = this.mergeCells(value, this.tableForm.tableData, "platoOneName", index);//(当前内容,原数组,key值,下标)
            return value.value;
          }
        },
        {
          title: '相关控制点/设备', dataIndex: 'platoTwoName', key: 'platoTwoName', align: "center", width: '120px', customRender: (value, row, index) => {
            // const obj = {
            //   children: value !== null ? value : "",
            //   attrs: {},
            // };
            //obj.attrs.rowSpan = this.mergeCells(value, this.tableForm.tableData, "platoOneName", index);//(当前内容,原数组,key值,下标)
            return value.value;
          }
        },
        { title: 'PI名称', dataIndex: 'platoThreeName', key: 'platoThreeName', width: '120px' },
        { title: '控制活动', dataIndex: 'controlActivity', key: 'controlActivity', width: '100px', scopedSlots: { customRender: 'htmlSlot' } },
        { title: '标准', dataIndex: 'standard', align: "center", key: 'standard', width: '100px' },
        { title: '单位', dataIndex: 'unit', align: "center", key: 'unit', width: '100px', },
        ...editArr

      ]
      getTreeParents(columnsData);
      //设置表头
      columns.value = columnsData;
      //获取数据
      if(days && days.length > 0){
        loadData()
      }
    }

  //设置录入项
  function getTreeParents(treeData, childrenStr = 'children', keyStr = 'key') {
      // const parentKeys = []
      treeData.forEach((item) => {
        if (item[childrenStr] && item[childrenStr].length) {
          getTreeParents(item[childrenStr], childrenStr, keyStr)
          // if (temp.length) {
          //   parentKeys.push(...temp)
          // }
        } else {
          //排查
          let arr = ["kpiName", "parentName", "platoOneName", "platoTwoName", "platoThreeName", "controlActivity", "standard", "unit"]
          if (!arr.includes(item.dataIndex)) {
            item.slots = { customRender: item.dataIndex }
            parentKeys.value.push(item.dataIndex)
          }
        }
      })
      // return parentKeys
    }



  // 保存
  function onClickSave() {
    // this.$refs['formRef'].validate((valid) => {
      // if (valid) {
      if(tableForm.value && tableForm.value.tableData.length>0){
        console.log(JSON.stringify(tableForm.value.tableData))
        let params = []
        tableForm.value.tableData.forEach(item => {
          debugger
          let stringListMap = []
          Object.keys(item).forEach(elm => {
            if (elm.includes("|")) {
              let temp = elm.split("|")
              stringListMap.push({ timeDay: temp[0], classTitle: temp[1], planName: temp[2], planValues: item[elm], threeId: item.platoThreeId })
            }
          })
          params.push({
            ...item,
            stringListMap
          })
        })
        kpiControlConfigAddEdit(params).then((response) => {
          if (!response.success) return createMessage.error(response.message);
          createMessage.success(response.message);
          loadData()
          // this.editRow = false
        }).catch((err) => {
        });
      } else {
        console.log('error submit!!')
        createMessage.warning('有未输入项')
      }
    // })
  }
  //查询
   async function loadData() {
    //let params = { kpiCode: reportObj.kpiParams.kpiCode, tian: reportObj.tian.year, type: 1 }
    // let { kpiCode, tian } = reportObj.value
    let kpiCode = reportObj.value.kpiParams.kpiCode;
    let tian = reportObj.value.tian;
    let year = reportObj.value.kpiParams.year;
    let factoryCode = userStore.factoryCode

    let params = { kpiCode: kpiCode, tian: tian,year: year,factoryCode:factoryCode }  //tian 是日期数组
    //baseLoading.value = true;
    let res = await kpiControlConfiguration(params)
    //baseLoading.value = false;
    if (!res.success || !res.result.length) return;
    res.result.forEach(element => {
      if (!element.stringListMap || element.stringListMap.length == 0) return;
      for (let index = 0; index < element.stringListMap.length; index++) {
        const item = element.stringListMap[index];
        let key = item.timeDay + "|" + item.classTitle + "|" + item.planName
        element[key] = item.planValues
      }
    });
    console.log(res.result);
    tableForm.value.tableData = []
    tableForm.value.tableData = res.result
  }
  //合并
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
  defineExpose({
    init,
  });
</script>

<style lang="less">
  /**隐藏样式-modal确定按钮 */
  .jee-hidden {
    display: none !important;
  }

  .t1 {
    width: 100%;
    height: 40px;
    background: #fbfbfb;
    font-size: 16px;
    font-weight: bold;
    text-align: center;
    line-height: 40px;
    //border: 1px solid #e8e8e8
}

.t2 {
    .t1();
    font-weight: 400;
}

.title {
  width: 100%;
  background: #fbfbfb;
  line-height: 30px;
  text-align: center;
  font-weight: bold;
}
</style>
<style lang="less" scoped></style>
