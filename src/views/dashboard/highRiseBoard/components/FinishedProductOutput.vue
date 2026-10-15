<template>
  <div style="height: 20%">
    <h1 class="title">年累总产量</h1>
    <div class="countTo-container">
      <CountTo class="total total-bottle" :color="'#eb6f2e'" :startVal="1" :endVal="data.bottle" :decimals="0" :duration="1500" />
      <i style="color: #eb6f2e">瓶</i>
    </div>
    <div class="countTo-container" style="display: flex; justify-content: flex-end;">
      <CountTo class="total total-ton" style="border-bottom: 4px solid #eb6f2e" :color="'#eb6f2e'" :startVal="1" :endVal="data.ton" :decimals="0" :duration="1500" />
      <i>吨</i>
    </div>
    <!--<div style="display: flex; justify-content: space-between; padding: 0 10%">-->
    <!--  <div class="countTo-container">-->
    <!--    <CountTo class="total total-ton" :color="'#eb6f2e'" :startVal="1" :endVal="data.bottle" :decimals="0" :duration="1500" />-->
    <!--    <i>瓶</i>-->
    <!--  </div>-->
    <!--  <div class="countTo-container">-->
    <!--    <CountTo class="total total-box" :color="'#eb6f2e'" :startVal="1" :endVal="data.ton" :decimals="0" :duration="1500" />-->
    <!--    <i>吨</i>-->
    <!--  </div>-->
    <!--</div>-->
  </div>
</template>

<script setup lang="ts">
import {formatNumber} from "@/utils";
import {CountTo} from "@/components/CountTo";
import {onBeforeUnmount, onMounted, ref, unref} from "vue";
import {REPORT_HIGHRISE_YEARYIELD} from "@/views/dashboard/highRiseBoard/index.api";
import dayjs from "dayjs";

const data = ref({
  ton: 0,
  bottle: 0
})
// 设置定时器，每5分钟执行一次 getData 方法
const intervalId = setInterval(getData, 5 * 60 * 1000);
// 在组件销毁之前清除定时器，以防止内存泄漏
onBeforeUnmount(() => {
  clearInterval(intervalId);
});


onMounted(() => {
  getData()
})

async function getData() {
  let dic = {
    startTime: dayjs().startOf('year').format('YYYY-MM-DD'),
    endTime: dayjs().endOf('year').format('YYYY-MM-DD')
  }
  const res = await REPORT_HIGHRISE_YEARYIELD(dic)
  if(res.code === 200) {
    data.value = res.result
    console.log(unref(data), 'data')
  }
}
</script>

<style scoped>
.title {
  text-align: center;
  font-size: 18px;
  font-weight: bold;
  color: #fff;
}
.countTo-container {
  display: flex;
  align-items: flex-end;
  justify-content: center;
  i {
    font-size: 18px;
    color: #eb6f2e
  }
}
.total {
  color: #eb6f2e;
  display: flex;
  font-weight: bold;
}
.total-bottle {
  text-align: center;
  font-size: 42px;
}
.total-ton {
  font-size: 36px;
  font-style: italic;
}
.total-box {
  font-size: 30px;
  font-style: italic;
}
</style>
