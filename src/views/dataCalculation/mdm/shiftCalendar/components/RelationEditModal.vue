<template>
  <BasicModal @register="registerModal" v-bind="$attrs" :title="title" :width="width" height="280" @ok="handleSubmit"
              :cancelText="t('language.closeText')" :showOkBtn=!disabledRef>
    <BasicForm @register="registerAddForm" @submit="handleSubmit">

    </BasicForm>
  </BasicModal>
</template>

<script lang="ts" setup>
import { ref } from 'vue';
import { BasicModal, useModalInner } from '/@/components/Modal';
import { useForm, BasicForm } from '/@/components/Form';
import { useI18n } from '/@/hooks/web/useI18n';
import { groupList,shiftList,saveOrUpdate} from "@/views/dataCalculation/mdm/shiftCalendar/ShiftCalendar.api";
import {useMessage} from "@/hooks/web/useMessage";
import {cloneDeep} from "lodash-es";
import {useUserStore} from "@/store/modules/user";
import {editFormSchemas} from "@/views/dataCalculation/mdm/shiftCalendar/ShiftCalendar.data";

const { t } = useI18n();
const record = ref();
const emit = defineEmits(['success']);

const userInfo = useUserStore();
// 禁用
const disabledRef = ref(false);
const selectedValues = ref([]);
const title = ref<string>('');
const width = ref<number>(500);


const shift = ref([]);

// 判断新增或编辑或详情标识
const isAdd = ref();
const isDetail = ref();
// 禁用
const disabledShow = ref(false);
//需修改记录的id
const recordId = ref();

const current = ref();

//注册modal
//注册modal
const [registerModal, { closeModal }] = useModalInner(async (data: any) => {
  disabledRef.value = false;
  console.log(data.record)
  current.value = data.record.currentDate
  // await getListData(current.value);
  // await getGroupListData(current.value);
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
    //赋值需修改记录的id
    recordId.value = newData.id;
    //设置表单值
    await setFieldsValue(newData);
  }
});

/**
 * BasicForm绑定注册;
 */
const [registerAddForm, { resetFields, validate, setFieldsValue, updateSchema }] = useForm({
  //注册表单列
  schemas: editFormSchemas,
  //回车提交
  autoSubmitOnEnter: true,
  showResetButton: false,
  showActionButtonGroup:false,
  disabled: disabledShow,
  //表格label宽度
  labelCol: { style: { width: '100px' } },
});

//注册Message
const { createMessage: msg } = useMessage();
//表单提交事件
async function handleSubmit() {
  try {
    const values = await validate();
    console.log(isAdd.value)
    //提交表单
    if (isAdd.value) {
      //调用后台新增接口
      const result = await saveOrUpdate(values, isAdd.value);
      msg.success({ content: result.message, key: 'saving' });
    } else {
      values.id = recordId.value;
      //调用后台修改接口
      await saveOrUpdate(values, true);
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

async function getListData(value){
  console.log(value)
  shift.value = await shiftList({deptCode:userInfo?.userInfo?.orgCode,currentDate:value});
  console.log(shift.value)
}

async function getGroupListData(value){
  console.log(value)
  selectedValues.value = await groupList({deptCode:userInfo?.userInfo?.orgCode});
  console.log(selectedValues.value)
}


function handleClose(){
  closeModal();
  emit('success');
}
</script>

<style scoped>
.custom-select-item {
  display: flex;
  align-items: center;
  margin-right: 20px;
  margin-top: 20px;
}

.label {
  margin-right: 38px;
  width: 152px;
  text-align: right;
}
</style>
