<template>
    <BasicModal
        @register="registerModal"
        title="配置COP清洗点"
        width="800"
        v-bind="$attrs"
        destroyOnClose
        :maskClosable="false"
    >
      <copPointList ref="copPointForm" @close="handleClose" :record="record" />
    </BasicModal>
  </template>

  <script lang="ts" setup>
    import {ref} from 'vue';
    import { BasicModal, useModalInner } from '/@/components/Modal';
    import copPointList from '../copPoint/copPointList.vue'
    const emit = defineEmits(['success']);
    const copPointForm = ref();

    // 设备名称和id
    const record = ref();

    //注册modal
    const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data: any) => {
      record.value = data.record
      setModalProps({ confirmLoading: false, showCancelBtn: false, showOkBtn: false });
    });


    function handleClose(){
      closeModal();
      emit('success');
    }

  </script>

  <style>
    /**隐藏样式-modal确定按钮 */
    .jee-hidden {
        display: none !important;
    }
  </style>
