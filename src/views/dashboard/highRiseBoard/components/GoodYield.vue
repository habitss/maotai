<template>
  <div style="margin-top: 10px">
    <BarMulti :chartData="barMultiData" :option="multiBarOption" height="25vh"></BarMulti>
  </div>
</template>

<script lang="ts" setup>
import {formatNumber} from "@/utils";
import LineMulti from "@/components/chart/LineMulti.vue";
import {getData} from "@/views/report/chartdemo/chartdemo.data";
import BarMulti from "@/components/chart/BarMulti.vue";

const {barDataSource, barMultiData, pieData, barLineData, radarData} = getData;
const multiBarOption = {
  title: {text: '良品率', left: 'left', textStyle: {color: '#fff'}},
  grid: {
    bottom: 30
  },
  xAxis: {
    axisLine: {
      lineStyle: {
        color: '#fff',
      }
    }
  },
  yAxis: {
    axisLine: {
      lineStyle: {
        color: '#fff'
      }
    }
  },
  legend: {
    textStyle: {
      color: '#fff'
    }
  }
};
</script>

<style scoped>

</style>
