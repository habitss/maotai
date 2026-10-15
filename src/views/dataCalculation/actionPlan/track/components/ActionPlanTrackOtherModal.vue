<template>
  <BasicModal v-bind="$attrs" @register="registerModal" destroyOnClose title="行动计划" width="90vw"
    @cancel="onSuccess" :showCancelBtn=false :showOkBtn=false>
    <ActionPlanTrackList ref="trackListmRef" @success="onSuccess" :raId="params?.id" :dataId="params?.dataId" :showSearch="false"></ActionPlanTrackList>
  </BasicModal>
</template>

<script lang="ts" setup>
import { ref } from 'vue';
import { BasicModal, useModalInner } from '/@/components/Modal';
import ActionPlanTrackList from '../ActionPlanTrackList.vue'
// Emits声明
const emit = defineEmits(['register', 'success']);
//查询参数
const params = ref({} as any);
//行动计划ref引用
const trackListmRef = ref();
//表单赋值
const [registerModal, { closeModal }] = useModalInner(async (data) => {
  params.value = data.params;
});

function onSuccess() {
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