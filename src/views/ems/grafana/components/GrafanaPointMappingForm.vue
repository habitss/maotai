<template>
  <a-spin :spinning="confirmLoading">
    <JFormContainer :disabled="disabled">
      <template #detail>
        <a-form ref="formRef" class="antd-modal-form" :labelCol="labelCol" :wrapperCol="wrapperCol" name="GrafanaPointMappingForm">
          <a-row>
            <a-col :span="24">
              <a-form-item label="旧厂点位ID或字段" v-bind="validateInfos.oldPointId" id="GrafanaPointMappingForm-oldPointId" name="oldPointId">
                <a-input v-model:value="formData.oldPointId" placeholder="请输入旧厂点位ID或字段" allow-clear />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item
                label="旧厂点位中文描述(若有)"
                v-bind="validateInfos.oldPointName"
                id="GrafanaPointMappingForm-oldPointName"
                name="oldPointName"
              >
                <a-input v-model:value="formData.oldPointName" placeholder="请输入旧厂点位中文描述(若有)" allow-clear />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item
                label="新厂候选点位ID(确认后写入)"
                v-bind="validateInfos.newPointId"
                id="GrafanaPointMappingForm-newPointId"
                name="newPointId"
              >
                <a-input v-model:value="formData.newPointId" placeholder="请输入新厂候选点位ID(确认后写入)" allow-clear />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="新厂点位中文描述" v-bind="validateInfos.newPointName" id="GrafanaPointMappingForm-newPointName" name="newPointName">
                <a-input v-model:value="formData.newPointName" placeholder="请输入新厂点位中文描述" allow-clear />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="匹配得分(0 ~ 1)" v-bind="validateInfos.score" id="GrafanaPointMappingForm-score" name="score">
                <a-input-number v-model:value="formData.score" placeholder="请输入匹配得分(0 ~ 1)" style="width: 100%" />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item
                label="匹配度(high medium low)"
                v-bind="validateInfos.confidence"
                id="GrafanaPointMappingForm-confidence"
                name="confidence"
              >
                <j-dict-select-tag
                  v-model:value="formData.confidence"
                  dictCode="mes3_mdm_dict where group_code = 'GF_Confidence',item_key,item_val"
                  placeholder="请选择匹配度(high medium low)"
                  allow-clear
                />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item
                label="匹配类型(auto manual)"
                v-bind="validateInfos.matchedFlag"
                id="GrafanaPointMappingForm-matchedFlag"
                name="matchedFlag"
              >
                <j-dict-select-tag
                  v-model:value="formData.matchedFlag"
                  dictCode="mes3_mdm_dict where group_code = 'GF_MatchedFlag',item_key,item_val"
                  placeholder="请选择匹配类型(auto manual)"
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
  import { saveOrUpdate } from '../GrafanaPointMapping.api';
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
    oldPointId: '',
    oldPointName: '',
    newPointId: '',
    newPointName: '',
    score: undefined,
    confidence: undefined,
    matchedFlag: undefined,
  });
  const { createMessage } = useMessage();
  const labelCol = ref<any>({ xs: { span: 24 }, sm: { span: 5 } });
  const wrapperCol = ref<any>({ xs: { span: 24 }, sm: { span: 16 } });
  const confirmLoading = ref<boolean>(false);
  //表单验证
  const validatorRules = reactive({
    oldPointId: [{ required: true, message: '请输入旧厂点位ID或字段!' }],
    oldPointName: [{ required: true, message: '请输入旧厂点位中文描述(若有)!' }],
    newPointId: [{ required: true, message: '请输入新厂候选点位ID(确认后写入)!' }],
    newPointName: [{ required: true, message: '请输入新厂点位中文描述!' }],
    score: [{ required: true, message: '请输入匹配得分(0 ~ 1)!' }],
    confidence: [{ required: true, message: '请输入匹配度(high medium low)!' }],
    matchedFlag: [{ required: true, message: '请输入匹配类型(auto manual)!' }],
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
