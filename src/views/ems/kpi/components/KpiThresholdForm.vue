<template>
  <a-spin :spinning="confirmLoading">
    <JFormContainer :disabled="disabled">
      <template #detail>
        <a-form ref="formRef" class="antd-modal-form" :labelCol="labelCol" :wrapperCol="wrapperCol" name="KpiThresholdForm">
          <a-row>
            <a-col :span="24">
              <a-form-item label="KPI点ID" v-bind="validateInfos.kpiTagId" id="KpiThresholdForm-kpiTagId" name="kpiTagId">
                <a-input-number v-model:value="formData.kpiTagId" placeholder="请输入KPI点ID" style="width: 100%" />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="点位大类" v-bind="validateInfos.tagKind" id="KpiThresholdForm-tagKind" name="tagKind">
                <a-input-number v-model:value="formData.tagKind" placeholder="请选择点位大类" style="width: 100%" />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="点位ID" v-bind="validateInfos.tagId" id="KpiThresholdForm-tagId" name="tagId">
                <a-input v-model:value="formData.tagId" placeholder="请输入点位ID" allow-clear />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="生效月份" v-bind="validateInfos.yearmonth" id="KpiThresholdForm-yearmonth" name="yearmonth">
                <a-input v-model:value="formData.yearmonth" placeholder="请选择生效月份" allow-clear />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="红色下线" v-bind="validateInfos.rlbelow" id="KpiThresholdForm-rlbelow" name="rlbelow">
                <a-input-number v-model:value="formData.rlbelow" placeholder="请输入红色下线" style="width: 100%" />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="黄色上线" v-bind="validateInfos.ylover" id="KpiThresholdForm-ylover" name="ylover">
                <a-input-number v-model:value="formData.ylover" placeholder="请输入黄色上线" style="width: 100%" />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="红色上线" v-bind="validateInfos.rlover" id="KpiThresholdForm-rlover" name="rlover">
                <a-input-number v-model:value="formData.rlover" placeholder="请输入红色上线" style="width: 100%" />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="黄色下线" v-bind="validateInfos.ylbelow" id="KpiThresholdForm-ylbelow" name="ylbelow">
                <a-input-number v-model:value="formData.ylbelow" placeholder="请输入黄色下线" style="width: 100%" />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="标准值" v-bind="validateInfos.standard" id="KpiThresholdForm-standard" name="standard">
                <a-input-number v-model:value="formData.standard" placeholder="请输入标准值" style="width: 100%" />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="KPI阈值类型" v-bind="validateInfos.vmold" id="KpiThresholdForm-vmold" name="vmold">
                <j-dict-select-tag
                  v-model:value="formData.vmold"
                  dictCode="mes3_mdm_dict where group_code = 'EMS_Vmold',item_key,item_val"
                  placeholder="请选择KPI阈值类型"
                  allow-clear
                />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="产量" v-bind="validateInfos.yields" id="KpiThresholdForm-yields" name="yields">
                <a-input-number v-model:value="formData.yields" placeholder="请输入产量" style="width: 100%" />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="BU用量" v-bind="validateInfos.buUsage" id="KpiThresholdForm-buUsage" name="buUsage">
                <a-input-number v-model:value="formData.buUsage" placeholder="请输入BU用量" style="width: 100%" />
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
  import { saveOrUpdate } from '../KpiThreshold.api';
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
    kpiTagId: undefined,
    tagKind: undefined,
    tagId: '',
    yearmonth: '',
    rlbelow: undefined,
    ylover: undefined,
    rlover: undefined,
    ylbelow: undefined,
    standard: undefined,
    vmold: '',
    yields: undefined,
    buUsage: undefined,
  });
  const { createMessage } = useMessage();
  const labelCol = ref<any>({ xs: { span: 24 }, sm: { span: 5 } });
  const wrapperCol = ref<any>({ xs: { span: 24 }, sm: { span: 16 } });
  const confirmLoading = ref<boolean>(false);
  //表单验证
  const validatorRules = reactive({
    kpiTagId: [{ required: true, message: '请输入KPI点ID!' }],
    tagKind: [{ required: true, message: '请选择点位大类!' }],
    tagId: [{ required: true, message: '请输入点位ID!' }],
    yearmonth: [{ required: true, message: '请选择生效月份!' }],
    rlbelow: [{ required: true, message: '请输入红色下线!' }],
    ylover: [{ required: true, message: '请输入黄色上线!' }],
    rlover: [{ required: true, message: '请输入红色上线!' }],
    ylbelow: [{ required: true, message: '请输入黄色下线!' }],
    standard: [{ required: true, message: '请输入标准值!' }],
    vmold: [{ required: true, message: '请输入KPI阈值类型!' }],
    yields: [{ required: true, message: '请输入产量!' }],
    buUsage: [{ required: true, message: '请输入BU用量!' }],
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
