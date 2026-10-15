<template>
  <BasicModal v-bind="$attrs" @register="registerModal" destroyOnClose :title="title" :width="800" @ok="handleSubmit">
    <BasicForm @register="registerForm" name="PiDfTableFieldMappingForm" />
  </BasicModal>
</template>

<script lang="ts" setup>
import { ref } from 'vue';
import { BasicModal, useModalInner } from '/@/components/Modal';
import { BasicForm, useForm } from '/@/components/Form/index';
import { mappingFormSchema } from '../PiDfTableFieldMapping.data';
import { queryMappingData, saveOrUpdate } from '../PiDfTableFieldMapping.api';
// Emits声明
const emit = defineEmits(['register', 'success']);
const isUpdate = ref(true);
//设置标题
const title = ref("DF字段映射");
//表单配置
const [registerForm, { setProps, resetFields, setFieldsValue, validate, scrollToField }] = useForm({
  labelWidth: 150,
  schemas: mappingFormSchema,
  showActionButtonGroup: false,
  baseColProps: { span: 24 }
});
//表单赋值
const [registerModal, { setModalProps, closeModal, }] = useModalInner(async (data) => {
  setModalProps({ confirmLoading: true });
  try{
    title.value = `DF字段映射（${data.record.name}）`;
    //重置表单
    await resetFields();
    //获取映射数据
    const res = await queryMappingData({ 
      tableId: data.record.tableId,
      fieldId: data.record.id,
    });
    let mappingData = res ?? {};
    if(!mappingData.id){
      isUpdate.value = false
      mappingData.factoryCode = data.record.factoryCode;
      mappingData.deptCode = data.record.deptCode;
      mappingData.lineCode = data.record.lineCode;
      mappingData.tableId = data.record.tableId;
      mappingData.tableName = data.record.tableName;
      mappingData.fieldId = data.record.id;
      mappingData.fieldName = data.record.fieldName;
    } else {
      isUpdate.value = true
      mappingData.dfTableNameSelect = mappingData.dfTableId;
      mappingData.dfFieldNameSelect = mappingData.dfFieldName;
    }
    await setFieldsValue({
      ...mappingData
    });
  } catch (error) {
    console.log('error :>> ', error);
  } finally {
    setModalProps({ confirmLoading: false });
  }
});
//表单提交事件
async function handleSubmit(v) {
  try {
    let values = await validate();
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