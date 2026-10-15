<template>
    <BasicModal
        @register="registerModal"
        title="配置核查方式"
        width="800"
        v-bind="$attrs"
        destroyOnClose
        :maskClosable="false"
    >
      <ConfigListModel ref="configListModal" @close="handleClose" :record="record" />
    </BasicModal>
  </template>

  <script lang="ts" setup>
    import {ref} from 'vue';
    import { BasicModal, useModalInner } from '/@/components/Modal';
    import ConfigListModel
      from "@/views/dataCalculation/periodCheck/periodConfig/components/ConfigListModel.vue";
    const emit = defineEmits(['success']);
    const configListModal = ref();

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
