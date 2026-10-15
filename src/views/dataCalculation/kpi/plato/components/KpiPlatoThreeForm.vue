<template>
  <a-spin :spinning="confirmLoading">
    <JFormContainer :disabled="disabled">
      <template #detail>
        <a-form ref="formRef" class="antd-modal-form" :labelCol="labelCol" :wrapperCol="wrapperCol" name="KpiPlatoThreeForm">
          <a-row>
            <!-- <a-col :span="24">
							<a-form-item label="年份" v-bind="validateInfos.year" id="KpiInfoForm-unit" name="year">
								<a-date-picker v-model:value="formData.year" picker="year" placeholder="请选择所属年份" valueFormat="YYYY" format="YYYY" style="width: 100%" @change="platoTwoList"/>
							</a-form-item>
						</a-col> -->
            <a-col :span="24">
							<a-form-item label="控制点名称" v-bind="validateInfos.parentCode" id="KpiPlatoThreeForm-superCode" name="parentCode">
								<!-- <a-input v-model:value="formData.superCode" placeholder="请输入控制点名称"  allow-clear ></a-input> -->
                <a-tree-select
                  v-model:value="formData.parentCode"
                  show-search
                  style="width: 100%"
                  :dropdown-style="{ maxHeight: '400px', overflow: 'auto' }"
                  placeholder="请选择控制点"
                  allow-clear
                  tree-default-expand-all
                  :tree-data="platoTwoData"
                  tree-node-filter-prop="kpiType"
                  :fieldNames= "{children:'children', label:'controllerName', key: 'id', value: 'code' }"
                  @change="platoTwo"
                ></a-tree-select>
							</a-form-item>
						</a-col>
            <!-- <a-col :span="24">
							<a-form-item label="PI名称" v-bind="validateInfos.piName" id="KpiPlatoThreeForm-piName" name="piName">
								<a-input v-model:value="formData.piName" placeholder="请输入PI名称"  allow-clear ></a-input>
							</a-form-item>
						</a-col> -->
            <a-col :span="24">
							<a-form-item label="控制活动" v-bind="validateInfos.controlActivity" id="KpiPlatoThreeForm-controlActivity" name="controlActivity">
								<a-input v-model:value="formData.controlActivity" placeholder="请输入控制活动"  allow-clear ></a-input>
							</a-form-item>
						</a-col>
						<a-col :span="24">
							<a-form-item label="标准" v-bind="validateInfos.standard" id="KpiPlatoThreeForm-standard" name="standard">
								<a-input v-model:value="formData.standard" placeholder="请输入标准"  allow-clear ></a-input>
							</a-form-item>
						</a-col>
						<a-col :span="24">
							<a-form-item label="单位" v-bind="validateInfos.unit" id="KpiPlatoThreeForm-unit" name="unit">
								<a-input v-model:value="formData.unit" placeholder="请输入单位"  allow-clear ></a-input>
							</a-form-item>
						</a-col>
            <a-col :span="24">
							<a-form-item label="备注" v-bind="validateInfos.remarks" id="KpiPlatoThreeForm-remarks" name="remarks">
								<a-input v-model:value="formData.remarks" placeholder="请输入备注"  allow-clear ></a-input>
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
  import { saveOrUpdate } from '../KpiPlatoThree.api';
  import { Form } from 'ant-design-vue';
  import JFormContainer from '/@/components/Form/src/container/JFormContainer.vue';
  import {list} from '../KpiPlatoTwo.api';
  import { useUserStore } from '/@/store/modules/user';
  // 用户信息
  const userStore = useUserStore();
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
    code: '',   
    piCode: '',   
    piName: '',   
    parentCode: undefined,   
    superCode: '',   
    remarks: '',   
    controlActivity: '',   
    standard: '',   
    unit: '',   
    sort: undefined,
    delFlag: undefined,
    // year: '',
  });
  const { createMessage } = useMessage();
  const labelCol = ref<any>({ xs: { span: 24 }, sm: { span: 5 } });
  const wrapperCol = ref<any>({ xs: { span: 24 }, sm: { span: 16 } });
  const confirmLoading = ref<boolean>(false);
  const platoTwoData = ref([]);
  //表单验证
  const validatorRules = reactive({
    // year: [{ required: true, message: '请选择年份!'},],
    parentCode: [{ required: true, message: '请选择控制点名称!'},],
    controlActivity: [{ required: true, message: '请输入控制活动!'},],
    // factoryCode: [{ required: true, message: '请输入工厂Code!'},],
    // deptCode: [{ required: true, message: '请输入部门Code!'},],
    // lineCode: [{ required: true, message: '请输入产线Code!'},],
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
      // if(tmpData.year){
      //   platoTwoList(tmpData.year,false);
      // }
      platoTwoList();
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
    }else{
      model.deptCode = userStore.departCode;
      model.factoryCode = userStore.factoryCode;
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

  /**
   * 二级柏拉图(控制点)
   */
  async function platoTwoList(){
    // if(isupdate != false){
    //   formData.parentCode = ''
    // }
    platoTwoData.value = [];
    let res = await list({pageNo: 1,pageSize: 9999});
    if(res && res.records){
      platoTwoData.value = res.records;
    }
  }

  //获取二级柏拉图中一级柏拉图等信息
  function platoTwo(val){
    const result = platoTwoData.value.find(item => item.code === val);
    formData.superCode = result.superCode;
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
