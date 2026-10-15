<template>
  <a-spin :spinning="confirmLoading">
    <JFormContainer :disabled="disabled">
      <template #detail>
        <a-form ref="formRef" class="antd-modal-form" style="height: 80vh" :labelCol="labelCol" :wrapperCol="wrapperCol" name="DfDataLogForm">
          <a-row>
            <a-col :span="24">
              <a-form-item label="事件ID" v-bind="validateInfos.eventId" id="DfDataLogForm-eventId" name="eventId">
                <a-input v-model:value="formData.eventId" placeholder="请输入事件ID" allow-clear />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="事件开始时间" v-bind="validateInfos.startTime" id="DfDataLogForm-startTime" name="startTime">
                <a-date-picker
                  placeholder="请选择事件开始时间"
                  v-model:value="formData.startTime"
                  showTime
                  value-format="YYYY-MM-DD HH:mm:ss"
                  style="width: 100%"
                  allow-clear
                />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="事件结束时间" v-bind="validateInfos.endTime" id="DfDataLogForm-endTime" name="endTime">
                <a-date-picker
                  placeholder="请选择事件结束时间"
                  v-model:value="formData.endTime"
                  showTime
                  value-format="YYYY-MM-DD HH:mm:ss"
                  style="width: 100%"
                  allow-clear
                />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="指标PI值" v-bind="validateInfos.piValue" id="DfDataLogForm-piValue" name="piValue">
                <a-input v-model:value="formData.piValue" placeholder="请输入指标PI值" allow-clear />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="其他参数" v-bind="validateInfos.dataJson" id="DfDataLogForm-dataJson" name="dataJson">
                <a-textarea v-model:value="formData.dataJson" :rows="4" placeholder="请输入其他参数" />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="EOP编号" v-bind="validateInfos.eopId" id="DfDataLogForm-eopId" name="eopId">
                <a-input-number v-model:value="formData.eopId" placeholder="请输入EOP编号" style="width: 100%" />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="指标实例编码" v-bind="validateInfos.piCode" id="DfDataLogForm-piCode" name="piCode">
                <a-input v-model:value="formData.piCode" placeholder="请输入指标实例编码" allow-clear />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="指标名称" v-bind="validateInfos.piName" id="DfDataLogForm-piName" name="piName">
                <a-input v-model:value="formData.piName" placeholder="请输入指标名称" allow-clear />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="指标类型" v-bind="validateInfos.piType" id="DfDataLogForm-piType" name="piType">
                <a-input v-model:value="formData.piType" placeholder="请输入指标类型" allow-clear />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="UDE规则编码" v-bind="validateInfos.ruleId" id="DfDataLogForm-ruleId" name="ruleId">
                <a-input v-model:value="formData.ruleId" placeholder="请输入UDE规则编码" allow-clear />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="运行模式状态" v-bind="validateInfos.status" id="DfDataLogForm-status" name="status">
                <a-input-number v-model:value="formData.status" placeholder="请输入运行模式状态" style="width: 100%" />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="步骤号" v-bind="validateInfos.stepId" id="DfDataLogForm-stepId" name="stepId">
                <a-input-number v-model:value="formData.stepId" placeholder="请输入步骤号" style="width: 100%" />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="批次号" v-bind="validateInfos.batchId" id="DfDataLogForm-batchId" name="batchId">
                <a-input-number v-model:value="formData.batchId" placeholder="请输入批次号" style="width: 100%" />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="订单号" v-bind="validateInfos.orderId" id="DfDataLogForm-orderId" name="orderId">
                <a-input-number v-model:value="formData.orderId" placeholder="请输入订单号" style="width: 100%" />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="运行状态" v-bind="validateInfos.running" id="DfDataLogForm-running" name="running">
                <a-input-number v-model:value="formData.running" placeholder="请输入运行状态" style="width: 100%" />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="实例ID" v-bind="validateInfos.thingId" id="DfDataLogForm-thingId" name="thingId">
                <a-input v-model:value="formData.thingId" placeholder="请输入实例ID" allow-clear />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="配方号" v-bind="validateInfos.recipeId" id="DfDataLogForm-recipeId" name="recipeId">
                <a-input-number v-model:value="formData.recipeId" placeholder="请输入配方号" style="width: 100%" />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="UDE规则描述" v-bind="validateInfos.ruleName" id="DfDataLogForm-ruleName" name="ruleName">
                <a-input v-model:value="formData.ruleName" placeholder="请输入UDE规则描述" allow-clear />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="配方类型ID" v-bind="validateInfos.recipeTypeId" id="DfDataLogForm-recipeTypeId" name="recipeTypeId">
                <a-input-number v-model:value="formData.recipeTypeId" placeholder="请输入配方类型ID" style="width: 100%" />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="最后更新人" v-bind="validateInfos.updateBy" id="DfDataLogForm-updateBy" name="updateBy">
                <a-input v-model:value="formData.updateBy" placeholder="请输入最后更新人" allow-clear />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="最后更新时间" v-bind="validateInfos.updateTime" id="DfDataLogForm-updateTime" name="updateTime">
                <a-date-picker
                  placeholder="请选择最后更新时间"
                  v-model:value="formData.updateTime"
                  showTime
                  value-format="YYYY-MM-DD HH:mm:ss"
                  style="width: 100%"
                  allow-clear
                />
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
  import { getValueType } from '/@/utils';
  import { saveOrUpdate } from '../DfDataLog.api';
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
    eventId: '',
    startTime: '',
    endTime: '',
    piValue: '',
    dataJson: '',
    eopId: undefined,
    piCode: '',
    piName: '',
    piType: '',
    ruleId: '',
    status: undefined,
    stepId: undefined,
    batchId: undefined,
    orderId: undefined,
    running: undefined,
    thingId: '',
    recipeId: undefined,
    ruleName: '',
    recipeTypeId: undefined,
    updateBy: '',
    updateTime: '',
  });
  const { createMessage } = useMessage();
  const labelCol = ref<any>({ xs: { span: 24 }, sm: { span: 7 } });
  const wrapperCol = ref<any>({ xs: { span: 24 }, sm: { span: 16 } });
  const confirmLoading = ref<boolean>(false);
  //表单验证
  const validatorRules = reactive({
    eventId: [{ required: true, message: '请输入事件ID!' }],
    startTime: [{ required: true, message: '请输入事件开始时间!' }],
    endTime: [{ required: true, message: '请输入事件结束时间!' }],
    ruleId: [{ required: true, message: '请输入UDE规则编码!' }],
    thingId: [{ required: true, message: '请输入实例ID!' }],
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
