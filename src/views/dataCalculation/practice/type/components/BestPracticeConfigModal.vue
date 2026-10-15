<template>
  <BasicModal v-bind="$attrs" @register="registerModal" destroyOnClose :title="title" :width="800" @ok="handleSubmit">
    <BestPracticeConfigList :typeId></BestPracticeConfigList>
  </BasicModal>
</template>

<script lang="ts" setup>
import { ref } from 'vue';
import { BasicModal, useModalInner } from '/@/components/Modal';
import BestPracticeConfigList from '/@/views/dataCalculation/practice/config/BestPracticeConfigList.vue'
// Emits声明
const emit = defineEmits(['register', 'success']);
//标题
const title = ref('');
const typeId = ref('');
//表单赋值
const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
  console.log('data :>> ', data);
  title.value = `最佳实践配置(${data?.record?.typeName})`;
  typeId.value = data?.record?.id;
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