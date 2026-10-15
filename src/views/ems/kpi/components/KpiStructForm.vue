<template>
  <a-spin :spinning="confirmLoading">
    <JFormContainer :disabled="disabled">
      <template #detail>
        <a-form ref="formRef" class="antd-modal-form" :labelCol="labelCol" :wrapperCol="wrapperCol" name="KpiStructForm">
          <a-row>
						<a-col :span="24">
							<a-form-item label="工厂Code" v-bind="validateInfos.factoryCode" id="KpiStructForm-factoryCode" name="factoryCode">
								<a-input v-model:value="formData.factoryCode" placeholder="请输入工厂Code"  allow-clear ></a-input>
							</a-form-item>
						</a-col>
						<a-col :span="24">
							<a-form-item label="部门Code" v-bind="validateInfos.deptCode" id="KpiStructForm-deptCode" name="deptCode">
								<a-input v-model:value="formData.deptCode" placeholder="请输入部门Code"  allow-clear ></a-input>
							</a-form-item>
						</a-col>
						<a-col :span="24">
							<a-form-item label="产线Code" v-bind="validateInfos.lineCode" id="KpiStructForm-lineCode" name="lineCode">
								<a-input v-model:value="formData.lineCode" placeholder="请输入产线Code"  allow-clear ></a-input>
							</a-form-item>
						</a-col>
						<a-col :span="24">
							<a-form-item label="级别" v-bind="validateInfos.level" id="KpiStructForm-level" name="level">
								<a-input-number v-model:value="formData.level" placeholder="请输入级别" style="width: 100%" />
							</a-form-item>
						</a-col>
						<a-col :span="24">
							<a-form-item label="父类ID" v-bind="validateInfos.parentId" id="KpiStructForm-parentId" name="parentId">
								<a-input-number v-model:value="formData.parentId" placeholder="请输入父类ID" style="width: 100%" />
							</a-form-item>
						</a-col>
						<a-col :span="24">
							<a-form-item label="名称" v-bind="validateInfos.name" id="KpiStructForm-name" name="name">
								<a-input v-model:value="formData.name" placeholder="请输入名称"  allow-clear ></a-input>
							</a-form-item>
						</a-col>
						<a-col :span="24">
							<a-form-item label="能源类型" v-bind="validateInfos.energyType" id="KpiStructForm-energyType" name="energyType">
								<a-input v-model:value="formData.energyType" placeholder="请输入能源类型"  allow-clear ></a-input>
							</a-form-item>
						</a-col>
						<a-col :span="24">
							<a-form-item label="排序号" v-bind="validateInfos.sort" id="KpiStructForm-sort" name="sort">
								<a-input-number v-model:value="formData.sort" placeholder="请输入排序号" style="width: 100%" />
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
  import { useMessage } from '/@/hooks/web/useMessage';
  import { getValueType } from '/@/utils';
  import { saveOrUpdate } from '../KpiStruct.api';
  import { Form } from 'ant-design-vue';
  import JFormContainer from '/@/components/Form/src/container/JFormContainer.vue';
  const props = defineProps({
    formDisabled: { type: Boolean, default: false },
    formBpm: { type: Boolean, default: true }
  });
  const formRef = ref();
  const useForm = Form.useForm;
  const emit = defineEmits(['register', 'ok']);
  const formData = reactive<Record<string, any>>({
    id: '',
    factoryCode: '',   
    deptCode: '',   
    lineCode: '',   
    level: undefined,
    parentId: undefined,
    name: '',   
    energyType: '',   
    sort: undefined,
  });
  const { createMessage } = useMessage();
  const labelCol = ref<any>({ xs: { span: 24 }, sm: { span: 5 } });
  const wrapperCol = ref<any>({ xs: { span: 24 }, sm: { span: 16 } });
  const confirmLoading = ref<boolean>(false);
  //表单验证
  const validatorRules = reactive({
    factoryCode: [{ required: true, message: '请输入工厂Code!'},],
    deptCode: [{ required: true, message: '请输入部门Code!'},],
    lineCode: [{ required: true, message: '请输入产线Code!'},],
    level: [{ required: true, message: '请输入级别!'},],
    parentId: [{ required: true, message: '请输入父类ID!'},],
    name: [{ required: true, message: '请输入名称!'},],
    energyType: [{ required: true, message: '请输入能源类型!'},],
    sort: [{ required: true, message: '请输入排序号!'},],
  });
  const { resetFields, validate, validateInfos } = useForm(formData, validatorRules, { immediate: false });

  // 表单禁用
  const disabled = computed(()=>{
    if(props.formBpm === true){
      return false;
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
