<template>
    <OpenPointDiagramModel @register="registerModal" @success="handleSuccess"></OpenPointDiagramModel>
    <OpenBoxDiagramModel @register="registerBoxModal" @success="handleSuccess"></OpenBoxDiagramModel>
  </template>
  
  <script lang="ts" name="dataCalculation-fiveWhy-measureList" setup>
  //点图
  import OpenPointDiagramModel from './OpenPointDiagramModel.vue';
  import OpenBoxDiagramModel from './OpenBoxDiagramModel.vue';
  import { useModal } from '/@/components/Modal';
  const emit = defineEmits(['success']);
  //注册点图model
  const [registerModal, { openModal }] = useModal();
  //注册箱图model
  const [registerBoxModal, { openModal: openBoxDiagramModal }] = useModal();
  /**
   * 打开点图弹窗
   */
  function openPointDiagram(params) {
    openModal(true, {
      params,
    });
  }
  /**
   * 打开箱图弹窗
   */
  function openBoxDiagram(params) {
    openBoxDiagramModal(true, {
      params,
    });
  }
  function handleSuccess(){
    emit('success');
  }
  //暴露方法
  defineExpose({
    openPointDiagram,
    openBoxDiagram,
  })
  </script>
  
  <style lang="less" scoped>
  :deep(.ant-picker),
  :deep(.ant-input-number) {
    width: 100%;
  }
  </style>