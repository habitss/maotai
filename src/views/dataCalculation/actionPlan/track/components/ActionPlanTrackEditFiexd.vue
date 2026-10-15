<template>
  <BasicForm @register="registerForm" name="ActionPlanTrackForm" style="padding-right: 20px;  background: #fff;"></BasicForm>
</template>

<script lang="ts" setup>
import { BasicForm, useForm } from '/@/components/Form/index';
import { fiexdFormSchema } from '../ActionPlanTrack.data';
import { cloneDeep } from 'lodash-es';
//表单配置
const [registerForm, {validate, resetFields, setFieldsValue, setProps}] = useForm({
  labelWidth: 140,
  showActionButtonGroup: false,
  baseColProps: { span: 8 },
  autoSetPlaceHolder: false,
  wrapperCol: { span: 24 }
});

async function initData(isDisabled, data){
  //表单配置
  let newSchema = cloneDeep(fiexdFormSchema);
  //处理是否禁用表单
  if(isDisabled){
    for(const item of newSchema){
      item.dynamicDisabled = true;
    }
  }
  // 设置表单
  await setProps({ schemas: newSchema })
  await setFieldsValue({
    ...data,
  });
}

//暴露方法
defineExpose({
  validate,
  resetFields,
  initData,
})
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