<template>
  <a-spin :spinning="confirmLoading">
    <JFormContainer :disabled="disabled">
      <template #detail>
        <a-form ref="formRef" class="antd-modal-form" :labelCol="labelCol" :wrapperCol="wrapperCol" name="CollectTagForm">
          <a-row>
            <a-col :span="24">
              <a-form-item label="工厂/部门/产线" v-bind="validateInfos.factoryCode" id="OrsdataTagForm-factoryCode" name="factoryCode">
                <JDeptCascader
                  ref="deptCascaderRef"
                  v-model:modelValue="formData.factoryDeptCode"
                  :show-default="true"
                  @change="deptChangeCascader"
                />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="点位ID" v-bind="validateInfos.tagId" id="CollectTagForm-tagId" name="tagId">
                <a-input v-model:value="formData.tagId" placeholder="请输入点位ID" allow-clear />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="点位类型" v-bind="validateInfos.tagType" id="OrsdataTagForm-tagType" name="tagType">
                <j-dict-select-tag
                  v-model:value="formData.tagType"
                  dictCode="mes3_mdm_dict where group_code = 'EMS_TagType',item_key,item_val"
                  placeholder="请选择点位类型"
                  allow-clear
                />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="点位名称" v-bind="validateInfos.tagName" id="CollectTagForm-tagName" name="tagName">
                <a-input v-model:value="formData.tagName" placeholder="请输入点位名称" allow-clear />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="单位" v-bind="validateInfos.unit" id="CollectTagForm-unit" name="unit">
                <j-search-select v-model:value="formData.unit" dict="mes3_mdm_dict where group_code = 'EMS_Unit',item_key,item_val" allow-clear />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="采集编码" v-bind="validateInfos.pointCode" id="CollectTagForm-pointCode" name="pointCode">
                <a-input v-model:value="formData.pointCode" placeholder="请输入采集编码" allow-clear />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="采集周期" v-bind="validateInfos.collectPeriod" id="CollectTagForm-collectPeriod" name="collectPeriod">
                <j-dict-select-tag
                  type="radio"
                  v-model:value="formData.collectPeriod"
                  dictCode="mes3_mdm_dict where group_code = 'EMS_CollectPeriod',item_key,item_val"
                  placeholder="请选择采集周期"
                  allow-clear
                />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="值类型" v-bind="validateInfos.valType" id="CollectTagForm-valType" name="valType">
                <j-dict-select-tag
                  type="radio"
                  v-model:value="formData.valType"
                  dictCode="mes3_mdm_dict where group_code = 'EMS_ValType',item_key,item_val"
                  placeholder="请选择采集周期"
                  allow-clear
                />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="排序号" v-bind="validateInfos.sort" id="CollectTagForm-sort" name="sort">
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
  import JSearchSelect from '/@/components/Form/src/jeecg/components/JSearchSelect.vue';
  import JDeptCascader from '../../components/JDeptCascader.vue';
  import { getValueType } from '/@/utils';
  import { saveOrUpdate } from '../CollectTag.api';
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
    deptCode: '',
    lineCode: '',
    deviceId: undefined,
    pointCode: '',
    tagName: '',
    sort: undefined,
    tagType: '',
    collectPeriod: undefined,
    unit: '',
    tagId: '',
    valType: undefined,
  });
  const { createMessage } = useMessage();
  const labelCol = ref<any>({ xs: { span: 24 }, sm: { span: 5 } });
  const wrapperCol = ref<any>({ xs: { span: 24 }, sm: { span: 16 } });
  const confirmLoading = ref<boolean>(false);
  //表单验证
  const validatorRules = reactive({
    factoryCode: [{ required: true, message: '请输入工厂Code!' }],
    deptCode: [{ required: false, message: '请输入部门Code!' }],
    lineCode: [{ required: false, message: '请输入产线Code!' }],
    tagType: [{ required: true, message: '请输入能源类型!' }],
    collectPeriod: [{ required: true, message: '请选择采集周期!' }],
    unit: [{ required: true, message: '请输入单位!' }],
    tagId: [{ required: true, message: '请输入点位ID!' }],
    valType: [{ required: true, message: '请选择值类型!' }],
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
