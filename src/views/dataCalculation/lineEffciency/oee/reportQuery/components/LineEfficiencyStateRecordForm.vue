<template>
  <a-spin :spinning="confirmLoading">
    <JFormContainer :disabled="disabled">
      <template #detail>
        <a-form ref="formRef" class="antd-modal-form" :labelCol="labelCol" :wrapperCol="wrapperCol" name="LineEfficiencyStateRecordForm">
          <a-row>
						<a-col :span="24">
							<a-form-item label="产线编码" v-bind="validateInfos.lineCode" id="LineEfficiencyStateRecordForm-lineCode" name="lineCode">
								<a-input v-model:value="formData.lineCode" placeholder="请输入产线编码"  allow-clear ></a-input>
							</a-form-item>
						</a-col>
						<a-col :span="24">
							<a-form-item label="产线名称" v-bind="validateInfos.lineName" id="LineEfficiencyStateRecordForm-lineName" name="lineName">
								<a-input v-model:value="formData.lineName" placeholder="请输入产线名称"  allow-clear ></a-input>
							</a-form-item>
						</a-col>
						<a-col :span="24">
							<a-form-item label="设备编码" v-bind="validateInfos.sbId" id="LineEfficiencyStateRecordForm-sbId" name="sbId">
								<a-input v-model:value="formData.sbId" placeholder="请输入设备编码"  allow-clear ></a-input>
							</a-form-item>
						</a-col>
						<a-col :span="24">
							<a-form-item label="设备名称" v-bind="validateInfos.sbName" id="LineEfficiencyStateRecordForm-sbName" name="sbName">
								<a-input v-model:value="formData.sbName" placeholder="请输入设备名称"  allow-clear ></a-input>
							</a-form-item>
						</a-col>
						<a-col :span="24">
							<a-form-item label="运行状态" v-bind="validateInfos.stateCode" id="LineEfficiencyStateRecordForm-stateCode" name="stateCode">
								<!-- <j-dict-select-tag v-model:value="formData.stateCode" dictCode="" placeholder="请选择运行状态 0未运行 1运行"  allow-clear /> -->
                <a-select
                  v-model:value="formData.stateCode"
                  allow-clear
                  style="width: 100%;"
                  placeholder="请选择"
                >
                  <a-select-option value="1">运行</a-select-option>
                  <a-select-option value="0">未运行</a-select-option>
                </a-select>
							</a-form-item>
						</a-col>
						<a-col :span="24">
							<a-form-item label="开始时间" v-bind="validateInfos.startTime" id="LineEfficiencyStateRecordForm-startTime" name="startTime">
								<a-date-picker placeholder="请选择开始时间"  v-model:value="formData.startTime" showTime value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%"  allow-clear />
							</a-form-item>
						</a-col>
            <a-col :span="24">
							<a-form-item label="实际结束时间" v-bind="validateInfos.endTimeActual" id="LineEfficiencyStateRecordForm-endTimeActual" name="endTimeActual">
								<a-date-picker placeholder="请选择实际结束时间"  v-model:value="formData.endTimeActual" showTime value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%"  allow-clear />
							</a-form-item>
						</a-col>
						<a-col :span="24">
							<a-form-item label="理论结束时间" v-bind="validateInfos.endTime" id="LineEfficiencyStateRecordForm-endTime" name="endTime">
								<a-date-picker placeholder="请选择理论结束时间"  v-model:value="formData.endTime" showTime value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%"  allow-clear />
							</a-form-item>
						</a-col>
						<a-col :span="24">
							<a-form-item label="运行时长（秒）" v-bind="validateInfos.runningTime" id="LineEfficiencyStateRecordForm-runningTime" name="runningTime">
								<a-input-number v-model:value="formData.runningTime" placeholder="请输入运行时长（秒）" style="width: 100%" />
							</a-form-item>
						</a-col>
						<!-- <a-col :span="24">
							<a-form-item label="分类0线效状态、1设备运行状态" v-bind="validateInfos.type" id="LineEfficiencyStateRecordForm-type" name="type">
								<j-dict-select-tag v-model:value="formData.type" dictCode="" placeholder="请选择分类0线效状态、1设备运行状态"  allow-clear />
							</a-form-item>
						</a-col> -->
						<a-col :span="24">
							<a-form-item label="线效值" v-bind="validateInfos.lineEfficiencyValue" id="LineEfficiencyStateRecordForm-lineEfficiencyValue" name="lineEfficiencyValue">
								<a-input v-model:value="formData.lineEfficiencyValue" placeholder="请输入线效值"  allow-clear ></a-input>
							</a-form-item>
						</a-col>
						<a-col :span="24">
							<a-form-item label="产量" v-bind="validateInfos.production" id="LineEfficiencyStateRecordForm-production" name="production">
								<a-input v-model:value="formData.production" placeholder="请输入产量"  allow-clear ></a-input>
							</a-form-item>
						</a-col>
						<a-col :span="24">
							<a-form-item label="额定产量" v-bind="validateInfos.ratedProduction" id="LineEfficiencyStateRecordForm-ratedProduction" name="ratedProduction">
								<a-input-number v-model:value="formData.ratedProduction" placeholder="请输入额定产量" style="width: 100%" />
							</a-form-item>
						</a-col>
						<!-- <a-col :span="24">
							<a-form-item label="报警名称" v-bind="validateInfos.alarmName" id="LineEfficiencyStateRecordForm-alarmName" name="alarmName">
								<a-input v-model:value="formData.alarmName" placeholder="请输入报警名称"  allow-clear ></a-input>
							</a-form-item>
						</a-col>
						<a-col :span="24">
							<a-form-item label="排序" v-bind="validateInfos.sort" id="LineEfficiencyStateRecordForm-sort" name="sort">
								<a-input-number v-model:value="formData.sort" placeholder="请输入排序" style="width: 100%" />
							</a-form-item>
						</a-col> -->
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
  import JDictSelectTag from '/@/components/Form/src/jeecg/components/JDictSelectTag.vue';
  import { getValueType } from '/@/utils';
  import { saveOrUpdate } from '../LineEfficiencyStateRecord.api';
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
    lineCode: '',   
    lineName: '',   
    sbId: '',   
    sbName: '',   
    stateCode: '',   
    startTime: '',   
    endTime: '',   
    runningTime: undefined,
    type: 0,
    endTimeActual: '',   
    lineEfficiencyValue: '',   
    production: '',   
    ratedProduction: undefined,
    alarmName: '',   
    sort: undefined,
    delFlag: 0,
  });
  const { createMessage } = useMessage();
  const labelCol = ref<any>({ xs: { span: 24 }, sm: { span: 5 } });
  const wrapperCol = ref<any>({ xs: { span: 24 }, sm: { span: 16 } });
  const confirmLoading = ref<boolean>(false);
  //表单验证
  const validatorRules = reactive({
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
    //如果type为空，则赋值为0
    if (!model.type) {
      model.type = 0;
    }
    //表单提交
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
