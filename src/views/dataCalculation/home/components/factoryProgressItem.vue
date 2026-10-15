<template>
  <div class='factory-progress'>
    <div class='progress-item'>
      <!--      style='margin-bottom: 5px;'-->
      <div>
        <span class='card-title'>{{ initData.title }}</span>
      </div>
      <div>
        <!-- <span class='card-title font-right'> -->
        <span class='card-title '>
          <span v-text='initData.titleRateText'></span>
          <span v-if="(parseFloat(initData.titleRate) || initData.titleRate == '0') && initData.showTitleRate" :class='getColor(initData.titleRate)' style='margin-left: 15px;'
            v-text='`${(Number(initData.titleRate) * 100).toFixed(2)}%`'></span>
          <span v-if="(parseFloat(initData.titleRate) || initData.titleRate == '0') && initData.showTitle" :class='getColor(initData.titleRate)' style='margin-left: 15px;'
          v-text='`${(Number(initData.titleRate)).toFixed(2)}%`'></span>
          <span style="display: inline-block;height: 28px" v-else></span>
        </span>
      </div>
      <!--      style='margin-bottom: 5px;'-->
      <div style='padding: 6px 0'>
        <span class='sub-title' v-text='initData.subTitle'></span>
        <div class='item-panel' v-if='initData.list.length > 0'>
          <div v-for='(item, index) in initData.list' v-bind:key='index' style='padding: 4px 0;'>
            <div style='width: 120px;float: left;'>
              <img v-if='index == 0' src='@/assets/images/rank/first.png' class='icon' alt=""/>
              <img v-if='index == 1' src='@/assets/images/rank/second.png' class='icon' alt=""/>
              <img v-if='index == 2' src='@/assets/images/rank/third.png' class='icon' alt=""/>
              <img v-if='index == 3' src='@/assets/images/rank/fourth.png' class='icon' alt=""/>
              <!-- <span v-if='index > 3' v-text='index + 1' class='sort font-bold font-16'></span> -->
              <span v-if='index > 3' v-text='index + 1' class='sort font-bold font-12'></span>
              <!-- <span style='width: 75px;display: inline;' v-text='item.name.slice(0, 4)'></span> -->
              <span style='width: 75px;display: inline;' v-text='item.name?.slice(0, 5)'></span>
            </div>
            <div style='margin-left: 122px'>
              <a-progress :percent='item.lv ? Math.floor(Number(item.lv.toFixed(2)) * 100) : 0'
                :stroke-color='getStrokeColor(item.lv * 100)'></a-progress>
            </div>
          </div>
        </div>
        <a-empty v-if='initData.list.length < 1'></a-empty>
      </div>
      <slot name='content'></slot>
    </div>
    <!--    </a-card>-->
  </div>
</template>

<script lang="ts" setup>
  import { ref,defineProps,onMounted, nextTick } from 'vue';
  const props = defineProps(['initData']);
  const initData = ref({
    list:[],
    title:''
  });


  function getStrokeColor(value) {
    if (value < 80) {
      return {
        '0%': '#F55855',
        '100%': '#FF9391' // 红色
      };
    } else {
      return {
        '0%': '#31EE04', //亮绿色
        '100%': '#4AFB21'
      };
    }
  }

  function getColor(value) {
    return value * 100 > 80 ? 'green' : 'red';
  }

  onMounted(() => {
    nextTick(() => { 
      initData.value = props.initData;
    })
  });
</script>
<style lang='less' scoped>

.factory-progress {
  .card-title {
    font-weight: 600;
    font-size: 13px;
  }

  .sub-title {
    //font-weight: bold;
    font-size: 16px;
  }

  .font-bold {
    color: #666666;
  }

  .font-12 {
    font-size: 12px;
  }

  .font-14 {
    font-size: 14px;
  }

  .font-right {
    float: right;
  }

  .icon {
    width: 20px;
    margin: 0 10px;
  }

  .sort {
    margin-right: 10px;
    margin-left: 9px;
    display: inline-block;
    width: 18px;
    height: 18px;
    line-height: 30px;
    border-radius: 30px;
    text-align: center;
    background-color: rgba(238, 238, 238, 0.493);
    color: rgba(158, 156, 154, 0.925);
  }

  .red {
    color: red;
  }

  .green {
    color: #28D100;
  }

  .blue {
    color: blue;
  }

  .time-item {
    float: right;
    width: 115px;
  }

  .time {
    height: 28px;
    line-height: 28px;

    ::v-deep input {
      height: 100%;
    }
  }

  .progress-item {
    overflow: hidden;
    //height: 247px;
    //overflow-y: auto;
    //margin: 0 0 20px 0
  }

  // .ant-card-bordered{
  //   border: 0px solid gray;
  // }

  .item-panel {
    height: 160px;
    overflow: hidden;
    overflow-y: auto;
  }
}


</style>
