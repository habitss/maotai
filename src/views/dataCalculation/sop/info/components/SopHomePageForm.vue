<template>
  <a-spin :spinning="confirmLoading">
    <JFormContainer :disabled="disabled">
      <template #detail>
        <a-form ref="formRef" class="antd-modal-form" :labelCol="labelCol" :wrapperCol="wrapperCol" name="SopHomePageForm">
          <a-row>
						<a-col :span="24">
							<a-form-item label="租户ID" v-bind="validateInfos.tenantId" id="SopHomePageForm-tenantId" name="tenantId">
								<a-input-number v-model:value="formData.tenantId" placeholder="请输入租户ID" style="width: 100%" />
							</a-form-item>
						</a-col>
						<a-col :span="24">
							<a-form-item label="工厂Code" v-bind="validateInfos.factoryCode" id="SopHomePageForm-factoryCode" name="factoryCode">
								<a-input v-model:value="formData.factoryCode" placeholder="请输入工厂Code"  allow-clear ></a-input>
							</a-form-item>
						</a-col>
						<a-col :span="24">
							<a-form-item label="部门Code" v-bind="validateInfos.deptCode" id="SopHomePageForm-deptCode" name="deptCode">
								<a-input v-model:value="formData.deptCode" placeholder="请输入部门Code"  allow-clear ></a-input>
							</a-form-item>
						</a-col>
						<a-col :span="24">
							<a-form-item label="产线Code" v-bind="validateInfos.lineCode" id="SopHomePageForm-lineCode" name="lineCode">
								<a-input v-model:value="formData.lineCode" placeholder="请输入产线Code"  allow-clear ></a-input>
							</a-form-item>
						</a-col>
						<a-col :span="24">
							<a-form-item label="sop类型" v-bind="validateInfos.sopType" id="SopHomePageForm-sopType" name="sopType">
								<a-input v-model:value="formData.sopType" placeholder="请输入sop类型"  allow-clear ></a-input>
							</a-form-item>
						</a-col>
						<a-col :span="24">
							<a-form-item label="sop活动" v-bind="validateInfos.sopActivity" id="SopHomePageForm-sopActivity" name="sopActivity">
								<a-input v-model:value="formData.sopActivity" placeholder="请输入sop活动"  allow-clear ></a-input>
							</a-form-item>
						</a-col>
						<a-col :span="24">
							<a-form-item label="sop岗位" v-bind="validateInfos.sopPost" id="SopHomePageForm-sopPost" name="sopPost">
								<a-input v-model:value="formData.sopPost" placeholder="请输入sop岗位"  allow-clear ></a-input>
							</a-form-item>
						</a-col>
						<a-col :span="24">
							<a-form-item label="sop编号" v-bind="validateInfos.sopNumber" id="SopHomePageForm-sopNumber" name="sopNumber">
								<a-input v-model:value="formData.sopNumber" placeholder="请输入sop编号"  allow-clear ></a-input>
							</a-form-item>
						</a-col>
						<a-col :span="24">
							<a-form-item label="sop唯一描述" v-bind="validateInfos.sopDesc" id="SopHomePageForm-sopDesc" name="sopDesc">
								<a-textarea v-model:value="formData.sopDesc" :rows="4" placeholder="请输入sop唯一描述" />
							</a-form-item>
						</a-col>
						<a-col :span="24">
							<a-form-item label="sop上传记录" v-bind="validateInfos.sopDescStr" id="SopHomePageForm-sopDescStr" name="sopDescStr">
								<a-textarea v-model:value="formData.sopDescStr" :rows="4" placeholder="请输入sop上传记录" />
							</a-form-item>
						</a-col>
						<a-col :span="24">
							<a-form-item label="关键sop" v-bind="validateInfos.sopState" id="SopHomePageForm-sopState" name="sopState">
								<a-input-number v-model:value="formData.sopState" placeholder="请输入关键sop" style="width: 100%" />
							</a-form-item>
						</a-col>
						<a-col :span="24">
							<a-form-item label="owd频次" v-bind="validateInfos.owdFrequency" id="SopHomePageForm-owdFrequency" name="owdFrequency">
								<a-input-number v-model:value="formData.owdFrequency" placeholder="请输入owd频次" style="width: 100%" />
							</a-form-item>
						</a-col>
						<a-col :span="24">
							<a-form-item label="编写职责" v-bind="validateInfos.writeTwoRr" id="SopHomePageForm-writeTwoRr" name="writeTwoRr">
								<a-textarea v-model:value="formData.writeTwoRr" :rows="4" placeholder="请输入编写职责" />
							</a-form-item>
						</a-col>
						<a-col :span="24">
							<a-form-item label="最新版版本号" v-bind="validateInfos.versionNumber" id="SopHomePageForm-versionNumber" name="versionNumber">
								<a-input v-model:value="formData.versionNumber" placeholder="请输入最新版版本号"  allow-clear ></a-input>
							</a-form-item>
						</a-col>
						<a-col :span="24">
							<a-form-item label="实施时间" v-bind="validateInfos.putIntoTime" id="SopHomePageForm-putIntoTime" name="putIntoTime">
								<a-date-picker placeholder="请选择实施时间"  v-model:value="formData.putIntoTime" showTime value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%"  allow-clear />
							</a-form-item>
						</a-col>
						<a-col :span="24">
							<a-form-item label="排序" v-bind="validateInfos.sort" id="SopHomePageForm-sort" name="sort">
								<a-input-number v-model:value="formData.sort" placeholder="请输入排序" style="width: 100%" />
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
  import { saveOrUpdate } from '../SopHomePage.api';
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
    sopType: '',   
    sopActivity: '',   
    sopPost: '',   
    sopNumber: '',   
    sopDesc: '',   
    sopDescStr: '',   
    sopState: undefined,
    owdFrequency: undefined,
    writeTwoRr: '',   
    versionNumber: '',   
    putIntoTime: '',   
    sort: undefined,
    delFlag: undefined,
  });
  const { createMessage } = useMessage();
  const labelCol = ref<any>({ xs: { span: 24 }, sm: { span: 5 } });
  const wrapperCol = ref<any>({ xs: { span: 24 }, sm: { span: 16 } });
  const confirmLoading = ref<boolean>(false);
  //表单验证
  const validatorRules = reactive({
    tenantId: [{ required: true, message: '请输入租户ID!'},],
    factoryCode: [{ required: true, message: '请输入工厂Code!'},],
    deptCode: [{ required: true, message: '请输入部门Code!'},],
    lineCode: [{ required: true, message: '请输入产线Code!'},],
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
