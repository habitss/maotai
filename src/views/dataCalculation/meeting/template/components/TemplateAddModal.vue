<template>
  <BasicModal @register="registerAddModal" v-bind="$attrs" :title="title" :width="width"
              :cancelText="t('language.closeText')" @ok="handleSubmit" :showOkBtn=!disabledRef>
    <BasicForm @register="registerAddForm" @submit="handleSubmit">
      <!--  deptCode部门插槽    -->
      <template #deptCode="{ model, field }">
        <kt-depart-select
          v-model:value="model[field]"
          select-type="depart"
          :placeholder="t('language.selectDepartment')"
          @change="handleDeptChange"
          :disabled = disabledShow
        />
      </template>
      <!--  lineCode产线插槽    -->
      <template #lineCode="{ model, field }">
        <kt-depart-select
          v-model:value="model[field]"
          select-type="line"
          :placeholder="t('language.selectProductLine')"
          :parentKey="model.deptCode"
          :disabled = disabledShow
        />
      </template>

      <!--  添加input的插槽  -->
      <template #addForm="{ field }">
        <a-button v-if="Number(field) === 0" @click="addField" style="width: 50px">+</a-button>
        <a-button v-if="Number(field) > 0" @click="removeField(field)" style="width: 50px">-</a-button>
      </template>
    </BasicForm>
  </BasicModal>
</template>

<script lang="ts" setup>
import { ref } from 'vue';
import { BasicModal, useModalInner } from '/@/components/Modal';
import { useForm, BasicForm } from '/@/components/Form';
import { saveOrUpdate } from '../MeetingTemplate.api';
import { addFormSchemas } from '../MeetingTemplate.data';
import { useMessage } from '/@/hooks/web/useMessage';
import { cloneDeep } from 'lodash-es';
import { useI18n } from '/@/hooks/web/useI18n';
import KtDepartSelect from '/@/components/KtDepartSelect/KtDepart.vue';
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
  meetingType: '',
  meetingName: '',
  participant: '',
  factoryCode:'',
  deptCode:'',
  lineCode:''
})
const emit = defineEmits(['success']);
//组件个数
let n = ref<number>(0);
//注册modal
const [registerAddModal, { closeModal }] = useModalInner(async (data: any) => {
  let rowValue = data.depData;
  disabledShow.value = false;
  await resetSchema(addFormSchemas);
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
    title.value = '添加会议模版信息';
    baseData.value.meetingType = rowValue.id;
    baseData.value.meetingName = rowValue.meetingName;
    baseData.value.participant = rowValue.participant;
    baseData.value.factoryCode = rowValue.factoryCode;
    baseData.value.deptCode = rowValue.deptCode;
    baseData.value.lineCode = rowValue.lineCode;
    baseData.value.workshop = rowValue.workshop;
    await setFieldsValue(baseData.value)
  } else {
    title.value = '编辑会议模版信息';
    let newData = cloneDeep(data.record);
    console.log(newData)
    if(null != newData.links){
      newData.links.forEach(function (value,index){
            ++index;
        appendSchemaByField(
          {
            label: '链接'+index,
            field: `link${index}`,
            component: 'Input',
            colProps: { span: 16 },
          },
          '',
        );
           appendSchemaByField(
            {
              label: ' ',
              field: `${index}`,
              component: 'Input',
              colProps: { span: 4 },
              slot: 'addForm',
            },
            '',
          );

          appendSchemaByField(
            {
              label: '链接地址'+index,
              field: `linkAddress${index}`,
              component: 'InputTextArea',
              colProps: { span: 24 },
            },
            ''
          );
          const idKey = `${index}`;
          const linkKey = `link${index}`;
          const addressKey = `linkAddress${index}`;
          newData[idKey] = value.id;
          newData[linkKey] = value.link;
          newData[addressKey] = value.linkAddress
          n.value = index;
      })
    }
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
const [registerAddForm, { appendSchemaByField,resetFields, validate, setFieldsValue, updateSchema , removeSchemaByFiled, resetSchema }] = useForm({
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

async function addField() {
  n.value++;
  //添加表单字段，里面为schemas对应的属性，可自行配置
  await appendSchemaByField(
    {
      label: '链接'+n.value,
      field: `link${n.value}`,
      component: 'Input',
      colProps: { span: 16 },
    },
    '',
  );
  await appendSchemaByField(
    {
      label: ' ',
      field: `${n.value}`,
      component: 'Input',
      colProps: { span: 4 },
      slot: 'addForm',
    },
    '',
  );

  await appendSchemaByField(
    {
      label: '链接地址'+n.value,
      field: `linkAddress${n.value}`,
      component: 'InputTextArea',
      colProps: { span: 24 },
    },
    ''
  );

}

async function removeField(field) {

  await removeSchemaByFiled([`link${field}`, `linkAddress${field}`, `${field}`]);
}

//表单提交事件
async function handleSubmit() {
  try {
    const links = [{}];
    const values = await validate();
    console.log(values);
    // 处理无数字后缀的 link 和 linkAddress
    // if (values.link && values.linkAddress) {
    //   links.push({
    //     link: values.link,
    //     linkAddress: values.linkAddress
    //   });
    // }

    // 处理带数字后缀的 link 和 linkAddress
    let index = n.value;
    console.log(index)
    for (let i = 1; i <= index; i++) {
      const idKey = `${i}`;
      const linkKey = `link${i}`;
      const addressKey = `linkAddress${i}`;
      links.push({
          id: values[idKey],
          link: values[linkKey],
          linkAddress: values[addressKey]
        });
      console.log(links)
    }
    console.log(links)
    let valueData = Object.assign({links:links}, values)
    console.log(valueData)
    //提交表单
    if (isAdd.value) {
      //调用后台新增接
      let result = Object.assign({},valueData, baseData.value)
      await saveOrUpdate(result, isAdd.value);
    } else {
      values.id = recordId.value;
      //调用后台修改接口
      await saveOrUpdate(valueData, isAdd.value);
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
