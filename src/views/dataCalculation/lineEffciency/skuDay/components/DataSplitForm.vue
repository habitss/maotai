<template>
  <a-spin :spinning="confirmLoading">
    <JFormContainer :disabled="disabled">
      <template #detail>
        <a-form ref="formRef" class="antd-modal-form" :labelCol="labelCol" :wrapperCol="wrapperCol" name="TbPkgClassSkuDetailOpForm">
          <a-row>
						<a-col :span="24">
							<a-form-item label="开始时间" v-bind="validateInfos.startTime" id="TbPkgClassSkuDetailOpForm-st" name="startTime">
                <a-date-picker
                  placeholder="请选择开始时间"
                  :show-time="true"
                  valueFormat="YYYY-MM-DD HH:mm:ss"
                  v-model:value="formData.startTime"
                  style="width: 100%"
                  :disabled = "true"
                ></a-date-picker>
							</a-form-item>
						</a-col>
						<a-col :span="24">
							<a-form-item label="结束时间" v-bind="validateInfos.endTime" id="TbPkgClassSkuDetailOpForm-et" name="endTime">
                <a-date-picker
                  placeholder="请选择结束时间"
                  :show-time="true"
                  valueFormat="YYYY-MM-DD HH:mm:ss"
                  v-model:value="formData.endTime"
                  :style="etDateWidth"
                  :disabled-date="disabledDate"
                  :disabled = "isDateTime"
                  :onChange = "dateChange"
                ></a-date-picker>
                <a-tooltip placement="topLeft">
                  <template #title>
                    <span>{{ title }}</span>
                  </template>
                  <SettingOutlined style="margin-left: 5px;font-size: 20px;color: #877e7e;" @click="setEtDate" v-if="!isDateTime"/>
                </a-tooltip>

                </a-form-item>
						</a-col>
            <a-col :span="24">
							<a-form-item label="运行时长" v-bind="validateInfos.speed" id="TbPkgClassSkuDetailOpForm-runningTime" name="runningTime">
								<a-input v-model:value="formData.runningTime" placeholder="运行时长"  allow-clear  :disabled = "true"></a-input>
							</a-form-item>
						</a-col>
						<!-- <a-col :span="24">
							<a-form-item label="产量" v-bind="validateInfos.outputValue" id="TbPkgClassSkuDetailOpForm-outputValue" name="outputValue">
								<a-input v-model:value="formData.outputValue" placeholder="产量"  allow-clear  :disabled = "true"></a-input>
							</a-form-item>
						</a-col> -->
						<a-col :span="24">
							<a-form-item label="SKU名称" v-bind="validateInfos.skuName" id="TbPkgClassSkuDetailOpForm-skuName" name="skuName">
								<!-- <a-input v-model:value="formData.skuName" placeholder="请输入SKU名称"  allow-clear></a-input> -->
                <a-select v-model:value="formData.skuCode" :options="skuArr" placeholder="请选择SKU名称" :field-names="{ label: 'skuName', value: 'skuCode' }" @change="skuChange" allowClear></a-select>
						  </a-form-item>
						</a-col>
            <a-col :span="24">
							<a-form-item label="SKU速度" v-bind="validateInfos.skuSpeed" id="TbPkgClassSkuDetailOpForm-skuSpeed" name="skuSpeed">
								<a-input v-model:value="formData.skuSpeed" placeholder="请输入SKU速度"  allow-clear></a-input>
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
  import { defHttp } from '/@/utils/http/axios';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { getValueType } from '/@/utils';
  import { saveOrUpdate,getOutputValue,getSkuList } from '../SkuDay.api';
  import { Form } from 'ant-design-vue';
  import JFormContainer from '/@/components/Form/src/container/JFormContainer.vue';
  import { buildUUID } from '/@/utils/uuid';
  import { SettingOutlined } from '@ant-design/icons-vue';
  import dayjs from 'dayjs';
  const props = defineProps({
    formDisabled: { type: Boolean, default: false },
    formData: { type: Object, default: () => ({})},
    formBpm: { type: Boolean, default: true }
  });
  const formRef = ref();
  const mode = ref('add')
  const useForm = Form.useForm;
  const emit = defineEmits(['register', 'ok']);
  const title = ref('');
  const etDateWidth = ref('width: 95%');
  const formData = reactive<Record<string, any>>({
    id: '',
    lineCode: '',
    lineName: '',
    skuCode: '',
    skuName: '',
    skuSpeed: '',   
    dateDay: '',   
    startTime: '',
    endTime: '',
    runningTime: '',
    outputValue:'',
    outputCode: ''
  });
  const { createMessage } = useMessage();
  const labelCol = ref<any>({ xs: { span: 24 }, sm: { span: 5 } });
  const wrapperCol = ref<any>({ xs: { span: 24 }, sm: { span: 16 } });
  const confirmLoading = ref<boolean>(false);
  const isDateTime = ref<boolean>(false);
  const startDate = ref('');
  const endDate = ref('');
  const skuArr = ref([]);
  //表单验证
  const validatorRules = reactive({
    startTime: [{ required: true, message: '请选择开始时间!'},],
    endTime: [{ required: true, message: '请选择结束时间!'},],
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
  function add(record) {
    mode.value = 'add';
    isDateTime.value = false;
    startDate.value = record.startTime;
    endDate.value = record.dateMax;
    title.value ="设置结束时间为: "+ record.dateMax;
    etDateWidth.value = 'width: 95%';

    edit(record);
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
      if(tmpData.id){
        isDateTime.value = true;
        mode.value = 'edit';
        etDateWidth.value = 'width: 100%';
      }
      //获取sku
      getSku();
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
    if(!formData.id){
      formData.id = 'temp_'+buildUUID();
    }
    emit('ok', formData,mode.value);
  }

  function disabledDate(current){
    //限制时间段
    return current && (current < dayjs(startDate.value) || current > dayjs(endDate.value));
  }
  //设置结束时间
  function setEtDate(){
    formData.endTime = endDate.value;
    dateChange();
  }
  //计算时差(秒)
  function dateChange(){
    // 使用Day.js解析时间
    const start = dayjs(formData.startTime);
    const end = dayjs(formData.endTime);
    // 计算时间差（秒）
    if(start && end){
      //运行时长
      formData.runningTime = end.diff(start, 'second');
      //获取产量
      outputValue();
    }else{
      formData.runningTime = 0
      formData.outputValue = 0
    }
    
    
  }
  //获取产量
  async function outputValue(){
    await getOutputValue(formData)
      .then((res) => {
        if (res.success) {
          formData.outputValue = res.result
        } else {
          createMessage.warning(res.message);
        }
      })
      .finally(() => {});
  }
  //获取SKU
  async function getSku(){
    await getSkuList(formData)
      .then((res) => {
        if (res.success) {
          skuArr.value = res.result
        } else {
          createMessage.warning(res.message);
        }
      })
      .finally(() => {});
  }
  //设置sku速度/sku名称
  function skuChange(){
    const skuObj = skuArr.value.find(item => item.skuCode === formData.skuCode)
    formData.skuName = skuObj.skuName;
    formData.skuSpeed = skuObj.skuSpeed;
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
