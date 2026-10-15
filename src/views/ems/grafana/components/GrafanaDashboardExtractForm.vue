<template>
  <a-spin :spinning="confirmLoading">
    <JFormContainer :disabled="disabled">
      <template #detail>
        <a-form ref="formRef" class="antd-modal-form" :labelCol="labelCol" :wrapperCol="wrapperCol" name="GrafanaDashboardExtractForm">
          <a-row>
            <a-col :span="24">
              <a-form-item label="面板ID" v-bind="validateInfos.panelId" id="GrafanaDashboardExtractForm-panelId" name="panelId">
                <a-input v-model:value="formData.panelId" placeholder="请输入面板ID" allow-clear />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="面板标题" v-bind="validateInfos.panelTitle" id="GrafanaDashboardExtractForm-panelTitle" name="panelTitle">
                <a-input v-model:value="formData.panelTitle" placeholder="请输入面板标题" allow-clear />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="数据源类型" v-bind="validateInfos.dbType" id="GrafanaDashboardExtractForm-dbType" name="dbType">
                <j-dict-select-tag
                  v-model:value="formData.dbType"
                  dictCode="mes3_mdm_dict where group_code = 'GF_DBType',item_key,item_val"
                  placeholder="请选择数据源类型"
                  allow-clear
                />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="原始查询语句" v-bind="validateInfos.queryText" id="GrafanaDashboardExtractForm-queryText" name="queryText">
                <a-textarea v-model:value="formData.queryText" :rows="8" placeholder="请输入原始查询语句" />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="查询语句点位表" v-bind="validateInfos.pointIds" id="GrafanaDashboardExtractForm-pointIds" name="pointIds">
                <a-textarea v-model:value="formData.pointIds" :rows="4" placeholder="请输入查询语句点位表" />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="自动匹配点位建议" v-bind="validateInfos.autoMatch" id="GrafanaDashboardExtractForm-autoMatch" name="autoMatch">
                <a-textarea v-model:value="formData.autoMatch" :rows="4" placeholder="请输入自动匹配点位建议" />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="匹配状态" v-bind="validateInfos.matchState" id="GrafanaDashboardExtractForm-matchState" name="matchState">
                <j-dict-select-tag
                  v-model:value="formData.matchState"
                  dictCode="mes3_mdm_dict where group_code = 'GF_MatchState',item_key,item_val"
                  placeholder="请选择匹配状态"
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
  import JDictSelectTag from '/@/components/Form/src/jeecg/components/JDictSelectTag.vue';
  import { getValueType } from '/@/utils';
  import { saveOrUpdate } from '../GrafanaDashboardExtract.api';
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
    panelId: '',
    panelTitle: '',
    dbType: undefined,
    queryText: '',
    pointIds: '',
    autoMatch: '',
    matchState: undefined,
  });
  const { createMessage } = useMessage();
  const labelCol = ref<any>({ xs: { span: 24 }, sm: { span: 5 } });
  const wrapperCol = ref<any>({ xs: { span: 24 }, sm: { span: 16 } });
  const confirmLoading = ref<boolean>(false);
  //表单验证
  const validatorRules = reactive({
    panelId: [{ required: true, message: '请输入面板ID!' }],
    panelTitle: [{ required: true, message: '请输入面板标题!' }],
    dbType: [{ required: true, message: '请输入数据库类型(mysql influxdb sqlserver)!' }],
    matchState: [{ required: true, message: '请输入匹配状态(auto manual verified fail)!' }],
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
      if (tmpData['dbType'] !== null && tmpData['dbType'] !== undefined) {
        tmpData['dbType'] = String(tmpData['dbType']);
      }
      if (tmpData['matchState'] !== null && tmpData['matchState'] !== undefined) {
        tmpData['matchState'] = String(tmpData['matchState']);
      }
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
