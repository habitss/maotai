<template>
  <BasicModal v-bind="$attrs" @register="registerModal" destroyOnClose :title="title" :width="800" :min-height="400" :bodyStyle="{height: '70vh'}" @ok="handleSubmit">
    <BasicForm @register="registerForm" name="MdmItemForm">
      <template #selectOption="{model}">
        <MdmItemSelectOption :key="model.id" :baseInfo="{
            deptCode : model.deptCode,
            factoryCode : model.factoryCode,
            lineCode : model.lineCode,
            itemId : model.id
        }"></MdmItemSelectOption>
      </template>
    </BasicForm>
  </BasicModal>
</template>

<script lang="ts" setup>
import { ref, unref } from 'vue';
import { BasicModal, useModalInner } from '/@/components/Modal';
import { BasicForm, useForm } from '/@/components/Form/index';
import { formSchema } from '../MdmItem.data';
import { saveOrUpdate } from '../MdmItem.api';
import MdmItemSelectOption from './MdmItemSelectOption.vue';
// Emits声明
const emit = defineEmits(['register', 'success']);
const isUpdate = ref(true);
const isDetail = ref(false);
//表单配置
const [registerForm, { setProps, resetFields, setFieldsValue, validate, scrollToField }] = useForm({
  labelWidth: 160,
  schemas: formSchema,
  showActionButtonGroup: false,
  baseColProps: { span: 24 }
});
//表单赋值
const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
  //重置表单
  await resetFields();
  setModalProps({ confirmLoading: false, showCancelBtn: !!data?.showFooter, showOkBtn: !!data?.showFooter });
  isUpdate.value = !!data?.isUpdate;
  isDetail.value = !!data?.showFooter;
  isDetail.value = data?.isCopy;
  if (unref(isUpdate) || data.isCopy) {
    data.record.deptDeviceEdit = unref(isUpdate) && !data.isCopy ? true : false;
    //表单赋值
    await setFieldsValue({
      ...data.record,
    });
  } else {
    await setFieldsValue({
      checkItemType: data.checkItemType,
    });
  }
  if(data.isCopy){
    title.value = '复制';
    isUpdate.value = false;
  } else if(unref(isUpdate)){
    title.value = '编辑';
  } else {
    title.value = '新增';
  }
  // 隐藏底部时禁用整个表单
  setProps({ disabled: !data?.showFooter })
});
//设置标题
const title = ref('');
//表单提交事件
async function handleSubmit(v) {
  try {
    let values = await validate();
    values.factoryCode = values.deptCode.substr(0,9);
    setModalProps({ confirmLoading: true });
    //提交表单
    await saveOrUpdate(values, isUpdate.value);
    //关闭弹窗
    closeModal();
    //刷新列表
    emit('success');
  } catch ({ errorFields }) {
    if (errorFields) {
      const firstField = errorFields[0];
      if (firstField) {
        scrollToField(firstField.name, { behavior: 'smooth', block: 'center' });
      }
    }
    return Promise.reject(errorFields);
  } finally {
    setModalProps({ confirmLoading: false });
  }
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