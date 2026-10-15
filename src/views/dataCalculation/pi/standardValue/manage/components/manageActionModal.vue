<template>
  <BasicModal 
    @register="registerModal" 
    v-bind="$attrs" 
    :title="`${title}`"
    :cancelText="t('language.closeText')"
    okText="保存"
    @ok="handleSubmit"
    :loading = "modalLoading"
    :confirmLoading
    :show-ok-btn="!isDetail"
  >
    <BasicForm @register="registerForm" @submit="handleSubmit">
      <!--  条件插槽    -->
      <template #conditionSlot="{ model, field }">
        <a-row justify="space-between">
          <a-col :span="!isDetail ? 11 : 13">
            <a-select v-model:value="model[field]" placeholder="请选择字段" 
              allowClear
              show-search
              :filterOption = "(input, option) => handleFilterOption(input, option, 'name')"
              :options="conditionArray"
              :fieldNames="{label: 'name', value: 'fieldName'}" 
              @change="handleChange"
              :disabled="isDetail"
            />
          </a-col>
          <a-col :span="5">
            <a-select v-model:value="model['symbol_'+field]" placeholder="请选择条件" 
              allowClear
              :options="symbolOption"
              @change="handleChange"
              :disabled="isDetail"
            />
          </a-col>
          <a-col :span="5">
            <a-input v-model:value="model['value_'+field]" placeholder="请输入值" allowClear :disabled="isDetail" />
          </a-col>
          <a-col :span="2" v-if="!isDetail">
            <a-button @click="removeCondition(field)" type="link" danger style="padding: 0">删除</a-button>
          </a-col>
        </a-row>
      </template>

      <!-- 底部插槽 -->
      <template #formFooter>
        <div style="margin: 0 auto" v-if="!isDetail">
          <a-button type="link" @click="addCondition()">添加条件{{ isDetail }}</a-button>
        </div>
      </template>
    </BasicForm>
  </BasicModal>
</template>
<script lang="ts" setup>
import { BasicModal, useModalInner } from '/@/components/Modal';
import { useForm, BasicForm } from '/@/components/Form';
import { getFieldList, addData, updateData, getMaxSort } from '../standardManage.api';
import { addFormSchemas } from '../standardManage.data';
import { useMessage } from '/@/hooks/web/useMessage';
import { ref, toRaw } from 'vue';
import { cloneDeep } from 'lodash-es';
import { useI18n } from '/@/hooks/web/useI18n';
import dayjs from 'dayjs';
const { t } = useI18n();

//注册Message
const { createMessage: msg } = useMessage();
// 声明Emits
const emit = defineEmits(['success']);
//标题
const title = ref();
//操作标识
const isAdd = ref();
//详情标识
const isDetail = ref(false);
//条件符号下拉选项数据
const symbolOption = ref([
  { value: '1', label: '>' },
  { value: '2', label: '≥' },
  { value: '3', label: '=' },
  { value: '4', label: '≠' },
  { value: '5', label: '<' },
  { value: '6', label: '≤' }
]);
//条件下拉选项数据
const conditionArray = ref([]);
//条件下拉选项数据
const conditionOption = ref([]);
//条件总数
const conditionTotal = ref(0);
//初始加载loading
const modalLoading = ref(true);
//保存loading
const confirmLoading = ref(false);
//PI基础数据
const configData = ref(false);

//注册modal
const [registerModal, { closeModal }] = useModalInner(async (data: any) => {
  conditionTotal.value = 0;
  configData.value = data.configData;
  modalLoading.value = true;
  title.value = '-';
  isDetail.value = data.isDetail;
  //因有动态添加项操作，需要重置表单配置
  resetSchema(addFormSchemas);
  //重置表单数据
  resetFields();
  //赋值操作标识
  isAdd.value = data.isAdd;
  //获取字段数据
  const res = await getFieldList({pageSize: 9999, tableId: data.configData.id});
  conditionArray.value = res.records;
  //处理计算时间下拉数据
  const dateOption = res.records.filter((item) => item.type == '4' || item.type == '6');

  updateSchema({
    label: '重算时间对照字段',
    field: 'calcTimeField',
    componentProps: {
      showSearch: true,
      options: dateOption,
      fieldNames: {label: 'name', value: 'fieldName'},
      filterOption: (input, option) => handleFilterOption(input, option, 'name'),
      disabled: data.isAdd && '' != data.calcTimeField
    },
  })
  
  //判断执行新增还是编辑操作
  if(data.isAdd){
    title.value = '新增';
    setFieldsValue({
      tableName: data.configData.tableName,
      customizeId: data.configData.id,
      sort: (await getMaxSort({customizeId: data.configData.id})??0)+1,
      calcTimeField: data.calcTimeField
    });
  }else{
    const newData = cloneDeep(data.record);
    //设置表单值
    await setFieldsValue(newData);
    const conditions = newData.conditionJson ? JSON.parse(newData.conditionJson): '';
    for(const item of conditions){
      await addCondition(item.field, item.value, item.symbol);
    }
    title.value = '编辑';
    if(data.isDetail){
      title.value = '详情';
    }
  }
  // processingConditionArray();
  modalLoading.value = false;
});

/**
 * BasicForm绑定注册;
 */
const [registerForm, { resetFields, validate, setFieldsValue, getFieldsValue, setProps, updateSchema, appendSchemaByField, removeSchemaByFiled, resetSchema }] = useForm({
  //注册表单列
  schemas: addFormSchemas,
  //回车提交
  autoSubmitOnEnter: true,
  showActionButtonGroup: false,
  //表格label宽度
  labelCol: { style: { width: '140px' } },
  disabled: isDetail,
});

/**
 * 添加条件
 */
async function addCondition(conditionField, conditionValue, conditionSymbol){
  conditionTotal.value++;
  //生成唯一值
  const id = 'c_field_'+dayjs().valueOf().toString(36)+conditionTotal.value;
  await appendSchemaByField(
    {
      field: id,
      component: 'Select',
      label: '条件',
      colProps: { span: 24 },
      slot: 'conditionSlot',
      dynamicRules: function(obj){
        return [
          {
            //默认开启表单检验
            required: false,
            validator: (_, value) => {
              //需要return 一个Promise对象
              return new Promise((resolve, reject) => {
                const model = obj.model;
                const field = obj.field;
                if(value || model['symbol_'+field] || model['value_'+field]){
                  if (!value) {
                    reject('请选择字段');
                  }
                  if (!model['symbol_'+field]) {
                    reject('请选择条件');
                  }
                  if (!model['value_'+field]) {
                    reject('请输入值');
                  }
                }
                resolve();
              });
            },
          },
        ];
      },
    },
    ''
  );
  await appendSchemaByField(
    {
      field: `symbol_${id}`,
      component: 'Input',
      label: '条件符号',
      show: false,
    },
    ''
  );
  await appendSchemaByField(
    {
      field: `value_${id}`,
      component: 'Input',
      label: '条件值',
      show: false,
    },
    ''
  );
  if(conditionField){
    //分开赋值，避免编辑时校验异常
    await setFieldsValue({['symbol_'+id]: conditionSymbol});
    await setFieldsValue({['value_'+id]: conditionValue});
    await setFieldsValue({[id]: conditionField});
  }
}

/**
 * 下拉选项筛选
 * @param input 输入的值 
 * @param option 选项
 * @param searchField 需要查找的字段 
 */
function handleFilterOption(input, option, searchField) {
  return option[searchField].toLowerCase().indexOf(input.toLowerCase()) >= 0;
}

/**
 * 删除条件
 */
async function removeCondition(field){
  conditionTotal.value--;
  removeSchemaByFiled([field, 'value_'+field]);
  // processingConditionArray();
}

/**
 * 下拉改变监听
 */
function handleChange(value, option){
  // processingConditionArray();
}

/**
 * 处理条件下拉可选数据
 */
function processingConditionArray(){
  const obj = getFieldsValue();
  let newArr = cloneDeep(conditionArray.value);
  for (let key in obj) {
    //判断条件表单项前缀，处理数据
    if (key.startsWith('c_field_')) {
      for(const item of newArr){
        if(item.fieldName == obj[key]){
          item.disabled = true;
          break;
        }
      }
    }
  }
  conditionOption.value = newArr;
}

//表单提交事件
async function handleSubmit() {
  const values = await validate();
    
  try {
    msg.loading({ content: '正在保存...', duration: 0, key: 'saving' });
    confirmLoading.value = true;
    let conditionText = '';
    const conditionJson = [] as any;
    //处理数据
    for (let key in values) {
      //判断条件表单项前缀，处理数据
      if (key.startsWith('c_field_') && values[key]) {
        //查找对应字段
        const findObj = conditionArray.value.find((item) => item.fieldName == values[key]);
        //查找对应条件符号
        const findSymbolObj = symbolOption.value.find((item) => item.value == values[`symbol_${key}`]);
        //拼接描述字典
        conditionText += findObj?.name+findSymbolObj?.label+values[`value_${key}`]+'；';
        //拼接json数据
        conditionJson.push({field: values[key], value: values[`value_${key}`], symbol: values[`symbol_${key}`]});
      }
    }
    values.conditionText = conditionText;
    values.conditionJson = JSON.stringify(conditionJson);
    //提交表单
    if(isAdd.value){
      //调用后台新增接口
      await addData(values);
    }else{
      //调用后台修改接口
      await updateData(values);  
    }
    msg.success({ content: '保存成功', key: 'saving' });
    //关闭弹窗
    closeModal();
    //重置表单
    resetFields();
    //刷新外部表格
    emit('success');
  } catch (error) {
    msg.error({ content: '保存失败', key: 'saving' });
  } finally {
    confirmLoading.value = false;
  }
}
</script>

<style scoped>
  :deep(.ant-form-item-control){
    margin-top: 0 !important;
  }

  /** 时间和数字输入框样式 */
  :deep(.ant-input-number) {
    width: 100%;
  }

  :deep(.ant-picker) {
    width: 100%;
  }

  /** 下拉框默认文字颜色 */
  :deep(.ant-select-selection-placeholder) {
    color: #BEBEBE !important;
  }
</style>