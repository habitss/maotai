<template>
    <AbnormalModal @register="registerModal" @success="handleSuccess" @openScheduleModal="openScheduleModal"></AbnormalModal>
    <ScheduleModal @register="registerScheduleModal"></ScheduleModal>
  </template>
  
  <script lang="ts" name="dataCalculation-fiveWhy-measureList" setup>
  import AbnormalModal from './components/abnormalModal.vue';
  import ScheduleModal from './components/scheduleModal.vue'
  import { useModal } from '/@/components/Modal';
  const emit = defineEmits(['success']);
  //注册表单model
  const [registerModal, { openModal }] = useModal();

  const [registerScheduleModal, { openModal: openSchedule }] = useModal();
  /**
   * 打开弹窗
   */
  function openAbnormal(params) {
    params.type = '1';
    openModal(true, {
      params,
    });
  }
  
  function openScheduleModal(params) {
    openSchedule(true, {
      params,
    });
  }
  function handleSuccess(){
    emit('success');
  }
  //暴露方法
  defineExpose({
    openAbnormal,
    openScheduleModal
  })
  </script>
  
  <style lang="less" scoped>
  :deep(.ant-picker),
  :deep(.ant-input-number) {
    width: 100%;
  }
  </style>