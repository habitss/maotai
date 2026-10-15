<template>
  <BasicModal v-bind="$attrs" @register="registerModal" destroyOnClose title="5WHY分析" width="80vw" :height="600" :maskClosable="false">
    <FiveWhyList ref="fiveWhyRef" @close="handleClose"></FiveWhyList>
  </BasicModal>
</template>

<script lang="ts" setup>
import { ref } from 'vue';
import { BasicModal, useModalInner } from '/@/components/Modal';
import FiveWhyList from './fiveWhyList.vue';
const emit = defineEmits(['success']);
const fiveWhyRef = ref();
//表单赋值
const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
  setModalProps({ confirmLoading: false, showCancelBtn: false, showOkBtn: false });
  fiveWhyRef.value.init(data.params);
});

function handleClose(){
  closeModal();
  emit('success');
}
</script>