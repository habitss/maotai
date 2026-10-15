<template>
  <!--<div class="background-image">-->
    <!--<img :src="getImageUrl('highRiseBoard.jpg')" alt="">-->
  <div class="background-image" :style="{ backgroundImage: `url(${getImageUrl('highRiseBoard.jpg')})` }">
    <a-row style="position: relative; z-index: 1;" :gutter="[5]">
      <a-col style="margin-top: 15px" :span="7">
        <a-col :span="24">
          <finished-product-output></finished-product-output>
        </a-col>
        <a-col :span="24">
          <finished-product-yield :chartData="finishedProductYield_chartData"></finished-product-yield>
        </a-col>
        <a-col :span="24">
          <workpiece-ratio :chartData="workpieceRatio_chartData"></workpiece-ratio>
        </a-col>
        <a-col :span="24">
          <!--<good-yield></good-yield>-->
        </a-col>
      </a-col>
      <a-col :span="10">
        <a-col :span="24">
          <finished-product-today></finished-product-today>
        </a-col>
        <a-col :span="24">
          <production-plan-board></production-plan-board>
        </a-col>
      </a-col>
      <a-col style="margin-top: 15px" :span="7">
        <a-col :span="24">
          <energy-unit-consumption :data="energyUnitConsumption_data"></energy-unit-consumption>
        </a-col>
        <a-col :span="24">
          <bottle-inspection-machine-yield></bottle-inspection-machine-yield>
        </a-col>
        <a-col :span="24">
          <shutdown-analysis></shutdown-analysis>
        </a-col>
      </a-col>
    </a-row>
  </div>
</template>

<script lang="ts" setup>
import FinishedProductOutput
  from "@/views/dashboard/highRiseBoard/components/FinishedProductOutput.vue";
import FinishedProductYield
  from "@/views/dashboard/highRiseBoard/components/FinishedProductYield.vue";
import WorkpieceRatio from "@/views/dashboard/highRiseBoard/components/WorkpieceRatio.vue";
import GoodYield from "@/views/dashboard/highRiseBoard/components/GoodYield.vue";
import FinishedProductToday
  from "@/views/dashboard/highRiseBoard/components/FinishedProductToday.vue";
import ProductionPlanBoard
  from "@/views/dashboard/highRiseBoard/components/ProductionPlanBoard.vue";
import EnergyUnitConsumption
  from "@/views/dashboard/highRiseBoard/components/EnergyUnitConsumption.vue";
import BottleInspectionMachineYield
  from "@/views/dashboard/highRiseBoard/components/BottleInspectionMachineYield.vue";
import ShutdownAnalysis from "@/views/dashboard/highRiseBoard/components/ShutdownAnalysis.vue";
import {onBeforeUnmount, onMounted, reactive, ref} from "vue";
import {REPORT_EFFICIENCY_DAPINGEFFICIENCY} from "@/views/dashboard/highRiseBoard/index.api";

// 月产量
let finishedProductYield_chartData = ref([
  // {
  //   "lineCode": "A04A01A01A01A01",
  //   "lineName": "1#",
  //   "year": 2024,
  //   "month": 7,
  //   "yield": "6484.60"
  // },
  // {
  //   "lineCode": "A04A01A01A01A01",
  //   "lineName": "1#",
  //   "year": 2024,
  //   "month": 6,
  //   "yield": "8081.17"
  // },
  // {
  //   "lineCode": "A04A01A01A01A01",
  //   "lineName": "2#",
  //   "year": 2024,
  //   "month": 7,
  //   "yield": "4534.60"
  // },
  // {
  //   "lineCode": "A04A01A01A01A01",
  //   "lineName": "2#",
  //   "year": 2024,
  //   "month": 6,
  //   "yield": "6666.17"
  // },
  //
  // {
  //   "lineCode": "A04A01A01A01A01",
  //   "lineName": "1#",
  //   "year": 2024,
  //   "month": 9,
  //   "yield": "5467.60"
  // },
  // {
  //   "lineCode": "A04A01A01A01A01",
  //   "lineName": "1#",
  //   "year": 2024,
  //   "month": 8,
  //   "yield": "8081.17"
  // },
  // {
  //   "lineCode": "A04A01A01A01A01",
  //   "lineName": "2#",
  //   "year": 2024,
  //   "month": 9,
  //   "yield": "8888.60"
  // },
  // {
  //   "lineCode": "A04A01A01A01A01",
  //   "lineName": "2#",
  //   "year": 2024,
  //   "month": 8,
  //   "yield": "6666.17"
  // },
  //
  // {
  //   "lineCode": "A04A01A01A01A01",
  //   "lineName": "1#",
  //   "year": 2024,
  //   "month": 11,
  //   "yield": "6484.60"
  // },
  // {
  //   "lineCode": "A04A01A01A01A01",
  //   "lineName": "1#",
  //   "year": 2024,
  //   "month": 10,
  //   "yield": "8081.17"
  // },
  // {
  //   "lineCode": "A04A01A01A01A01",
  //   "lineName": "2#",
  //   "year": 2024,
  //   "month": 11,
  //   "yield": "4444.60"
  // },
  // {
  //   "lineCode": "A04A01A01A01A01",
  //   "lineName": "2#",
  //   "year": 2024,
  //   "month": 10,
  //   "yield": "123.17"
  // }
])
// 月效率
let workpieceRatio_chartData = ref([
  // {
  //   "lineCode": "A04A01A01A01A01",
  //   "lineName": "1#",
  //   "year": 2024,
  //   "month": 7,
  //   "yield": "93.38"
  // },
  // {
  //   "lineCode": "A04A01A01A01A01",
  //   "lineName": "1#",
  //   "year": 2024,
  //   "month": 6,
  //   "yield": "91.26"
  // },
  // {
  //   "lineCode": "A04A01A01A01A01",
  //   "lineName": "1#",
  //   "year": 2024,
  //   "month": 5,
  //   "yield": "86.14"
  // },
  // {
  //   "lineCode": "A04A01A01A01A01",
  //   "lineName": "1#",
  //   "year": 2024,
  //   "month": 4,
  //   "yield": "87.8"
  // },
  // {
  //   "lineCode": "A04A01A01A01A01",
  //   "lineName": "1#",
  //   "year": 2024,
  //   "month": 3,
  //   "yield": "86.36"
  // },
  // {
  //   "lineCode": "A04A01A01A01A01",
  //   "lineName": "1#",
  //   "year": 2024,
  //   "month": 2,
  //   "yield": "82.07"
  // },
  // {
  //   "lineCode": "A04A01A01A01A01",
  //   "lineName": "1#",
  //   "year": 2024,
  //   "month": 1,
  //   "yield": "83.18"
  // },
  // {
  //   "lineCode": "A04A01A01A01A02",
  //   "lineName": "2#",
  //   "year": 2024,
  //   "month": 1,
  //   "yield": "81.95"
  // },
  //
  // {
  //   "lineCode": "A04A01A01A01A01",
  //   "lineName": "2#",
  //   "year": 2024,
  //   "month": 7,
  //   "yield": "56.38"
  // },
  // {
  //   "lineCode": "A04A01A01A01A01",
  //   "lineName": "2#",
  //   "year": 2024,
  //   "month": 6,
  //   "yield": "11.26"
  // },
  // {
  //   "lineCode": "A04A01A01A01A01",
  //   "lineName": "2#",
  //   "year": 2024,
  //   "month": 5,
  //   "yield": "66.14"
  // },
  // {
  //   "lineCode": "A04A01A01A01A01",
  //   "lineName": "2#",
  //   "year": 2024,
  //   "month": 4,
  //   "yield": "37.8"
  // },
  // {
  //   "lineCode": "A04A01A01A01A01",
  //   "lineName": "2#",
  //   "year": 2024,
  //   "month": 3,
  //   "yield": "36.36"
  // },
  // {
  //   "lineCode": "A04A01A01A01A01",
  //   "lineName": "2#",
  //   "year": 2024,
  //   "month": 2,
  //   "yield": "12.07"
  // },
])
// 单耗
let energyUnitConsumption_data = ref([
  // {
  //   lineName: '1#线',
  //   date: '2024-07',
  //   yield: '7190',
  //   shui: '0.99',
  //   dian: '19.43',
  //   zhengqi: '0.097',
  // },
  // {
  //   lineName: '2#线',
  //   date: '2024-07',
  //   yield: '7190',
  //   shui: '0.99',
  //   dian: '19.43',
  //   zhengqi: '0.097',
  // },
])
// 设置定时器，每5分钟执行一次 getData 方法
const intervalId = setInterval(getData, 5 * 60 * 1000);
// 在组件销毁之前清除定时器，以防止内存泄漏
onBeforeUnmount(() => {
  clearInterval(intervalId);
});
onMounted(() => {

  getData()
})


function getImageUrl(item) {
  // 定制化http协议
  const isSecureConnection = document.location.protocol === 'https:';
  let base = isSecureConnection ? 'https://' : 'http://';
  let locationUrl = base + window.location.host + '/';
  return `${locationUrl}resource/home/${item}`;
}

async function getData() {
  const res = await REPORT_EFFICIENCY_DAPINGEFFICIENCY()
  if(res.code === 200) {
    console.log(res, 'resresres')
    finishedProductYield_chartData.value = res.result['monthYield']
    workpieceRatio_chartData.value = res.result['monthEfficiency']
    energyUnitConsumption_data.value = res.result['danHao']
  }
}
</script>

<style scoped>
.background-image {
  height: 100%;
  position: relative;
  background-size: 100% 100%;
  background-position: top; /* 背景图片居中 */
  background-repeat: no-repeat; /* 背景图片不重复 */
}
.background-image img {
  display: block; /* 让图片作为块级元素显示 */
  width: 100%; /* 图片宽度占满父容器 */
  position: absolute; /* 设置绝对定位 */
  top: 0; /* 图片顶部对齐容器顶部 */
  left: 0; /* 图片左侧对齐容器左侧 */
}

.total-title {
  position: absolute;
  left: 0;
  top: 2%;
  right: 0;
  bottom: 0;
  margin: auto;
  text-align: center;
  color: #fff;
  font-size: 38px;
  font-weight: bold;
}
</style>
