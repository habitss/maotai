<template>
  <BasicModal 
    @register="registerModal" 
    v-bind="$attrs" 
    :destroyOnClose = true
    @ok= "copy"
    cancelText="关闭"
    title="标准复制">
    <StandardCopy ref="standardCopyRef" @ok="submitCallback" @closeSelect="closeSelect"></StandardCopy>
  </BasicModal>

</template>
<script lang="ts" setup>
import { BasicModal, useModalInner } from '/@/components/Modal';
import StandardCopy from './standardCopy.vue';
import { nextTick, ref } from 'vue';
//emit组件
const emit = defineEmits(['closeSelect','success']);
const standardCopyRef = ref();
//注册modal
const [registerModal, { closeModal }] = useModalInner(async (data: any) => {
  standardCopyRef.value.setProps(data.record)
});

function submitCallback(){
  emit('success');
  closeModal();
}
function copy(){
  standardCopyRef.value.submitForm()
}
</script>
<style scoped>
  /** 时间和数字输入框样式 */
  :deep(.ant-input-number) {
    width: 100%;
  }

  :deep(.ant-picker) {
    width: 100%;
  }

  /** 下拉框默认文字颜色 */
  :deep(.ant-select-selection-placeholder) {
    color: #BEBEBE !important;
  }
</style>