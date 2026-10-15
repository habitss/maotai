<template>
  <BasicModal @register="registerModal" v-bind="$attrs" :title="title" :width="width" @ok="handleSubmit"
              :cancelText="t('language.closeText')" :showOkBtn=!disabledRef>
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
    <ActionPlanTrackForm ref="actionPlanRef" @success="handleSuccess" :initRecord="false" :isShowTopForm="false" :isShowSubmitBtn="false"></ActionPlanTrackForm>
  </BasicModal>
</template>

<script lang="ts" setup>
import { ref } from 'vue';
import { BasicModal, useModalInner } from '/@/components/Modal';
import { useForm, BasicForm } from '/@/components/Form';
import { saveOrUpdate } from '../Report.api';
import { addFormSchemas } from '../Report.data';
import { useMessage } from '/@/hooks/web/useMessage';
import { cloneDeep } from 'lodash-es';
import { useI18n } from '/@/hooks/web/useI18n';
import KtDepartSelect from '/@/components/KtDepartSelect/KtDepart.vue';
import ActionPlanTrackForm from '@/views/dataCalculation/actionPlan/track/ActionPlanTrackForm.vue';
import { list as actionList } from '@/views/dataCalculation/actionPlan/track/ActionPlanTrack.api';
import { useUserStore } from '/@/store/modules/user';
const { t } = useI18n();

// 判断新增或编辑或详情标识
const isAdd = ref();
const isDetail = ref();
const userInfo = useUserStore();
// 禁用
const disabledRef = ref(false);

//需修改记录的id
const recordId = ref();

const title = ref<string>('新增异常信息');
const width = ref<number>(1200);


const emit = defineEmits(['success']);
//行动计划
const actionPlanRef = ref();

//注册modal
const [registerModal, { setModalProps,closeModal }] = useModalInner(async (data: any) => {
  disabledRef.value = false;
  //重置表单数据
  await resetFields();
  //赋值操作标识
  isAdd.value = data.isAdd;
  isDetail.value = data.isDetail
  // //获取行动计划
  // const {records} = await actionList({pageSize: 999, dataId: data?.record.id });
  // actionPlanRef.value?.otherPageInit(data?.record, actionPlanData || [], 'hide');
  //判断执行新增、编辑、详情操作
  let newData = cloneDeep(data.record);
  console.log(data.record)
  if (data.isDetail) {
    title.value = '详情';
    disabledRef.value = true;
    await initAction(newData);
  }
  if (data.isAdd) {
    title.value = '新增';
    await initAction(data);
    await resetFields()
  } else {
    title.value = '编辑';
    //赋值需修改记录的id
    recordId.value = newData.id;
    await initAction(newData);
    //设置表单值
    await setFieldsValue(newData);
  }
});

//注册Message
const { createMessage: msg } = useMessage();

/**
 * BasicForm绑定注册;
 */
const [registerForm, { resetFields, validate, setFieldsValue, updateSchema }] = useForm({
  //注册表单列
  schemas: addFormSchemas,
  //回车提交
  autoSubmitOnEnter: true,
  showActionButtonGroup: false,
  //自定义提交按钮文本和图标
  showSubmitButton: true,
  disabled: disabledRef,
  //表格label宽度
  labelCol: { style: { width: '100px' } },
});


async function initAction(data){
  console.log('------======',data)
  //获取数据
  let datas = [];
  if(data.id){
    const {records} = await actionList({pageSize: 999, dataId: data?.id });
    console.log(records)
    datas = records;
  }
  //初始化
  await actionPlanRef.value.otherPageInit({}, datas, true);
}

//部门改变事件
async function handleDeptChange(value) {
  //清空产线
  await setFieldsValue({
    lineCode: '',
  });
}

//表单提交事件
async function handleSubmit() {
  console.log("actionResult")
  try {
    const values = await validate();
    //组装行动计划所需数据
    const actionForm = {
      raiseDate: values.exceptionTime,
      problemDescription: values.exception,
      createDept: userInfo?.orgCode,
      problemTypeId: '15',
    }
    //获取行动计划数据
    const actionResult = await actionPlanRef.value?.setFormDataAndResultData(actionForm, false);
    console.log(actionResult)
    //行动计划数据校验
    if(false === actionResult) return;
    //提交表单
    if (isAdd.value) {
      //调用后台新增接
      console.log('`````````' , values)
      values.actionPlanTrackList = actionResult;
      //let result = Object.assign(values, baseData)
      await saveOrUpdate(values, isAdd.value);
    } else {
      values.id = recordId.value;
      values.actionPlanTrackList = actionResult;
      //调用后台修改接口
      await saveOrUpdate(values, isAdd.value);
    }
    //关闭弹窗
    closeModal();
    //重置表单
    await resetFields();
    //刷新外部表格
    emit('success');
  } catch (error) {
    msg.error({ content: '保存失败', key: 'saving' });
  }
}

function handleSuccess(){
  closeModal();
  emit('success');
}
</script>

<style>
/**隐藏样式-modal确定按钮 */
.jee-hidden {
  display: none !important;
}
</style>
