<template>
  <a-spin :spinning="confirmLoading">
    <JFormContainer :disabled="disabled">
      <template #detail>
        <a-form ref="formRef" class="antd-modal-form" :labelCol="labelCol" :wrapperCol="wrapperCol" name="LineEfficiencyHandleForm">
          <a-row>
            <a-col :span="24">
              <a-form-item label="产线编码" v-bind="validateInfos.lineCode" id="LineEfficiencyHandleForm-lineCode" name="lineCode">
                <a-input v-model:value="formData.lineCode" placeholder="请输入产线编码" allow-clear></a-input>
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="产线名称" v-bind="validateInfos.lineName" id="LineEfficiencyHandleForm-lineName" name="lineName">
                <a-input v-model:value="formData.lineName" placeholder="请输入产线名称" allow-clear></a-input>
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="设备编码" v-bind="validateInfos.sbId" id="LineEfficiencyHandleForm-sbId" name="sbId">
                <a-input v-model:value="formData.sbId" placeholder="请输入设备编码" allow-clear></a-input>
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="设备名称" v-bind="validateInfos.sbName" id="LineEfficiencyHandleForm-sbName" name="sbName">
                <a-input v-model:value="formData.sbName" placeholder="请输入设备名称" allow-clear></a-input>
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="产量点位通道号" v-bind="validateInfos.pointCode" id="LineEfficiencyHandleForm-pointCode" name="pointCode">
                <a-input v-model:value="formData.pointCode" placeholder="请输入产量点位通道号" allow-clear></a-input>
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="产量点位名称" v-bind="validateInfos.pointName" id="LineEfficiencyHandleForm-pointName" name="pointName">
                <a-input v-model:value="formData.pointName" placeholder="请输入产量点位名称" allow-clear></a-input>
              </a-form-item>
            </a-col>

            <a-col :span="24">
              <a-form-item
                label="酒机运行信号点位通道号"
                v-bind="validateInfos.pointCodeNew"
                id="LineEfficiencyHandleForm-pointCodeNew"
                name="pointCodeNew"
              >
                <a-input v-model:value="formData.pointCodeNew" placeholder="请输入酒机运行信号点位通道号" allow-clear></a-input>
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item
                label="酒机运行信号点位名称"
                v-bind="validateInfos.pointNameNew"
                id="LineEfficiencyHandleForm-pointNameNew"
                name="pointNameNew"
              >
                <a-input v-model:value="formData.pointNameNew" placeholder="请输入酒机运行信号点位名称" allow-clear></a-input>
              </a-form-item>
            </a-col>

            <a-col :span="24">
              <a-form-item label="CIP运行信号点位通道号" v-bind="validateInfos.cipCode" id="LineEfficiencyHandleForm-cipCode" name="cipCode">
                <a-input v-model:value="formData.cipCode" placeholder="请输入CIP运行信号点位通道号" allow-clear></a-input>
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="CIP运行信号点位名称" v-bind="validateInfos.cipName" id="LineEfficiencyHandleForm-cipName" name="cipName">
                <a-input v-model:value="formData.cipName" placeholder="请输入CIP运行信号点位名称" allow-clear></a-input>
              </a-form-item>
            </a-col>

            <a-col :span="24">
              <a-form-item label="进酒温度点位通道号" v-bind="validateInfos.jjwdCode" id="LineEfficiencyHandleForm-jjwdCode" name="jjwdCode">
                <a-input v-model:value="formData.jjwdCode" placeholder="请输入进酒温度点位通道号" allow-clear></a-input>
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="进酒温度点位名称" v-bind="validateInfos.jjwdName" id="LineEfficiencyHandleForm-jjwdName" name="jjwdName">
                <a-input v-model:value="formData.jjwdName" placeholder="请输入进酒温度点位名称" allow-clear></a-input>
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item
                label="额定产量"
                v-bind="validateInfos.ratedProduction"
                id="LineEfficiencyHandleForm-ratedProduction"
                name="ratedProduction"
              >
                <a-input v-model:value="formData.ratedProduction" placeholder="请输入额定产量" allow-clear></a-input>
              </a-form-item>
            </a-col>

            <a-col :span="24">
              <a-form-item
                label="线效点位通道号"
                v-bind="validateInfos.lineEfficiencyPointCode"
                id="LineEfficiencyHandleForm-lineEfficiencyPointCode"
                name="lineEfficiencyPointCode"
              >
                <a-input v-model:value="formData.lineEfficiencyPointCode" placeholder="请输入线效点位通道号" allow-clear></a-input>
              </a-form-item>
            </a-col>

            <a-col :span="24">
              <a-form-item label="是否推送" v-bind="validateInfos.ifSend" id="LineEfficiencyHandleForm-ifSend" name="ifSend">
                <a-select v-model:value="formData.ifSend" allow-clear style="width: 100%" placeholder="请选择">
                  <a-select-option :value="1">推送</a-select-option>
                  <a-select-option :value="0">不推送</a-select-option>
                </a-select>
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
  import { defHttp } from '/@/utils/http/axios';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { getValueType } from '/@/utils';
  import { saveOrUpdate } from '../LineEfficiencyHandle.api';
  import { Form } from 'ant-design-vue';
  import JFormContainer from '/@/components/Form/src/container/JFormContainer.vue';
  const props = defineProps({
    formDisabled: { type: Boolean, default: false },
    formData: { type: Object, default: () => ({}) },
    formBpm: { type: Boolean, default: true },
  });
  const formRef = ref();
  const useForm = Form.useForm;
  const emit = defineEmits(['register', 'ok']);
  const formData = reactive<Record<string, any>>({
    id: '',
    lineCode: '',
    lineName: '',
    sbId: '',
    sbName: '',
    pointCode: '',
    pointName: '',
    pointCodeNew: '',
    pointNameNew: '',
    cipCode: '',
    cipName: '',
    jjwdCode: '',
    jjwdName: '',
    handleType: '',
    initialTime: '',
    lineEfficiencyPointCode: '',
    initialNum: undefined,
    filterUp: undefined,
    filterDown: undefined,
    ratedProduction: undefined,
    ifSend: undefined,
    sort: undefined,
    delFlag: undefined,
  });
  const { createMessage } = useMessage();
  const labelCol = ref<any>({ xs: { span: 24 }, sm: { span: 5 } });
  const wrapperCol = ref<any>({ xs: { span: 24 }, sm: { span: 16 } });
  const confirmLoading = ref<boolean>(false);
  //表单验证
  const validatorRules = reactive({});
  const { resetFields, validate, validateInfos } = useForm(formData, validatorRules, { immediate: false });

  // 表单禁用
  const disabled = computed(() => {
    if (props.formBpm === true) {
      if (props.formData.disabled === false) {
        return false;
      } else {
        return true;
      }
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
    } catch ({ errorFields }) {
      if (errorFields) {
        const firstField = errorFields[0];
        if (firstField) {
          formRef.value.scrollToField(firstField.name, { behavior: 'smooth', block: 'center' });
        }
      }
      return Promise.reject(errorFields);
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
