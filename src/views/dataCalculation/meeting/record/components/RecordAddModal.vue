<template>
  <BasicModal @register="registerAddModal" v-bind="$attrs" :title="title" :width="width"
              :cancelText="t('language.closeText')" @ok="handleSubmit" :showOkBtn=!disabledRef>
    <BasicForm @register="registerAddForm" @submit="handleSubmit">

    </BasicForm>
  </BasicModal>
</template>

<script lang="ts" setup>
import { ref } from 'vue';
import { BasicModal, useModalInner } from '/@/components/Modal';
import { useForm, BasicForm } from '/@/components/Form';
import { saveOrUpdate } from '../MeetingRecord.api';
import { addFormSchemas } from '../MeetingRecord.data';
import { useMessage } from '/@/hooks/web/useMessage';
import { cloneDeep } from 'lodash-es';
import { useI18n } from '/@/hooks/web/useI18n';
const { t } = useI18n();

// 判断新增或编辑或详情标识
const isAdd = ref();
const isDetail = ref();
// 禁用
const disabledShow = ref(false);

//需修改记录的id
const recordId = ref();

const title = ref<string>('');
const width = ref<number>(800);

const baseData = ref({
  meetingType: 0,
  meetingName: '',
  participant: '',
  factoryCode:'',
  deptCode:'',
  lineCode:''
})
const emit = defineEmits(['success']);

//注册modal
const [registerAddModal, { closeModal }] = useModalInner(async (data: any) => {
  let rowValue = data.depData;
  console.log(data)
  disabledShow.value = false;
  //重置表单数据
  await resetFields();
  //赋值操作标识
  isAdd.value = data.isAdd;
  isDetail.value = data.isDetail

  //判断执行新增、编辑、详情操作
  if (data.isDetail) {
    title.value = '详情';
    disabledShow.value = true;
  }
  if (data.isAdd) {
    title.value = '添加会议评估模版信息';
    //baseData.value.meetingType = 0;
    // baseData.value.meetingName = rowValue.meetingName;
    // baseData.value.participant = rowValue.participant;
    // baseData.value.deptCode = rowValue.deptCode;
    // baseData.value.lineCode = rowValue.lineCode;
    await setFieldsValue(baseData.value)
  } else {
    title.value = '编辑会议评估模版信息';
    let newData = cloneDeep(data.record);
    console.log(newData)
    //赋值需修改记录的id
    recordId.value = newData.id;
    //设置表单值
    await setFieldsValue(newData);
  }
});

//注册Message
const { createMessage: msg } = useMessage();

/**
 * BasicForm绑定注册;
 */
const [registerAddForm, { resetFields, validate, setFieldsValue, updateSchema }] = useForm({
  //注册表单列
  schemas: addFormSchemas,
  //回车提交
  autoSubmitOnEnter: true,
  showResetButton: false,
  showActionButtonGroup:false,
  disabled: disabledShow,
  //表格label宽度
  labelCol: { style: { width: '100px' } },
});

//部门改变事件
async function handleDeptChange(value) {
  //清空产线
  await setFieldsValue({
    lineCode: '',
  });
}

//表单提交事件
async function handleSubmit() {
  try {
    const values = await validate();
    console.log(values)
    //提交表单
    if (isAdd.value) {
      //调用后台新增接
      let result = Object.assign({},values, baseData.value)
      await saveOrUpdate(result, isAdd.value);
    } else {
      values.id = recordId.value;
      if(values.recordType == '会议内容'){
        values.recordType = 1;
      } else {
        values.recordType = 2;
      }
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
</script>

<style>
/**隐藏样式-modal确定按钮 */
.jee-hidden {
  display: none !important;
}
</style>
