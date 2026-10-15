<template>
  <BasicModal v-bind="$attrs" @register="registerModal" destroyOnClose :title="title" :width="400" :min-height="80"
    :max-height="500" @ok="handleSubmit(false)" :maskClosable="false">
    <BasicForm @register="registerForm" name="ManagerCheckConfigForm" style="margin-top: 20px" />
    <template #insertFooter>
      <a-popconfirm v-if="showClear" title="是否确认删除配置？" ok-text="是" cancel-text="否" @confirm="handleSubmit(true)">
        <a-button type="link">清除</a-button>
      </a-popconfirm>
    </template>
  </BasicModal>
</template>

<script lang="ts" setup>
import { BasicModal, useModalInner } from '/@/components/Modal';
import { BasicForm, useForm } from '/@/components/Form/index';
import { gwpzFormSchema, disabledWeekOption, disabledMonDayOption } from '../ManagerCheckConfig.data';
import { saveOrUpdate } from '../ManagerCheckConfig.api';
import { ref } from 'vue';
import { cloneDeep } from 'lodash-es';
// Emits声明
const emit = defineEmits(['register', 'success']);
//设置标题
const title = ref('矩阵频率调整');
//上级传入的记录
const record = ref({} as any);
//上级传入的列配置
const column = ref({} as any);
//配置数据
const gwpzArr = ref([] as any[]);
//岗位id
const postId = ref('');
//是否显示清除按钮
const showClear = ref(false);

//表单配置
const [registerForm, { resetFields, setFieldsValue, validate, scrollToField }] = useForm({
  labelWidth: 110,
  schemas: gwpzFormSchema,
  showActionButtonGroup: false,
  baseColProps: { span: 24 }
});
//表单赋值
const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
  //重置表单
  showClear.value = false;
  await resetFields();
  disabledWeekOption(7, null);
  //数据赋值
  title.value = data.column.customTitle + '-矩阵频率调整';
  record.value = data.record;
  column.value = data.column;
  gwpzArr.value = record.value.gwpzJson ? JSON.parse(record.value.gwpzJson) : [];
  postId.value = String(column.value.dataIndex);
  const obj = cloneDeep(gwpzArr.value.find((item) => item.id == postId.value));
  obj.plSelect = obj.pl;
  obj.weekSelect = obj?.week?.split(',') || [];
  if(obj?.day){
    obj.day = String(obj.day).split(',');
  } else {
    obj.day = [];
  }
  showClear.value = obj ? true : false;
  //处理周选择禁用项
  disabledWeekOption(obj.pl, obj.week);
  //处理日期选择禁用项
  disabledMonDayOption(obj.pl, obj.day);
  //表单赋值
  await setFieldsValue({
    ...obj,
  });
});

//表单提交事件
async function handleSubmit(isClear) {
  try {
    if (isClear) {
      gwpzArr.value = gwpzArr.value.filter(item => item.id != postId.value);
    } else {
      //获取表单数据及校验
      let values = await validate();
      setModalProps({ confirmLoading: true });
      //删除不需要的字段
      delete values.plSelect;
      delete values.weekSelect;
      if (values.id) {
        //替换原有数据
        gwpzArr.value = gwpzArr.value.map(item => item.id == postId.value ? values : item);
      } else {
        //无id新增数据
        values.id = postId.value;
        values.name = column.value.customTitle;
        gwpzArr.value.push(values);
      }
    }
    record.value.gwpzJson = JSON.stringify(gwpzArr.value);
    //提交表单
    await saveOrUpdate(record.value, true);
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