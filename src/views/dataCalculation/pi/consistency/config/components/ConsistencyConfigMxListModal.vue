<template>
  <BasicModal v-bind="$attrs" @register="registerModal" destroyOnClose title="pi配置" width="80vw" :showCancelBtn="false"
    :showOkBtn=false>
    <ConsistencyConfigMxList ref="mxListRef" />
  </BasicModal>
</template>

<script lang="ts" setup>
import { ref } from 'vue';
import { BasicModal, useModalInner } from '/@/components/Modal';
import ConsistencyConfigMxList from '../detail/ConsistencyConfigMxList.vue';
// Emits声明
const emit = defineEmits(['register', 'success']);
const mxListRef = ref();
//表单赋值
const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
  mxListRef.value.reloadList(data.record);
});
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