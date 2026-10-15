<template>
  <BasicModal @register="registerModal" v-bind="$attrs" :title="title" :width="width" @ok="handleSubmit"
              :cancelText="t('language.closeText')" :showOkBtn=!disabledRef>
    <BasicForm @register="registerForm" @submit="handleSubmit" style="padding: 20px">
      <!--  deptCode部门查询插槽    -->
      <template #product="{ model, field }">
        <a-input v-model:value="serviceName" @click="showList" />
      </template>

      <template #affectPiCode="{ model, field }">
        <a-input v-model:value="affectPiTable" @click="showPIList" />
      </template>
    </BasicForm>
    <Drawer @register="register" @custom-event="handleEvent"  @closeDrawer="handleCloseDrawer" :showFooter="true" />
    <PIDrawer @register="registerPIDrawer" @custom-event="handlePIEvent"  @closeDrawer="handleCloseDrawer" :showFooter="true" />
  </BasicModal>

</template>

<script lang="ts" setup>
import { ref } from 'vue';
import { BasicModal, useModalInner } from '/@/components/Modal';
import { useForm, BasicForm } from '/@/components/Form';
import { useMessage } from '/@/hooks/web/useMessage';
import { cloneDeep } from 'lodash-es';
import { useI18n } from '/@/hooks/web/useI18n';
import Drawer from './ServiceInputDrawer.vue'
import {addFormSchemas} from "@/views/dataCalculation/serviceAgreement/ServiceAgreementException.data";
import {saveOrUpdate} from "@/views/dataCalculation/serviceAgreement/ServiceAgreementException.api";
import {useDrawer} from "@/components/Drawer";
import PIDrawer from './PIDrawer.vue';
import {useUserStore} from "@/store/modules/user";
const { t } = useI18n();

// 用户信息
const userStore = useUserStore();
// 判断新增或编辑或详情标识
const isAdd = ref();
const isDetail = ref();

// 禁用
const disabledRef = ref(false);

//需修改记录的id
const recordId = ref();
const serviceAgreementId = ref();
const serviceName = ref();
const affectPiCode = ref();
const affectPiTable = ref();
const title = ref<string>('');
const width = ref<number>(800);

const emit = defineEmits(['success']);
//注册抽屉
const [register, { openDrawer,closeDrawer }] = useDrawer();
const [registerPIDrawer, { openDrawer:openPIDrawer,closeDrawer:closePIDrawer }] = useDrawer();
//注册modal
const [registerModal, { closeModal }] = useModalInner(async (data: any) => {
  disabledRef.value = false;
  //重置表单数据
  await resetFields();
  //赋值操作标识
  isAdd.value = data.isAdd;
  isDetail.value = data.isDetail

  //判断执行新增、编辑、详情操作
  if (data.isDetail) {
    title.value = '详情';
    disabledRef.value = true;
  }
  if (data.isAdd) {
    title.value = '新增';
    await setFieldsValue({})
  } else {
    title.value = '编辑';
    let newData = cloneDeep(data.record);
    console.log(newData)
    serviceName.value = newData.serviceAgreementIdVal;
    serviceAgreementId.value = newData.serviceAgreementId;
    affectPiTable.value = newData.affectPiTable;
    affectPiCode.value = newData.id
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
const [registerForm, { resetFields, validate, setFieldsValue, updateSchema }] = useForm({
  //注册表单列
  schemas: addFormSchemas,
  //回车提交
  autoSubmitOnEnter: true,
  showActionButtonGroup: false,
  disabled: disabledRef,
  //表格label宽度
  labelCol: { style: { width: '120px' } },
});

const handleEvent = (data) => {
  console.log('--',data)
  serviceName.value = data.product;
  serviceAgreementId.value = data.id
  closeDrawer();
}

const handlePIEvent = (data) => {
  console.log('--',data)
  affectPiTable.value = data.name;
  affectPiCode.value = data.id
  closePIDrawer();
}

function showList(){
  openDrawer(true,{});
}

function showPIList(){
  openPIDrawer(true,{});
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
  try {
    const values = await validate();
    console.log(isAdd.value)
    //提交表单
    if (isAdd.value) {
      //调用后台新增接
      console.log('`````````' , values)
      values.factoryCode = userStore.factoryCode;
      // params.dept = userStore.departCode;
      values.deptCode = userStore?.userInfo?.orgCode;
      values.serviceAgreementId = serviceAgreementId.value;
      values.affectPiTable = affectPiTable.value;
      values.affectPiCode = affectPiCode.value;
      await saveOrUpdate(values, isAdd.value);
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
  }
}
</script>

<style>
/**隐藏样式-modal确定按钮 */
.jee-hidden {
  display: none !important;
}
</style>
