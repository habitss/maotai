<template>
  <a-row>
    <a-col :span="24" class="chartTitle"><span>{{ chartTitle }}</span></a-col>
    <a-col :span="12">
      <BarMulti ref="aa" class="chartPart" :chartData :option="barOption" :height="'200px'"></BarMulti>
    </a-col>
    <a-col :span="12">
      <Pie class="chartPart" :chartData :option="pieOption"></Pie>
    </a-col>
  </a-row>
</template>

<script lang="ts" name="action.plan-raList" setup>
import { onMounted, ref } from 'vue';
import { barOption, pieOption } from '../ManageCheckSummary.data';
import BarMulti from '/@/components/chart/BarMulti.vue';
import Pie from '/@/components/chart/Pie.vue';
//父组件传值
const props = defineProps({
  type: { type: Number, default: 1 },
  monthRange: { type: Array, default: ''}
});
//标题信息
const chartTitle = ref('');
const aa = ref('');
//柱状图数据
const chartData = ref([] as any);

function reloadData(data){
  //先清空数据数组
  chartData.value = [];
  //删除最后一行汇总数据
  data.pop();
  data.forEach((item)=>{
    chartData.value.push({
      name: item.dynamicText,
      value: item.problemCount
    });
  });
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
  chartTitle.value = text+'汇总分析';
})
//暴露方法
defineExpose({
  reloadData
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

.chartTitle{
  text-align: center;
  font-size: 20px;
  font-weight: bold;
  margin-top: 20px;
}

.chartPart {
  width: 100% !important;
  height: 700px !important;
}
</style>