<template>
  <BasicModal v-bind="$attrs" @register="registerModal" destroyOnClose :title="title" :width="800" @ok="handleSubmit">
    <BasicForm @register="registerForm" name="KpiReportExceptionLogForm" style="padding-right: 20px;">
      <template #textSlot="{ model, field }">
        {{ model[field] }}
      </template>
      <template #hintSlot="{ model, field }">
        <div style="color: red; text-align: center;">当前数据处于审核中，无法再次进行修改和发起审核，请先点击“撤回申请，重新修改”！</div>
      </template>
    </BasicForm>
  </BasicModal>
</template>

<script lang="ts" setup>
import { ref } from 'vue';
import { BasicModal, useModalInner } from '/@/components/Modal';
import { BasicForm, useForm } from '/@/components/Form/index';
import { formSchema } from '../KpiReportExceptionLog.data';
import { deleteOne, batchAddOrEdit, list } from '../KpiReportExceptionLog.api';
import { useUserStore } from '/@/store/modules/user';
import dayjs from 'dayjs';
import { cloneDeep } from 'lodash-es';
// 用户信息
const { userInfo } = useUserStore();
// Emits声明
const emit = defineEmits(['register', 'success']);
const isUpdate = ref(true);
const isDetail = ref(false);
//设置标题
const title = ref('');
//处理后的数据
const newData = ref({} as any);

//表单配置
const [registerForm, { setProps, resetFields, setFieldsValue, validate, scrollToField }] = useForm({
  labelWidth: 100,
  schemas: formSchema,
  showActionButtonGroup: false,
  baseColProps: { span: 12 },
  wrapperCol: {
    xs: { span: 24 },
    sm: { span: 24 },
  },
});
//表单赋值
const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
  //重置表单
  await resetFields();
  newData.value = {};

  isUpdate.value = !!data?.isUpdate;
  isDetail.value = !!data?.showFooter;

  let datac = {...cloneDeep(data.rowData), ...cloneDeep(data.params)}
  let colour = datac[datac.colKey] && datac[datac.colKey]['colour'];
  const colorIndex = colour == 'red' ? 2 : colour == 'green' ? 1 : 0;
  //判断当前数据类型 1:周累实际 2:月累实际 3:周累目标 4:月累目标 5:月年累实际 6:月年累目标
  let dataType = '';
  let dataTypeName = '';
  let kpiDataKey = datac.colKey;

  if (datac.colKey.includes('yearAccumulate')) {
    kpiDataKey = kpiDataKey.substring(0, 7);
    let typec = datac.curve.value;
    if (typec === '实际') {
      dataType = '5';
      dataTypeName = '月年累实际值';
    } else {
      dataType = '6';
      dataTypeName = '月年累目标值';
    }
  } else if (datac.colKey.length > 7) {
    let typec = datac.curve.value;
    if (typec === '实际') {
      dataType = '1';
      dataTypeName = '周累实际值';
    } else {
      dataType = '3';
      dataTypeName = '周累目标值';
    }
  } else {
    let typec = datac.curve.value;
    if (typec === '实际') {
      dataType = '2';
      dataTypeName = '月累实际值';
    } else {
      dataType = '4';
      dataTypeName = '月累目标值';
    }
  }
  newData.value = {
    kpiParent: datac.perentId && datac.perentId.value,
    kpiCode: datac.kpiCode,
    kpiName: datac.kpiName,
    createBy_dictText: userInfo?.realname,
    oldColor: colorIndex,
    oldValue: datac.colKey && datac[datac.colKey] && datac[datac.colKey]['value'] || '',
    weekId: datac.colKey && datac[datac.colKey] && datac[datac.colKey]['id'] || '',
    indicator: dataType,
    kpiDataKey: kpiDataKey,
    year: datac.year,
    reportId: userInfo?.orgCode,
    indicator_dictText: dataTypeName,
  }
  //获取后台数据做后续判断
  const res = await list({ approveType: 3, pageSize: 1, ...newData.value});
  
  title.value = '修改数据-'+datac.kpiName;
  //判断是否有审核中的数据
  if(res && res.total>0){
    title.value += '(审核中)';
    newData.value = {...newData.value, ...res.records[0]};
  }else{
    newData.value.createTime = dayjs().format('YYYY-MM-DD HH:mm:ss');
  }

  setModalProps({ confirmLoading: false, showCancelBtn: !!data?.showFooter, showOkBtn: !!data?.showFooter, okText: res && res.total>0 ? '撤回申请，重新修改' : '保存' });
  //表单赋值
  await setFieldsValue({
    ...newData.value,
  });
  // 隐藏底部时禁用整个表单
  setProps({ disabled: !data?.showFooter })

  console.log('newData.value :>> ', newData.value);
});
//表单提交事件
async function handleSubmit(v) {
  try {
    let values = await validate();
    setModalProps({ confirmLoading: true });
    newData.value.newValue = values.newValue;
    newData.value.updateCause = values.updateCause;
    //判断是否有id，有则调用撤销接口，无则调用新增接口
    if(values.id){
      await deleteOne({id: values.id}, handleSuccess);
    } else {
      delete newData.value.updateTime;
      delete newData.value.createTime;
      //提交表单
      await batchAddOrEdit([newData.value]);
      handleSuccess();
    }
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

function handleSuccess(){
    //关闭弹窗
    closeModal();
    //刷新列表
    emit('success');
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