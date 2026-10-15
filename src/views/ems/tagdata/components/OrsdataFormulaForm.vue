<template>
  <a-spin :spinning="confirmLoading">
    <JFormContainer :disabled="disabled">
      <template #detail>
        <a-form ref="formRef" class="antd-modal-form" :labelCol="labelCol" :wrapperCol="wrapperCol" name="OrsdataFormulaForm">
          <a-row>
            <a-col :span="24">
              <a-form-item label="点位名称" v-bind="validateInfos.tagId" id="OrsdataFormulaForm-tagId" name="tagId">
                <j-dict-select-tag v-model:value="formData.tagId" dictCode="ems_orsdata_tag,tag_name,tag_id" placeholder="请选择点位" disabled />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="版本名称" v-bind="validateInfos.verName" id="OrsdataFormulaForm-verName" name="verName">
                <a-input v-model:value="formData.verName" placeholder="请输入版本名称" allow-clear />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="版本描述" v-bind="validateInfos.verDesc" id="OrsdataFormulaForm-verDesc" name="verDesc">
                <a-input v-model:value="formData.verDesc" placeholder="请输入版本描述" allow-clear />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="生效日期" v-bind="validateInfos.effectSdate" id="OrsdataFormulaForm-effectSdate" name="effectSdate">
                <a-date-picker
                  placeholder="请选择生效日期"
                  v-model:value="formData.effectSdate"
                  value-format="YYYY-MM-DD"
                  style="width: 100%"
                  allow-clear
                />
              </a-form-item>
            </a-col>
            <a-col :span="24" v-if="formData.id">
              <a-form-item label="失效日期" v-bind="validateInfos.effectEdate" id="OrsdataFormulaForm-effectEdate">
                <a-input v-model:value="formData.effectEdate" disabled="disabled" />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="计算公式" v-bind="validateInfos.formulaOrigin" id="OrsdataFormulaForm-formulaOrigin" name="formulaOrigin">
                <a-input v-model:value="formData.formulaOrigin" placeholder="请输入原计算公式" disabled="disabled" />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="公式描述" v-bind="validateInfos.formulaDesc" id="OrsdataFormulaForm-formulaDesc" name="formulaDesc">
                <a-textarea v-model:value="formData.formulaDesc" placeholder="请输入公式描述" :rows="3" disabled="disabled" />
              </a-form-item>
            </a-col>
          </a-row>
        </a-form>

        <!-- 表达式编辑器 -->
        <ExpressionEditor ref="expEditorRef" :tagTypeList="tagTypeList" :expressValue="initFormulaExp" @on-change="handExpValueChange" />
      </template>
    </JFormContainer>
  </a-spin>
</template>

<script lang="ts" setup>
  import { ref, reactive, defineExpose, nextTick, defineProps, computed } from 'vue';
  import { useMessage } from '/@/hooks/web/useMessage';
  import JDictSelectTag from '/@/components/Form/src/jeecg/components/JDictSelectTag.vue';
  import { getValueType } from '/@/utils';
  import { saveOrUpdate } from '../OrsdataFormula.api';
  import { Form } from 'ant-design-vue';
  import JFormContainer from '/@/components/Form/src/container/JFormContainer.vue';
  import ExpressionEditor from '../../components/ExpressionEditor.vue';

  const props = defineProps({
    formDisabled: { type: Boolean, default: false },
    formBpm: { type: Boolean, default: true },
  });
  const formRef = ref();
  const expEditorRef = ref();
  const useForm = Form.useForm;
  const emit = defineEmits(['register', 'ok']);
  const formData = reactive<Record<string, any>>({
    id: '',
    factoryCode: '',
    deptCode: '',
    lineCode: '',
    tagId: '',
    verName: '',
    verDesc: '',
    effectSdate: '',
    effectEdate: '',
    formulaOrigin: '',
    formulaDesc: '',
    analyseFormula: '',
  });
  const { createMessage } = useMessage();
  const labelCol = ref<any>({ xs: { span: 24 }, sm: { span: 5 } });
  const wrapperCol = ref<any>({ xs: { span: 24 }, sm: { span: 16 } });
  const confirmLoading = ref<boolean>(false);
  //表单验证
  const validatorRules = reactive({
    factoryCode: [{ required: false, message: '请输入工厂!' }],
    deptCode: [{ required: false, message: '请输入部门!' }],
    lineCode: [{ required: false, message: '请输入产线!' }],
    tagId: [{ required: true, message: '请输入点位ID!' }],
    verName: [{ required: true, message: '请输入版本名称!' }],
    verDesc: [{ required: false, message: '请输入版本描述!' }],
    effectSdate: [{ required: true, message: '请输入开始日期!' }],
    formulaOrigin: [
      { required: true, message: '请输入原计算公式!' },
      {
        validator: (rule: any, value: string, callback: Function) => {
          console.log(rule);
          // 使用 validateFormula 方法检查 formulaOrigin 格式是否合法
          if (value && !validateFormula(value)) {
            callback(new Error('公式格式不合法!')); // 如果格式不合法，回调一个错误信息
          } else {
            callback(); // 如果合法，继续执行后续验证
          }
        },
      },
    ],
    formulaDesc: [{ required: false, message: '请输入公式描述!' }],
    analyseFormula: [{ required: false, message: '请输入解析计算公式!' }],
  });
  const { resetFields, validate, validateInfos } = useForm(formData, validatorRules, { immediate: false });

  // 表单禁用
  const disabled = computed(() => {
    if (props.formBpm === true) {
      return false;
    }
    return props.formDisabled;
  });

  const tagTypeList = ref<any[]>([]);
  const initFormulaExp = ref<string>('');

  /**
   * 新增
   */
  function add() {
    edit({}, []);
  }

  /**
   * 编辑
   */
  function edit(record, tagTList: any[]) {
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
      tagTypeList.value = Array.isArray(tagTList) ? [...tagTList] : [];
      //给公式编辑器一个初始值
      initFormulaExp.value = formData.formulaOrigin;
    });
  }
  /**
   * 编辑点位公式变化
   */
  function handExpValueChange(expFormula, expFormulaDesc) {
    formData.formulaOrigin = expFormula;
    formData.formulaDesc = expFormulaDesc;
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

  // 验证表达式是否合法
  function validateFormula(formula: string): boolean {
    // 替换所有[参数]格式为(1)
    const sanitizedFormula = formula.replace(/\[.+?\]/g, '(1)');

    try {
      // 替换所有'x'为合法的乘号'*'
      const evaluatedFormula = sanitizedFormula.replace(/x/g, '*');

      // 检查是否包含非法字符（在替换参数和乘号后）
      if (/[^0-9+\-*/().\s]/.test(evaluatedFormula)) {
        return false;
      }

      // 检查括号是否匹配
      let parenCount = 0;
      for (const char of evaluatedFormula) {
        if (char === '(') parenCount++;
        if (char === ')') parenCount--;
        if (parenCount < 0) return false;
      }
      if (parenCount !== 0) return false;

      // 检查相邻的数字或括号之间是否缺少运算符
      if (/(?:\d|\))\s*\(\s*|\)\s*(?:\(|\d)/.test(evaluatedFormula)) {
        return false;
      }

      // 尝试计算表达式
      const result = Function(`'use strict';return (${evaluatedFormula})`)();

      // 检查结果是否为有效数字
      return !isNaN(result) && isFinite(result);
    } catch (e) {
      console.error(e);
      return false;
    }
  }

  defineExpose({
    add,
    edit,
    submitForm,
  });
</script>

<style lang="less" scoped>
  @import url('/@/styles/disabled.less');
  .antd-modal-form {
    padding: 14px;
  }
</style>
