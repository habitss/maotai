<template>
  <a-spin :spinning="confirmLoading">
    <JFormContainer :disabled="disabled">
      <template #detail>
        <a-form ref="formRef" class="antd-modal-form" :labelCol="labelCol" :wrapperCol="wrapperCol" name="CalcWarningLogForm">
          <a-row>
            <a-col :span="24">
              <a-form-item label="部门" v-bind="validateInfos.deptCode" id="CalcWarningLogForm-deptCode" name="deptCode">
                <j-dict-select-tag
                  v-model:value="formData.deptCode"
                  dictCode="sys_depart,depart_name,org_code"
                  placeholder="请选择部门"
                  allow-clear
                />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="点位大类" v-bind="validateInfos.tagKind" id="CalcWarningLogForm-tagKind" name="tagKind">
                <j-dict-select-tag
                  v-model:value="formData.tagKind"
                  dictCode="mes3_mdm_dict where group_code = 'EMS_TagKind',item_key,item_val"
                  placeholder="请选择点位大类"
                  allow-clear
                  @change="handleTagKindChange"
                />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="关联点位" v-bind="validateInfos.tagId" id="CalcWarningLogForm-tagId" name="tagId">
                <j-dict-select-tag v-model:value="formData.tagId" :dictCode="tagIdDictCode" placeholder="请选择关联点位" allow-clear />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="时间周期" v-bind="validateInfos.grainType" id="CalcWarningLogForm-grainType" name="grainType">
                <j-dict-select-tag
                  v-model:value="formData.grainType"
                  dictCode="mes3_mdm_dict where group_code = 'EMS_GrainType',item_key,item_val"
                  placeholder="请选择时间周期"
                  allow-clear
                />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="预警等级" v-bind="validateInfos.warnLevel" id="CalcWarningLogForm-warnLevel" name="warnLevel">
                <j-dict-select-tag
                  v-model:value="formData.warnLevel"
                  dictCode="mes3_mdm_dict where group_code = 'EMS_WarnLevel',item_key,item_val"
                  placeholder="请选择预警等级"
                  allow-clear
                />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="时间点" v-bind="validateInfos.timePoint" id="CalcWarningLogForm-timePoint" name="timePoint">
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
              <a-form-item label="预警详情" v-bind="validateInfos.warnMsg" id="CalcWarningLogForm-warnMsg" name="warnMsg">
                <a-textarea v-model:value="formData.warnMsg" :rows="4" placeholder="请输入预警详情" />
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
  import { saveOrUpdate } from '../CalcWarningLog.api';
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
    tagId: '',
    grainType: undefined,
    tagKind: undefined,
    timePoint: '',
    warnMsg: '',
    warnLevel: undefined,
  });
  const { createMessage } = useMessage();
  const labelCol = ref<any>({ xs: { span: 24 }, sm: { span: 5 } });
  const wrapperCol = ref<any>({ xs: { span: 24 }, sm: { span: 16 } });
  const confirmLoading = ref<boolean>(false);
  //表单验证
  const validatorRules = reactive({
    factoryCode: [{ required: false, message: '请输入工厂!' }],
    deptCode: [{ required: true, message: '请输入部门!' }],
    lineCode: [{ required: false, message: '请输入产线!' }],
    tagId: [{ required: true, message: '请输入关联点位!' }],
    grainType: [{ required: true, message: '请输入时间周期!' }],
    tagKind: [{ required: true, message: '请输入点位大类!' }],
    timePoint: [{ required: true, message: '请输入时间点!' }],
    warnMsg: [{ required: true, message: '请输入预警详情!' }],
    warnLevel: [{ required: true, message: '请输入预警等级!' }],
  });
  const { resetFields, validate, validateInfos } = useForm(formData, validatorRules, { immediate: false });
  //动态获取字典
  const tagIdDictCode = ref<string>("mes3_pi_base_point,point_name,tag_id,energy_type!=''");

  // 表单禁用
  const disabled = computed(() => {
    if (props.formBpm === true) {
      return false;
    }
    return props.formDisabled;
  });

  // 处理点位大类变化
  const handleTagKindChange = (value) => {
    if (value === '1') {
      tagIdDictCode.value = "mes3_pi_base_point,point_name,tag_id,energy_type!=''";
    } else if (value === '2') {
      tagIdDictCode.value = 'ems_manual_tag,tag_name,tag_id';
    } else if (value === '3') {
      tagIdDictCode.value = 'ems_thirdparty_tag,tag_name,tag_id';
    } else {
      tagIdDictCode.value = 'ems_orsdata_tag,tag_name,tag_id';
    }
  };

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
      if (formData.grainType) {
        formData.grainType = String(formData.grainType);
      }
      if (formData.tagKind) {
        formData.tagKind = String(formData.tagKind);
      }
      if (formData.warnLevel) {
        formData.warnLevel = String(formData.warnLevel);
      }
      //处理点位大类变化
      handleTagKindChange(formData.tagKind);
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
