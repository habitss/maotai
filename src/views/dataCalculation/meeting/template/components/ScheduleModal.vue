<template>
  <BasicModal @register="registerAddModal" v-bind="$attrs" :title="title" :width="width" height="270"
              :cancelText="t('language.closeText')" @ok="handleSubmit" :showOkBtn=!disabledRef>
    <a-spin
      :spinning="submitting"
      tip="正在提交，请稍候..."
      size="large"
    >
    <BasicForm @register="registerAddForm" @submit="handleSubmit" >
    </BasicForm>
    </a-spin>
    <div v-if="scheduleId != null"><a-button
      style="background-color: red; color: white;margin-left: 30px;"
      @click="handleChange"
    >取消排期</a-button></div>

  </BasicModal>
</template>

<script lang="ts" setup>
import { ref } from 'vue';
import { BasicModal, useModalInner } from '/@/components/Modal';
import { useForm, BasicForm } from '/@/components/Form';
import {cancelSchedule, getSchedule, saveSchedule} from '../MeetingTemplate.api';
import {scheduleFormSchemas} from '../MeetingTemplate.data';
import { useMessage } from '/@/hooks/web/useMessage';
import { useI18n } from '/@/hooks/web/useI18n';
const { t } = useI18n();

// 判断新增或编辑或详情标识
const isAdd = ref();
const isDetail = ref();
// 禁用
const disabledShow = ref(false);
//需修改记录的id
const recordId = ref();
const scheduleId = ref();
const submitting = ref();
const title = ref<string>('');
const width = ref<number>(800);
const disabledRef = ref(false);

const baseData = ref({
  meetingType: '',
})
const emit = defineEmits(['success']);

//注册modal
const [registerAddModal, { closeModal }] = useModalInner(async (data: any) => {
  let rowValue = data.depData;
  submitting.value =false;
  disabledShow.value = false;
  disabledRef.value =false;
  //重置表单数据
  await resetFields();

  //赋值操作标识
  isAdd.value = data.isAdd;
  isDetail.value = data.isDetail
  title.value = rowValue.meetingName + '会议排期';
  baseData.value.meetingType = rowValue.id;
  await initSubmit(rowValue.id);
  //await setFieldsValue(baseData.value)
});

//注册Message
const { createMessage: msg } = useMessage();

/**
 * BasicForm绑定注册;
 */
const [registerAddForm, { resetFields, validate, setFieldsValue, updateSchema }] = useForm({
  //注册表单列
  schemas: scheduleFormSchemas,
  //回车提交
  autoSubmitOnEnter: true,
  showResetButton: false,
  showActionButtonGroup:false,
  disabled: disabledShow,
  //表格label宽度
  labelCol: { style: { width: '100px' } },
});

async function initSubmit(types) {
  let schedule = await getSchedule({meetingType:types});
  disabledRef.value = false;
  console.log(schedule)
  //设置表单值
  if(null != schedule && schedule != "无排期"){
    disabledRef.value = true;
    scheduleId.value = schedule.id;
    if(schedule.meetingType == 1){
      await setFieldsValue({type:schedule.meetingType,dayWeek:schedule.meetingWeek.split(','),time:schedule.meetingTime});
    } else {
      await setFieldsValue({type:schedule.meetingType,week:schedule.meetingNum,
        day:schedule.meetingWeek,dayWeek:schedule.meetingWeek,time:schedule.meetingTime});
    }
  } else {
    console.log(disabledRef.value)
    disabledRef.value = false;
  }


}

async function handleChange(){
  await cancelSchedule({scheduleId:scheduleId.value});
  //重置表单
  await resetFields();
  disabledRef.value = false;
  scheduleId.value = null;
  //刷新外部表格
  emit('success');
}

//表单提交事件
async function handleSubmit() {
  submitting.value =true;
  try {
    const values = await validate();
      //调用后台新增接
    let result = Object.assign({},values, baseData.value)
    console.log(result.week)
    await saveSchedule(result);
    submitting.value =false;
    //关闭弹窗
    closeModal();
    //重置表单
    await resetFields();
    //刷新外部表格
    emit('success');
  } catch (error) {
    msg.error({ content: '保存失败', key: 'saving' });
    submitting.value =false;
  }
}
</script>

<style>
/**隐藏样式-modal确定按钮 */
.jee-hidden {
  display: none !important;
}
</style>
