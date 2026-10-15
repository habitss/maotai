<template>
  <BasicModal
    @register="registerModal"
    title="清洗点信息配置"
    :width="1000"
    v-bind="$attrs"
    destroyOnClose
    :maskClosable="false"
  >
    <CopPointConfigList ref="copPointConfig" @close="handleClose" :record="record" />
  </BasicModal>
</template>

<script lang="ts" name="dataCalculation-cop-point-config" setup>
import {ref} from "vue";
import CopPointConfigList from '../copPoint/copPointConfigList.vue';
import { BasicModal, useModalInner } from '/@/components/Modal';
const emit = defineEmits(['success']);
const copPointConfig = ref();

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
