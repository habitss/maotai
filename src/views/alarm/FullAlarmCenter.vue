<template>
  <a-button style="margin: 10px" type="primary" @click="visible = true">进入报警中心大屏</a-button>
  <a-modal
    v-model:visible="visible"
    :title="null"
    :footer="null"
    :width="'100vw'"
    :wrap-style="{
      position: 'fixed',
      top: 0,
      left: 0,
      right: 0,
      bottom: 0,
      overflow: 'hidden',
    }"
    :style="{
      position: 'absolute',
      top: 0,
      left: 0,
      right: 0,
      bottom: 0,
      margin: 0,
      padding: 0,
      maxWidth: '100vw',
      height: '100vh',
    }"
    :body-style="{
      height: '100vh',
      padding: 0,
      overflow: 'auto',
    }"
    :mask-style="{
      backgroundColor: 'rgba(0, 0, 0, 0.45)',
      position: 'absolute',
      top: 0,
      left: 0,
      right: 0,
      bottom: 0,
    }"
    :closable="false"
    :maskClosable="false"
    :destroyOnClose="true"
    @after-close="afterClose"
  >
    <AlarmCenter ref="refAlarmCenter" />
  </a-modal>
</template>

<script lang="ts" name="FullAlarmCenter" setup>
  import { ref } from 'vue';
  import AlarmCenter from './AlarmCenter.vue';

  const visible = ref(true);

  const afterClose = () => {
    visible.value = false;
  };
</script>
<style scoped>
  /* 使用深度选择器覆盖 antd 样式 */
  :deep(.ant-modal) {
    max-width: 100vw !important;
    top: 0 !important;
  }

  :deep(.ant-modal-content) {
    height: 100vh !important;
    border-radius: 0 !important;
  }

  :deep(.ant-modal-body) {
    height: 100vh !important;
    overflow: auto;
    padding: 0 !important;
  }
</style>
