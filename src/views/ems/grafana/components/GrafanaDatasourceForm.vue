<template>
  <a-spin :spinning="confirmLoading">
    <JFormContainer :disabled="disabled">
      <template #detail>
        <a-form ref="formRef" class="antd-modal-form" :labelCol="labelCol" :wrapperCol="wrapperCol" name="GrafanaDatasourceForm">
          <a-row>
            <a-col :span="24">
              <a-form-item label="GF实例" v-bind="validateInfos.instanceId" id="GrafanaDatasourceForm-instanceId" name="instanceId">
                <j-dict-select-tag
                  v-model:value="formData.instanceId"
                  dictCode="grafana_instance,instance_name,id"
                  placeholder="请选择GF实例"
                  allow-clear
                />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="GF数据源类型" v-bind="validateInfos.type" id="GrafanaDatasourceForm-type" name="type">
                <j-dict-select-tag
                  v-model:value="formData.type"
                  dictCode="mes3_mdm_dict where group_code = 'GF_RDSType',item_key,item_val"
                  placeholder="请选择GF数据源类型"
                  allow-clear
                />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="GF数据源标识" v-bind="validateInfos.uid" id="GrafanaDatasourceForm-uid" name="uid">
                <a-input v-model:value="formData.uid" placeholder="请输入GF数据源标识" allow-clear />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="GF数据源名称" v-bind="validateInfos.name" id="GrafanaDatasourceForm-name" name="name">
                <a-input v-model:value="formData.name" placeholder="请输入GF数据源名称" allow-clear />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="GF数据源URL" v-bind="validateInfos.url" id="GrafanaDatasourceForm-url" name="url">
                <a-input v-model:value="formData.url" placeholder="请输入GF数据源URL" allow-clear />
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
  import { saveOrUpdate } from '../GrafanaDatasource.api';
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
    instanceId: '',
    type: '',
    uid: '',
    name: '',
    url: '',
  });
  const { createMessage } = useMessage();
  const labelCol = ref<any>({ xs: { span: 24 }, sm: { span: 5 } });
  const wrapperCol = ref<any>({ xs: { span: 24 }, sm: { span: 16 } });
  const confirmLoading = ref<boolean>(false);
  //表单验证
  const validatorRules = reactive({
    instanceId: [{ required: true, message: '请输入GF实例ID!' }],
    type: [{ required: true, message: '请输入GF数据源类型!' }],
    uid: [{ required: true, message: '请输入GF数据源标识!' }],
    name: [{ required: true, message: '请输入GF数据源名称!' }],
    url: [{ required: true, message: '请输入GF数据源URL!' }],
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
