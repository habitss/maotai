<template>
  <BasicModal v-bind="$attrs" @register="registerModal" destroyOnClose :title="title" :width="1000" @ok="handleSubmit">
    <BasicForm @register="registerForm" name="BestPracticePromotionForm" style="padding-right: 20px;">
      <!--  部门插槽    -->
      <template #deptCode="{ model, field }">
        <kt-depart-select
          v-model:value="model[field]"
          select-type="depart"
          :placeholder="'请选择提报部门'"
          :disabled="model.dataId"
        />
      </template>

      <!-- 结果插槽 -->
      <template #reviewResult="{ model, field }">
        <a-tag v-if="model[field] == 2" color="#FFFF00" style="padding: 4px 20px; color: #000">建议推广</a-tag>
        <a-tag v-else-if="model[field] == 3" color="#008000" style="padding: 4px 20px;">强制推广</a-tag>
        <a-tag v-else color="#FE0000" style="padding: 4px 20px;">不推广</a-tag>
        (强制推广≥23分；建议推广≥18分；不推广＜18分)
      </template>
    </BasicForm>
  </BasicModal>
</template>

<script lang="ts" setup>
import { ref, computed, unref } from 'vue';
import { BasicModal, useModalInner } from '/@/components/Modal';
import { BasicForm, useForm } from '/@/components/Form/index';
import { formSchema, options, getDepart } from '../BestPracticePromotion.data';
import { saveOrUpdate } from '../BestPracticePromotion.api';
import KtDepartSelect from '/@/components/KtDepartSelect/KtDepart.vue';
// Emits声明
const emit = defineEmits(['register', 'success']);
const isUpdate = ref(true);
const isDetail = ref(false);
//表单配置
const [registerForm, { setProps, resetFields, setFieldsValue, validate, scrollToField, appendSchemaByField }] = useForm({
  labelWidth: 150,
  schemas: formSchema,
  showActionButtonGroup: false,
  wrapperCol: {
    span: 24,
  },
  baseColProps: { span: 8 }
});
//表单赋值
const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
  //重置表单
  await resetFields();

  setModalProps({ confirmLoading: false, showCancelBtn: !!data?.showFooter, showOkBtn: !!data?.showFooter });
  isUpdate.value = !!data?.isUpdate;
  isDetail.value = !!data?.showFooter;
  await initDepartField();
  if (unref(isUpdate)) {
    //表单赋值
    await setFieldsValue({
      ...data.record,
    });
  }
  // 隐藏底部时禁用整个表单
  setProps({ disabled: !data?.showFooter })
});
//设置标题
const title = computed(() => (!unref(isUpdate) ? '新增' : !unref(isDetail) ? '详情' : '编辑'));
//表单提交事件
async function handleSubmit() {
  try {
    let values = await validate();
    setModalProps({ confirmLoading: true });
    //解析获取识别部门数据
    let identificationShop = [] as any;
    for(const key in values){
      if(key.indexOf('sbcj_') != -1){
        const dArr = key.split('_')
        identificationShop.push({
          field: key,
          orgCode: dArr[1],
          departName: dArr[2],
          value: values[key] ?? ''
        });
        delete values[key];
      }
    }
    values.identificationShop = JSON.stringify(identificationShop);
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

//添加识别部门字段
async function initDepartField(){
  //获取部门数据
  const deptArr = await getDepart() ?? [];
  deptArr.forEach(async (item) => {
    await appendSchemaByField(
      {
        field: 'sbcj_'+item.orgCode+'_'+item.departName,
        component: 'Select',
        label: item.departName,
        componentProps:{
          placeholder: '请选择',
          options
        },
        colProps: {
          span: 8,
        },
      },
      ''
    );
  });
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