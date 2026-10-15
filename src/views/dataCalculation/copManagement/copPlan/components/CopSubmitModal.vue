<template>
  <BasicModal @register="registerModal" v-bind="$attrs" :cancelText="t('language.closeText')" :destroyOnClose="true">
    <CopPlanForm ref="copScheduleForm" @close="handleClose" :record="scheduleRecord" />
  </BasicModal>
</template>
<script lang="ts" setup>
import { ref } from 'vue';
import { BasicModal, useModalInner } from '/@/components/Modal';
import { useI18n } from '/@/hooks/web/useI18n';
import CopPlanForm from '/@/views/dataCalculation/copManagement/copPlan/components/CopPlanForm.vue'

const copScheduleForm = ref();
const { t } = useI18n();
const scheduleRecord = ref();
const emit = defineEmits(['close']);
//注册modal
const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data: any) => {
  scheduleRecord.value = data.record
  copScheduleForm.value.receiveRecord(data.record);
  setModalProps({ confirmLoading: false, showCancelBtn: false, showOkBtn: false });
});


function handleClose(){
  closeModal();
  emit('close');
}
</script>
<style>
/**隐藏样式-modal确定按钮 */
.jee-hidden {
  display: none !important;
}
</style>
