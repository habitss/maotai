<template>
    <!-- <AbnormalModal @register="registerModal" @success="handleSuccess" @openScheduleModal="openScheduleModal"></AbnormalModal> -->
    <!--5why分析(根本原因)-->
    <RootCause @register="rootCause" @success="handleSuccess" @close="fiveWhyClose"></RootCause>
    <ActionRecord @register="actionRecord"></ActionRecord>

</template>

<script lang="ts" name="dataCalculation-fiveWhy-measureList" setup>
  import RootCause from './rootCause.vue'
  import ActionRecord from './actionRecord.vue'
  import { useModal } from '/@/components/Modal';
  const emit = defineEmits(['success']);
  //注册表单model
  const [rootCause, { openModal }] = useModal();
  const [actionRecord, { openModal:openAction}] = useModal();
  /**
   * 打开弹窗
   */
  //根本原因
  function openRootCause(params) {
    if(!params.recordType){
      params.examineStatus = 1;
    }
    
    openModal(true, {
      params,
    });
  }
  
  function openActionRecord(params) {
    openAction(true, {
      params,
    });
  }
  function handleSuccess(){
    emit('success');
  }
  function fiveWhyClose(){
    openModal(false);
    emit('success');
  }
  //暴露方法
  defineExpose({
    openRootCause,
    openActionRecord
  })
  </script>
  
  <style lang="less" scoped>
  :deep(.ant-picker),
  :deep(.ant-input-number) {
    width: 100%;
  }
  </style>