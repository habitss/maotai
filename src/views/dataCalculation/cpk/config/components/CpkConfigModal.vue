<template>
  <BasicModal v-bind="$attrs" @register="registerModal" destroyOnClose :width="800" :title="getTitle"
    @ok="handleSubmit">
    <BasicForm @register="registerForm" name="CpkConfigForm" />
  </BasicModal>
</template>
<script lang="ts" setup>
import { ref, computed, unref } from 'vue';
import { BasicModal, useModalInner } from '/@/components/Modal';
import { BasicForm, useForm } from '/@/components/Form';
import { formSchema } from '../CpkConfig.data';
import { loadTreeData, saveOrUpdate } from '../CpkConfig.api';
import { useUserStore } from '/@/store/modules/user';
// 用户信息
const userStore = useUserStore();
// 获取emit
const emit = defineEmits(['register', 'success']);
const isUpdate = ref(true);
const isDetail = ref(false);
const treeData = ref([]);
// 当前编辑的数据
let model: Nullable<Recordable> = null;
//表单配置
const [registerForm, { setProps, resetFields, setFieldsValue, validate, scrollToField }] = useForm({
  schemas: formSchema,
  showActionButtonGroup: false,
  baseColProps: { span: 24 },
  labelCol: {
    xs: { span: 24 },
    sm: { span: 4 },
  },
  wrapperCol: {
    xs: { span: 24 },
    sm: { span: 18 },
  },
});
//表单赋值
const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
  //重置表单
  await resetFields();
  setModalProps({ confirmLoading: false, minHeight: 80, showOkBtn: !!!data?.hideFooter });
  isUpdate.value = !!data?.isUpdate;
  isDetail.value = !!data?.showFooter;
  if (data?.record) {
    model = data.record;
    //表单赋值
    await setFieldsValue({
      ...data.record,
    });
  } else {
    model = null;
  }
  //父级节点树信息
  treeData.value = await loadTreeData({ 'async': false, 'pcode': '' });
  // 隐藏底部时禁用整个表单
  setProps({ disabled: !!data?.hideFooter })
});
//设置标题
const getTitle = computed(() => (!unref(isUpdate) ? '新增' : '编辑'));
//表单提交事件
async function handleSubmit() {
  try {
    let values = await validate();
    setModalProps({ confirmLoading: true });
    //提交表单
    values.factoryCode = userStore.factoryCode;
    await saveOrUpdate(values, isUpdate.value);
    //关闭弹窗
    closeModal();
    emit('success', {
      isUpdate: unref(isUpdate),
      parentId: values.parentId,
    });
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