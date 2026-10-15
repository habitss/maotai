<template>
  <a-spin :spinning="confirmLoading">
    <JFormContainer :disabled="disabled">
      <template #detail>
        <a-form ref="formRef" class="antd-modal-form" :labelCol="labelCol" :wrapperCol="wrapperCol" name="AlarmEventForm">
          <a-row>
            <!-- 第一列 -->
            <a-col :span="12">
              <a-col :span="24">
                <a-form-item label="部门区域" v-bind="validateInfos.factoryDeptCode" id="AlarmEventForm-factoryDeptCode" name="factoryDeptCode">
                  <JDeptCascader
                    ref="deptCascaderRef"
                    v-model:modelValue="formData.factoryDeptCode"
                    :show-default="true"
                    @change="deptChangeCascader"
                  />
                </a-form-item>
              </a-col>
              <a-col :span="24">
                <a-form-item label="报警点位" v-bind="validateInfos.alarmPointId" id="AlarmEventForm-alarmPointId" name="alarmPointId">
                  <a-select
                    v-model:value="formData.alarmPointId"
                    mode="default"
                    placeholder="请选择报警点位"
                    style="width: 100%"
                    allowClear
                    @change="changeAlarmPoiont"
                  >
                    <a-select-option v-for="(item, index) in data.alarmPointOptions" :key="index" :value="item.id">{{
                      item.alarmName
                    }}</a-select-option>
                  </a-select>
                </a-form-item>
              </a-col>
              <a-col :span="24">
                <a-form-item label="报警点位编码" v-bind="validateInfos.alarmPiCode" id="AlarmEventForm-alarmPiCode" name="alarmPiCode">
                  <a-input v-model:value="formData.alarmPiCode" placeholder="请输入报警点位编码" disabled />
                </a-form-item>
              </a-col>
              <a-col :span="24">
                <a-form-item label="来源系统" v-bind="validateInfos.sourceSystem" id="AlarmEventForm-sourceSystem" name="sourceSystem">
                  <a-input v-model:value="formData.sourceSystem" placeholder="请输入来源系统" allow-clear />
                </a-form-item>
              </a-col>
              <a-col :span="24">
                <a-form-item label="功能位置编码" v-bind="validateInfos.funcLocCode" id="AlarmEventForm-funcLocCode" name="funcLocCode">
                  <a-input v-model:value="formData.funcLocCode" placeholder="请输入功能位置编码" allow-clear />
                </a-form-item>
              </a-col>
              <a-col :span="24">
                <a-form-item label="在岗人员" v-bind="validateInfos.onDutyPerson" id="AlarmEventForm-onDutyPerson" name="onDutyPerson">
                  <a-input v-model:value="formData.onDutyPerson" placeholder="请输入在岗人员" allow-clear />
                </a-form-item>
              </a-col>
            </a-col>
            <!-- 第二列 -->
            <a-col :span="12">
              <a-col :span="24">
                <a-form-item label="设备编码" v-bind="validateInfos.deviceCode" id="AlarmEventForm-deviceCode" name="deviceCode">
                  <a-input v-model:value="formData.deviceCode" placeholder="请输入设备编码" allow-clear />
                </a-form-item>
              </a-col>
              <a-col :span="24">
                <a-form-item label="报警等级" v-bind="validateInfos.alarmLevel" id="AlarmEventForm-alarmLevel" name="alarmLevel">
                  <a-input-number v-model:value="formData.alarmLevel" placeholder="请输入报警等级" style="width: 100%" />
                </a-form-item>
              </a-col>
              <a-col :span="24">
                <a-form-item label="报警时间" v-bind="validateInfos.alarmTime" id="AlarmEventForm-alarmTime" name="alarmTime">
                  <a-date-picker
                    placeholder="请选择报警时间"
                    v-model:value="formData.alarmTime"
                    showTime
                    value-format="YYYY-MM-DD HH:mm:ss"
                    style="width: 100%"
                    allow-clear
                  />
                </a-form-item>
              </a-col>
              <a-col :span="24">
                <a-form-item label="当前状态" v-bind="validateInfos.status" id="AlarmEventForm-status" name="status">
                  <j-dict-select-tag v-model:value="formData.status" :options="alarmStatusList" placeholder="请选择当前状态" allow-clear />
                </a-form-item>
              </a-col>
              <a-col :span="24">
                <a-form-item label="报警描述" v-bind="validateInfos.alarmDesc" id="AlarmEventForm-alarmDesc" name="alarmDesc">
                  <a-textarea v-model:value="formData.alarmDesc" placeholder="请输入报警描述" allow-clear />
                </a-form-item>
              </a-col>
            </a-col>
          </a-row>
        </a-form>
      </template>
    </JFormContainer>
  </a-spin>
</template>

<script lang="ts" setup>
  import { ref, reactive, nextTick, computed, onMounted } from 'vue';
  import { useMessage } from '/@/hooks/web/useMessage';
  import JDictSelectTag from '/@/components/Form/src/jeecg/components/JDictSelectTag.vue';
  import JDeptCascader from '../../../ems/components/JDeptCascader.vue';
  import { getValueType } from '/@/utils';
  import { saveOrUpdate } from '../AlarmEvent.api';
  import { alarmStatusList } from '../AlarmEvent.data';
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
    alarmPiCode: '',
    alarmPointId: undefined,
    sourceSystem: '',
    funcLocCode: '',
    deviceCode: '',
    alarmLevel: undefined,
    alarmTime: '',
    alarmDesc: '',
    onDutyPerson: '',
    status: '',
    sort: undefined,
  });
  const { createMessage } = useMessage();
  const labelCol = ref<any>({ xs: { span: 24 }, sm: { span: 5 } });
  const wrapperCol = ref<any>({ xs: { span: 24 }, sm: { span: 16 } });
  const confirmLoading = ref<boolean>(false);
  //表单验证
  const validatorRules = reactive({
    factoryDeptCode: [{ required: true, message: '请选择部门区域!' }],
    alarmPointId: [{ required: true, message: '请选择报警点位!' }],
    alarmPiCode: [{ required: true, message: '请输入报警点位编码!' }],
    sourceSystem: [{ required: true, message: '请输入来源系统!' }],
    funcLocCode: [{ required: true, message: '请输入功能位置编码!' }],
    deviceCode: [{ required: true, message: '请输入设备编码!' }],
    alarmLevel: [{ required: true, message: '请输入报警等级!' }],
    alarmTime: [{ required: true, message: '请输入报警时间!' }],
    status: [{ required: true, message: '请输入当前状态!' }],
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
    formData.factoryDeptCode = 'X';
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

  //切换报警点位
  function changeAlarmPoiont(val) {
    formData.alarmPiCode = '';
    if (val) {
      const alarmPoint = data.alarmPointOptions.find((item) => item.id === val);
      if (alarmPoint) {
        formData.alarmPiCode = alarmPoint.alarmPiCode;
      }
    }
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
