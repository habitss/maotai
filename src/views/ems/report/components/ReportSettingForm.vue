<template>
  <a-spin :spinning="confirmLoading">
    <JFormContainer :disabled="disabled">
      <template #detail>
        <a-form ref="formRef" class="antd-modal-form" :labelCol="labelCol" :wrapperCol="wrapperCol" name="ReportSettingForm">
          <a-row>
            <a-col :span="24">
              <a-form-item label="工厂/部门/产线" v-bind="validateInfos.factoryCode" id="ReportSettingForm-factoryCode" name="factoryCode">
                <JDeptCascader v-model:modelValue="formData.factoryDeptCode" :show-default="true" @change="deptChangeCascader" />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="报表分类" v-bind="validateInfos.repType" id="ReportSettingForm-repType" name="repType">
                <j-dict-select-tag
                  v-model:value="formData.repType"
                  dictCode="mes3_mdm_dict where group_code = 'EMS_ReportType',item_key,item_val"
                  placeholder="请选择报表分类"
                  allow-clear
                />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="报表名称" v-bind="validateInfos.repName" id="ReportSettingForm-repName" name="repName">
                <a-input v-model:value="formData.repName" placeholder="请输入报表名称" allow-clear />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="报表图标" v-bind="validateInfos.iconName" id="ReportSettingForm-iconName" name="iconName">
                <a-radio-group v-model:value="formData.iconName" style="border-radius: 0px">
                  <a-radio-button v-for="item in repIconList" :key="item.id" :value="item.id" :title="item.label" class="customRadioBtn">
                    <template #default>
                      <img :src="getPngIcon(item.id)" :alt="item.label" />
                    </template>
                  </a-radio-button>
                </a-radio-group>
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="排序号" v-bind="validateInfos.sort" id="ReportSettingForm-sort" name="sort">
                <a-input-number v-model:value="formData.sort" placeholder="请输入排序号" style="width: 100%" />
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
  import { saveOrUpdate, getPngIcon } from '../ReportSetting.api';
  import { repIconList } from '../ReportSetting.data';
  import { Form } from 'ant-design-vue';
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
    factoryDeptCode: '',
    factoryCode: '',
    deptCode: '',
    lineCode: '',
    repType: undefined,
    repName: '',
    iconName: '',
    sort: undefined,
  });
  const { createMessage } = useMessage();
  const labelCol = ref<any>({ xs: { span: 24 }, sm: { span: 5 } });
  const wrapperCol = ref<any>({ xs: { span: 24 }, sm: { span: 16 } });
  const confirmLoading = ref<boolean>(false);
  //表单验证
  const validatorRules = reactive({
    factoryCode: [{ required: true, message: '请选择工厂/部门/产线!' }],
    deptCode: [{ required: false, message: '请输入部门!' }],
    lineCode: [{ required: false, message: '请输入产线!' }],
    repType: [{ required: true, message: '请输入报表分类!' }],
    repName: [{ required: true, message: '请输入报表名称!' }],
    iconName: [{ required: true, message: '请输入报表图标!' }],
    sort: [{ required: true, message: '请输入排序号!' }],
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
      //整数无法反选常量下拉框
      if (formData.repType || formData.repType === 0) {
        formData.repType = String(formData.repType);
      }
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
      const error = err as { errorFields?: Array<any> }; // 类型断言
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
    .customRadioBtn {
      width: 60px;
      height: 60px;
      line-height: 60px;
      margin-bottom: 4px;
    }
    .ant-radio-button-wrapper:first-child {
      border-start-start-radius: 0px;
      border-end-start-radius: 0px;
    }
    .ant-radio-button-wrapper:last-child {
      border-start-end-radius: 0px;
      border-end-end-radius: 0px;
    }
    /deep/ .ant-radio-button-checked {
      border: 2px solid #d92622;
    }
  }
</style>
