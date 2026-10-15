<template>
  <a-spin :spinning="confirmLoading">
    <JFormContainer :disabled="disabled">
      <template #detail>
        <a-form ref="formRef" class="antd-modal-form" :labelCol="labelCol" :wrapperCol="wrapperCol" name="PiSistarRtStepsForm">
          <a-row>
			<a-col :span="24">
				<a-form-item label="年份" v-bind="validateInfos.yearNo" id="PiSistarRtStepsForm-yearNo" name="yearNo">
					<a-input-number v-model:value="formData.yearNo" placeholder="请输入yearNo" style="width: 100%" />
				</a-form-item>
			</a-col>
			<a-col :span="24">
				<a-form-item label="设备" v-bind="validateInfos.reccatNo" id="PiSistarRtStepsForm-reccatNo" name="reccatNo">
					<a-input-number v-model:value="formData.reccatNo_dictText" placeholder="请输入设备" style="width: 100%" />
				</a-form-item>
			</a-col>
			<a-col :span="24">
				<a-form-item label="订单号" v-bind="validateInfos.orderNo" id="PiSistarRtStepsForm-orderNo" name="orderNo">
					<a-input-number v-model:value="formData.orderNo" placeholder="请输入orderNo" style="width: 100%" />
				</a-form-item>
			</a-col>
			<a-col :span="24">
				<a-form-item label="批次号" v-bind="validateInfos.batchNo" id="PiSistarRtStepsForm-batchNo" name="batchNo">
					<a-input-number v-model:value="formData.batchNo" placeholder="请输入batchNo" style="width: 100%" />
				</a-form-item>
			</a-col>
			<a-col :span="24">
				<a-form-item label="PCU" v-bind="validateInfos.pcuNo" id="PiSistarRtStepsForm-pcuNo" name="pcuNo">
					<a-input-number v-model:value="formData.pcuNo_dictText" placeholder="请输入PCU" style="width: 100%" />
				</a-form-item>
			</a-col>
			<a-col :span="24">
				<a-form-item label="设备单元" v-bind="validateInfos.unitNo" id="PiSistarRtStepsForm-unitNo" name="unitNo">
					<a-input-number v-model:value="formData.unitNo_dictText" placeholder="请输入设备单元" style="width: 100%" />
				</a-form-item>
			</a-col>
			<a-col :span="24">
				<a-form-item label="EOP" v-bind="validateInfos.eopNo" id="PiSistarRtStepsForm-eopNo" name="eopNo">
					<a-input-number v-model:value="formData.eopNo_dictText" placeholder="请输入EOP" style="width: 100%" />
				</a-form-item>
			</a-col>
			<a-col :span="24">
				<a-form-item label="开始时间" v-bind="validateInfos.startTime" id="PiSistarRtStepsForm-startTime" name="startTime">
					<a-date-picker placeholder="请选择startTime"  v-model:value="formData.startTime" showTime value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%"  allow-clear />
				</a-form-item>
			</a-col>
			<a-col :span="24">
				<a-form-item label="结束时间" v-bind="validateInfos.endTime" id="PiSistarRtStepsForm-endTime" name="endTime">
					<a-date-picker placeholder="请选择endTime"  v-model:value="formData.endTime" showTime value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%"  allow-clear />
				</a-form-item>
			</a-col>
			<a-col :span="24">
				<a-form-item label="ropId" v-bind="validateInfos.ropId" id="PiSistarRtStepsForm-ropId" name="ropId">
					<a-input-number v-model:value="formData.ropId" placeholder="请输入ropId" style="width: 100%" />
				</a-form-item>
			</a-col>
			<a-col :span="24">
				<a-form-item label="userProtId" v-bind="validateInfos.userProtId" id="PiSistarRtStepsForm-userProtId" name="userProtId">
					<a-input-number v-model:value="formData.userProtId" placeholder="请输入userProtId" style="width: 100%" />
				</a-form-item>
			</a-col>
			<a-col :span="24">
				<a-form-item label="事件事件" v-bind="validateInfos.eventTime" id="PiSistarRtStepsForm-eventTime" name="eventTime">
					<a-date-picker placeholder="请选择eventTime"  v-model:value="formData.eventTime" showTime value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%"  allow-clear />
				</a-form-item>
			</a-col>
			
			<a-col :span="24">
				<a-form-item label="监控时间" v-bind="validateInfos.monitoringTime" id="PiSistarRtStepsForm-monitoringTime" name="monitoringTime">
					<a-input-number v-model:value="formData.monitoringTime" placeholder="请输入monitoringTime" style="width: 100%" />
				</a-form-item>
			</a-col>
			<a-col :span="24">
				<a-form-item label="持续时间" v-bind="validateInfos.duration" id="PiSistarRtStepsForm-duration" name="duration">
					<a-input-number v-model:value="formData.duration" placeholder="请输入duration" style="width: 100%" />
				</a-form-item>
			</a-col>
			<a-col :span="24">
				<a-form-item label="ordercatName" v-bind="validateInfos.ordercatName" id="PiSistarRtStepsForm-ordercatName" name="ordercatName">
					<a-input v-model:value="formData.ordercatName" placeholder="请输入ordercatName"  allow-clear ></a-input>
				</a-form-item>
			</a-col>
			<a-col :span="24">
				<a-form-item label="reccatName" v-bind="validateInfos.reccatName" id="PiSistarRtStepsForm-reccatName" name="reccatName">
					<a-input v-model:value="formData.reccatName" placeholder="请输入reccatName"  allow-clear ></a-input>
				</a-form-item>
			</a-col>
			<a-col :span="24">
				<a-form-item label="配方" v-bind="validateInfos.recipeNo" id="PiSistarRtStepsForm-recipeNo" name="recipeNo">
					<a-input-number v-model:value="formData.recipeNo_dictText" placeholder="请输入配方" style="width: 100%" />
				</a-form-item>
			</a-col>
			<a-col :span="24">
				<a-form-item label="配方名称" v-bind="validateInfos.recipeName" id="PiSistarRtStepsForm-recipeName" name="recipeName">
					<a-input v-model:value="formData.recipeName" placeholder="请输入"  allow-clear ></a-input>
				</a-form-item>
			</a-col>
			<a-col :span="24">
				<a-form-item label="recprocNo" v-bind="validateInfos.recprocNo" id="PiSistarRtStepsForm-recprocNo" name="recprocNo">
					<a-input-number v-model:value="formData.recprocNo" placeholder="请输入" style="width: 100%" />
				</a-form-item>
			</a-col>
			<a-col :span="24">
				<a-form-item label="productId" v-bind="validateInfos.productId" id="PiSistarRtStepsForm-productId" name="productId">
					<a-input-number v-model:value="formData.productId" placeholder="请输入" style="width: 100%" />
				</a-form-item>
			</a-col>
			<a-col :span="24">
				<a-form-item label="productName" v-bind="validateInfos.productName" id="PiSistarRtStepsForm-productName" name="productName">
					<a-input v-model:value="formData.productName" placeholder="请输入"  allow-clear ></a-input>
				</a-form-item>
			</a-col>
			<a-col :span="24">
				<a-form-item label="equipmentName" v-bind="validateInfos.equipmentName" id="PiSistarRtStepsForm-equipmentName" name="equipmentName">
					<a-input v-model:value="formData.equipmentName" placeholder="请输入"  allow-clear ></a-input>
				</a-form-item>
			</a-col>
			<a-col :span="24">
				<a-form-item label="equipmentId" v-bind="validateInfos.equipmentId" id="PiSistarRtStepsForm-equipmentId" name="equipmentId">
					<a-input v-model:value="formData.equipmentId" placeholder="请输入"  allow-clear ></a-input>
				</a-form-item>
			</a-col>
			<a-col :span="24">
				<a-form-item label="PCU名称" v-bind="validateInfos.pcuName" id="PiSistarRtStepsForm-pcuName" name="pcuName">
					<a-input v-model:value="formData.pcuName" placeholder="请输入pcuName"  allow-clear ></a-input>
				</a-form-item>
			</a-col>
			<a-col :span="24">
				<a-form-item label="设备单元名称" v-bind="validateInfos.unitName" id="PiSistarRtStepsForm-unitName" name="unitName">
					<a-input v-model:value="formData.unitName" placeholder="请输入unitName"  allow-clear ></a-input>
				</a-form-item>
			</a-col>
			<a-col :span="24">
				<a-form-item label="EOP名称" v-bind="validateInfos.eopName" id="PiSistarRtStepsForm-eopName" name="eopName">
					<a-input v-model:value="formData.eopName" placeholder="请输入eopName"  allow-clear ></a-input>
				</a-form-item>
			</a-col>
			<a-col :span="24">
				<a-form-item label="参数计数" v-bind="validateInfos.paramCount" id="PiSistarRtStepsForm-paramCount" name="paramCount">
					<a-input-number v-model:value="formData.paramCount" placeholder="请输入paramCount" style="width: 100%" />
				</a-form-item>
			</a-col>
			<a-col :span="24">
				<a-form-item label="步骤状态" v-bind="validateInfos.stepStatus" id="PiSistarRtStepsForm-stepStatus" name="stepStatus">
					<a-input v-model:value="formData.stepStatus" placeholder="请输入stepStatus"  allow-clear ></a-input>
				</a-form-item>
			</a-col>
			<a-col :span="24">
				<a-form-item label="写入时间" v-bind="validateInfos.insertTimestamp" id="PiSistarRtStepsForm-insertTimestamp" name="insertTimestamp">
					<a-date-picker placeholder="请选择insertTimestamp"  v-model:value="formData.insertTimestamp" showTime value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%"  allow-clear />
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
  import { saveOrUpdate } from '../PiSistarRtSteps.api';
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
    unitNo: undefined,
    eopNo: undefined,
    startTime: '',   
    ropId: undefined,
    userProtId: undefined,
    eventTime: '',   
    endTime: '',   
    monitoringTime: undefined,
    duration: undefined,
    ordercatName: '',   
    reccatName: '',   
    recipeNo: undefined,
    recipeName: '',   
    recprocNo: undefined,
    productId: undefined,
    productName: '',   
    equipmentName: '',   
    equipmentId: '',   
    pcuName: '',   
    unitName: '',   
    eopName: '',   
    paramCount: undefined,
    stepStatus: '',   
    insertTimestamp: '',   
    userData: undefined,
    sort: undefined,
    delFlag: undefined,
	reccatNo_dictText: '',
	pcuNo_dictText:'',
	unitNo_dictText:'',
	eopNo_dictText:'',
	recipeNo_dictText:''
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
    // unitNo: [{ required: true, message: '请输入unitNo!'},],
    // eopNo: [{ required: true, message: '请输入eopNo!'},],
    // startTime: [{ required: true, message: '请输入startTime!'},],
    // ropId: [{ required: true, message: '请输入ropId!'},],
    // userProtId: [{ required: true, message: '请输入userProtId!'},],
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
