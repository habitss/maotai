<template>
  <div style="height: 20%">
    <h1 class="total-title">当月能源单耗</h1>
    <a-row :gutter="16">
      <a-col :span="8">
        <div class="box">
          <div class="title">水单耗</div>
          <p>
            <span style="color: #fff">1#线 </span>
            <span :style="{color: getColor(data[0]?.shui, 'shui')}"> {{ data[0]?.shui }} 立方/t</span>
          </p>
          <p>
            <span style="color: #fff">2#线 </span>
            <span :style="{color: getColor(data[1]?.shui, 'shui')}"> {{ data[1]?.shui || '未生产' }}</span>
          </p>
        </div>
      </a-col>
      <a-col :span="8">
        <div class="box">
          <div class="title">蒸汽单耗</div>
          <p>
            <span style="color: #fff">1#线 </span>
            <span :style="{color: getColor(data[0]?.zhengqi, 'zhengqi')}"> {{ data[0]?.zhengqi }} t/t</span>
          </p>
          <p>
            <span style="color: #fff">2#线 </span>
            <span :style="{color: getColor(data[1]?.zhengqi, 'zhengqi')}"> {{ data[1]?.zhengqi || '未生产' }}</span>
          </p>
        </div>
      </a-col>
      <a-col :span="8">
        <div class="box">
          <div class="title">电单耗</div>
          <p>
            <span style="color: #fff">1#线 </span>
            <span :style="{color: getColor(data[0]?.dian, 'dian')}"> {{ data[0]?.dian }} kw.h/t</span>
          </p>
          <p>
            <span style="color: #fff">2#线 </span>
            <span :style="{color: getColor(data[1]?.dian, 'dian')}"> {{ data[1]?.dian || '未生产' }}</span>
          </p>
        </div>
      </a-col>
    </a-row>
  </div>
</template>

<script setup lang="ts">
const props = defineProps({
  data: {
    type: Object,
    default: () => {}
  }
})

function getColor(value, type) {
  let color;
  if(type === 'shui') {
    color = value < 1.5 ? 'green' : 'red'
  } else if(type === 'dian') {
    color = value < 24.92 ? 'green' : 'red'
  } else {
    color = value < 0.19 ? 'green' : 'red'
  }
  return color
}
</script>

<style scoped>
.total-title {
  text-align: center;
  font-size: 24px;
  font-weight: bold;
  color: #fff;
}

.box {
  border: 1px solid #eb6f2e;
  border-radius: 5px;
  text-align: center;
  padding: 10px 0;
  box-shadow: 5px 5px 10px 0 #0d1d7d;
  .title {
    color: #eb6f2e;
    font-size: 1rem;
  }
  p {
    color: #fff;
    margin: 0;
    font-size: 1rem;
  }
}
</style>
