<template>
  <!-- <div :id='id' class='pie'></div> -->
  <div ref="chartRef" class='pie'></div>
</template>

<script lang="ts" setup>
import { ref,defineProps,onMounted,nextTick,watch } from 'vue';
// import * as echarts from 'echarts';
import * as echarts from 'echarts';


const props = defineProps(['id', 'percent', 'num1', 'num2']);
const chartRef = ref();

function initPie(pieId, percent, num1, num2) {
  // var getchart = echarts.init(document.getElementById(pieId));
  const colorRed = [
    {
      offset: 0,
      color: 'red' // 0% 处的颜色
    },
    {
      offset: 1,
      color: 'red' // 100% 处的颜色
    }
  ];
  const colorGreen = [
    {
      offset: 0,
      color: '#00ff00' // 0% 处的颜色
    },
    {
      offset: 1,
      color: '#00ff00' // 100% 处的颜色
    }
  ];
  console.log('num1:', num1);
  console.log('num2:', num2);
  var option = {
    title: [
      {
        text: percent + '%',
        x: 'center',
        y: '35%',
        textStyle: {
          fontWeight: 'normal',
          color: '#0580f2',
          fontSize: 20
        }
      },
      {
        text: '达标率',
        // left: "50%",
        top: '53%',
        x: 'center',
        y: 'center',
        // textAlign: "center",
        textStyle: {
          fontSize: 14,
          fontWeight: 400,
          color: '#333',
          textAlign: 'center'
        }
      }
    ],
    tooltip: {
      trigger: 'item',
      //  formatter: '{a} <br/>{b}: {c} ({d}%)',
      axisPointer: {
        type: 'shadow'
      }
    },
    color: ['red'], //剩余的环颜色
    legend: {
      show: false
      // itemGap: 12,
      // data: ["01", "02"],
    },
    series: [
      {
        name: '达标率',
        type: 'pie',
        clockWise: false,
        radius: ['82%', '99%'],
        itemStyle: {
          normal: {
            label: {
              show: false
            },
            labelLine: {
              show: false
            }
          }
        },
        hoverAnimation: false,
        data: [
          {
            value: num1,
            name: '达标',
            itemStyle: {
              normal: {
                color: {
                  colorStops: colorGreen
                },
                label: {
                  show: false
                },
                labelLine: {
                  show: false
                }
              }
            }
          },
          {
            name: '未达标',
            value: num2,
            itemStyle: {
              normal: {
                'color': { colorStops: colorRed },
                label: {
                  show: false
                },
                labelLine: {
                  show: false
                }
              }
            }

          }
        ]
      }
    ]
  };

  // // 4.3 更新配置
  // getchart.setOption(option);
  // //随着屏幕大小调节图表
  // window.addEventListener('resize', () => {
  //   getchart.resize();
  // });
  var myChart = echarts.init(chartRef.value);
  myChart.setOption(option);
}

const initChart = () => {
  nextTick(() => {
    if (chartRef.value) {
      initPie(props.id, props.percent, props.num1, props.num2);
    }
  });
};

onMounted(() => {
  initChart();
});

// 监听 props 的变化
watch(
  () => [props.percent, props.num1, props.num2],
  () => {
    initChart();
  },
  { deep: true }
);

</script>
<style scoped>


.pie {
  width: 210px;
  height: 152px;
  background: url('@/assets/images/query.jpg');
  background-position: center;
  background-size: 115%;
  background-repeat: no-repeat;
  background-position: -23px -4px;
}


</style>
