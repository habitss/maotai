<template>
  <BasicModal v-bind="$attrs" @register="registerModal" destroyOnClose title="复制重复异常配置" :width="800" @ok="handleSubmit">
    <PiRepeatExceptionTypeList ref="listRef" v-if="recordData && recordData.tableId" :recordData="recordData" :openType="'copy'"/>
  </BasicModal>
</template>

<script lang="ts" setup>
import { ref } from 'vue';
import { BasicModal, useModalInner } from '/@/components/Modal';
import PiRepeatExceptionTypeList from '../PiRepeatExceptionTypeList.vue';
import { addCopy } from '../PiRepeatExceptionType.api';
// Emits声明
const emit = defineEmits(['register', 'success']);
const listRef = ref();
const recordData = ref<any>({});
//表单赋值
const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
  recordData.value = data.record;
});

async function handleSubmit(){
  try {
    setModalProps({ confirmLoading: true });
    //提交表单
    await addCopy({
      piTableId: recordData.value.tableId,
      piTableCode: recordData.value.tableName,
      piFieldId: recordData.value.id,
      piFieldCode: recordData.value.fieldName,
      copyList: listRef.value.getDataSource(),
    });
    //关闭弹窗
    closeModal();
    //刷新列表
    emit('success');
  } catch ({ errorFields }) {
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