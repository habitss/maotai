<template>
  <BasicModal v-bind="$attrs" @register="registerModal" destroyOnClose :title="title" :width="800" :min-height="480"
    @ok="handleSubmit">
    <BasicForm @register="registerForm" name="ConsistencyConfigMxForm"></BasicForm>
  </BasicModal>
</template>

<script lang="ts" setup>
import { ref, computed, unref } from 'vue';
import { BasicModal, useModalInner } from '/@/components/Modal';
import { BasicForm, useForm } from '/@/components/Form/index';
import { formSchema } from '../ConsistencyConfigMx.data';
import { saveOrUpdate, list as mxList } from '../ConsistencyConfigMx.api';
import { list } from '../../ConsistencyConfigZb.api';
// Emits声明
const emit = defineEmits(['register', 'success']);
const isUpdate = ref(true);
const isDetail = ref(false);
//表单配置
const [registerForm, { setProps, resetFields, setFieldsValue, validate, scrollToField, updateSchema }] = useForm({
  labelWidth: 150,
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
  setPiCodeList(data.zbRecord, data.record?.piCode);
  //表单赋值
  await setFieldsValue({
    zbId: data.zbRecord.id,
    consistencyType: data.zbRecord.consistencyType,
    piType: !data?.record?.piCode ? '1' : '2',
  });
  if (unref(isUpdate)) {
    //表单赋值
    await setFieldsValue({
      ...data.record,
    });
  }
  // 隐藏底部时禁用整个表单
  setProps({ disabled: !data?.showFooter })
});

/**
* pi编码下拉数据
*/
async function setPiCodeList(zbData, piCode) {
  //获取所有数据
  const { records } = await list({ pageSize: 999, consistencyType: 2 });
  //获取已选择的明细数据
  const { records: mxRecords } = await mxList({ pageSize: 999, zbId: zbData.id });
  //排除已选择过的数据和主表
  let newArr = [] as any;
  for(const item of records){
    //编辑时的piCode不排除，用于显示
    if(piCode === item.id){
      newArr.push(item);
    }else if(item.id !== zbData.id){
      let flag = true;
      //排除非主表id和已选择数据
      for(const mxItem of mxRecords){
        if(mxItem.piCode == item.id){
          flag = false;
        }
      }
      if(flag) newArr.push(item);
    }
  }
  updateSchema({
    field: 'piCode',
    componentProps: ({ schema, tableAction, formActionType, formModel }) => {
      return {
        options: newArr,
        fieldNames: { label: 'consistencyName', value: 'id' },
        onChange: function(value, option){
          formModel.piName = option?.consistencyName;
          formModel.piCode = option?.id;
        }
      };
    },
  });
}

//设置标题
const title = computed(() => (!unref(isUpdate) ? '新增' : !unref(isDetail) ? '详情' : '编辑'));
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

function handleChange(option, model){
  model.piName = option.consistencyName;
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

:deep(.piCode .ant-select-selection-item){
  display: none;
}
</style>