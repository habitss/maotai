<template>
  <div>
    <pie :chartData="chartData" height="35vh" :option="option" />
  </div>
</template>

<script setup lang="ts">
import Pie from "@/components/chart/Pie.vue";
import {getData} from "@/views/report/chartdemo/chartdemo.data";
const { barDataSource, barMultiData, pieData, barLineData, radarData } = getData;
const option = {
  color: ['#ccc'],
  title: {text: '上月停机分析', left: 'center',bottom: '80%', textStyle: {color: '#fff'}},
  legend: {
    show: false,
    top: '10%',
    bottom: '5%',
    textStyle: {
      color: '#fff'
    }
  },
  series: [
    {
      type: 'pie',
      center: ['50%', '60%'],
      radius: ['20%', '60%'],
      labelLine: { show: true },
      label: {
        show: true,
        formatter: '{b} \n ({d}%)',
        color: '#B1B9D3',
      },
      itemStyle: {
        borderRadius: 10,
        borderWidth: 5,
        borderColor: '#0e1731',
        color: function (params) {
          return params.data.color || '#ccc'
        }
      },
    },
  ],
}

const chartData = [
  { value: 332.27, name: '速度', color: '#eb6f2e' },
  { value: 178.34, name: '故障', color: '#17de02' },
  { value: 117.19, name: '瓶源', color: '#eabf5a' },
  { value: 30.36, name: '物料', color: '#88cbd5' },
  { value: 20.22, name: '酒液', color: '#df7c41' },
  { value: 6.74, name: '能源', color: '#c95948' },
  { value: 3, name: '次品', color: 'hotPink' },
  { value: 20.23, name: '其他', color: '#689e61' },
  { value: 675.01, name: '换膜', color: '#086cec' },
];
</script>

<style scoped>

</style>
