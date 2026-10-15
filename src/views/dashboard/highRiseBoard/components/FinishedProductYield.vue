<template>
  <div style="margin-top: 10px">
    <LineMulti :chartData="chartData" :option="multiBarOption" :itemStyle="itemStyle" height="33vh"
               type="line"></LineMulti>
  </div>
</template>

<script lang="ts" setup>
import LineMulti from "@/components/chart/LineMulti.vue";
import {getData} from "@/views/report/chartdemo/chartdemo.data";
import {computed, reactive, ref, unref, watchEffect} from "vue";

const {barDataSource, barMultiData, pieData, barLineData, radarData} = getData;

const props = defineProps({
  chartData: {
    type: Object,
    default: () => {
      return []
    }
  },
})
const multiBarOption: any = ref({
  title: {text: '每月产量（万瓶）', left: 'left', textStyle: {color: '#fff'}, bottom: '90%'},
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
        return value;
      },
    },
  },
  legend: {
    top: '10%',
    textStyle: {
      color: '#fff',
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
})
const itemStyle = {
  color: function (params) {
    return params.seriesName === '1#' ? '#e69840' : '#f1c993'
  }
}
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
watchEffect(() => {
  props.chartData && setMultiBarOption()
})

function setMultiBarOption() {
  multiBarOption.value.xAxis['data'] = Array.from(new Set(unref(chartData).map((item) => item.name)))
  multiBarOption.value.series = [
    {
      name: '1#',
      type: 'line',
      itemStyle: {
        color: '#4564af'
      },
      label: {
        show: true,
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
      type: 'line',
      itemStyle: {
        color: '#76afbb'
      },
      label: {
        show: true,
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
