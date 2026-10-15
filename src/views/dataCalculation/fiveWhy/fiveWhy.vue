<template>
  <FiveWhyModal @register="registerModal" @success="handleSuccess"></FiveWhyModal>
</template>

<script lang="ts" name="dataCalculation-fiveWhy-measureList" setup>
import FiveWhyModal from './components/FiveWhyModal.vue';
import { useModal } from '/@/components/Modal';
const emit = defineEmits(['success']);
//注册表单model
const [registerModal, { openModal }] = useModal();
/**
 * 打开弹窗
 */
function openFiveWhy(params) {
  openModal(true, {
    params,
  });
}

function handleSuccess(){
  emit('success');
}
//暴露方法
defineExpose({
  openFiveWhy
})
</script>

<style lang="less" scoped>
:deep(.ant-picker),
:deep(.ant-input-number) {
  width: 100%;
}
</style>