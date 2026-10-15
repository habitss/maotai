<template>
  <a-spin :spinning="confirmLoading">
    <JFormContainer :disabled="disabled">
      <template #detail>
        <a-form ref="formRef" class="antd-modal-form" :labelCol="labelCol" :wrapperCol="wrapperCol" name="CollectPointStatusForm">
          <a-row>
            <a-col :span="24">
              <a-form-item label="工厂/部门/产线" v-bind="validateInfos.factoryCode" id="CollectPointStatusForm-factoryCode" name="factoryCode">
                <JDeptCascader
                  class="my-disabled"
                  ref="deptCascaderRef"
                  v-model:modelValue="formData.factoryDeptCode"
                  :show-default="true"
                  disabled="disabled"
                  @change="deptChangeCascader"
                />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="关联点位" v-bind="validateInfos.tagId" id="CollectPointStatusForm-tagId" name="tagId">
                <j-search-select
                  v-model:value="formData.tagId"
                  dict="mes3_pi_base_point WHERE energy_type != '',point_name,tag_id"
                  placeholder="请选择关联点位"
                  allow-clear
                />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="对应采集时间段" v-bind="validateInfos.collectTime" id="CollectPointStatusForm-collectTime" name="collectTime">
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
              <a-form-item label="是否完成" v-bind="validateInfos.finishFlag" id="CollectPointStatusForm-finishFlag" name="finishFlag">
                <j-dict-select-tag
                  v-model:value="formData.finishFlag"
                  dictCode="mes3_mdm_dict where group_code = 'EMS_StatusFinishFlag',item_key,item_val"
                  placeholder="请选择是否完成"
                  allow-clear
                />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="参考值" v-bind="validateInfos.lastBaseVal" id="CollectPointStatusForm-lastBaseVal" name="lastBaseVal">
                <a-input-number v-model:value="formData.lastBaseVal" placeholder="请输入参考值" style="width: 100%" />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="是否异常" v-bind="validateInfos.errorFlag" id="CollectPointStatusForm-errorFlag" name="errorFlag">
                <j-dict-select-tag
                  v-model:value="formData.errorFlag"
                  dictCode="mes3_mdm_dict where group_code = 'EMS_CollectErrorFlag',item_key,item_val"
                  placeholder="请选择是否异常"
                  allow-clear
                />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="重试次数" v-bind="validateInfos.retryCount" id="CollectPointStatusForm-retryCount" name="retryCount">
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
  import { saveOrUpdate } from '../CollectPointStatus.api';
  import { Form } from 'ant-design-vue';
  import JFormContainer from '/@/components/Form/src/container/JFormContainer.vue';
  import JSearchSelect from '/@/components/Form/src/jeecg/components/JSearchSelect.vue';
  import JDeptCascader from '../../components/JDeptCascader.vue';

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
    tagId: undefined,
    collectTime: '',
    finishFlag: undefined,
    lastBaseVal: undefined,
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
    tableId: [{ required: false, message: '请输入报表ID!' }],
    fieldCode: [{ required: false, message: '请输入字段Code!' }],
    pointId: [{ required: true, message: '请输入关联点位!' }],
    collectTime: [{ required: true, message: '请输入对应采集时间段!' }],
    finishFlag: [{ required: true, message: '请选择是否完成!' }],
    lastBaseVal: [{ required: true, message: '请输入参考值!' }],
    errorFlag: [{ required: true, message: '请选择是否异常!' }],
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
      if (formData.finishFlag !== undefined) {
        formData.finishFlag = String(formData.finishFlag);
      }
      if (formData.errorFlag !== undefined) {
        formData.errorFlag = String(formData.errorFlag);
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
