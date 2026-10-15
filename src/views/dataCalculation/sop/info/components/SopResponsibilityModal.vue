<template>
  <j-modal :title="title" width="70%" :visible="visible"  :okButtonProps="{ style: { display: 'none' } }" 
  @cancel="handleCancel" cancelText="关闭" :destroyOnClose="true">
    <SopResponsibility ref="registerForm" @ok="submitCallback" :formDisabled="disableSubmit" :formBpm="false"></SopResponsibility>
  </j-modal>
</template>

<script lang="ts" setup>
  import { ref, nextTick, defineExpose } from 'vue';
  import SopResponsibility from './SopResponsibility.vue'
  import JModal from '/@/components/Modal/src/JModal/JModal.vue';
  
  const title = ref<string>('');
  // const width = ref<number>(800);
  const visible = ref<boolean>(false);
  const disableSubmit = ref<boolean>(false);
    const okbuttonType = ref({ disabled: true });
  const registerForm = ref();
  const emit = defineEmits(['register', 'success']);

  /**
   * 编辑
   * @param record
   */
  function edit(record) {
    title.value = '编写职责';
    visible.value = true;
    nextTick(() => {
      registerForm.value.edit(record);
    });
  }
  
  /**
   * 确定按钮点击事件
   */
  function handleOk() {
    registerForm.value.submitForm();
  }

  /**
   * form保存回调事件
   */
  function submitCallback() {
    handleCancel();
    emit('success');
  }

  /**
   * 取消按钮回调事件
   */
  function handleCancel() {
    visible.value = false;
  }

  defineExpose({
    edit,
    disableSubmit,
  });
</script>

<style lang="less">
  /**隐藏样式-modal确定按钮 */
  .jee-hidden {
    display: none !important;
  }
</style>
<style lang="less" scoped></style>
