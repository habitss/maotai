<template>
  <div style="margin-top: 10px">
    <BarMulti :chartData="chartData" :option="multiBarOption" height="35vh" :itemStyle="itemStyle"></BarMulti>
  </div>
</template>

<script lang="ts" setup>
import {getData} from "@/views/report/chartdemo/chartdemo.data";
import BarMulti from "@/components/chart/BarMulti.vue";
import {computed, ref, unref, watchEffect} from "vue";

const {barDataSource, barMultiData, pieData, barLineData, radarData} = getData;
const props = defineProps({
  chartData: {
    type: Object,
    default: () => {
      return []
    }
  },
})

const chartData = computed(() => {
  let arr = Array.from(new Set(props.chartData.map((item) => item.lineName)))
  let data: any = []
  let xData = Array.from(new Set(props.chartData.map((item) => item.month)))
  arr.forEach(line => {
    xData.forEach(x => {
      let obj = {
        name: x + '月',
        type: line,
        value: 0
      }
      let dataArr = props.chartData.filter((item) => line === item.lineName && item.month == x)
      console.log(dataArr, 'dataArr')
      if (dataArr && dataArr.length > 0) {
        obj.value = dataArr[0].yield
      }
      data.push(obj)
    })
  })
  return data
})
const multiBarOption: any = ref({
  title: {text: '每月效率(%)', left: 'left', textStyle: {color: '#fff'}},
  grid: {
    bottom: 30,
    left: '15%'
  },
  xAxis: {
    boundaryGap: true,
    axisLine: {
      lineStyle: {
        color: '#fff',
      }
    },
    axisLabel: {
      interval: 0,
      textStyle: {
        fontSize: 14
      },
    },
    data: []
  },
  yAxis: {
    axisLine: {
      lineStyle: {
        color: '#fff'
      }
    },
    splitLine: {
      show: true,
      lineStyle: {
        opacity: 0.2
      }
    },
    axisLabel: {
      textStyle: {
        fontSize: 14
      },
      formatter: function (value) {
        return value + "%";
      },
    },
  },
  legend: {
    top: '10%',
    textStyle: {
      color: '#fff'
    },
    data: [
      {
        name: '1#',
        itemStyle: {
          color: '#4564af'
        }
      },
      {
        name: '2#',
        itemStyle: {
          color: '#76afbb'
        }
      },
    ]
  },
  series: []
});

watchEffect(() => {
  props.chartData && setMultiBarOption()
})

const itemStyle = {
  color: function (params) {
    return params.seriesName === '1#' ? '#dc7640' : '#3474f6'
  }
}

function setMultiBarOption() {
  multiBarOption.value.xAxis['data'] = Array.from(new Set(unref(chartData).map((item) => item.name)))
  multiBarOption.value.series = [
    {
      name: '1#',
      type: 'bar',
      itemStyle: {
        color: '#4564af'
      },
      label: {
        show: true,
        position: 'top',
        textStyle: {
          color: '#fff',
          fontSize: 12
        }
      },
      lineStyle: {
        color: '#4564af',
      },
      areaStyle: {
        opacity: 0.4,
        color: '#4564af'
      },
      emphasis: {
        focus: 'series'
      },
      data: unref(chartData).filter(item => item.type === '1#').map(item => item.value)
    },
    {
      name: '2#',
      type: 'bar',
      itemStyle: {
        color: '#76afbb'
      },
      label: {
        show: true,
        position: 'inside',
        textStyle: {
          color: '#fff',
          fontSize: 12
        }
      },
      lineStyle: {
        color: '#76afbb'
      },
      areaStyle: {
        opacity: 0.4,
        color: '#76afbb'
      },
      emphasis: {
        focus: 'series'
      },
      data: unref(chartData).filter(item => item.type === '2#').map(item => item.value)
    }
  ]
  console.log(unref(multiBarOption), 'multiBarOptionmultiBarOptionmultiBarOption')
}
</script>

<style scoped>

</style>
