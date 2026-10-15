<template>
  <div class="container">
    <div ref="chartRef" :style="{ height, width }"></div>
  </div>
</template>
<script lang="ts">
import { defineComponent, ref, Ref, reactive, watchEffect } from 'vue';
import { useECharts } from '/@/hooks/web/useECharts';
import { useRootSetting } from '/@/hooks/setting/useRootSetting';
export default defineComponent({
  props: {
    chartData: {
      type: Array,
      default: () => [],
    },
    width: {
      type: String as PropType<string>,
      default: '100%',
    },
    height: {
      type: String as PropType<string>,
      default: '330px',
    },
  },

  setup(props) {
    const chartRef = ref<HTMLDivElement | null>(null);
    const { setOptions } = useECharts(chartRef as Ref<HTMLDivElement>);
    const { getThemeColor } = useRootSetting();

    const option = reactive({
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          lineStyle: {
            width: 1,
            color: getThemeColor.value,
          },
        },
      },

      // toolbox: {
      //     feature: {
      //       dataZoom: {
      //         iconStyle: {
      //           borderColor: '#ffffff'
      //         },
      //         yAxisIndex: 'none'
      //       },
      //       dataView: { readOnly: false },
      //       restore: { 
      //         iconStyle: {
      //           borderColor: '#ffffff'
      //         }
      //       },
      //       saveAsImage: { 
      //         iconStyle: {
      //           borderColor: '#ffffff'
      //         }
      //       }
      //     },
      //     show: true
      //   },
      toolbox: {
        feature: {
          dataZoom: {
            yAxisIndex: 'none'
          },
        }
      },
      dataZoom: [
        {
          show: true,
          realtime: true,
          start: 46,
          end: 50
        },
        {
          type: 'inside',
          realtime: true,
          start: 46,
          end: 50
        }
      ],
      xAxis: {
        type: 'category',
        interval: 0,
        splitLine: {
          show: true,
          lineStyle: {
            width: 1,
            type: 'solid',
            color: 'rgba(226,226,226,0.5)',
          },
        },
        axisTick: {
          show: false,
        },
        axisLabel: {
          interval: 0,
          formatter: function(params) {
              var newParamsName = ''
              var paramsNameNumber = params.length
              var provideNumber = 11
              var rowNumber = Math.ceil(paramsNameNumber / provideNumber)
              if (paramsNameNumber > provideNumber) {
                for (var p = 0; p < rowNumber; p++) {
                  var tempStr = ''
                  var start = p * provideNumber
                  var end = start + provideNumber
                  if (p === rowNumber - 1) {
                    tempStr = params.substring(start, paramsNameNumber)
                  } else {
                    tempStr = params.substring(start, end) + '\n'
                  }
                  newParamsName += tempStr
                }
              } else {
                newParamsName = params
              }
              return newParamsName
            }
        }
      },
      yAxis: {
        type: 'value',
        axisTick: {
          show: false,
        },
        splitArea: {
          show: true,
          areaStyle: {
            color: ['rgba(255,255,255,0.2)', 'rgba(226,226,226,0.2)'],
          },
        },
      },
      series: [
        {
          type: 'line',
          smooth: true
        }
      ]
    })

    watchEffect(() => {
        props.chartData && initCharts();
      });

    function initCharts() {
      const xData = props.chartData.map((item) => { return item.time })
      const seriesData = props.chartData.map((item) => { return item.value})
      option.xAxis.data = xData
      option.series[0].data = seriesData
      setOptions(option);
    }

    return { chartRef };
  }
})
</script>
