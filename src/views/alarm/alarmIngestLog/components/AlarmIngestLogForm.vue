<template>
  <a-spin :spinning="confirmLoading">
    <JFormContainer :disabled="disabled">
      <template #detail>
        <a-form ref="formRef" class="antd-modal-form" :labelCol="labelCol" :wrapperCol="wrapperCol" name="AlarmIngestLogForm">
          <a-row>
            <a-col :span="24">
              <a-form-item label="部门区域" v-bind="validateInfos.factoryDeptCode" id="AlarmIngestLogForm-factoryDeptCode" name="factoryDeptCode">
                <JDeptCascader
                  ref="deptCascaderRef"
                  v-model:modelValue="formData.factoryDeptCode"
                  :show-default="true"
                  @change="deptChangeCascader"
                />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="来源系统" v-bind="validateInfos.sourceSystem" id="AlarmIngestLogForm-sourceSystem" name="sourceSystem">
                <a-input v-model:value="formData.sourceSystem" placeholder="请输入来源系统" allow-clear />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="请求ID" v-bind="validateInfos.requestId" id="AlarmIngestLogForm-requestId" name="requestId">
                <a-input v-model:value="formData.requestId" placeholder="请输入请求ID" allow-clear />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="报警PI" v-bind="validateInfos.alarmPiCode" id="AlarmIngestLogForm-alarmPiCode" name="alarmPiCode">
                <a-select v-model:value="formData.alarmPiCode" mode="default" placeholder="请选择报警点位" style="width: 100%" allowClear>
                  <a-select-option v-for="(item, index) in data.alarmPointOptions" :key="index" :value="item.alarmPiCode">{{
                    item.alarmName
                  }}</a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="接入状态" v-bind="validateInfos.ingestStatus" id="AlarmIngestLogForm-ingestStatus" name="ingestStatus">
                <a-input v-model:value="formData.ingestStatus" placeholder="请输入接入状态" allow-clear />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="错误信息" v-bind="validateInfos.errorMsg" id="AlarmIngestLogForm-errorMsg" name="errorMsg">
                <a-input v-model:value="formData.errorMsg" placeholder="请输入错误信息" allow-clear />
              </a-form-item>
            </a-col>
            <a-col :span="24">
              <a-form-item label="接收时间" v-bind="validateInfos.receiveTime" id="AlarmIngestLogForm-receiveTime" name="receiveTime">
                <a-date-picker
                  placeholder="请选择接收时间"
                  v-model:value="formData.receiveTime"
                  showTime
                  value-format="YYYY-MM-DD HH:mm:ss"
                  style="width: 100%"
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
  import { ref, reactive, defineExpose, nextTick, defineProps, computed, onMounted } from 'vue';
  import { useMessage } from '/@/hooks/web/useMessage';
  import JDeptCascader from '../../../ems/components/JDeptCascader.vue';
  import { getValueType } from '/@/utils';
  import { saveOrUpdate } from '../AlarmIngestLog.api';
  import { listOptions } from '../../alarmPoint/AlarmPoint.api';
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
    sourceSystem: '',
    requestId: '',
    alarmPiCode: '',
    ingestStatus: '',
    errorMsg: '',
    receiveTime: '',
    sort: undefined,
  });
  const { createMessage } = useMessage();
  const labelCol = ref<any>({ xs: { span: 24 }, sm: { span: 5 } });
  const wrapperCol = ref<any>({ xs: { span: 24 }, sm: { span: 16 } });
  const confirmLoading = ref<boolean>(false);
  //表单验证
  const validatorRules = reactive({
    factoryDeptCode: [{ required: true, message: '请选择工厂部门区域!' }],
    deptCode: [{ required: true, message: '请输入部门Code!' }],
    lineCode: [{ required: true, message: '请输入产线Code!' }],
    sourceSystem: [{ required: true, message: '请输入来源系统!' }],
    requestId: [{ required: true, message: '请输入请求ID!' }],
    alarmPiCode: [{ required: true, message: '请输入报警PI!' }],
    ingestStatus: [{ required: true, message: '请输入接入状态!' }],
    receiveTime: [{ required: true, message: '请输入接收时间!' }],
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
    alarmPointOptions: [] as any,
  });

  onMounted(async () => {
    listAlarmOptions();
  });

  /**
   * 获取报警点位下拉选项
   */
  async function listAlarmOptions() {
    const res = await listOptions({});
    data.alarmPointOptions = [...res];
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
