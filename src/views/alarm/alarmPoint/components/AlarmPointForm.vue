<template>
  <a-spin :spinning="confirmLoading">
    <JFormContainer :disabled="disabled">
      <template #detail>
        <a-form ref="formRef" class="antd-modal-form" :labelCol="labelCol" :wrapperCol="wrapperCol" name="AlarmPointForm">
          <a-row>
            <a-col :span="24">
              <a-form-item label="部门区域" v-bind="validateInfos.factoryDeptCode" id="AlarmPointForm-factoryDeptCode" name="factoryDeptCode">
                <JDeptCascader
                  ref="deptCascaderRef"
                  v-model:modelValue="formData.factoryDeptCode"
                  :show-default="true"
                  @change="deptChangeCascader"
                />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="PI编码" v-bind="validateInfos.alarmPiCode" id="AlarmPointForm-alarmPiCode" name="alarmPiCode">
                <a-input v-model:value="formData.alarmPiCode" placeholder="请输入PI编码" allow-clear />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="报警名称" v-bind="validateInfos.alarmName" id="AlarmPointForm-alarmName" name="alarmName">
                <a-input v-model:value="formData.alarmName" placeholder="请输入报警名称" allow-clear />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="报警大类" v-bind="validateInfos.alarmCategory" id="AlarmPointForm-alarmCategory" name="alarmCategory">
                <a-select v-model:value="formData.alarmCategory" allowClear placeholder="请选择" style="width: 100%" @change="handleCategoryChange">
                  <a-select-option v-for="(d, index) in data.categoryOptions" :key="index" :value="d.typeName">
                    {{ d.typeName }}
                  </a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="报警小类" v-bind="validateInfos.alarmSubCategory" id="AlarmPointForm-alarmSubCategory" name="alarmSubCategory">
                <a-select v-model:value="formData.alarmSubCategory" allowClear placeholder="请选择" style="width: 100%">
                  <a-select-option v-for="(d, index) in data.subCategoryOptions" :key="index" :value="d.typeName">
                    {{ d.typeName }}
                  </a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item
                label="默认报警等级"
                v-bind="validateInfos.alarmLevelDefault"
                id="AlarmPointForm-alarmLevelDefault"
                name="alarmLevelDefault"
              >
                <a-input-number v-model:value="formData.alarmLevelDefault" placeholder="请输入默认报警等级" style="width: 100%" />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="功能位置编码" v-bind="validateInfos.funcLocCode" id="AlarmPointForm-funcLocCode" name="funcLocCode">
                <a-input v-model:value="formData.funcLocCode" placeholder="请输入功能位置编码" allow-clear />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="所属系统" v-bind="validateInfos.sourceSystem" id="AlarmPointForm-sourceSystem" name="sourceSystem">
                <a-input v-model:value="formData.sourceSystem" placeholder="请输入所属系统" allow-clear />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="是否启用" v-bind="validateInfos.enableFlag" id="AlarmPointForm-enableFlag" name="enableFlag">
                <j-dict-select-tag type="radio" v-model:value="formData.enableFlag" :options="yn" placeholder="请选择是否启用" allow-clear />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="备注" v-bind="validateInfos.remark" id="AlarmPointForm-remark" name="remark">
                <a-input v-model:value="formData.remark" placeholder="请输入备注" allow-clear />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="排序号" v-bind="validateInfos.sort" id="AlarmPointForm-sort" name="sort">
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
  import { ref, reactive, defineExpose, nextTick, defineProps, computed, onMounted } from 'vue';
  import { useMessage } from '/@/hooks/web/useMessage';
  import JDictSelectTag from '/@/components/Form/src/jeecg/components/JDictSelectTag.vue';
  import JDeptCascader from '../../../ems/components/JDeptCascader.vue';
  import { getValueType } from '/@/utils';
  import { saveOrUpdate } from '../AlarmPoint.api';
  import { yn } from '../AlarmPoint.data';
  import { Form } from 'ant-design-vue';
  import JFormContainer from '/@/components/Form/src/container/JFormContainer.vue';
  import { listOptions as listAlaryType } from '../../alarmTypes/AlarmTypes.api';
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
    alarmPiCode: '',
    alarmName: '',
    alarmCategory: '',
    alarmSubCategory: '',
    alarmLevelDefault: undefined,
    funcLocCode: '',
    sourceSystem: '',
    enableFlag: undefined,
    remark: '',
    sort: undefined,
  });
  const { createMessage } = useMessage();
  const labelCol = ref<any>({ xs: { span: 24 }, sm: { span: 5 } });
  const wrapperCol = ref<any>({ xs: { span: 24 }, sm: { span: 16 } });
  const confirmLoading = ref<boolean>(false);
  //表单验证
  const validatorRules = reactive({
    factoryDeptCode: [{ required: true, message: '请选择部门区域!' }],
    alarmPiCode: [{ required: true, message: '请输入PI编码!' }],
    alarmName: [{ required: true, message: '请输入报警名称!' }],
    alarmCategory: [{ required: true, message: '请输入报警大类!' }],
    alarmLevelDefault: [{ required: true, message: '请输入默认报警等级!' }],
    funcLocCode: [{ required: true, message: '请输入功能位置编码!' }],
    sourceSystem: [{ required: true, message: '请输入所属系统!' }],
    enableFlag: [{ required: true, message: '请输入是否启用!' }],
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
   * 数据
   */
  const data = reactive({
    categoryOptions: [] as any,
    subCategoryOptions: [] as any,
  });

  onMounted(() => {
    listCategory();
  });

  //大类选项
  async function listCategory() {
    const params = { parentId: 0 };
    const res = await listAlaryType(params);
    data.categoryOptions = res;
  }

  //小类选项
  async function listSubCategory(pid) {
    let options = [] as any;
    if (pid) {
      const params = { parentId: pid };
      const res = await listAlaryType(params);
      options = res;
    }
    data.subCategoryOptions = options;
  }

  /**
   * 新增
   */
  function add() {
    formData.factoryDeptCode = 'X';
    edit({});
  }

  /**
   * 编辑
   */
  function edit(record) {
    handleCategoryChange(record.alarmCategory);

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

  async function handleCategoryChange(val) {
    formData.alarmSubCategory = '';
    const category = data.categoryOptions.find((item) => item.typeName === val);
    const pid = category?.id ?? null;
    listSubCategory(pid);
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
