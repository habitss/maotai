<template>
  <div class="container">
    <!-- 查询区域 -->
    <a-row class="search-part">
      <a-col :span="24">
        <BasicForm @register="registerForm" @submit="handleSubmit" />
      </a-col>
      <a-col :span="24">
        <div ref="chart" class="chartPart"></div>
      </a-col>
    </a-row>
  </div>
</template>

<script lang="ts" name="actionPlan-summaryIndex" setup>
import { ref } from 'vue';
import { useForm, BasicForm } from '/@/components/Form';
import { searchFormSchema } from './pointChart.data';
import { list } from './pointChart.api';
import * as echarts from 'echarts';
//
const lineChartOption = ref();
const chart = ref();

/**
 * BasicForm绑定注册;
 */
const [registerForm, { validate }] = useForm({
  schemas: searchFormSchema,
  showAdvancedButton: false,
  showResetButton: false,
  actionColOptions: { span: 2 },
  fieldMapToTime: [['searchTime', ['searchTimeStart', 'searchTimeEnd'], 'YYYY-MM-DD HH:mm:ss']],
  fieldMapToNumber: [],
  baseColProps: { span: 6 },
  labelCol: { style: { width: '80px' } },
  wrapperCol: { style: { marginRight: '20px' } },
});

/**
 * 点击提交按钮的value值
 */
async function handleSubmit() {
  const values = await validate();
  //获取后台数据
  const res = await list(values);
  if (!res) {
    return;
  }

  let legendData = [] as any;
  for (const item of res.fieldList) {
    legendData.push(item.name);
    item.type = 'line'
    let tempArr = [] as any;
    for (const obj of res.data) {
      tempArr.push(obj[item.code] ? obj[item.code] : 0);
    }
    item.data = tempArr;
  }

  let xAxisData = [] as any;
  for (const item of res.data) {
    xAxisData.push(item.timeCode);
  }

  //柱状图配置
  lineChartOption.value = {
    title: {
      left: 'center'
    },
    tooltip: {
      trigger: 'axis'
    },
    grid: {
      show: false,
      top: '12%',
      right: '5%',
      bottom: '10%',
      left: '5%'
    },
    legend: {
      data: legendData
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: xAxisData
    },
    yAxis: {
      type: 'value'
    },
    series: res.fieldList,
    dataZoom: [
      {
        type: 'slider',
        show: true,
        start: 0,
        end: 30,
      },
      {
        type: 'inside',
        show: true,
        start: 0,
        end: 100,
        xAxisIndex: [0],
        filterMode: 'filter'
      },
    ],
  };
  var myChart = echarts.init(chart.value);
  myChart.setOption(lineChartOption.value);
}
</script>

<style scoped>
/** 时间和数字输入框样式 */
:deep(.ant-input-number) {
  width: 100%;
}

:deep(.ant-picker) {
  width: 100%;
}

.container {
  max-width: 100%;
  padding: 10px;
}

.search-part {
  padding: 14px;
  background-color: #fff;
}

.list-part {
  background-color: #fff;
  margin-top: 10px;
}

.chartPart {
  height: calc(100vh - 300px) !important;
  margin-top: 20px;
}
</style>