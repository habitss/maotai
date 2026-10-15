<template>
  <BasicModal @register="registerAddModal" v-bind="$attrs" :title="title" :width="width"
              :cancelText="t('language.closeText')" @ok="handleSubmit" :showOkBtn=!disabledRef>
    <BasicForm @register="registerAddForm" @submit="handleSubmit">
      <template #optime="{ model, field }">

        <a-button v-if="flag" type="primary" @click="startTimer">
          <template #icon><PoweroffOutlined /></template>
          开始计时
        </a-button>
        <a-button v-else type="primary" @click="stopTimer">
          <template #icon><PoweroffOutlined /></template>
          结束计时
        </a-button>
      </template>
      <template #realTime="{ model, field }">
        <a-input v-model:value="realTime" disabled />
      </template>
    </BasicForm>
  </BasicModal>
</template>

<script lang="ts" setup>
import {computed, ref} from 'vue';
import { BasicModal, useModalInner } from '/@/components/Modal';
import { useForm, BasicForm } from '/@/components/Form';
import { saveOrUpdateInfo } from '../Meeting.api';
import { editFormSchemas } from '../Meeting.data';
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
// 计时器状态
const startTime = ref(null);
const isTiming = ref(false);
const timerInterval = ref(null);
const flag = ref(true);
const realTime = ref(0);

const baseData = ref({
  meetingType: '',
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
  disabledShow.value = false;
  await resetSchema(editFormSchemas);
  //重置表单数据
  await resetFields();
  //赋值操作标识
  isAdd.value = data.isAdd;
  isDetail.value = data.isDetail
  realTime.value = 0;

  //判断执行新增、编辑、详情操作
  if (data.isDetail) {
    title.value = '详情';
    disabledShow.value = true;
  }
  if (data.isAdd) {
    await setFieldsValue(baseData.value)
  } else {
    title.value = '编辑会议日程';
    let newData = cloneDeep(data.record);
    if(null == newData.realTime){
      realTime.value = 0;
    } else {
      realTime.value = newData.realTime
    }
    if(null != newData.links){
      newData.links.forEach(function (value,index){
        ++index;
        appendSchemaByField(
          {
            label: 'id'+index,
            field: 'id'+index,
            component: 'Input',
            show: false,
          },
          '',
        );
        appendSchemaByField(
          {
            label: '链接'+index,
            field: 'link'+index,
            component: 'Input',
            colProps: { span: 16 },
          },
          '',
        );
        appendSchemaByField(
          {
            label: '链接地址'+index,
            field: 'linkAddress'+index,
            component: 'InputTextArea',
            colProps: { span: 24 },
          },
          ''
        );
        const idKey = `id${index}`;
        const linkKey = `link${index}`;
        const addressKey = `linkAddress${index}`;
        newData[idKey] = value.id;
        newData[linkKey] = value.link;
        newData[addressKey] = value.linkAddress

      })
    }
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
const [registerAddForm, { resetFields, validate, setFieldsValue, updateSchema ,appendSchemaByField,resetSchema}] = useForm({
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

//部门改变事件
async function handleDeptChange(value) {
  //清空产线
  await setFieldsValue({
    lineCode: '',
  });
}


// 开始计时
const startTimer = () => {
  if (isTiming.value) return;
  flag.value = false;
  isTiming.value = true;
  timerInterval.value = setInterval(() => {
    realTime.value += 1;
  }, 1000);
};

// 停止计时并记录
const stopTimer = () => {
  if (!isTiming.value) return;
  flag.value = true;
  clearInterval(timerInterval.value);
  isTiming.value = false;
};


//表单提交事件
async function handleSubmit() {
  try {
    const links = [{}];
    const values = await validate();
    console.log(values)
    // 处理带数字后缀的 link 和 linkAddress
    let index = 1;
    while (true) {
      const idKey = `id${index}`;
      const linkKey = `link${index}`;
      const addressKey = `linkAddress${index}`;

      if (values[linkKey] && values[addressKey]) {
        links.push({
          id: values[idKey],
          link: values[linkKey],
          linkAddress: values[addressKey]
        });
        index++;
      } else {
        break;
      }
    }
    let valueData = Object.assign({links:links}, values)
    //提交表单
    if (isAdd.value) {
      //调用后台新增接
      let result = Object.assign(valueData, baseData.value)
      await saveOrUpdateInfo(result, isAdd.value);
    } else {
      valueData.id = recordId.value;
      valueData.realTime = realTime.value;
      //调用后台修改接口
      await saveOrUpdateInfo(valueData, isAdd.value);
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
