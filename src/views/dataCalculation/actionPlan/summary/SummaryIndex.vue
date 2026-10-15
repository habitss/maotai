<template>
  <div class="container">
    <!-- 查询区域 -->
    <a-row class="search-part">
      <a-col :span="24">
        <BasicForm @register="registerForm" @submit="handleSubmit" />
      </a-col>
    </a-row>
    <a-row class="list-part">
      <a-col :span="24">
        <BarMulti class="chartPart" :chartData="barChartData" :option="barOption" :seriesColor="colorArray"></BarMulti>
        <Pie class="chartPart" :chartData="pieChartData" :option="pieOption"></Pie>
      </a-col>
    </a-row>
    <a-row class="list-part">
      <a-col :span="24">
        <SummaryList ref="summaryListRef"></SummaryList>
      </a-col>
    </a-row>
  </div>
</template>

<script lang="ts" name="actionPlan-summaryIndex" setup>
import { onMounted, ref } from 'vue';
import { useForm, BasicForm } from '/@/components/Form';
import { searchFormSchema, colorArray, barOption, pieOption } from './Summary.data';
import { list } from './Summary.api';
import BarMulti from '/@/components/chart/BarMulti.vue';
import Pie from '/@/components/chart/Pie.vue';
import SummaryList from './components/SummaryList.vue';
//柱状图数据
const barChartData = ref([] as any);

//饼图数据
const pieChartData = ref([] as any);

//列表数据
const summaryListRef = ref();

/**
 * BasicForm绑定注册;
 */
const [registerForm, { submit }] = useForm({
  schemas: searchFormSchema,
  showAdvancedButton: false,
  showResetButton: false,
  actionColOptions: { span: 2 },
  fieldMapToTime: [
    ['dateRangeField', ['dateRangeField_begin', 'dateRangeField_end'], 'YYYY-MM-DD'],
  ],
  fieldMapToNumber: [],
  baseColProps: { span: 6 },
  labelCol: { style: { width: '80px' } },
  wrapperCol: { style: { marginRight: '20px' } },
});

/**
 * 点击提交按钮的value值
 * @param values
 */
async function handleSubmit(values: any) {
  //设置柱状图标题
  barOption.value.title.text = values.userOrDept === '1' ? '个人行动统计' : '部门行动统计';
  //获取后台数据
  const res = await list(values);
  if(!res){
    return;
  }
  const { chartData, summaryListData } = res;
  //设置饼图数据
  pieChartData.value = [
    {
      "name": "进行中",
      "value": chartData.progressTotal,
    },
    {
      "name": "延迟",
      "value": chartData.delayTotal,
    },
    {
      "name": "已完成",
      "value": chartData.finishedTotal,
    }
  ];

  //设置柱状图数据
  let barArr = [] as any;
  for(const item of chartData.objectList){
    const name = Object.keys(item)[0];
    barArr.push({
      "name": name,
      "value": item[name].progressTotal,
      "type": "进行中",
    });
    barArr.push({
      "name": name,
      "value": item[name].delayTotal,
      "type": "延迟",
    });
    barArr.push({
      "name": name,
      "value": item[name].finishedTotal,
      "type": "已完成",
    });
  }
  barChartData.value = barArr;

  //调整图例显示文本
  barOption.value.legend.formatter = (name) => legendFormatter(name, chartData);
  pieOption.value.legend.formatter = (name) => legendFormatter(name, chartData);

  //设置列表数据
  summaryListRef.value.reloadData(summaryListData);
}

//格式化图例显示文本
function legendFormatter(name, data) {
  // 计算总数
  const total = data.progressTotal + data.delayTotal + data.finishedTotal;
  let percentageA, percentageB, percentageC;

  // 确保总数不为0，避免除以0的错误
  if (total !== 0) {
    // 计算每个数的百分比
    percentageA = ((data.progressTotal / total) * 100).toFixed(2);
    percentageB = ((data.delayTotal / total) * 100).toFixed(2);
    percentageC = ((data.finishedTotal / total) * 100).toFixed(2);
  }
  

  if (name === '进行中') {
    return `进行中：${data.progressTotal}(${percentageA ?? 0}%)`;
  } else if (name === '延迟') {
    return `延迟：${data.delayTotal}(${percentageB ?? 0}%)`;
  } else if (name === '已完成') {
    return `已完成：${data.finishedTotal}(${percentageC ?? 0}%)`;
  }
}

onMounted(() => {
  submit();
});
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
  height: 30vh !important;
  margin-top: 20px;
}
</style>