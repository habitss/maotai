<template>
  <a-spin :spinning="confirmLoading">
    <JFormContainer>
      <template #detail>
        <a-form ref="formRef" class="antd-modal-form" :labelCol="labelCol" :wrapperCol="wrapperCol" name="AlarmTsmLogForm">
          <a-row>
            <a-col :span="24">
              <a-form-item label="类型" v-bind="validateInfos.type" id="AlarmTsmLogForm-type" name="type">
                <j-dict-select-tag
                  v-model:value="formData.type"
                  dictCode="mes3_mdm_dict WHERE group_code='TSM_LogType',item_key,item_val"
                  placeholder="请选择类型"
                  allow-clear
                />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="请求参数" v-bind="validateInfos.requestParams" id="AlarmTsmLogForm-requestParams" name="requestParams">
                <a-textarea v-model:value="formData.requestParams" :rows="4" placeholder="请输入请求参数" :disabled="false" />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="返回结果" v-bind="validateInfos.responseResult" id="AlarmTsmLogForm-responseResult" name="responseResult">
                <a-textarea v-model:value="formData.responseResult" :rows="4" placeholder="请输入返回结果" />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="请求时间" v-bind="validateInfos.requestTime" id="AlarmTsmLogForm-requestTime" name="requestTime">
                <a-date-picker
                  placeholder="请选择请求时间"
                  v-model:value="formData.requestTime"
                  showTime
                  value-format="YYYY-MM-DD HH:mm:ss"
                  style="width: 100%"
                  allow-clear
                />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="响应时间" v-bind="validateInfos.responseTime" id="AlarmTsmLogForm-responseTime" name="responseTime">
                <a-date-picker
                  placeholder="请选择响应时间"
                  v-model:value="formData.responseTime"
                  showTime
                  value-format="YYYY-MM-DD HH:mm:ss"
                  style="width: 100%"
                  allow-clear
                />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="请求状态" v-bind="validateInfos.status" id="AlarmTsmLogForm-status" name="status">
                <j-dict-select-tag
                  v-model:value="formData.status"
                  dictCode="mes3_mdm_dict WHERE group_code='TSM_ReqStatus',item_key,item_val"
                  placeholder="请选择请求状态"
                  allow-clear
                />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="错误信息" v-bind="validateInfos.errorMsg" id="AlarmTsmLogForm-errorMsg" name="errorMsg">
                <a-textarea v-model:value="formData.errorMsg" :rows="4" placeholder="请输入错误信息" />
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
  import { saveOrUpdate } from '../AlarmTsmLog.api';
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
    type: undefined,
    requestParams: '',
    responseResult: '',
    requestTime: '',
    responseTime: '',
    status: undefined,
    errorMsg: '',
  });
  const { createMessage } = useMessage();
  const labelCol = ref<any>({ xs: { span: 24 }, sm: { span: 5 } });
  const wrapperCol = ref<any>({ xs: { span: 24 }, sm: { span: 16 } });
  const confirmLoading = ref<boolean>(false);
  //表单验证
  const validatorRules = reactive({
    type: [{ required: true, message: '请输入类型!' }],
    requestParams: [{ required: true, message: '请输入请求参数!' }],
    requestTime: [{ required: true, message: '请输入请求时间!' }],
    status: [{ required: true, message: '请输入请求状态!' }],
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
      //赋值
      Object.assign(formData, tmpData);
      if (formData.type) {
        formData.type = String(formData.type);
      }
      if (formData.status || formData.status === 0) {
        formData.status = String(formData.status);
      }
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
  .antd-modal-form {
    padding: 14px;
  }
</style>
