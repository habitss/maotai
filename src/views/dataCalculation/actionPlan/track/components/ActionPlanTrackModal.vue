<template>
  <BasicModal v-bind="$attrs" @register="registerModal" destroyOnClose title="行动计划录入" width="90vw" @ok="trackFormRef.handleSubmit()" okText="保存"
    :maskClosable="false">
    <ActionPlanTrackForm ref="trackFormRef" @success="onSuccess" :initRecord="false"></ActionPlanTrackForm>
  </BasicModal>
</template>

<script lang="ts" setup>
import { ref } from 'vue';
import { BasicModal, useModalInner } from '/@/components/Modal';
import ActionPlanTrackForm from '../ActionPlanTrackForm.vue'
// Emits声明
const emit = defineEmits(['register', 'success']);
//表单引用
const trackFormRef = ref();
//表单赋值
const [registerModal, { closeModal }] = useModalInner(async (data) => {
  trackFormRef.value.copyAdd(data.record);
});

function onSuccess(){
  closeModal();
  emit('success');
}
</script>

<style lang="less" scoped>
/** 时间和数字输入框样式 */
:deep(.ant-input-number) {
  width: 100%;
}

:deep(.ant-calendar-picker) {
  width: 100%;
}
</style>