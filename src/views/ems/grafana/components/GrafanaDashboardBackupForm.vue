<template>
  <a-spin :spinning="confirmLoading">
    <JFormContainer :disabled="disabled">
      <template #detail>
        <a-form ref="formRef" class="antd-modal-form" :labelCol="labelCol" :wrapperCol="wrapperCol" name="DashboardBackupForm">
          <a-row>
            <a-col :span="24">
              <a-form-item label="GF实例名称" v-bind="validateInfos.instanceName" id="DashboardBackupForm-instanceName" name="instanceName">
                <a-input v-model:value="formData.instanceName" placeholder="请输入GF实例名称" allow-clear :disabled="!!formData.id" />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="GF实例URL" v-bind="validateInfos.instanceUrl" id="DashboardBackupForm-instanceUrl" name="instanceUrl">
                <a-input v-model:value="formData.instanceUrl" placeholder="请输入GF实例URL" allow-clear :disabled="!!formData.id" />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="报表ID" v-bind="validateInfos.uid" id="DashboardBackupForm-uid" name="uid">
                <a-input v-model:value="formData.uid" placeholder="请输入报表ID" allow-clear :disabled="!!formData.id" />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="报表标识" v-bind="validateInfos.slug" id="DashboardBackupForm-slug" name="slug">
                <a-input v-model:value="formData.slug" placeholder="请输入报表标识" allow-clear :disabled="!!formData.id" />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="报表标题" v-bind="validateInfos.title" id="DashboardBackupForm-title" name="title">
                <a-input v-model:value="formData.title" placeholder="请输入报表标题" allow-clear />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="报表原始JSON" v-bind="validateInfos.rawJson" id="DashboardBackupForm-rawJson" name="rawJson">
                <a-textarea v-model:value="formData.rawJson" :rows="10" placeholder="请输入GF原始JSON" />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="状态" v-bind="validateInfos.state" id="DashboardBackupForm-state" name="state">
                <j-dict-select-tag
                  v-model:value="formData.state"
                  dictCode="mes3_mdm_dict where group_code = 'GF_DashboardState',item_key,item_val"
                  placeholder="请选择状态"
                  allow-clear
                />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="错误信息" v-bind="validateInfos.errorMsg" id="DashboardBackupForm-errorMsg" name="errorMsg">
                <a-input v-model:value="formData.errorMsg" placeholder="请输入错误信息" allow-clear />
              </a-form-item>
            </a-col>
          </a-row>
        </a-form>
      </template>
    </JFormContainer>
  </a-spin>
</template>

<script lang="ts" setup name="GrafanaDashboardBackupForm">
  import { ref, reactive, defineExpose, nextTick, defineProps, computed } from 'vue';
  import { useMessage } from '/@/hooks/web/useMessage';
  import JDictSelectTag from '/@/components/Form/src/jeecg/components/JDictSelectTag.vue';
  import { getValueType } from '/@/utils';
  import { saveOrUpdate } from '../GrafanaDashboardBackup.api';
  import { Form } from 'ant-design-vue';
  import JFormContainer from '/@/components/Form/src/container/JFormContainer.vue';
  import { t } from '/@/hooks/web/useI18n';
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
    uid: '',
    slug: '',
    title: '',
    orgId: '',
    rawJson: '',
    state: undefined,
    errorMsg: '',
  });
  const { createMessage } = useMessage();
  const labelCol = ref<any>({ xs: { span: 24 }, sm: { span: 5 } });
  const wrapperCol = ref<any>({ xs: { span: 24 }, sm: { span: 16 } });
  const confirmLoading = ref<boolean>(false);
  //表单验证
  const validatorRules = reactive({
    instanceName: [{ required: true, message: '请输入GF实例名称!' }],
    instanceUrl: [{ required: true, message: '请输入GF实例URL!' }],
    uid: [{ required: true, message: '请输入报表ID!' }],
    slug: [{ required: true, message: '请输入报表标识!' }],
    title: [{ required: true, message: '请输入报表标题!' }],
    state: [{ required: true, message: '请选择备份状态!' }],
    rawJson: [{ required: true, message: '请输入报表原始JSON!' }],
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
      tmpData['instanceName'] = record.instanceIdVal;
      tmpData['instanceUrl'] = record.instanceIdUrlVal;
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
