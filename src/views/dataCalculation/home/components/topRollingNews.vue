<template>
  <div class="top-roolling">
      <span style=" " class="top-name" >
        <span v-text="showName()"></span>
        <div class="triangle"></div>
      </span>
      <img src="@/assets/images/tip.png" alt="" srcset="" /> 
      <div class="news-text">
          <div class="content" v-text="rollingNews()"></div>
      </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted } from 'vue';
import { useUserStore } from '/@/store/modules/user';
const props = defineProps(['newsData']);
const userStore = useUserStore();
const cardData = ref({})
const chartRef = ref();

function rollingNews() {
  // return this.$props.newsData
  return props.newsData;
}

function showName() {
  const name = userStore?.userInfo?.realname;
  let hellowordText = '12321321';
  const h = new Date().getHours()
  // console.log('userInfo：',userInfo)
  if(h < 6){
    hellowordText = '凌晨好';
  }else if(h < 9){
    hellowordText = '早上好';
  }else if(h < 12){
    hellowordText = '上午好';
  }else if(h < 14){
    hellowordText = '中午好';
  }else if(h < 17){
    hellowordText = '下午好';
  }else if(h < 19){
    hellowordText = '傍晚好';
  }else{
    hellowordText = '晚上好';
  }
  return `${hellowordText}，${name}！`
}

onMounted(() => {
});


</script>
<style lang="less" scoped>
.top-roolling{
  background-color: white;
  .top-name{
    padding-left: 15px;
    padding-right: 15px;
    background:linear-gradient(102deg,#e59e8a 0%, #fb6339 50%, #c52826 100%);
    display: inline-block;
    height:40px;
    line-height: 40px;
    color: rgb(240, 240, 240);
    font-size: 16px;
    font-weight: bold;
    position: relative;
    border-top-left-radius: 5px; 
    border-bottom-left-radius: 5px; 
    
  }
  .triangle {
    width: 0;
    height: 0;
    border-top: 10px solid white;
    border-right: 10px solid transparent;
    border-bottom: 10px solid transparent;
    -webkit-transform: rotate(-45deg);
    transform: rotate(-45deg);
    position:  absolute;
    top:13px;
    right: -10px;
  }
  .news-text{
    display: inline;
    overflow: hidden;
    height: 3.5rem;
    line-height: 2.5rem;
    width: calc(87%);
    margin-left: 0rem;
    top: 0px;
    position: absolute;
  }

  .content {
    white-space: nowrap;
    width: max-content;
    animation: roll 70s infinite linear;
  }

  @keyframes roll {
    0% {
      transform: translateX(380px);
    }
    100% {
      transform: translateX(-100%);
    }
    
  }
}

</style>