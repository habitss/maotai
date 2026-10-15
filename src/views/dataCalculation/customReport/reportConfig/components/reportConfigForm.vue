<template>
    <a-spin :spinning="confirmLoading">
    <a-form ref="formRef" class="antd-modal-form" :labelCol="labelCol" :wrapperCol="wrapperCol">
      <a-row>
        <!-- 部门 -->
        <a-col :span="24">
          <a-form-item label="部门" v-bind="validateInfos.deptCode">
              <kt-depart-select 
                v-model:value="formData.deptCode" 
                :placeholder="t('language.selectDepartment')"
                select-type="depart" 
                @change="handleDeptChange" 
              />
          </a-form-item>
        </a-col>
        <!-- 产线 -->
        <a-col :span="24">
          <a-form-item label="产线" v-bind="validateInfos.lineCode">
              <kt-depart-select 
                v-model:value="formData.lineCode" 
                select-type="line"
                :placeholder="t('language.selectProductLine')" 
                :parentKey="formData.deptCode" 
                :disabled="disabled" 
              />
          </a-form-item>
        </a-col>
        <!--报表名称-->
         <a-col :span="24">
          <a-form-item label="报表名称" v-bind="validateInfos.name">
            <a-input v-model:value="formData.name" placeholder="请输入报表名称" :disabled="disabled" />
          </a-form-item>
        </a-col>
        <!-- 排序 -->
        <a-col :span="24">
          <a-form-item label="排序" v-bind="validateInfos.sort">
            <a-input-number v-model:value="formData.sort" :placeholder="t('language.enterSort')" style="width: 100%" :disabled="disabled" />
          </a-form-item>
        </a-col>
        <!-- 备注 -->
        <a-col :span="24">
          <a-form-item :label="t('language.remark')" v-bind="validateInfos.notes">
            <a-textarea v-model:value="formData.notes" rows="3" :placeholder="t('language.enterRemark')" :disabled="disabled" />
          </a-form-item>
        </a-col>
      </a-row>
    </a-form>
  </a-spin>
</template>

<script lang="ts" setup>
  import { ref, reactive, nextTick, defineProps, computed } from 'vue';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { getValueType } from '/@/utils';
  import { Form } from 'ant-design-vue';
  import KtDepartSelect from '/@/components/KtDepartSelect/KtDepart.vue';
  import {saveOrUpdate} from '../reportConfig.api';
  import { useI18n } from '/@/hooks/web/useI18n';
  const { t } = useI18n();

  const props = defineProps({
    formDisabled: { type: Boolean, default: false },
    formData: { type: Object, default: () => {} },
    formBpm: { type: Boolean, default: true },
    isAdd: { type: Boolean, default: true },
  });

  const formRef = ref();
  const useForm = Form.useForm;
  const emit = defineEmits(['register', 'ok']);
  const formData = reactive<Record<string, any>>({
    "id": "",
    "tenantId": "",
    "factoryCode": "",
    "deptCode": "",
    "lineCode": "",
    "section": "",
    "name": "",
    "tableName": "",
    "delFlag": "",
    "createTime": "",
    "updateTime": "",
    "createBy": "",
    "updateBy": "",
    "orgTypeCode": "",
  });
  const { createMessage } = useMessage();
  const labelCol = ref<any>({ xs: { span: 24 }, sm: { span: 5 } });
  const wrapperCol = ref<any>({ xs: { span: 24 }, sm: { span: 16 } });
  const confirmLoading = ref<boolean>(false);
  //表单验证
  const validatorRules = {
    deptCode: [{ required: true, message: `部门不能为空!` }],
    lineCode: [{ required: true, message: `产线不能为空!` }],
    name: [{ required: true, message: `报表名称不能为空!` },],
  };
  const { resetFields, validate, validateInfos } = useForm(formData, validatorRules);

  // 表单禁用
  const disabled = computed(() => {
    if (props.formBpm === true) {
      if (props.formData.disabled === false) {
        return false;
      } else {
        return true;
      }
    }
    return props.formDisabled;
  });
  const setEditStatus = computed(() => {
    if (!props.isAdd) {
      return true;
    }
    return false;
  });

  /**
   * 新增
   */
  function add() {
    cleanData(formData);
    formData.value = {
      id: ''
    }
    edit({});
  }
  function cleanData(valObj){
    // 清空formData的值
    for (const key in valObj) {
        if (valObj.hasOwnProperty(key)) {
            valObj[key] = '';
        }
    }
  }
  /**
   * 编辑
   */
  function edit(record) {
    nextTick(() => {
      // resetFields();
      //赋值
      Object.assign(formData, record);
    });
  }

  /**
   * 提交数据
   */
  async function submitForm() {
    // 触发表单验证
    await validate();
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
    //设置工厂代码
    model.factoryCode = model.deptCode.substr(0, 9);
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

  //部门改变事件
  async function handleDeptChange(value){
    //清空产线
    formData.lineCode = '';
  }

  defineExpose({
    add,
    edit,
    submitForm,
  });
</script>