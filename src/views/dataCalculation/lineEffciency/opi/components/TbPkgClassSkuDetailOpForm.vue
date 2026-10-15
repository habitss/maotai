<template>
  <a-spin :spinning="confirmLoading">
    <JFormContainer :disabled="disabled">
      <template #detail>
        <a-form ref="formRef" class="antd-modal-form" :labelCol="labelCol" :wrapperCol="wrapperCol" name="TbPkgClassSkuDetailOpForm">
          <a-row>
						<a-col :span="24">
							<a-form-item label="产线" v-bind="validateInfos.line" id="TbPkgClassSkuDetailOpForm-line" name="line">
								<a-input v-model:value="formData.line" placeholder="请输入产线"  allow-clear ></a-input>
							</a-form-item>
						</a-col>
						<a-col :span="24">
							<a-form-item label="日期" v-bind="validateInfos.date" id="TbPkgClassSkuDetailOpForm-date" name="date">
								<a-date-picker placeholder="请选择日期"  v-model:value="formData.date" value-format="YYYY-MM-DD" format="YYYY-MM-DD" style="width: 100%"  allow-clear />
							</a-form-item>
						</a-col>
						<a-col :span="24">
							<a-form-item label="班次" v-bind="validateInfos.classtime" id="TbPkgClassSkuDetailOpForm-classtime" name="classtime">
								<a-input v-model:value="formData.classtime" placeholder="请输入班次"  allow-clear ></a-input>
							</a-form-item>
						</a-col>
						<a-col :span="24">
							<a-form-item label="SKU" v-bind="validateInfos.sku" id="TbPkgClassSkuDetailOpForm-sku" name="sku">
								<a-input v-model:value="formData.sku" placeholder="请输入SKU"  allow-clear ></a-input>
							</a-form-item>
						</a-col>
						<a-col :span="24">
							<a-form-item label="开始时间" v-bind="validateInfos.st" id="TbPkgClassSkuDetailOpForm-st" name="st">
								<!-- <a-input v-model:value="formData.st" placeholder="请输入开始时间"  allow-clear ></a-input> -->
                <a-date-picker
                  placeholder="请选择开始时间"
                  :show-time="true"
                  valueFormat="YYYY-MM-DD HH:mm:ss"
                  v-model:value="formData.st"
                  style="width: 100%;"
                  :disabled-date="stDisabledDate"
                  :disabled = "isDateTime"
                ></a-date-picker>
							</a-form-item>
						</a-col>
						<a-col :span="24">
							<a-form-item label="结束时间" v-bind="validateInfos.et" id="TbPkgClassSkuDetailOpForm-et" name="et">
								<!-- <a-input v-model:value="formData.et" placeholder="请输入结束时间"  allow-clear ></a-input> -->
                <a-date-picker
                  placeholder="请选择结束时间"
                  :show-time="true"
                  valueFormat="YYYY-MM-DD HH:mm:ss"
                  v-model:value="formData.et"
                  style="width: 100%;"
                  :disabled-date="etDisabledDate"
                  :disabled = "isDateTime"
                ></a-date-picker>
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
  import dayjs from 'dayjs';
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
  //表单验证
  const validatorRules = reactive({
    line: [{ required: true, message: '请输入产线!'},],
    date: [{ required: true, message: '请选择日期!'},],
    classtime: [{ required: true, message: '请输入班次!'},],
    sku: [{ required: true, message: '请输入SKU!'},],
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
      if(tmpData && tmpData.id){
        isDateTime.value = true;
      }else{
        isDateTime.value = false;
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

  function etDisabledDate(current){
    return current && current < dayjs(formData.st);
  }

  function stDisabledDate(current){
    return current && current > dayjs(formData.et);
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
