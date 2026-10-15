<template>
  <div class='card'>
    <a-card :title='card.title' :head-style='{borderBottom:0}' :body-style='{paddingTop:0,height:"200px"}' :border='false'>
      <template #extra><slot name="extra"></slot></template>
      <slot name='content'></slot>
    </a-card>
  </div>
</template>

<script lang="ts" setup>
import { ref,defineProps,onMounted,nextTick } from 'vue';
const props = defineProps(['card']);
const card = ref({
  title: ''
})

onMounted(() => {
  setTimeout(() => {
    card.value = props.card
  }, 500);
  // nextTick(() => { 
  //   card.value = props.card
  // })
});


</script>
<style lang='less' scoped>

.card {
  .sub-title {
    font-weight: 600;
    font-size: 14px;
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
    :deep(input) {
      height: 100%;
    }
  }
}

:deep(.factory-progress .card-title) {
    font-weight: 600;
    font-size: 13px
}
</style>
