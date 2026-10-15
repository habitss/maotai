<template>
  <a-spin :spinning="confirmLoading">
    <JFormContainer :disabled="disabled">
      <template #detail>
        <a-form ref="formRef" class="antd-modal-form" :labelCol="labelCol" :wrapperCol="wrapperCol" name="GrafanaInstanceForm">
          <a-row>
            <a-col :span="24">
              <a-form-item label="GF实例名称" v-bind="validateInfos.instanceName" id="GrafanaInstanceForm-instanceName" name="instanceName">
                <a-input v-model:value="formData.instanceName" placeholder="请输入GF实例名称" allow-clear />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="GF实例URL" v-bind="validateInfos.instanceUrl" id="GrafanaInstanceForm-instanceUrl" name="instanceUrl">
                <a-input v-model:value="formData.instanceUrl" placeholder="请输入GF实例URL" allow-clear />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="GF实例Token" v-bind="validateInfos.apiToken" id="GrafanaInstanceForm-apiToken" name="apiToken">
                <a-input v-model:value="formData.apiToken" placeholder="请输入GF实例Token" allow-clear />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="状态" v-bind="validateInfos.state" id="GrafanaInstanceForm-state" name="state">
                <j-dict-select-tag
                  v-model:value="formData.state"
                  dictCode="mes3_mdm_dict where group_code = 'GF_InstanceBackupState',item_key,item_val"
                  placeholder="请选择状态"
                  allow-clear
                  :disabled="true"
                />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="错误信息" v-bind="validateInfos.errorMsg" id="GrafanaInstanceForm-errorMsg" name="errorMsg">
                <a-input v-model:value="formData.errorMsg" placeholder="请输入错误信息" allow-clear />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="主数据库类型" v-bind="validateInfos.pointRdsType" id="GrafanaInstanceForm-pointRdsType" name="pointRdsType">
                <j-dict-select-tag
                  v-model:value="formData.pointRdsType"
                  dictCode="mes3_mdm_dict where group_code = 'GF_RDSType',item_key,item_val"
                  placeholder="请选择主数据库类型"
                  allow-clear
                />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="主数据存放库URL" v-bind="validateInfos.pointDbUrl" id="GrafanaInstanceForm-pointDbUrl" name="pointDbUrl">
                <a-input v-model:value="formData.pointDbUrl" placeholder="请输入主数据存放库URL" allow-clear />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="主数据存放库用户" v-bind="validateInfos.pointDbUname" id="GrafanaInstanceForm-pointDbUname" name="pointDbUname">
                <a-input v-model:value="formData.pointDbUname" placeholder="请输入主数据存放库用户" allow-clear />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item
                label="主数据存放库密码"
                v-bind="validateInfos.pointDbPassword"
                id="GrafanaInstanceForm-pointDbPassword"
                name="pointDbPassword"
              >
                <a-input v-model:value="formData.pointDbPassword" placeholder="请输入主数据存放库密码" allow-clear />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="基础点位表" v-bind="validateInfos.basePointTb" id="GrafanaInstanceForm-basePointTb" name="basePointTb">
                <a-input v-model:value="formData.basePointTb" placeholder="请输入基础点位表" allow-clear />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="基础点位ID字段" v-bind="validateInfos.baseTbIdCol" id="GrafanaInstanceForm-baseTbIdCol" name="baseTbIdCol">
                <a-input v-model:value="formData.baseTbIdCol" placeholder="请输入基础点位ID字段" allow-clear />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="基础点位名称字段" v-bind="validateInfos.baseTbNameCol" id="GrafanaInstanceForm-baseTbNameCol" name="baseTbNameCol">
                <a-input v-model:value="formData.baseTbNameCol" placeholder="请输入基础点位名称字段" allow-clear />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="中间点位表" v-bind="validateInfos.orsPointTb" id="GrafanaInstanceForm-orsPointTb" name="orsPointTb">
                <a-input v-model:value="formData.orsPointTb" placeholder="请输入中间点位表" allow-clear />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="中间点位ID字段" v-bind="validateInfos.orsTbIdCol" id="GrafanaInstanceForm-orsTbIdCol" name="orsTbIdCol">
                <a-input v-model:value="formData.orsTbIdCol" placeholder="请输入中间点位ID字段" allow-clear />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="中间点位名称字段" v-bind="validateInfos.orsTbNameCol" id="GrafanaInstanceForm-orsTbNameCol" name="orsTbNameCol">
                <a-input v-model:value="formData.orsTbNameCol" placeholder="请输入中间点位名称字段" allow-clear />
              </a-form-item>
            </a-col>
          </a-row>
        </a-form>
      </template>
    </JFormContainer>
  </a-spin>
</template>

<script lang="ts" setup>
  import { ref, reactive, defineExpose, nextTick, defineProps, computed } from 'vue';
  import { useMessage } from '/@/hooks/web/useMessage';
  import JDictSelectTag from '/@/components/Form/src/jeecg/components/JDictSelectTag.vue';
  import { getValueType } from '/@/utils';
  import { saveOrUpdate } from '../GrafanaInstance.api';
  import { Form } from 'ant-design-vue';
  import JFormContainer from '/@/components/Form/src/container/JFormContainer.vue';
  const props = defineProps({
    formDisabled: { type: Boolean, default: false },
    formBpm: { type: Boolean, default: true },
  });
  const formRef = ref();
  const useForm = Form.useForm;
  const emit = defineEmits(['register', 'ok']);
  const formData = reactive<Record<string, any>>({
    id: '',
    instanceName: '',
    instanceUrl: '',
    apiToken: '',
    state: undefined,
    errorMsg: '',
    pointRdsType: '',
    pointDbUrl: '',
    pointDbUname: '',
    pointDbPassword: '',
    basePointTb: '',
    baseTbIdCol: '',
    baseTbNameCol: '',
    orsPointTb: '',
    orsTbIdCol: '',
    orsTbNameCol: '',
  });
  const { createMessage } = useMessage();
  const labelCol = ref<any>({ xs: { span: 24 }, sm: { span: 5 } });
  const wrapperCol = ref<any>({ xs: { span: 24 }, sm: { span: 16 } });
  const confirmLoading = ref<boolean>(false);
  //表单验证
  const validatorRules = reactive({
    instanceName: [{ required: true, message: '请输入GF实例名称!' }],
    instanceUrl: [{ required: true, message: '请输入GF实例URL!' }],
    apiToken: [{ required: true, message: '请输入GF实例Token!' }],
    state: [{ required: false, message: '请选择状态!' }],
    errorMsg: [{ required: false, message: '请输入错误信息!' }],
    pointRdsType: [{ required: false, message: '请输入主数据库类型!' }],
    pointDbUrl: [{ required: false, message: '请输入主数据存放库URL!' }],
    pointDbUname: [{ required: false, message: '请输入主数据存放库用户!' }],
    pointDbPassword: [{ required: false, message: '请输入主数据存放库密码!' }],
    basePointTb: [{ required: false, message: '请输入基础点位表!' }],
    baseTbIdCol: [{ required: false, message: '请输入基础点位ID字段名!' }],
    baseTbNameCol: [{ required: false, message: '请输入基础点位NAME字段名!' }],
    orsPointTb: [{ required: false, message: '请输入中间点位表!' }],
    orsTbIdCol: [{ required: false, message: '请输入中间点位ID字段名!' }],
    orsTbNameCol: [{ required: false, message: '请输入中间点位NAME字段名!' }],
  });
  const { resetFields, validate, validateInfos } = useForm(formData, validatorRules, { immediate: false });

  // 表单禁用
  const disabled = computed(() => {
    if (props.formBpm === true) {
      return false;
    }
    return props.formDisabled;
  });

  /**
   * 新增
   */
  function add() {
    edit({});
  }

  /**
   * 编辑
   */
  function edit(record) {
    nextTick(() => {
      resetFields();
      const tmpData = {};
      Object.keys(formData).forEach((key) => {
        if (record.hasOwnProperty(key)) {
          tmpData[key] = record[key];
        }
      });
      if (tmpData['state'] !== null && tmpData['state'] !== undefined) {
        tmpData['state'] = String(tmpData['state']);
      }
      //赋值
      Object.assign(formData, tmpData);
    });
  }

  /**
   * 提交数据
   */
  async function submitForm() {
    try {
      // 触发表单验证
      await validate();
    } catch (err) {
      const error = err as { errorFields?: Array<{ name: string }> };
      if (error.errorFields) {
        const firstField = error.errorFields[0];
        if (firstField) {
          formRef.value.scrollToField(firstField.name, { behavior: 'smooth', block: 'center' });
        }
      }
      return Promise.reject(error.errorFields);
    }
    confirmLoading.value = true;
    const isUpdate = ref<boolean>(false);
    //时间格式化
    let model = formData;
    if (model.id) {
      isUpdate.value = true;
    }
    //循环数据
    for (let data in model) {
      //如果该数据是数组并且是字符串类型
      if (model[data] instanceof Array) {
        let valueType = getValueType(formRef.value.getProps, data);
        //如果是字符串类型的需要变成以逗号分割的字符串
        if (valueType === 'string') {
          model[data] = model[data].join(',');
        }
      }
    }
    await saveOrUpdate(model, isUpdate.value)
      .then((res) => {
        if (res.success) {
          createMessage.success(res.message);
          emit('ok');
        } else {
          createMessage.warning(res.message);
        }
      })
      .finally(() => {
        confirmLoading.value = false;
      });
  }

  defineExpose({
    add,
    edit,
    submitForm,
  });
</script>

<style lang="less" scoped>
  @import url('/@/styles/disabled.less');
  .antd-modal-form {
    padding: 14px;
  }
</style>
