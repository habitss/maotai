<template>
  <BasicModal v-bind="$attrs" @register="registerModal" destroyOnClose :title="title" :width="800" :minHeight="400" @ok="handleSubmit">
    <BasicForm @register="registerForm" name="ActionPlanProblemTypeForm"></BasicForm>
  </BasicModal>
</template>

<script lang="ts" setup>
import { ref, computed, unref } from 'vue';
import { BasicModal, useModalInner } from '/@/components/Modal';
import { BasicForm, useForm } from '/@/components/Form/index';
import { getRecipeFormSchema } from '../PiCurveStandard.data';
import { recipeSaveOrUpdate,getRecipe } from '../PiCurveStandard.api';
import { useUserStore } from '/@/store/modules/user';
// 用户信息
const userStore = useUserStore();
// Emits声明
const emit = defineEmits(['register', 'success']);
const isUpdate = ref(true);
const isDetail = ref(false);
const expandedRowKeys = ref([]);
const recipeArray = ref([]);
// 当前编辑的数据
let model: Nullable<Recordable> = null;

// 加载配方
const fetchRecipes = async (dataSource, areaNo) => {
  let res = await getRecipe({dataSource, areaNo})
  recipeArray.value = []
  if(res){
    res.forEach((item, index, array) => {
      //0标准和1目标
      recipeArray.value.push({
        value: `${item.recipeNo}`,
        label: item.recipeName
      })
    })
    return recipeArray.value;
  }
  return [];
};

//区域变更
const handleAreaChange = async (value , data, first = false) => {
  const dataSource = getFieldsValue().dataSource;
  //const areaNo = getFieldsValue().areaNo;
  const options = await fetchRecipes(dataSource,value);
  updateSchema({ field: 'recipeNo', componentProps: { options } });
  if(!first){
    setFieldsValue({ recipeNo: undefined,recipeName: ''});
  }
};
//配方变更
const handleRecipeChange = async (value , data,first = false) => {
  const item = recipeArray.value.find(item => item.value === value);
  let recipeCn = ""
  if(item && item.label){
    recipeCn = item.label
  }
  if(!first){
    setFieldsValue({ recipeName:recipeCn });
  }
};

//表单配置
const [registerForm, { setProps,updateSchema, resetFields, setFieldsValue, validate, scrollToField,getFieldsValue }] = useForm({
  labelWidth: 150,
  schemas: getRecipeFormSchema(handleAreaChange,handleRecipeChange),
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
  if (data?.record) {
    model = data.record;
    //表单赋值
    await setFieldsValue({
      ...data.record,
    });
  } else {
    model = null;
  }
  // 隐藏底部时禁用整个表单
  setProps({ disabled: !data?.showFooter })
});
//设置标题
const title = computed(() => (!unref(isUpdate) ? '新增' : !unref(isDetail) ? '详情' : '编辑'));
//表单提交事件
async function handleSubmit(v) {
  try {
    let values = await validate();
    values.deptCode = userStore.departCode;
    values.factoryCode = userStore.factoryCode;
    setModalProps({ confirmLoading: true });
    //提交表单
    await recipeSaveOrUpdate(values);
    //关闭弹窗
    closeModal();
    //刷新列表
    emit('success', values.recipeId);
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