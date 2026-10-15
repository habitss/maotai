<template>
  <a-spin :spinning="confirmLoading">
    <JFormContainer :disabled="disabled">
      <template #detail>
        <a-form ref="formRef" class="antd-modal-form" :labelCol="labelCol" :wrapperCol="wrapperCol" name="CollectErrorLogForm">
          <a-row>
            <a-col :span="24">
              <a-form-item label="工厂/部门/产线" v-bind="validateInfos.factoryCode" id="CollectErrorLogForm-factoryCode" name="factoryCode">
                <JDeptCascader
                  ref="deptCascaderRef"
                  v-model:modelValue="formData.factoryDeptCode"
                  :show-default="true"
                  @change="deptChangeCascader"
                />
              </a-form-item>
            </a-col>
            <a-col :span="24" v-if="false">
              <a-form-item label="报表" v-bind="validateInfos.tableId" id="CollectErrorLogForm-tableId" name="tableId">
                <j-dict-select-tag v-model:value="formData.tableId" dictCode="ems_custom_table,rep_name,id" placeholder="请选择报表" allow-clear />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="字段" v-if="false" v-bind="validateInfos.fieldCode" id="CollectErrorLogForm-fieldCode" name="fieldCode">
                <j-dict-select-tag
                  v-model:value="formData.fieldCode"
                  dictCode="ems_custom_field,field_name,id"
                  placeholder="请选择字段"
                  allow-clear
                />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="关联点位" v-bind="validateInfos.tagId" id="CollectErrorLogForm-tagId" name="tagId">
                <j-search-select
                  v-model:value="formData.tagId"
                  dictCode="mes3_pi_base_point WHERE energy_type != '',point_name,tag_id"
                  placeholder="请选择关联点位"
                  allow-clear
                />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="对应采集时间段" v-bind="validateInfos.collectTime" id="CollectErrorLogForm-collectTime" name="collectTime">
                <a-date-picker
                  placeholder="请选择对应采集时间段"
                  v-model:value="formData.collectTime"
                  showTime
                  value-format="YYYY-MM-DD HH:mm:ss"
                  style="width: 100%"
                  allow-clear
                />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="错误类型" v-bind="validateInfos.errorType" id="CollectErrorLogForm-errorType" name="errorType">
                <j-dict-select-tag
                  v-model:value="formData.errorType"
                  dictCode="mes3_mdm_dict where group_code = 'EMS_CollectErrorType',item_key,item_val"
                  placeholder="请选择错误类型"
                  allow-clear
                />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="详细错误信息" v-bind="validateInfos.errorMsg" id="CollectErrorLogForm-errorMsg" name="errorMsg">
                <a-textarea v-model:value="formData.errorMsg" :rows="4" allow-clear />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="当前重试次数" v-bind="validateInfos.retryCount" id="CollectErrorLogForm-retryCount" name="retryCount">
                <a-input-number v-model:value="formData.retryCount" placeholder="请输入当前重试次数" style="width: 100%" />
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
  import { saveOrUpdate } from '../CollectErrorLog.api';
  import { Form } from 'ant-design-vue';
  import JFormContainer from '/@/components/Form/src/container/JFormContainer.vue';
  import JSearchSelect from '/@/components/Form/src/jeecg/components/JSearchSelect.vue';
  import JDeptCascader from '../../components/JDeptCascader.vue';

  const props = defineProps({
    formDisabled: { type: Boolean, default: false },
    formBpm: { type: Boolean, default: true },
  });
  const formRef = ref();
  const deptCascaderRef = ref();
  const useForm = Form.useForm;
  const emit = defineEmits(['register', 'ok']);
  const formData = reactive<Record<string, any>>({
    id: '',
    factoryCode: '',
    deptCode: '',
    lineCode: '',
    tableId: undefined,
    fieldCode: '',
    tagId: undefined,
    collectTime: '',
    errorType: undefined,
    errorMsg: '',
    retryCount: undefined,
  });
  const { createMessage } = useMessage();
  const labelCol = ref<any>({ xs: { span: 24 }, sm: { span: 5 } });
  const wrapperCol = ref<any>({ xs: { span: 24 }, sm: { span: 16 } });
  const confirmLoading = ref<boolean>(false);
  //表单验证
  const validatorRules = reactive({
    factoryCode: [{ required: true, message: '请选择工厂/部门/产线!' }],
    deptCode: [{ required: false, message: '请选择部门!' }],
    lineCode: [{ required: false, message: '请选择产线!' }],
    tableId: [{ required: true, message: '请选择报表!' }],
    fieldCode: [{ required: true, message: '请选择字段!' }],
    tagId: [{ required: true, message: '请选择关联点位!' }],
    collectTime: [{ required: true, message: '请选择对应采集时间段!' }],
    errorType: [{ required: true, message: '请选择错误类型!' }],
    errorMsg: [{ required: true, message: '请输入详细错误信息!' }],
    retryCount: [{ required: true, message: '请输入当前重试次数!' }],
  });
  const { resetFields, validate, validateInfos } = useForm(formData, validatorRules, { immediate: false });

  // 表单禁用
  const disabled = computed(() => {
    if (props.formBpm === true) {
      return false;
    }
    return props.formDisabled;
  });

  //分级部门选择事件
  function deptChangeCascader(selectDepts) {
    console.log(selectDepts);
    if (selectDepts.length > 0) {
      formData.factoryCode = selectDepts[0];
      if (selectDepts.length > 1) {
        formData.deptCode = selectDepts[1];
        if (selectDepts.length > 2) {
          formData.lineCode = selectDepts[2];
        } else {
          formData.lineCode = '';
        }
      } else {
        formData.deptCode = '';
        formData.lineCode = '';
      }
    } else {
      formData.factoryCode = '';
      formData.deptCode = '';
      formData.lineCode = '';
    }
  }

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
      if (formData.errorType) {
        formData.errorType = String(formData.errorType);
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
