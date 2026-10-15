<template>
  <BasicModal
    @register="registerModal"
    title="检查项配置"
    width="800"
    v-bind="$attrs"
    destroyOnClose
    :maskClosable="false"
  >

      <ConfigListModel ref="configListModal" @close="handleClose" :record="record" />
    </BasicModal>
</template>

<script lang="ts" setup>
import { ref } from 'vue';
import { BasicModal, useModalInner } from '/@/components/Modal';
import { useI18n } from '/@/hooks/web/useI18n';
import ConfigListModel
  from "@/views/dataCalculation/periodCheck/periodConfig/components/ConfigListModel.vue";
const configListModal = ref();
const { t } = useI18n();
const record = ref();
const emit = defineEmits(['success']);
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
