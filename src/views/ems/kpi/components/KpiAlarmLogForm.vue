<template>
  <a-spin :spinning="confirmLoading">
    <JFormContainer :disabled="disabled">
      <template #detail>
        <a-form ref="formRef" class="antd-modal-form" :labelCol="labelCol" :wrapperCol="wrapperCol" name="KpiAlarmLogForm">
          <a-row>
						<a-col :span="24">
							<a-form-item label="工厂" v-bind="validateInfos.factoryCode" id="KpiAlarmLogForm-factoryCode" name="factoryCode">
								<j-dict-select-tag v-model:value="formData.factoryCode" dictCode="sys_depart,depart_name,org_code" placeholder="请选择工厂Code"  allow-clear />
							</a-form-item>
						</a-col>
						<a-col :span="24">
							<a-form-item label="部门" v-bind="validateInfos.deptCode" id="KpiAlarmLogForm-deptCode" name="deptCode">
								<j-dict-select-tag v-model:value="formData.deptCode" dictCode="sys_depart,depart_name,org_code" placeholder="请选择部门Code"  allow-clear />
							</a-form-item>
						</a-col>
						<a-col :span="24">
							<a-form-item label="产线" v-bind="validateInfos.lineCode" id="KpiAlarmLogForm-lineCode" name="lineCode">
								<j-dict-select-tag v-model:value="formData.lineCode" dictCode="sys_depart,depart_name,org_code" placeholder="请选择产线Code"  allow-clear />
							</a-form-item>
						</a-col>
						<a-col :span="24">
							<a-form-item label="报警限值" v-bind="validateInfos.alarmLimitId" id="KpiAlarmLogForm-alarmLimitId" name="alarmLimitId">
								<a-input-number v-model:value="formData.alarmLimitId" placeholder="请输入报警限值ID" style="width: 100%" />
							</a-form-item>
						</a-col>
						<a-col :span="24">
							<a-form-item label="点位大类" v-bind="validateInfos.tagKind" id="KpiAlarmLogForm-tagKind" name="tagKind">
								<j-dict-select-tag
                  :disabled="formData.primaryTag === 1"
                  :showChooseOption="false"
                  placeholder="请选择点位大类"
                  v-model:value="formData.tagKind"
                  :options="tagKindList"
                  @change="changeTagKind"
                />
							</a-form-item>
						</a-col>
						<a-col :span="24">
							<a-form-item label="点位" v-bind="validateInfos.tagId" id="KpiAlarmLogForm-tagId" name="tagId">
                <a-select
                  v-model:value="formData.tagId"
                  placeholder="请选择点位"
                  show-search
                  :filterOption="filterOption"
                  allow-clear
                >
                  <a-select-option v-for="d in tagOptions" :key="d.tagId" :value="d.tagId">[{{ d.tagId }}] {{ d.tagName }}</a-select-option>
                </a-select>
							</a-form-item>
						</a-col>
						<a-col :span="24">
							<a-form-item label="报警时间" v-bind="validateInfos.alarmTime" id="KpiAlarmLogForm-alarmTime" name="alarmTime">
								<a-date-picker placeholder="请选择报警时间"  v-model:value="formData.alarmTime" showTime value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%"  allow-clear />
							</a-form-item>
						</a-col>
						<a-col :span="24">
							<a-form-item label="报警内容" v-bind="validateInfos.alarmContent" id="KpiAlarmLogForm-alarmContent" name="alarmContent">
								<a-input v-model:value="formData.alarmContent" placeholder="请输入报警内容"  allow-clear ></a-input>
							</a-form-item>
						</a-col>
						<a-col :span="24">
							<a-form-item label="报警接收人" v-bind="validateInfos.alarmReciver" id="KpiAlarmLogForm-alarmReciver" name="alarmReciver">
								<a-input v-model:value="formData.alarmReciver" placeholder="请输入报警接收人"  allow-clear ></a-input>
							</a-form-item>
						</a-col>
          </a-row>
        </a-form>
      </template>
    </JFormContainer>
  </a-spin>
</template>

<script lang="ts" setup>
  import { ref, reactive, defineExpose, nextTick, defineProps, computed, inject } from 'vue';
  import { useMessage } from '/@/hooks/web/useMessage';
  import JDictSelectTag from '/@/components/Form/src/jeecg/components/JDictSelectTag.vue';
  import { getValueType } from '/@/utils';
  import { saveOrUpdate } from '../KpiAlarmLog.api';
  import { Form } from 'ant-design-vue';
  import JFormContainer from '/@/components/Form/src/container/JFormContainer.vue';
  import { TagItem, tagKindList } from '../KpiStructSet.data'
  import { TagKindEnum } from '@/enums/EmsEnum';
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
    alarmLimitId: undefined,
    tagKind: undefined,
    tagId: '',   
    alarmTime: '',   
    alarmContent: '',   
    alarmReciver: '',   
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
    alarmLimitId: [{ required: true, message: '请输入报警限值ID!'},],
    tagKind: [{ required: true, message: '请输入点位大类!'},],
    tagId: [{ required: true, message: '请输入点位ID!'},],
    alarmTime: [{ required: true, message: '请输入报警时间!'},],
    alarmContent: [{ required: true, message: '请输入报警内容!'},],
    alarmReciver: [{ required: true, message: '请输入报警接收人!'},],
  });
  const { resetFields, validate, validateInfos } = useForm(formData, validatorRules, { immediate: false });

  // 表单禁用
  const disabled = computed(()=>{
    if(props.formBpm === true){
      return false;
    }
    return props.formDisabled;
  });

  // 注入响应式数据
  const orstagList = inject<TagItem[]>('orstagList') || ref<TagItem[]>([]);
  const collectTagList = inject<TagItem[]>('collectTagList') || ref<TagItem[]>([]);

  //选项
  const tagOptions = ref<TagItem[]>([]);

  function changeTagKind(value){
    formData.tagId = '';
    setTagOption(value);
  }

  function setTagOption(value){
    tagOptions.value = [];
    if(TagKindEnum.ORSDATA === value){
      tagOptions.value = [...orstagList.value];
    } else if(TagKindEnum.COLLECT === value){
      tagOptions.value = [...collectTagList.value];
    }  
  }

  function filterOption(input, option) {
    let value = '',
      label = '';
    try {
      value = option.value;
      label = option.children()[0].children;
    } catch (e) {
      console.log('获取下拉项失败', e);
    }
    let str = input.toLowerCase();
    return value.toLowerCase().indexOf(str) >= 0 || label.toLowerCase().indexOf(str) >= 0;
  }

  
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
      setTagOption(formData.tagKind);
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
