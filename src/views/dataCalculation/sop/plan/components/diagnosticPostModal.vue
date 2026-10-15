<template>
    <j-modal :title="title" width="85%" :visible="visible" @ok="handleOk" :okButtonProps="{ class: { 'jee-hidden': disableSubmit } }" @cancel="handleCancel" cancelText="关闭"
    :ok-button-props="okbuttonType" :destroyOnClose="true">
      <DiagnosticPost ref="registerForm" @ok="submitCallback" :formDisabled="disableSubmit" :formBpm="false" ></DiagnosticPost>
    </j-modal>
  </template>
  
  <script lang="ts" setup>
    import { ref, nextTick, defineExpose } from 'vue';
    import DiagnosticPost from './diagnosticPost.vue'
    import JModal from '/@/components/Modal/src/JModal/JModal.vue';
    
    const title = ref<string>('');
    //const width = ref<number>(1200);
    const visible = ref<boolean>(false);
    const disableSubmit = ref<boolean>(false);
    const okbuttonType = ref({ disabled: false });
    const registerForm = ref();
    const emit = defineEmits(['register', 'success']);
      
    /**
     * 编辑
     * @param record
     */
    function edit(record) {
      title.value = '操作性工作诊断表';
      visible.value = true;
      nextTick(() => {
        // if(record.addType === 'add'){
        //     okbuttonType.value.disabled = false;
        // }else{
        //     okbuttonType.value.disabled = true;
        // }
        okbuttonType.value.disabled = false;
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
  