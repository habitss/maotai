<template>
  <a-spin :spinning="confirmLoading">
    <JFormContainer :disabled="disabled">
      <template #detail>
        <a-form ref="formRef" class="antd-modal-form" :labelCol="labelCol" :wrapperCol="wrapperCol" name="GrafanaMigrationLogForm">
          <a-row>
            <a-col :span="24">
              <a-form-item
                label="源GF实例"
                v-bind="validateInfos.sourceInstanceId"
                id="GrafanaMigrationLogForm-sourceInstanceId"
                name="sourceInstanceId"
              >
                <j-dict-select-tag
                  v-model:value="formData.sourceInstanceId"
                  dictCode="grafana_instance,instance_name,id"
                  placeholder="请选择源GF实例"
                  allow-clear
                />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item
                label="目标GF实例ID"
                v-bind="validateInfos.targetInstanceId"
                id="GrafanaMigrationLogForm-targetInstanceId"
                name="targetInstanceId"
              >
                <j-dict-select-tag
                  v-model:value="formData.targetInstanceId"
                  dictCode="grafana_instance,instance_name,id"
                  placeholder="请选择目标GF实例"
                  allow-clear
                />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="报表名称" v-bind="validateInfos.dashboardTitle" id="GrafanaMigrationLogForm-dashboardTitle" name="dashboardTitle">
                <a-input v-model:value="formData.dashboardTitle" placeholder="请输入报表名称" allow-clear />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="发布状态" v-bind="validateInfos.deployState" id="GrafanaMigrationLogForm-deployState" name="deployState">
                <j-dict-select-tag
                  v-model:value="formData.deployState"
                  dictCode="mes3_mdm_dict where group_code = 'GF_DeplyState',item_key,item_val"
                  placeholder="请选择发布状态"
                  allow-clear
                />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="错误信息" v-bind="validateInfos.errorMsg" id="GrafanaMigrationLogForm-errorMsg" name="errorMsg">
                <a-input v-model:value="formData.errorMsg" placeholder="请输入错误信息" allow-clear />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="目标新报表ID" v-bind="validateInfos.targetUid" id="GrafanaMigrationLogForm-targetUid" name="targetUid">
                <a-input v-model:value="formData.targetUid" placeholder="请输入目标新报表ID" allow-clear />
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
  import { saveOrUpdate } from '../GrafanaMigrationLog.api';
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
    sourceInstanceId: '',
    targetInstanceId: '',
    dashboardTitle: '',
    deployState: undefined,
    errorMsg: '',
    targetUid: '',
  });
  const { createMessage } = useMessage();
  const labelCol = ref<any>({ xs: { span: 24 }, sm: { span: 5 } });
  const wrapperCol = ref<any>({ xs: { span: 24 }, sm: { span: 16 } });
  const confirmLoading = ref<boolean>(false);
  //表单验证
  const validatorRules = reactive({
    sourceInstanceId: [{ required: true, message: '请输入源GF实例ID!' }],
    targetInstanceId: [{ required: true, message: '请输入目标GF实例ID!' }],
    dashboardTitle: [{ required: true, message: '请输入报表名称!' }],
    deployState: [{ required: true, message: '请输入发布状态!' }],
    errorMsg: [{ required: true, message: '请输入错误信息!' }],
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
      if (tmpData['deployState'] !== null && tmpData['deployState'] !== undefined) {
        tmpData['deployState'] = String(tmpData['deployState']);
      }
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
