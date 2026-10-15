<template>
  <BasicModal v-bind="$attrs" @register="registerModal" destroyOnClose title="行动计划" width="90vw"
    @cancel="onSuccess" :showCancelBtn=false :showOkBtn=false>
    <ActionPlanTrackForm v-if="type === 1" ref="trackFormRef" @success="onSuccess" :init-record="false"></ActionPlanTrackForm>
    <ActionPlanTrackList v-else-if="type === 2" ref="trackListmRef" @success="onSuccess" :raId="record?.id" :dataId="record?.dataId" :showSearch="false"></ActionPlanTrackList>
  </BasicModal>
</template>

<script lang="ts" setup>
import { nextTick, ref } from 'vue';
import { BasicModal, useModalInner } from '/@/components/Modal';
import ActionPlanTrackForm from '@/views/dataCalculation/actionPlan/track/ActionPlanTrackForm.vue'
import ActionPlanTrackList from '@/views/dataCalculation/actionPlan/track/ActionPlanTrackList.vue'
// Emits声明
const emit = defineEmits(['register', 'success']);
//维护类型
const type = ref(1);
//维护类型
const record = ref({} as any);
//简单维护引用
const trackFormRef = ref();
//完整维护引用
const trackListmRef = ref();
//表单赋值
const [registerModal, { closeModal }] = useModalInner(async (data) => {
  record.value = data.record;
  type.value = data.type;
  if (data.type === 1) {
    nextTick(() => {
      record.value.id = 'temp_1';
      trackFormRef.value.copyAdd(record.value, true, data.dataId);
    });
  }
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