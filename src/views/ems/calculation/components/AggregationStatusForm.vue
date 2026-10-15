<template>
  <a-spin :spinning="confirmLoading">
    <JFormContainer :disabled="disabled">
      <template #detail>
        <a-form ref="formRef" class="antd-modal-form" :labelCol="labelCol" :wrapperCol="wrapperCol" name="AggregationStatusForm">
          <a-row>
            <a-col :span="24">
              <a-form-item label="工厂Code" v-bind="validateInfos.factoryCode" id="AggregationStatusForm-factoryCode" name="factoryCode">
                <j-dict-select-tag
                  v-model:value="formData.factoryCode"
                  dictCode="sys_depart,depart_name,org_code"
                  placeholder="请选择工厂Code"
                  allow-clear
                />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="部门Code" v-bind="validateInfos.deptCode" id="AggregationStatusForm-deptCode" name="deptCode">
                <j-dict-select-tag
                  v-model:value="formData.deptCode"
                  dictCode="sys_depart,depart_name,org_code"
                  placeholder="请选择部门Code"
                  allow-clear
                />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="产线Code" v-bind="validateInfos.lineCode" id="AggregationStatusForm-lineCode" name="lineCode">
                <j-dict-select-tag
                  v-model:value="formData.lineCode"
                  dictCode="sys_depart,depart_name,org_code"
                  placeholder="请选择产线Code"
                  allow-clear
                />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="报表ID" v-bind="validateInfos.tableId" id="AggregationStatusForm-tableId" name="tableId">
                <j-dict-select-tag v-model:value="formData.tableId" dictCode="ems_custom_table,rep_name,id" placeholder="请选择报表ID" allow-clear />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="字段Code" v-bind="validateInfos.fieldCode" id="AggregationStatusForm-fieldCode" name="fieldCode">
                <j-dict-select-tag
                  v-model:value="formData.fieldCode"
                  dictCode="ems_custom_field,field_name,id"
                  placeholder="请选择字段Code"
                  allow-clear
                />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="关联点位ID" v-bind="validateInfos.tagId" id="AggregationStatusForm-tagId" name="tagId">
                <j-dict-select-tag v-model:value="formData.tagId" dictCode="" placeholder="请选择关联点位ID" allow-clear />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="粒度类型(小时/班/天/周/月)" v-bind="validateInfos.grainType" id="AggregationStatusForm-grainType" name="grainType">
                <j-dict-select-tag
                  v-model:value="formData.grainType"
                  dictCode="mes3_mdm_dict where group_code = 'EMS_GrainType',item_key,item_val"
                  placeholder="请选择粒度类型(小时/班/天/周/月)"
                  allow-clear
                />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="点位类型(collect/thirdparty/ors)" v-bind="validateInfos.tagKind" id="AggregationStatusForm-tagKind" name="tagKind">
                <j-dict-select-tag
                  v-model:value="formData.tagKind"
                  dictCode="mes3_mdm_dict where group_code = 'EMS_TagKind',item_key,item_val"
                  placeholder="请选择点位类型(collect/thirdparty/ors)"
                  allow-clear
                />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="时间点" v-bind="validateInfos.timePoint" id="AggregationStatusForm-timePoint" name="timePoint">
                <a-date-picker
                  placeholder="请选择时间点"
                  v-model:value="formData.timePoint"
                  showTime
                  value-format="YYYY-MM-DD HH:mm:ss"
                  style="width: 100%"
                  allow-clear
                />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item
                label="是否完成(0-未完成 1-完成)"
                v-bind="validateInfos.finishFlag"
                id="AggregationStatusForm-finishFlag"
                name="finishFlag"
              >
                <j-dict-select-tag
                  v-model:value="formData.finishFlag"
                  dictCode="mes3_mdm_dict where group_code = 'EMS_StatusFinishFlag',item_key,item_val"
                  placeholder="请选择是否完成(0-未完成 1-完成)"
                  allow-clear
                />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="是否异常(0-正常 1-异常)" v-bind="validateInfos.errorFlag" id="AggregationStatusForm-errorFlag" name="errorFlag">
                <j-dict-select-tag
                  v-model:value="formData.errorFlag"
                  dictCode="mes3_mdm_dict where group_code = 'EMS_CollectErrorFlag',item_key,item_val"
                  placeholder="请选择是否异常(0-正常 1-异常)"
                  allow-clear
                />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="重试次数" v-bind="validateInfos.retryCount" id="AggregationStatusForm-retryCount" name="retryCount">
                <a-input-number v-model:value="formData.retryCount" placeholder="请输入重试次数" style="width: 100%" />
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
  import { saveOrUpdate } from '../AggregationStatus.api';
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
    factoryCode: '',
    deptCode: '',
    lineCode: '',
    tableId: undefined,
    fieldCode: '',
    tagId: '',
    grainType: undefined,
    tagKind: undefined,
    timePoint: '',
    finishFlag: undefined,
    errorFlag: undefined,
    retryCount: undefined,
  });
  const { createMessage } = useMessage();
  const labelCol = ref<any>({ xs: { span: 24 }, sm: { span: 5 } });
  const wrapperCol = ref<any>({ xs: { span: 24 }, sm: { span: 16 } });
  const confirmLoading = ref<boolean>(false);
  //表单验证
  const validatorRules = reactive({
    factoryCode: [{ required: true, message: '请输入工厂Code!' }],
    deptCode: [{ required: true, message: '请输入部门Code!' }],
    lineCode: [{ required: true, message: '请输入产线Code!' }],
    tableId: [{ required: true, message: '请输入报表ID!' }],
    fieldCode: [{ required: true, message: '请输入字段Code!' }],
    tagId: [{ required: true, message: '请输入关联点位ID!' }],
    grainType: [{ required: true, message: '请输入粒度类型(小时/班/天/周/月)!' }],
    tagKind: [{ required: true, message: '请输入点位类型(collect/thirdparty/ors)!' }],
    timePoint: [{ required: true, message: '请输入时间点!' }],
    finishFlag: [{ required: true, message: '请输入是否完成(0-未完成 1-完成)!' }],
    errorFlag: [{ required: true, message: '请输入是否异常(0-正常 1-异常)!' }],
    retryCount: [{ required: true, message: '请输入重试次数!' }],
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
