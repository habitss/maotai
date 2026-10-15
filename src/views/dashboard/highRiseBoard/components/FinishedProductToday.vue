<template>
  <div>
    <div class="FinishedProductToday-title">今日产量</div>
    <div style="text-align: center; margin: 2% 0">
      <flip-number :fill-zero="true" :number="todayYied"></flip-number>
    </div>
    <a-row class="line-ampere-hour-efficiency-container">
      <a-col :span="5"></a-col>
      <a-col :span="9" class="line-ampere-hour-efficiency">1#线</a-col>
      <a-col :span="9" class="line-ampere-hour-efficiency">2#线</a-col>
    </a-row>
    <a-row class="line-ampere-hour-efficiency-container" type="flex" align="middle">
      <a-col :span="5" class="line-ampere-hour-efficiency" style="text-align: center">产量</a-col>
      <a-col :span="9">
        <CountTo v-if="oneLineBottleNum" :color="'#eb6f2e'" :decimals="0" :duration="1500"
                 :endVal="oneLineBottleNum"
                 :startVal="1" class="line-ampere-hour-yield-number"/>
        <i v-if="oneLineBottleNum" style="color: #eb6f2e; font-size: 24px">瓶</i>
        <div v-else class="line-ampere-hour-yield-number">未生产</div>
      </a-col>
      <a-col :span="9">
        <CountTo v-if="twoLineBottleNum" :color="'#eb6f2e'" :decimals="0" :duration="1500"
                 :endVal="twoLineBottleNum"
                 :startVal="1" class="line-ampere-hour-yield-number"/>
        <i v-if="twoLineBottleNum" style="color: #eb6f2e; font-size: 24px">瓶</i>
        <div v-else class="line-ampere-hour-yield-number">未生产</div>
      </a-col>
    </a-row>
    <a-row class="line-ampere-hour-efficiency-container" type="flex" align="middle">
      <a-col :span="5" class="line-ampere-hour-efficiency" style="text-align: center">效率</a-col>
      <a-col :span="9">
        <CountTo v-if="oneLineNum" :color="'#eb6f2e'" :decimals="0" :duration="1500" :endVal="oneLineNum"
                 :startVal="1" class="line-ampere-hour-efficiency-number" suffix="%"/>
        <div class="line-ampere-hour-efficiency-number" v-else>未生产</div>
      </a-col>
      <a-col :span="9">
        <CountTo v-if="twoLineNum" :color="'#eb6f2e'" :decimals="0" :duration="1500" :endVal="twoLineNum"
                 :startVal="1" class="line-ampere-hour-efficiency-number" suffix="%"/>
        <div class="line-ampere-hour-efficiency-number" v-else>未生产</div>
      </a-col>
    </a-row>
  </div>
</template>

<script lang="ts" setup>
import {CountTo} from "@/components/CountTo";
import FlipNumber from "@/views/dashboard/highRiseBoard/components/FlipNumber.vue";
import {onBeforeUnmount, onMounted, ref} from "vue";
import {REPORT_HIGHRISE_TODAYYIELD} from "@/views/dashboard/highRiseBoard/index.api";

const oneLineNum = ref(0)
const twoLineNum = ref(0)
const oneLineBottleNum = ref(0)
const twoLineBottleNum = ref(0)
const todayYied = ref(0)
// 设置定时器，每5分钟执行一次 getData 方法
const intervalId = setInterval(() => {
  getData()
  getTodayYield()
}, 5 * 60 * 1000);
onMounted(() => {
  getData()
  getTodayYield()
})
// 在组件销毁之前清除定时器，以防止内存泄漏
onBeforeUnmount(() => {
  clearInterval(intervalId);
});

async function getTodayYield() {
  const res = await REPORT_HIGHRISE_TODAYYIELD()
  // console.log(res, 'getTodayYield')
  if (res.code === 200) {
    todayYied.value = res.result?.todayYied || 0
  }
}

function fetchData(url, body) {
  return fetch(url, body)
    .then((response) => response.json()) // 将Response对象转换为JSON格式
    .then((data) => {
      // 在这里可以对获取到的数据进行处理
      return data; // 返回处理后的数据
    }).catch(() => {
    });
}

async function getData() {
  let values = {
    tagIds: [
      '24549', //一线效率
      '24717', //二线效率
      '20426', //一线瓶数
      '25408', //二线瓶数
    ]
  }
  const res = await fetchData('http://192.168.17.123:8000/api/v1/calc-pi-clean/InfluxDb/GetLastTagInfoByTagIds', {
    method: 'POST',
    headers: {'Content-Type': 'application/json'},
    body: JSON.stringify(values),
  });
  if (res.code === 200) {
    res.data.length && res.data.forEach(i => {
      if (i.tagId === '24549') {
        oneLineNum.value = i.value
      }
      if (i.tagId === '24717') {
        twoLineNum.value = i.value
      }
      if (i.tagId === '20426') {
        oneLineBottleNum.value = i.value
      }
      if (i.tagId === '25408') {
        twoLineBottleNum.value = i.value
      }
    })
  }
}

</script>

<style scoped>
.total-title {
  text-align: center;
  color: #fff;
  font-size: 34px;
  font-weight: bold;
  margin-bottom: 5%;
}

.FinishedProductToday-title {
  text-align: center;
  color: #fff;
  font-size: 34px;
  font-weight: bold;
  margin-top: 15%;
}

.FinishedProductToday-number-container {
  .FinishedProductToday-number {
    font-size: 48px;
  }

  backdrop-filter: blur(2px) saturate(200%);
  -webkit-backdrop-filter: blur(2px) saturate(200%);
  background-color: rgba(255, 255, 255, 0.23);
  border-radius: 12px;
  border: 1px solid rgba(209, 213, 219, 0.2);
  margin: 2% 2% 5% 2%;
}

.line-ampere-hour-efficiency-container {
  .line-ampere-hour-efficiency {
    color: #fff;
    font-weight: bold;
    font-size: 34px;
  }
  .line-ampere-hour-efficiency-number {
    font-size: 62px;
    font-weight: bold;
    color: #db7640
  }
  .line-ampere-hour-yield-number {
    font-size: 42px;
    font-weight: bold;
    color: #db7640
  }
}
</style>
