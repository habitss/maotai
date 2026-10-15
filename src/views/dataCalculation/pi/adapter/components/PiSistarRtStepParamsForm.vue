<template>
  <a-spin :spinning="confirmLoading">
    <JFormContainer :disabled="disabled">
      <template #detail>
        <a-form ref="formRef" class="antd-modal-form" :labelCol="labelCol" :wrapperCol="wrapperCol" name="PiSistarRtStepParamsForm">
          <a-row>
						
						<a-col :span="24">
							<a-form-item label="年份" v-bind="validateInfos.yearNo" id="PiSistarRtStepParamsForm-yearNo" name="yearNo">
								<a-input-number v-model:value="formData.yearNo" placeholder="请输入年份" style="width: 100%" />
							</a-form-item>
						</a-col>
						<a-col :span="24">
							<a-form-item label="设备" v-bind="validateInfos.reccatNo" id="PiSistarRtStepParamsForm-reccatNo" name="reccatNo">
								<a-input-number v-model:value="formData.reccatNo_dictText" placeholder="请输入设备" style="width: 100%" />
							</a-form-item>
						</a-col>
						<a-col :span="24">
							<a-form-item label="订单号" v-bind="validateInfos.orderNo" id="PiSistarRtStepParamsForm-orderNo" name="orderNo">
								<a-input-number v-model:value="formData.orderNo" placeholder="请输入orderNo" style="width: 100%" />
							</a-form-item>
						</a-col>
						<a-col :span="24">
							<a-form-item label="批次号" v-bind="validateInfos.batchNo" id="PiSistarRtStepParamsForm-batchNo" name="batchNo">
								<a-input-number v-model:value="formData.batchNo" placeholder="请输入批次号" style="width: 100%" />
							</a-form-item>
						</a-col>
						<a-col :span="24">
							<a-form-item label="PCU" v-bind="validateInfos.pcuNo" id="PiSistarRtStepParamsForm-pcuNo" name="pcuNo">
								<a-input-number v-model:value="formData.pcuNo_dictText" placeholder="请输入PCU" style="width: 100%" />
							</a-form-item>
						</a-col>
						<a-col :span="24">
							<a-form-item label="EOP号" v-bind="validateInfos.eopNo" id="PiSistarRtStepParamsForm-eopNo" name="eopNo">
								<a-input-number v-model:value="formData.eopNo_dictText" placeholder="请输入EOP号" style="width: 100%" />
							</a-form-item>
						</a-col>
						<a-col :span="24">
							<a-form-item label="开始时间" v-bind="validateInfos.startTime" id="PiSistarRtStepParamsForm-startTime" name="startTime">
								<a-date-picker placeholder="请选择开始时间"  v-model:value="formData.startTime" showTime value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%"  allow-clear />
							</a-form-item>
						</a-col>
						<a-col :span="24">
							<a-form-item label="ropId" v-bind="validateInfos.ropId" id="PiSistarRtStepParamsForm-ropId" name="ropId">
								<a-input-number v-model:value="formData.ropId" placeholder="请输入ropId" style="width: 100%" />
							</a-form-item>
						</a-col>
						<a-col :span="24">
							<a-form-item label="userProtId" v-bind="validateInfos.userProtId" id="PiSistarRtStepParamsForm-userProtId" name="userProtId">
								<a-input-number v-model:value="formData.userProtId" placeholder="请输入userProtId" style="width: 100%" />
							</a-form-item>
						</a-col>
						<a-col :span="24">
							<a-form-item label="paramPos" v-bind="validateInfos.paramPos" id="PiSistarRtStepParamsForm-paramPos" name="paramPos">
								<a-input-number v-model:value="formData.paramPos" placeholder="请输入paramPos" style="width: 100%" />
							</a-form-item>
						</a-col>
						<a-col :span="24">
							<a-form-item label="eparNo" v-bind="validateInfos.eparNo" id="PiSistarRtStepParamsForm-eparNo" name="eparNo">
								<a-input v-model:value="formData.eparNo" placeholder="请输入eparNo"  allow-clear ></a-input>
							</a-form-item>
						</a-col>
						<a-col :span="24">
							<a-form-item label="参数名" v-bind="validateInfos.paramName" id="PiSistarRtStepParamsForm-paramName" name="paramName">
								<a-input v-model:value="formData.paramName" placeholder="请输入参数名"  allow-clear ></a-input>
							</a-form-item>
						</a-col>
						<a-col :span="24">
							<a-form-item label="实际值" v-bind="validateInfos.actualValue" id="PiSistarRtStepParamsForm-actualValue" name="actualValue">
								<a-input v-model:value="formData.actualValue" placeholder="请输入实际值"  allow-clear ></a-input>
							</a-form-item>
						</a-col>
						<a-col :span="24">
							<a-form-item label="设定值" v-bind="validateInfos.setpointValue" id="PiSistarRtStepParamsForm-setpointValue" name="setpointValue">
								<a-input v-model:value="formData.setpointValue" placeholder="请输入设定值"  allow-clear ></a-input>
							</a-form-item>
						</a-col>
						<a-col :span="24">
							<a-form-item label="单位" v-bind="validateInfos.uom" id="PiSistarRtStepParamsForm-uom" name="uom">
								<a-input v-model:value="formData.uom" placeholder="请输入单位"  allow-clear ></a-input>
							</a-form-item>
						</a-col>
						<a-col :span="24">
							<a-form-item label="写入时间" v-bind="validateInfos.insertTimestamp" id="PiSistarRtStepParamsForm-insertTimestamp" name="insertTimestamp">
								<a-date-picker placeholder="请选择插入时间"  v-model:value="formData.insertTimestamp" showTime value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%"  allow-clear />
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
  import { saveOrUpdate } from '../PiSistarRtStepParams.api';
  import { Form } from 'ant-design-vue';
  import JFormContainer from '/@/components/Form/src/container/JFormContainer.vue';
  const props = defineProps({
    formDisabled: { type: Boolean, default: false },
    formData: { type: Object, default: () => ({})},
    formBpm: { type: Boolean, default: true }
  });
  const formRef = ref();
  const useForm = Form.useForm;
  const emit = defineEmits(['register', 'ok']);
  const formData = reactive<Record<string, any>>({
    id: '',
    tenantId: undefined,
    factoryCode: '',   
    deptCode: '',   
    lineCode: '',   
    siteNo: undefined,
    areaNo: undefined,
    yearNo: undefined,
    reccatNo: undefined,
    orderNo: undefined,
    batchNo: undefined,
    pcuNo: undefined,
    eopNo: undefined,
    startTime: '',   
    ropId: undefined,
    userProtId: undefined,
    paramPos: undefined,
    eparNo: '',   
    paramName: '',   
    actualValue: '',   
    setpointValue: '',   
    uom: '',   
    insertTimestamp: '',   
    sort: undefined,
    delFlag: undefined,
    reccatNo_dictText: '',
    pcuNo_dictText:'',
    eopNo_dictText:'',
  });
  const { createMessage } = useMessage();
  const labelCol = ref<any>({ xs: { span: 24 }, sm: { span: 5 } });
  const wrapperCol = ref<any>({ xs: { span: 24 }, sm: { span: 16 } });
  const confirmLoading = ref<boolean>(false);
  //表单验证
  const validatorRules = reactive({
    // tenantId: [{ required: true, message: '请输入租户ID!'},],
    // factoryCode: [{ required: true, message: '请输入工厂Code!'},],
    // deptCode: [{ required: true, message: '请输入部门Code!'},],
    // lineCode: [{ required: true, message: '请输入产线Code!'},],
    // siteNo: [{ required: true, message: '请输入siteNo!'},],
    // areaNo: [{ required: true, message: '请输入areaNo!'},],
    // yearNo: [{ required: true, message: '请输入yearNo!'},],
    // reccatNo: [{ required: true, message: '请输入reccatNo!'},],
    // orderNo: [{ required: true, message: '请输入orderNo!'},],
    // batchNo: [{ required: true, message: '请输入batchNo!'},],
    // pcuNo: [{ required: true, message: '请输入pcuNo!'},],
    // eopNo: [{ required: true, message: '请输入eopNo!'},],
    // startTime: [{ required: true, message: '请输入startTime!'},],
    // ropId: [{ required: true, message: '请输入ropId!'},],
    // userProtId: [{ required: true, message: '请输入userProtId!'},],
    // paramPos: [{ required: true, message: '请输入paramPos!'},],
    // insertTimestamp: [{ required: true, message: '请输入insertTimestamp!'},],
  });
  const { resetFields, validate, validateInfos } = useForm(formData, validatorRules, { immediate: false });

  // 表单禁用
  const disabled = computed(()=>{
    if(props.formBpm === true){
      if(props.formData.disabled === false){
        return false;
      }else{
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
        if(record.hasOwnProperty(key)){
          tmpData[key] = record[key]
        }
      })
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
