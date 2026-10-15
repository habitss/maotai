<template>
  <BasicModal v-bind="$attrs" @register="registerModal" destroyOnClose :title="title" :width="'60vw'" @ok="handleSubmit" :closeFunc="handleClose">
    <BasicForm @register="registerForm" name="ManageCheckListForm" style="padding-right: 20px;"></BasicForm>
    <ActionPlanTrackForm ref="actionPlanRef" @success="handleSuccess" :initRecord="false" :isShowTopForm="false" :isShowSubmitBtn="false"></ActionPlanTrackForm>
  </BasicModal>
</template>

<script lang="ts" setup>
import { ref, computed, unref, onMounted } from 'vue';
import { BasicModal, useModalInner } from '/@/components/Modal';
import { BasicForm, useForm } from '/@/components/Form/index';
import { formSchema } from '../ManageCheckList.data';
import { saveOrUpdate } from '../ManageCheckList.api';
import { useUserStore } from '/@/store/modules/user';
import { list as actionProblemList } from '/@/views/dataCalculation/actionPlan/problemType/ActionPlanProblemType.api';
import { list as actionList } from '@/views/dataCalculation/actionPlan/track/ActionPlanTrack.api';
import { getDeptOptions } from '/@/views/dataCalculation/actionPlan/track/ActionPlanTrack.data';
import ActionPlanTrackForm from '@/views/dataCalculation/actionPlan/track/ActionPlanTrackForm.vue';
import { useMessage } from '/@/hooks/web/useMessage';

//注册Message
const { createMessage } = useMessage();
//获取用户信息
const { userInfo } = useUserStore();
// Emits声明
const emit = defineEmits(['register', 'success', 'updateDesc']);
const isUpdate = ref(true);
const isDetail = ref(false);
const parentRecord = ref();
//行动计划id
const actionProblemId = ref('');
//行动计划
const actionPlanRef = ref();
//表单配置
const [registerForm, { setProps, resetFields, getFieldsValue, setFieldsValue, validate, scrollToField }] = useForm({
  labelWidth: 120,
  schemas: formSchema,
  showActionButtonGroup: false,
  baseColProps: { span: 8 },
  wrapperCol: {
    span: 24,
  },
});
//表单赋值
const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
  //重置表单
  await resetFields();
  //设置弹窗属性
  setModalProps({ confirmLoading: false, showCancelBtn: !!data?.showFooter, showOkBtn: !!data?.showFooter });
  isUpdate.value = !!data?.isUpdate;
  isDetail.value = !!data?.showFooter;
  parentRecord.value = data?.record;

  let actionPlanData = [] as any;
  if (unref(isUpdate)) {
    //获取行动计划
    const {records} = await actionList({pageSize: 999, dataId: data?.record.id });
    for(const item of records){
      item.id = item.id.toString();
      actionPlanData.push(item);
    }
  }
  const checkDrptArr = await getDeptOptions(data?.record?.checkUser);
  if(checkDrptArr.length === 1 && !data?.record?.checkUserDept){
    data.record.checkUserDept = checkDrptArr[0].value;
  }
  //表单赋值
  await setFieldsValue({
    ...data.record,
  });
  // 隐藏底部时禁用整个表单
  setProps({ disabled: !data?.showFooter });
  actionPlanRef.value?.otherPageInit(data?.record, actionPlanData || [], false);
});
//设置标题
const title = computed(() => (!unref(isUpdate) ? '新增' : !unref(isDetail) ? '详情' : '编辑'));
//表单提交事件
async function handleSubmit(v) {
  try {
    let values = await validate();

    //组装行动计划所需数据
    const actionForm = {
      raiseDate: values.checkDate,
      problemDescription: values.problemDesc,
      createDept: userInfo?.orgCode,
      problemTypeId: actionProblemId.value,
      pathText: values.problemImg,
      fileObj: values.optimizeImg,
    }
    //调用行动计划方法
    const actionResult = await actionPlanRef.value?.setFormDataAndResultData(actionForm, false);
    if(false === actionResult) return;
    values.actionPlanTrackList = actionResult;

    //设置loading状态
    setModalProps({ confirmLoading: true });
    
    //提交表单
    const res = await saveOrUpdate(values, isUpdate.value);
    createMessage.success('操作成功!');
    //关闭弹窗
    closeModal();
    //刷新列表
    emit('success', parentRecord.value, res.data.result);
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

async function handleClose(){
  emit('updateDesc', parentRecord?.value?.rIndex, getFieldsValue().problemDesc);
  return true;
}

function handleSuccess(){
  //刷新列表
  emit('success');
}

onMounted(async () => {
  const { records } = await actionProblemList({problemName: '分层检查'})
  actionProblemId.value = records[0]?.id;
});
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