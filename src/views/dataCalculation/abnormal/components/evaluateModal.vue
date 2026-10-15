<template>
  <BasicModal v-bind="$attrs" @register="registerModal" destroyOnClose :title=title :width="800" :height="200"
    :maskClosable="false" @ok="handleOk">
    <BasicForm @register="registerForm" style="margin-top: 20px; padding-right: 20px;"></BasicForm>
  </BasicModal>
</template>

<script lang="ts" setup>
import { ref } from 'vue';
import { BasicModal, useModalInner } from '/@/components/Modal';
import { useForm, BasicForm } from '/@/components/Form';
import { evaluationFormSchemas } from '../abnormal.data'
import { saveOrUpdateEvaluation, evaluationList } from '../abnormal.api'
const emit = defineEmits(['success']);
const title = ref('');
const parentData = ref();

const [registerForm, { setProps, resetFields, setFieldsValue, validate, scrollToField }] = useForm({
  //注册表单列
  schemas: evaluationFormSchemas,
  //回车提交
  autoSubmitOnEnter: true,
  showActionButtonGroup: false,
  //表格label宽度
  labelCol: { style: { width: '140px' } },
  //控制组件宽度占比
  wrapperCol: {
    xs: 24,
    sm: 24,
    md: 24,
    lg: 24,
    xl: 24,
    xxl: 24,
  },
});
//表单赋值
const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
  //重置表单
  await resetFields();
  let isUpload = false;
  //评价类型
  let evaluationType = '';
  if (data.field === 'teamEvaluation') {
    title.value = '班组评价';
    evaluationType = '1';
    isUpload = data.model.id && data.model.teamEvaluation ? true : false;
  } else if (data.field === 'workshopEvaluation') {
    title.value = '车间评价';
    evaluationType = '2';
    isUpload = data.model.id && data.model.workshopEvaluation ? true : false;
  }
  
  let formData = {
    recordId: data.model.id,
    evaluationType,
  };
  setModalProps({ confirmLoading: false, showCancelBtn: !isUpload, showOkBtn: !isUpload });
  setProps({ disabled: isUpload });
  if(isUpload){
    const res = await evaluationList({recordId: data.model.id, evaluationType});
    formData = res?.records[0];
  }

  await setFieldsValue(formData);

  parentData.value = data.model;
});

async function handleOk() {
  try {
    setModalProps({ confirmLoading: true });
    let values = await validate();
    if(parentData.value.id){
      values.recordId = parentData.value.id;
      await saveOrUpdateEvaluation(values, false);
    }
    emit('success', values);
    //关闭弹窗
    closeModal();
  } catch (error) {
    return Promise.reject(error);
  } finally {
    setModalProps({ confirmLoading: false });
  }
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