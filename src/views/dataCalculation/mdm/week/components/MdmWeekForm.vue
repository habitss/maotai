<template>
  <a-spin :spinning="confirmLoading">
    <JFormContainer :disabled="disabled">
      <template #detail>
        <a-form ref="formRef" class="antd-modal-form" :labelCol="labelCol" :wrapperCol="wrapperCol" name="MdmWeekForm">
          <a-row>
						<a-col :span="24">
							<a-form-item label="所属年份" v-bind="validateInfos.year" id="MdmWeekForm-year" name="year">
								<a-input v-model:value="formData.year" placeholder="请输入所属年份"  allow-clear :disabled="true"></a-input>
							</a-form-item>
						</a-col>
						<a-col :span="24">
							<a-form-item label="所属月份" v-bind="validateInfos.month" id="MdmWeekForm-month" name="month">
								<!-- <a-input v-model:value="formData.month" placeholder="请输入所属月份"  allow-clear ></a-input> -->
                <a-select v-model:value="formData.month" optionFilterProp="children" allowClear
                   placeholder="请选择" style="width:100%;height:100%">
                  <a-select-option v-for="d in monthList" :key="d.key" :value="d.key">
                    {{ d.label }}
                  </a-select-option>
                </a-select>
							</a-form-item>
						</a-col>
						<a-col :span="24">
							<a-form-item label="周数" v-bind="validateInfos.weekNumber" id="MdmWeekForm-number" name="number">
								<!-- <a-input-number v-model:value="formData.weekNumber" placeholder="请输入周数" style="width: 100%" /> -->
                <a-select v-model:value="formData.weekNumber" optionFilterProp="children" allowClear
                   placeholder="请选择" style="width:100%;height:100%" :disabled="disabledForm">
                  <a-select-option v-for="d in weekNumberList" :key="d.key" :value="d.key">
                    {{ d.label }}
                  </a-select-option>
                </a-select>
							</a-form-item>
						</a-col>
						<a-col :span="24">
							<a-form-item label="开始日期" v-bind="validateInfos.startDate" id="MdmWeekForm-weekStart" name="weekStart">
								<a-date-picker placeholder="请选择开始日期"  v-model:value="formData.startDate" value-format="YYYY-MM-DD"  style="width: 100%"  allow-clear :disabled="isEdit"/>
							</a-form-item>
						</a-col>
						<a-col :span="24">
							<a-form-item label="结束日期" v-bind="validateInfos.endDate" id="MdmWeekForm-weekEnd" name="weekEnd">
								<a-date-picker placeholder="请选择结束日期"  v-model:value="formData.endDate" value-format="YYYY-MM-DD"  style="width: 100%"  allow-clear :disabled="isEdit"/>
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
  import { saveOrUpdate } from '../MdmWeek.api';
  import { Form } from 'ant-design-vue';
  import JFormContainer from '/@/components/Form/src/container/JFormContainer.vue';
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
    factory: '',   
    dept: '',   
    line: '',   
    year: '',   
    month: '',   
    weekNumber: undefined,
    startDate: '',   
    endDate: '',   
    sort: undefined,
    delFlag: undefined,
  });
  const monthList = ref([]);
  const weekNumberList = ref([]);
  const { createMessage } = useMessage();
  const labelCol = ref<any>({ xs: { span: 24 }, sm: { span: 5 } });
  const wrapperCol = ref<any>({ xs: { span: 24 }, sm: { span: 16 } });
  const confirmLoading = ref<boolean>(false);
  const disabledForm = ref<boolean>(true);
  const isEdit = ref<boolean>(true);
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
  function add(record) {
    formData.year = record.year;
    // isEdit.value = false;
    disabledForm.value = false;
    record.isEnd = true;
    edit(record);
  }

  /**
   * 编辑
   */
  function edit(record) {
    //月份
    getMonth();
    //周
    getWeekNum();

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
      //判断是否可以编辑
      if(tmpData.weekNumber == '1' || record.isEnd){
        isEdit.value = false;
      }else{
        isEdit.value = true;
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
    model.deptCode = userStore.departCode;
    model.factoryCode = userStore.factoryCode;
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

  //获取月份
  function getMonth(){
    monthList.value = Array.from({ length: 12 }, (_, index) => {
        // 月份从 1 开始，所以要在索引基础上加 1
        const monthNumber = index + 1;
        return {
            label: `${monthNumber}月`,
            key: monthNumber.toString()
        };
    });
  }
  //获取周数
  function getWeekNum(){
    weekNumberList.value = Array.from({ length: 53 }, (_, index) => {
        // 月份从 1 开始，所以要在索引基础上加 1
        const weekNumber = index + 1;
        return {
            label: `${weekNumber}周`,
            key: weekNumber
        };
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
