<template>
  <BasicModal 
    @register="registerModal" 
    v-bind="$attrs" 
    :destroy-on-close = true
    :min-height="600"
    cancelText="关闭"
    :show-ok-btn="false"
    title="标准维护">
    <MaintenanceList ref="maintenanceRef" :manageData="manageData" ></MaintenanceList>
  </BasicModal>
</template>
<script lang="ts" setup>
import { BasicModal, useModalInner } from '/@/components/Modal';
import MaintenanceList from '../../maintenance/standardMaintenanceList.vue';
import { ref } from 'vue';
//标准管理数据
const manageData = ref();
const maintenanceRef = ref();
//注册modal
const [registerModal, { closeModal }] = useModalInner(async (data: any) => {
  manageData.value = data.record;
  maintenanceRef.value.initData(data);
});
</script>
<style scoped>
  /** 时间和数字输入框样式 */
  :deep(.ant-input-number) {
    width: 100%;
  }

  :deep(.ant-picker) {
    width: 100%;
  }

  /** 下拉框默认文字颜色 */
  :deep(.ant-select-selection-placeholder) {
    color: #BEBEBE !important;
  }
</style>