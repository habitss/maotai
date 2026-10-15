<template>
  <a-spin :spinning="confirmLoading">
    <JFormContainer :disabled="disabled">
      <template #detail>
        <a-form ref="formRef" class="antd-modal-form" :labelCol="labelCol" :wrapperCol="wrapperCol" name="ShiftDayPeriodSetForm">
          <a-row>
            <a-col :span="24">
              <a-form-item label="工厂/部门/产线" v-bind="validateInfos.factoryCode" id="ShiftDayPeriodSetForm-factoryCode" name="factoryCode">
                <JDeptCascader
                  ref="deptCascaderRef"
                  v-model:modelValue="formData.factoryDeptCode"
                  :show-default="true"
                  @change="deptChangeCascader"
                />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="时间区间" v-bind="validateInfos.dataPeriod" id="ShiftDayPeriodSetForm-dataPeriod" name="dataPeriod">
                <j-dict-select-tag
                  v-model:value="formData.dataPeriod"
                  dictCode="mes3_mdm_dict where group_code = 'EMS_RepDataPeriod',item_key,item_val"
                  placeholder="请选择时间区间"
                  allow-clear
                />
              </a-form-item>
            </a-col>
            <a-col :span="24" v-if="formData.dataPeriod === '1'">
              <a-form-item label="班别" v-bind="validateInfos.shift" id="ShiftDayPeriodSetForm-shift" name="shift">
                <j-dict-select-tag
                  v-model:value="formData.shift"
                  dictCode="mes3_mdm_dict where group_code = 'EMS_ShiftType',item_key,item_val"
                  placeholder="请选择班别"
                  allow-clear
                />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="结束时间点" v-bind="validateInfos.hour" id="ShiftDayPeriodSetForm-hour" name="hour">
                <a-input-number v-model:value="formData.hour" placeholder="请输入结束时间点" style="width: 100%" />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="区间时长(h)" v-bind="validateInfos.periodTime" id="ShiftDayPeriodSetForm-periodTime" name="periodTime">
                <a-input-number v-model:value="formData.periodTime" placeholder="请输入区间时长(h)" style="width: 100%" />
              </a-form-item>
            </a-col>
          </a-row>
        </a-form>
      </template>
    </JFormContainer>
  </a-spin>
</template>

<script lang="ts" setup>
  import { ref, reactive, defineExpose, nextTick, defineProps, computed, watch } from 'vue';
  import { useMessage } from '/@/hooks/web/useMessage';
  import JDictSelectTag from '/@/components/Form/src/jeecg/components/JDictSelectTag.vue';
  import { getValueType } from '/@/utils';
  import { saveOrUpdate } from '../ShiftDayPeriodSet.api';
  import { Form } from 'ant-design-vue';
  import type { Rule } from 'ant-design-vue/es/form';
  import JFormContainer from '/@/components/Form/src/container/JFormContainer.vue';
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
    factoryDeptCode: '',
    deptCode: '',
    lineCode: '',
    dataPeriod: undefined,
    shift: undefined,
    hour: undefined,
    periodTime: undefined,
  });
  const { createMessage } = useMessage();
  const labelCol = ref<any>({ xs: { span: 24 }, sm: { span: 5 } });
  const wrapperCol = ref<any>({ xs: { span: 24 }, sm: { span: 16 } });
  const confirmLoading = ref<boolean>(false);
  //表单验证
  const validatorRules: Partial<Record<string, Rule[]>> = reactive({
    factoryCode: [{ required: true, message: '请选择工厂/部门/产线!' }],
    deptCode: [{ required: false, message: '请选择部门!' }],
    lineCode: [{ required: false, message: '请选择产线!' }],
    dataPeriod: [{ required: true, message: '请选择时间区间!' }],
    shift: [],
    hour: [{ required: true, message: '请输入结束时间点!' }],
    periodTime: [{ required: true, message: '请输入区间时长(h)!' }],
  });
  const { resetFields, validate, validateInfos } = useForm(formData, validatorRules, { immediate: false });

  // 表单禁用
  const disabled = computed(() => {
    if (props.formBpm === true) {
      return false;
    }
    return props.formDisabled;
  });

  watch(
    () => formData.dataPeriod,
    (val) => {
      if (val === '1') {
        validatorRules.shift = [{ required: true, message: '请选择班别!' }];
      } else {
        validatorRules.shift = [];
        formData.shift = undefined;
      }
    }
  );

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
      formData.factoryDeptCode = formData.lineCode || formData.deptCode || formData.factoryCode;
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
