<template>
  <BasicModal @register="registerModal" v-bind="$attrs" :title="title" :width="width" height="280" @ok="handleSubmit"
              :cancelText="t('language.closeText')" :showOkBtn=!disabledRef>
    <BasicForm @register="registerForm" @submit="handleSubmit">

    </BasicForm>
  </BasicModal>
</template>

<script lang="ts" setup>
import { ref } from 'vue';
import { BasicModal, useModalInner } from '/@/components/Modal';
import { useI18n } from '/@/hooks/web/useI18n';
import {useForm,BasicForm} from "@/components/Form";
import {
  checkFormSchemas
} from "@/views/dataCalculation/meeting/calendar/Meeting.data";
import {checkEdit,getCheck} from "@/views/dataCalculation/meeting/calendar/Meeting.api";
import {useMessage} from "@/hooks/web/useMessage";
import {cloneDeep} from "lodash-es";

const { t } = useI18n();
const record = ref();
const emit = defineEmits(['success']);

// 禁用
const disabledRef = ref(false);

const title = ref<string>('');
const width = ref<number>(500);

// 判断新增或编辑或详情标识
const isAdd = ref();
const isDetail = ref();

//需修改记录的id
const recordId = ref();
const meetingId = ref();
const checkUser = ref();
const current = ref();

//注册modal
//注册modal
const [registerModal, { closeModal }] = useModalInner(async (data: any) => {
  disabledRef.value = false;
  //重置表单数据
  await resetFields();
  //赋值操作标识
  isAdd.value = data.isAdd;
  isDetail.value = data.isDetail

  let newData = cloneDeep(data.record);
  console.log(newData)
  title.value = '修改参会状态-' + newData.checkPersonVal;
  meetingId.value = newData.meetingId;
  checkUser.value = newData.checkPerson;
   let meeting = await getCheck({meetingId:meetingId.value,checkPerson:checkUser.value});
   console.log(meeting);
    //赋值需修改记录的id
  recordId.value = meeting.id;
    //设置表单值
  await setFieldsValue(meeting);

});


/**
 * BasicForm绑定注册;
 */
const [registerForm, { resetFields, validate, setFieldsValue, updateSchema }] = useForm({
  //注册表单列
  schemas: checkFormSchemas,
  //回车提交
  autoSubmitOnEnter: true,
  showActionButtonGroup: false,
  disabled: disabledRef,
  //表格label宽度
  labelCol: { style: { width: '100px' } },
});

//注册Message
const { createMessage: msg } = useMessage();
//表单提交事件
async function handleSubmit() {
  try {
    const values = await validate();
    //提交表单
    values.id = recordId.value;
    values.meetingId = meetingId.value;
    values.checkPerson = checkUser.value;
      //调用后台修改接口
    const result = await checkEdit(values);
    msg.success({ content: result.message, key: 'saving' });
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

function handleClose(){
  closeModal();
  emit('success');
}
</script>

<style scoped>
/* 增加弹出窗口的高度 */

/* App.css */
.custom-timepicker .ant-picker-dropdown {
  width: 200px; /* 调整宽度 */
  height: auto; /* 或设置一个固定高度 */
}
</style>
