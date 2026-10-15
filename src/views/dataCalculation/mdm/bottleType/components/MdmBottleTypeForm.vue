<template>
  <BasicModal 
    @register="registerModal" 
    @ok="handleSubmit"
    v-bind="$attrs" 
    :title="`${title}`"
    :cancelText="t('language.closeText')"
    :showOkBtn = !disabledRef
   >
    <BasicForm @register="registerForm" @submit="handleSubmit">
      <!--  deptCode部门插槽    -->
      <template #deptCode="{ model, field }">
        <kt-depart-select
          v-model:value="model[field]"
          select-type="depart"
          :placeholder="t('language.selectDepartment')"
          @change="handleDeptChange"
          :disabled = disabledRef
        />
      </template>
      
      <!--  lineCode产线插槽    -->
      <template #lineCode="{ model, field }">
        <kt-depart-select
          v-model:value="model[field]"
          select-type="line"
          :placeholder="t('language.selectProductLine')"
          :parentKey="model.deptCode"
          :disabled = disabledRef
        />
      </template>
    </BasicForm>
  </BasicModal>
</template>
<script lang="ts" setup>
import { ref } from 'vue';
import { BasicModal, useModalInner } from '/@/components/Modal';
import { useForm, BasicForm } from '/@/components/Form';
import { useI18n } from '/@/hooks/web/useI18n';
import { saveOrUpdate } from '../MdmBottleType.api';
import { addFormSchemas } from '../MdmBottleType.data';
import KtDepartSelect from '/@/components/KtDepartSelect/KtDepart.vue';

// 声明Emits
const emit = defineEmits(['success']);
// 国际化
const { t } = useI18n();
// 标题
const title = ref('');
//禁用
const disabledRef = ref(false);

//注册modal
const [registerModal, { closeModal }] = useModalInner(async (data: any) => {
  disabledRef.value = false;
  //重置表单数据
  resetFields();
  //设置表单数据
  setFieldsValue(data.record ?? {});
  //设置
  if(data.action === 'add'){
    title.value = t('language.add');
  }else if(data.action === 'edit'){
    title.value = t('language.edit');
  }else if(data.action === 'detail'){
    title.value = t('language.detail');
    disabledRef.value = true;
  }
  
  //新增时才可修改编码
  await updateSchema([
    {
      field: 'bottleTypeCode',
      componentProps: {
        disabled: data.action === 'add' ? false : true,
      },
    },
  ]);
});

/**
 * BasicForm绑定注册;
 */
const [registerForm, { resetFields, validate, setFieldsValue, updateSchema }] = useForm({
  //注册表单列
  schemas: addFormSchemas,
  //回车提交
  autoSubmitOnEnter: true,
  showActionButtonGroup: false,
  disabled: disabledRef,
  //表格label宽度
  labelCol: { style: { width: '100px' } },
});

//部门改变事件
async function handleDeptChange(value){
  //清空产线
  await setFieldsValue({ 
    lineCode: '', 
  });
}

//表单提交事件
async function handleSubmit() {
  try {
    //校验表单
    const values = await validate();
    //设置工厂代码
    values.factoryCode = values.deptCode.substr(0, 9);
    //提交表单，调用后台新增接口
    await saveOrUpdate(values);
    //关闭弹窗
    closeModal();
    //重置表单
    resetFields();
    //刷新外部表格
    emit('success');
  } catch (error) {
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
