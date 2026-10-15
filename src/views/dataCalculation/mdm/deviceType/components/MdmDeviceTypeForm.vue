<template>
  <a-spin :spinning="confirmLoading">
    <a-form ref="formRef" class="antd-modal-form" :labelCol="labelCol" :wrapperCol="wrapperCol">
      <a-row>
        <!-- 部门 -->
        <a-col :span="24">
          <a-form-item :label="t('language.department')" v-bind="validateInfos.deptCode">
            <kt-depart-select
              v-model:value="formData.deptCode"
              select-type="depart"
              :placeholder="t('language.selectDepartment')"
              :disabled="disabled"
              @change="handleDeptChange"
            />
          </a-form-item>
        </a-col>
        <!-- 产线 -->
        <a-col :span="24">
          <a-form-item :label="t('language.productLine')" v-bind="validateInfos.lineCode">
            <kt-depart-select
              v-model:value="formData.lineCode"
              select-type="line"
              @currentData="getDepartObject"
              :placeholder="t('language.selectProductLine')"
              :parentKey="formData.deptCode"
              :disabled="disabled"
            />
          </a-form-item>
        </a-col>
        <a-col :span="24">
          <a-form-item :label="t('language.deviceTypeName')" v-bind="validateInfos.deviceTypeName">
            <a-input v-model:value="formData.deviceTypeName" :placeholder="t('language.enterDeviceTypeName')" :disabled="disabled" />
          </a-form-item>
        </a-col>
        <a-col :span="24">
          <a-form-item :label="t('language.deviceTypeCode')" v-bind="validateInfos.deviceTypeCode">
            <a-input v-model:value="formData.deviceTypeCode" :placeholder="t('language.enterDeviceTypeCode')" :disabled="disabled" />
          </a-form-item>
        </a-col>
        <a-col :span="24">
          <a-form-item :label="t('language.sort')" v-bind="validateInfos.sort">
            <a-input-number v-model:value="formData.sort" :placeholder="t('language.enterSort')" style="width: 100%" :disabled="disabled" />
          </a-form-item>
        </a-col>
        <a-col :span="24">
          <a-form-item :label="t('language.remark')" v-bind="validateInfos.remark">
            <a-textarea v-model:value="formData.remark" rows="3" :placeholder="t('language.enterRemark')" :disabled="disabled" />
          </a-form-item>
        </a-col>
      </a-row>
    </a-form>
  </a-spin>
</template>

<script lang="ts" setup>
import { ref, reactive, defineExpose, nextTick, defineProps, computed } from 'vue';
import { useMessage } from '/@/hooks/web/useMessage';
import { Form } from 'ant-design-vue';
import { useI18n } from '/@/hooks/web/useI18n';
import KtDepartSelect from '/@/components/KtDepartSelect/KtDepart.vue';
const { t } = useI18n();

// 接收父组件参数
const props = defineProps({
  formDisabled: { type: Boolean, default: false },
  formData: { type: Object, default: () => {} },
  formBpm: { type: Boolean, default: true },
});
// 获取表单
const formRef = ref();
const useForm = Form.useForm;
// 接收父组件传递的事件
const emit = defineEmits(['register', 'ok']);
// 表单数据
const formData = reactive<Record<string, any>>({
  id: '',
  dept: '',
  lineCode: '',
  deviceTypeName: '',
  deviceTypeCode: '',
  sort: undefined,
  remark: '',
});
// 注册消息组件
const { createMessage } = useMessage();
// 布局
const labelCol = ref<any>({ xs: { span: 24 }, sm: { span: 5 } });
const wrapperCol = ref<any>({ xs: { span: 24 }, sm: { span: 16 } });
// 提交loading
const confirmLoading = ref<boolean>(false);
//表单验证
const validatorRules = {
  deptCode: [{ required: true, message: `${t('language.selectDepartment')}!` }],
  lineCode: [{ required: true, message: `${t('language.selectProductLine')}!` }],
  deviceTypeName: [{ required: true, message: `${t('language.enterDeviceTypeName')}!` }],
  deviceTypeCode: [{ required: true, message: `${t('language.enterDeviceTypeCode')}!` }],
};
const { resetFields, validate, validateInfos } = useForm(formData, validatorRules, { immediate: true });

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
    //赋值
    Object.assign(formData, record);
  });
}

//部门改变
function handleDeptChange(){
  //清空产线
  formData.lineCode = '';
}

//根据部门获取产线
function getDepartObject(ret) {
  if (ret.memo) {
    formData.orgTypeCode = undefined;
    formData.orgTypeCode = ret.memo;
  }
}

/**
 * 提交数据
 */
async function submitForm() {
  // 触发表单验证
  await validate();
  confirmLoading.value = true;
  const isUpdate = ref<boolean>(false);
  if (formData.id) {
    isUpdate.value = true;
  }
  formData.factoryCode = formData.deptCode.substr(0, 9);
}

defineExpose({
  add,
  edit,
  submitForm,
});
</script>

<style lang="less" scoped>
@import '../../common.less';
</style>
