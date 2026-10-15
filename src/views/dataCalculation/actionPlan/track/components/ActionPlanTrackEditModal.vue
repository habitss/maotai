<template>
  <BasicModal v-bind="$attrs" @register="registerModal" destroyOnClose :title="title" width="60vw" :height="600"
    @ok="handleSubmit" okText="保存" :maskClosable="false">
    <div ref="containerRef" style="height: 600px; overflow: auto;">
      <Affix :offset-top="0" :target="() => containerRef" style="width: 100%;">
        <ActionPlanTrackEditFiexd ref="fiexdFormRef" />
      </Affix>
      <BasicForm @register="registerForm" name="ActionPlanTrackForm" style="padding-right: 20px;">
        <template #finishState="{ model, field }">
          <a-row>
            <a-col :span="!isUser(model) || model.finishVerifyState == '2' || (model.finishState != 0 && model.finishState != 2) ? 24 : 16">
              <JDictSelectTag v-model:value="model[field]" :disabled="true"
              dictCode="mes3_mdm_dict, item_key, item_val, del_flag = 0 and group_code = 'MES3_ACTION_PLAN_FINISH'" />
            </a-col>
            <a-col :span="8" v-if="isUser(model) && model.finishVerifyState != '2' && (model.finishState == 0 || model.finishState == 2)">
              <a-button type="link" block @click="()=>model[field]='3'">取消</a-button>
            </a-col>
          </a-row>
        </template>
      </BasicForm>
    </div>
  </BasicModal>
</template>

<script lang="ts" setup>
import { ref, computed, unref } from 'vue';
import { BasicModal, useModalInner } from '/@/components/Modal';
import { BasicForm, useForm } from '/@/components/Form/index';
import { formSchema, getDeptOptions, setRawFormData, handleDutyDeptChange } from '../ActionPlanTrack.data';
import { queryById, list, saveOrUpdate } from '../ActionPlanTrack.api';
import JDictSelectTag from '/@/components/Form/src/jeecg/components/JDictSelectTag.vue';
import { useUserStore } from '/@/store/modules/user';
import { Affix } from 'ant-design-vue';
import ActionPlanTrackEditFiexd from './ActionPlanTrackEditFiexd.vue';
import { cloneDeep } from 'lodash-es';
const containerRef = ref();
// 用户信息
const { userInfo } = useUserStore();
// Emits声明
const emit = defineEmits(['register', 'success']);
const isUpdate = ref(true);
const isDetail = ref(false);
//固定表单ref声明
const fiexdFormRef = ref();
//表单配置
const [registerForm, { setProps, resetFields, setFieldsValue, validate, scrollToField, updateSchema, getProps }] = useForm({
  labelWidth: 140,
  // schemas: formSchema,
  showActionButtonGroup: false,
  baseColProps: { span: 8 },
  autoSetPlaceHolder: false,
  wrapperCol: { span: 24 }
});
//表单赋值
const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {

  //重置表单
  await resetFields();
  fiexdFormRef.value.resetFields();
  //表单赋值数据加载
  let res = {} as any;
  //数据加载逻辑处理,id：数据主键，fiveWhyMeasuresId：5why详情id，dataId：其他绑定数据id
  if(data.record?.id){
    res = await queryById(data.record?.id);
  } else {
    let params = {pageSize: 1} as any;
    if(data.record?.fiveWhyMeasuresId){
      params.fiveWhyMeasuresId = data.record?.fiveWhyMeasuresId;
    }else if(data.record?.dataId){
      params.dataId = data.record?.dataId;
    }
    const { records } = await list(params);
    res.result = records[0];
  }
  
  await setRawFormData(res.result);
  data.showFooter = false;
  //如果不是数据中的四种权限人员，或者完成状态为取消，或者确认状态为完成，禁止编辑
  if((res.result.putUserId != userInfo?.id 
    && res.result.dutyUserId != userInfo?.id
    && res.result.executeUserId != userInfo?.id
    && res.result.verifyUserId != userInfo?.id)
    || res.result.finishState == '3'
    || res.result.finishVerifyState == '2'
  ){
    data.showFooter = false;
  }else{
    data.showFooter = true;
  }
  //表单配置
  let newSchema = cloneDeep(formSchema);
  //处理是否禁用表单
  if(!data?.showFooter){
    for(const item of newSchema){
      //禁用表单时排除remark
      item.dynamicDisabled = item.field == 'remark' ? false : true;
    }
  }
  //设置表单
  await setProps({schemas: newSchema})
  //调用顶部表单页面方法
  await fiexdFormRef.value.initData(!data.showFooter, res.result);
  
  setModalProps({ confirmLoading: false });
  isUpdate.value = !!data?.isUpdate;
  isDetail.value = !!data?.showFooter;

  //表单赋值
  await setFieldsValue({
    ...res.result,
  });

  await updateSchema([
    {
      field: 'putDeptId',
      componentProps: {
        options: getDeptOptions(res.result.putUserId),
      },
    },
    {
      field: 'dutyDeptId',
      componentProps: ({ formModel }) => {
        return {
          onChange:(e) => handleDutyDeptChange(formModel, e),
          options: getDeptOptions(res.result.dutyUserId),
        }
      },
    },
    {
      field: 'executeDeptId',
      componentProps: {
        options: getDeptOptions(res.result.executeUserId),
      },
    },
    {
      field: 'verifyDeptId',
      componentProps: {
        options: getDeptOptions(res.result.verifyUserId),
      },
    },
  ]);
});
//设置标题
const title = computed(() => (!unref(isUpdate) ? '新增' : !unref(isDetail) ? '详情' : '编辑'));
//表单提交事件
async function handleSubmit(v) {
  try {
    setModalProps({ confirmLoading: true });
    let values = await validate();
    let fixedForm = await fiexdFormRef.value.validate();
    //提交表单
    await saveOrUpdate({ ...values, ...fixedForm }, isUpdate.value);
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

//判断是否为确认人或者提出人
function isUser(model){
  return userInfo.id == model?.verifyUserId || userInfo.id == model?.putUserId ? true : false;
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