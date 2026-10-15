<template>
  <BasicModal @register="registerModal" v-bind="$attrs" :title="title" :width="width" :height="340" @ok="handleSubmit"
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
  addConfigFormSchemas
} from "@/views/dataCalculation/meeting/calendar/Meeting.data";
import {departCode, saveOrUpdate} from "@/views/dataCalculation/meeting/calendar/Meeting.api";
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

const current = ref();

//注册modal
//注册modal
const [registerModal, { closeModal, setModalProps }] = useModalInner(async (data: any) => {
  disabledRef.value = false;
  //重置表单数据
  await resetFields();
  //赋值操作标识
  isAdd.value = data.isAdd;
  isDetail.value = data.isDetail

  if (data.isDetail) {
    title.value = '详情';
    disabledRef.value = true;
  }
  if (data.isAdd) {
    title.value = '新增';
    current.value = data.record.currentDate
  } else {
    title.value = '编辑';
    let newData = cloneDeep(data.record);

    let code = '';
    if(null != newData.deptCode && '' != newData.deptCode){
      code = newData.deptCode
    } else {
      code = newData.factoryCode
    }
    let depart = await departCode({deptCode:code})
    newData.deptCode = depart.id
    await handleTypeChange(depart.id)
    //赋值需修改记录的id
    recordId.value = newData.id;
    console.log('newData :>> ', newData);
    //设置表单值
    await setFieldsValue(newData);
  }
});


/**
 * BasicForm绑定注册;
 */
const [registerForm, { resetFields, validate, setFieldsValue, updateSchema }] = useForm({
  //注册表单列
  schemas: addConfigFormSchemas,
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
  const values = await validate();
  try {
    setModalProps({ confirmLoading: true });
    //提交表单
    if (isAdd.value) {
      //调用后台新增接口
      values.meetingDate = current.value;
      const result = await saveOrUpdate(values, isAdd.value);
      msg.success({ content: result.message, key: 'saving' });
    } else {
      values.id = recordId.value;
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
  } finally{
    setModalProps({ confirmLoading: false });
  }
}

//设备下拉列表联动
async function handleTypeChange(value) {
  await updateSchema([
    {
      field: 'meetingType',
      component: 'JDictSelectTag',
      componentProps: {
        dictCode: "mes3_meeting_config, meeting_type, id, del_flag = 0 and dept_id='" + value + "'" ,
      },
    },
  ]);
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
