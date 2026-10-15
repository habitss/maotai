<template>
  <a-spin :spinning="confirmLoading">
    <JFormContainer :disabled="disabled">
      <template #detail>
        <a-form ref="formRef" class="antd-modal-form" :labelCol="labelCol" :wrapperCol="wrapperCol" name="TbPkgClassSkuDetailOpForm">
          <a-row>
						<a-col :span="24">
							<a-form-item label="开始时间" v-bind="validateInfos.st" id="TbPkgClassSkuDetailOpForm-st" name="st">
                <a-date-picker
                  placeholder="请选择开始时间"
                  :show-time="true"
                  valueFormat="YYYY-MM-DD HH:mm:ss"
                  v-model:value="formData.st"
                  style="width: 100%"
                  :disabled = "true"
                ></a-date-picker>
							</a-form-item>
						</a-col>
						<a-col :span="24">
							<a-form-item label="结束时间" v-bind="validateInfos.et" id="TbPkgClassSkuDetailOpForm-et" name="et">
                <a-date-picker
                  placeholder="请选择结束时间"
                  :show-time="true"
                  valueFormat="YYYY-MM-DD HH:mm:ss"
                  v-model:value="formData.et"
                  :style="etDateWidth"
                  :disabled-date="disabledDate"
                  :disabled = "isDateTime"
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
							<a-form-item label="产量" v-bind="validateInfos.detail" id="TbPkgClassSkuDetailOpForm-detail" name="detail">
								<a-input v-model:value="formData.detail" placeholder="请输入产量"  allow-clear ></a-input>
							</a-form-item>
						</a-col>
						<a-col :span="24">
							<a-form-item label="速度" v-bind="validateInfos.speed" id="TbPkgClassSkuDetailOpForm-speed" name="speed">
								<a-input v-model:value="formData.speed" placeholder="请输入速度"  allow-clear ></a-input>
							</a-form-item>
						</a-col>
						<a-col :span="24">
							<a-form-item label="手动录入速度" v-bind="validateInfos.speedOp" id="TbPkgClassSkuDetailOpForm-speedOp" name="speedOp">
								<a-input v-model:value="formData.speedOp" placeholder="请输入手动录入速度"  allow-clear ></a-input>
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
  import { saveOrUpdate } from '../TbPkgClassSkuDetailOp.api';
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
    line: '',   
    date: '',   
    classtime: '',   
    sku: '',   
    st: '',   
    et: '',   
    detail: '',   
    speed: '',   
    speedOp: '',   
  });
  const { createMessage } = useMessage();
  const labelCol = ref<any>({ xs: { span: 24 }, sm: { span: 5 } });
  const wrapperCol = ref<any>({ xs: { span: 24 }, sm: { span: 16 } });
  const confirmLoading = ref<boolean>(false);
  const isDateTime = ref<boolean>(false);
  const startDate = ref('');
  const endDate = ref('');
  //表单验证
  const validatorRules = reactive({
    st: [{ required: true, message: '请选择开始时间!'},],
    et: [{ required: true, message: '请选择结束时间!'},],
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
    startDate.value = record.st;
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
  function setEtDate(){
    formData.et = endDate.value;
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
