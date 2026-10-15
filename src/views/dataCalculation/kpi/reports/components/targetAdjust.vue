<template>
    <!--目标值调整-->
    <a-card :bordered="false">
        <a-table size="small" bordered rowKey="id" :custom-row='customRow' :columns="columns" :scroll="{ x: true }" :loading="mounthLoading"
        :data-source="dataSource" :pagination="false">
            <!--编辑-->
            <!-- <template v-for="col in mounthColoumns" :slot="col" slot-scope="text, record, index"> -->

            <template v-for="col in mounthColoumns" #[col]="{ text, record, index }" :key="col">
                <!-- <div class="adjustNode" v-if="record.indicator == '调整目标值' && Object.prototype.toString.call(record[col]) == '[object Object]' && record[col].hasValue && record.editable"> -->
                <div class="adjustNode" v-if="record.indicator == '调整目标值' && Object.prototype.toString.call(record[col]) == '[object Object]' && record[col].hasValue">
                    <a-input @pressEnter="onClickSave" v-model:value="record[col].value" style="text-align: center;"/>
                </div>
                <div v-else>
                    <span>{{text.value}}</span>
                </div>
            </template>
        </a-table>
        <br>
        <a-button type="primary" @click="onClickSave()">保存</a-button>
        <br>
        <div ref="chartRef" style="width:100%;height:400px;"></div>
    </a-card>
</template>

<script lang="ts" setup>
  import { ref, nextTick, reactive,defineExpose, unref } from 'vue';
  import { monthTracking,monthTrackingUpdate } from '/@/views/dataCalculation/kpi/input/KpiTargetInput.api';
  import { useMessage } from '/@/hooks/web/useMessage';
  import * as echarts from 'echarts';
  import dayjs from "dayjs";

  const reportObj = ref({})
  const mounthLoading = ref(false)
  const { createMessage } = useMessage();
  //图表结构
  const dataOption = ref([] as any);
  const chartData = ref([] as any);
  const chartRef = ref();
  //年累key标识
  const yearAccumulateKey = ref('');

  //表头
  const columns = ref([
    {
        key: 'monthTracking', dataIndex: 'monthTracking', title: '', width: 80, align: 'center', 
        customCell:(value, index) => {
            const obj = {}
            if (index === 0) {
                obj.rowSpan = 3;
            } else {
                obj.rowSpan = 0;
            }
            return obj;
        }
    },
    { key: 'indicator', dataIndex: 'indicator', title: '', width: 80, },
    { key: 'janValue', dataIndex: 'janValue', title: '1月', width: 50, align: 'center',slots: { customRender: 'janValue' }},
    { key: 'febValue', dataIndex: 'febValue', title: '2月', width: 50, align: 'center',slots: { customRender: 'febValue' }},
    { key: 'marValue', dataIndex: 'marValue', title: '3月', width: 50, align: 'center',slots: { customRender: 'marValue' } },
    { key: 'aprValue', dataIndex: 'aprValue', title: '4月', width: 50, align: 'center',slots: { customRender: 'aprValue' }},
    { key: 'mayValue', dataIndex: 'mayValue', title: '5月', width: 50, align: 'center' ,slots: { customRender: 'mayValue' }},
    { key: 'junValue', dataIndex: 'junValue', title: '6月', width: 50, align: 'center',slots: { customRender: 'junValue' }},
    { key: 'julValue', dataIndex: 'julValue', title: '7月', width: 50, align: 'center',slots: { customRender: 'julValue' }},
    { key: 'augValue', dataIndex: 'augValue', title: '8月', width: 50, align: 'center' ,slots: { customRender: 'augValue' }},
    { key: 'sepValue', dataIndex: 'sepValue', title: '9月', width: 50, align: 'center' ,slots: { customRender: 'sepValue' }},
    { key: 'octValue', dataIndex: 'octValue', title: '10月', width: 50, align: 'center' ,slots: { customRender: 'octValue' }},
    { key: 'novValue', dataIndex: 'novValue', title: '11月', width: 50, align: 'center' ,slots: { customRender: 'novValue' }},
    { key: 'decValue', dataIndex: 'decValue', title: '12月', width: 50, align: 'center' ,slots: { customRender: 'decValue' }},
    { key: 'yearTarget', dataIndex: 'yearTarget', title: '年度', width: 80, align: 'center',slots: { customRender: 'yearTarget' }},
  ])
  
  const mounthColoumns= ref(['janValue', 'febValue', 'marValue', 'aprValue', 'mayValue', 'junValue', 'julValue', 'augValue', 'sepValue', 'octValue', 'novValue', 'decValue', 'yearTarget'])

  //数据
  const dataSource = ref([])

  async function init(params){
    //reportObj.value['kpiName'] = params.kpiParams.kpiName;
    reportObj.value = params.kpiParams;

    //表头
    setHeadMounth();
    //图表初始化
    //barEcharts([],[]);
    getMonthTracking()
  }

  function customRow(record, index) {
    return {
        on: {
            dblclick: (event) => {
                const newData = JSON.parse(JSON.stringify(dataSource.value));
                newData.forEach((ele, curIndex) => { // 双击的这一行可编辑,其他行的编辑状态关闭
                    ele.editable = curIndex === index
                })
                dataSource.value = newData
            }
        }
    }
  }
  

  //图表初始化
  function barEcharts(seriesList, optionNameList) {
      // 配置图表
      var option = {
        tooltip: {
          trigger: 'axis'
        },
        legend: {
          data: optionNameList
        },
        //x轴显示种类
        xAxis: {
          type: 'category',
          boundaryGap: false,
          data: [
            `${dayjs().year()}-01`,
            `${dayjs().year()}-02`,
            `${dayjs().year()}-03`,
            `${dayjs().year()}-04`,
            `${dayjs().year()}-05`,
            `${dayjs().year()}-06`,
            `${dayjs().year()}-07`,
            `${dayjs().year()}-08`,
            `${dayjs().year()}-09`,
            `${dayjs().year()}-10`,
            `${dayjs().year()}-11`,
            `${dayjs().year()}-12`,
          ],
          "axisTick": {
            "show": true
          },
          "axisLabel": {
            "show": true
          }
        },
        //y轴可填数值等
        yAxis: { type: 'value' },
        series: seriesList
      }
      dataOption.value = option;
      var myChart = echarts.init(chartRef.value);
      myChart.setOption(option);
      //chartData.value = seriesList;
      
      
  }
  //设置颜色
  function setCellBg(cell, col) {
    let colorObj = {}
    let record = cell[col]
    if (typeof record == 'undefined')
        return { style: {} }
    if (record.colour === 'green') {
        colorObj.bgColor = '#00FF00'
    } else if (record.colour === 'red') {
        colorObj.bgColor = '#FE0000'
    } else if (record.colour === 'yellow') {
        colorObj.bgColor = 'yellow'
    } else {
        colorObj.bgColor = ''
    }
    return {
        style: {
            'background-color': colorObj.bgColor,
            'color': colorObj.fontColor
        }
    }
  }
  //月度跟踪
  function getMonthTracking() {
    return new Promise(async (resolve) => {
        let params = {}
        console.log(reportObj.value);
        params.reportId = reportObj.value['reportId']
        params.month = reportObj.value['yearMonth']

        let parts = params.month.split('-');
        let month = parseInt(parts[1]);

        let tableKey = params.month+'-yearAccumulate';
        columns.value[14].title = month+"月年累";
        columns.value[14].dataIndex = tableKey;
        columns.value[14].key = tableKey;
        columns.value[14].slots = { customRender:  tableKey}
        mounthColoumns.value[14] = tableKey
        yearAccumulateKey.value = tableKey

        params.kpiCode = reportObj.value['kpiCode']
        mounthLoading.value = true;
        //查询
        await monthTracking(params).then(res => {
            // if (res.success) {
            //     let { result } = res
                res.forEach((ele, index) => {
                    ele.editable = false
                    for (let key in ele) {
                        let typeStr = Object.prototype.toString.call(ele[key])
                        if (typeStr == '[object Object]') ele[key].hasValue = checkHasValue(res, key);
                    }
                })
                dataSource.value = res
                setOptionSeries();
            // } else {
            //     createMessage.warning(res.message);
            // }
        }).finally(() => {
            mounthLoading.value = false;
        })
    })
 }
//保存
function onClickSave() {
    let params = JSON.parse(JSON.stringify(dataSource.value));
    debugger
    params.forEach(mm => {
    for (let key in mm) {
        let typeStr = Object.prototype.toString.call(mm[key])
        if (typeStr == '[object Object]') delete mm[key]['hasValue']
    }
    })
    return new Promise(async (resolve) => {
        params[2].yearAccumulateKey = yearAccumulateKey.value;
        await monthTrackingUpdate(params[2]).then(res => {
            if (res.success) {
            // let { result } = res   
            console.log('resultresultresultresult::', res)
            createMessage.success('数据更新成功！')
            getMonthTracking()

            } else {
                createMessage.warning(res.message);
            }
        }).finally(() => {
        
        })
    })
}


//图表设置值
function setOptionSeries() {
    let data = JSON.parse(JSON.stringify(dataSource.value));
    let seriesList = [],
    optionNameList = [];

    data.forEach(ele => {
        let seriseData = getSeriseData(ele)
        optionNameList.push(ele.indicator)
        seriesList.push({
            name: ele.indicator,
            type: 'line',
            // stack: 'Total',
            data: seriseData
        })
    })
    console.log(' setOptionSeries', seriesList)
    barEcharts(seriesList, optionNameList)
}
function checkHasValue(data, key) {
    let hasValue = false;
    data.forEach((ele, index) => {
        if(null != ele[key]){
            if (ele[key].value != '') {
                return hasValue = true;
            }
        }
    })
    return hasValue
}
//表头-颜色
function setHeadMounth() {
    columns.value.forEach((item, index) => {
        item.scopedSlots = { customRender: item.dataIndex }
        if (index > 1 && index < 14) {
            item.customCell = (record, rowIndex) => {
                return setCellBg(record, item.dataIndex)
            }
        }
    })
    console.log('this.columns:', columns.value)
}

function getSeriseData(data) {
    let keyList = ['janValue', 'febValue', 'marValue', 'aprValue', 'mayValue', 'junValue',
    'julValue', 'augValue', 'sepValue', 'octValue', 'novValue', 'decValue']
    let callbackData = [];
    for (let i = 0; i < keyList.length; i++) {
        callbackData.push(data[keyList[i]].value != '' ? parseInt(data[keyList[i]].value) : 0)
    }
    return callbackData;
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

:deep(.adjustNode .ant-input){
  padding: 0;
  height: auto;
}
</style>
<style lang="less" scoped></style>
