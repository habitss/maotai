<template>
  <a-spin :spinning="confirmLoading">
  <a-form ref="formRef" class="antd-modal-form" :labelCol="labelCol" :wrapperCol="wrapperCol">
    <a-row>
      <!-- 字段中文名称 -->
      <a-col :span="24">
        <a-form-item label="字段中文名称" v-bind="validateInfos.name">
          <a-input v-model:value="formData.name" placeholder="请输入字段中文名称" :disabled="disabled" />
        </a-form-item>
      </a-col>
      <!-- 字段类型 -->
      <a-col :span="24">
        <a-form-item label="字段类型" v-bind="validateInfos.type">
          <a-select v-model:value="formData.type" allowClear placeholder="请选择字段类型" :disabled="formData.id?true:false">
              <a-select-option v-for="(item, index) in fieldTypeArr" :key="item.itemVal" :value="item.itemVal">
                {{ item.itemKey }}
              </a-select-option>
          </a-select>
        </a-form-item>
      </a-col>
      <!--字段长度-->
      <a-col :span="24" v-if="formData.type == '1'">
        <a-form-item label="字段长度" v-bind="validateInfos.fieldLength">
          <a-input v-model:value="formData.fieldLength" placeholder="请输入字段长度" :disabled="formData.id?true:false" />
        </a-form-item>
      </a-col>
      
      <!-- 排序 -->
      <a-col :span="24">
        <a-form-item label="排序" v-bind="validateInfos.sort">
          <a-input-number v-model:value="formData.sort" :placeholder="t('language.enterSort')" style="width: 100%" :disabled="disabled" />
        </a-form-item>
      </a-col>
      <!--是否表单显示-->
      <!-- <a-col :span="24">
        <a-form-item label="报表名称" v-bind="validateInfos.name">
          <a-input v-model:value="formData.name" :placeholder="请输入报表名称" :disabled="disabled" />
        </a-form-item>
      </a-col> -->
      <!-- 是否列表显示 -->
      <a-col :span="24">
        <a-form-item label="是否列表显示" v-bind="validateInfos.isList">
          <a-switch checkedChildren="是" unCheckedChildren="否" v-model:checked="modelData.isList" />
        </a-form-item>
      </a-col>

      <!-- 是否排序 -->
      <a-col :span="24">
        <a-form-item label="是否排序" v-bind="validateInfos.isSort">
          <a-switch checkedChildren="是" unCheckedChildren="否" v-model:checked="modelData.isSort" />
        </a-form-item>
      </a-col>
      <!-- 排序方式 -->
      <a-col :span="24" v-if="modelData.isSort">
        <a-form-item label="排序方式" v-bind="validateInfos.sortType">
          <a-switch checkedChildren="升序" checkedValue="1" unCheckedChildren="降序" unCheckedValue="0" v-model:checked="modelData.sortType" />
        </a-form-item>
      </a-col>
      <!-- 是否查询条件 -->
      <a-col :span="24">
        <a-form-item label="是否查询条件" v-bind="validateInfos.isQuery">
          <a-switch checkedChildren="是" unCheckedChildren="否" v-model:checked="modelData.isQuery" />
        </a-form-item>
      </a-col>

      <!--是否唯一约束-->
      <a-col :span="24">
        <a-form-item label="是否唯一约束" v-bind="validateInfos.isUnique">
          <a-switch checkedChildren="是" unCheckedChildren="否" v-model:checked="modelData.isUnique" />
        </a-form-item>
      </a-col>
    </a-row>
     <div style="text-align: center;margin-top: 20px;">
      <a-button style="margin-right: 8px" @click="onClose">取消</a-button>
      <a-button type="primary" @click="submitForm">保存</a-button>
    </div>
  </a-form>
</a-spin>
</template>

<script lang="ts" setup>
import { ref, reactive, defineExpose, nextTick, defineProps, computed, onMounted, watch } from 'vue';
import { useMessage } from '/@/hooks/web/useMessage';
import { getValueType } from '/@/utils';
import { Form } from 'ant-design-vue';
import {saveOrUpdate, getDictItemList, getMaxSort} from '../reportConfigField.api';
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
  id: '',
  name: '',
  type: '',
});
const modelData = reactive<Record<string, any>>({
  // isForm: true,
  isList: true,
  isSort: false,
  sortType: true,
  isQuery: false,
  isUnique:false
});

const fieldTypeArr = reactive<Record<string, any>>([
  { value: '1', label: 'String' },
  { value: '2', label: 'Integer' },
  // { value: '3', label: 'Double' },
  { value: '4', label: 'Date' },
  { value: '5', label: 'Text' },
  { value: '6', label: 'DateTime' }
]);

const takingValueArr = reactive<Record<string, any>>([
  { value: '1', label: '向上取整' },
  { value: '2', label: '向下取整' },
  // { value: '3', label: '四舍五入' }
]);
const { createMessage } = useMessage();
const labelCol = ref<any>({ xs: { span: 24 }, sm: { span: 5 } });
const wrapperCol = ref<any>({ xs: { span: 24 }, sm: { span: 16 } });
const confirmLoading = ref<boolean>(false);
//表单验证
const validatorRules = {
  name: [{ required: true, message: `字段中文名称不为空!` }],
  type: [{ required: true, message: `字段类型不为空!` }],
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
const setEditStatus = computed(() => {
  if (!props.isAdd) {
    return true;
  }
  return false;
});
watch(
  () => formData.orgTypeCode,
  (factoryCode: string, oldVal: string) => {
      // formData.deviceTypeId = undefined;
  }
);


async function init(){
  modelData.isList = formData.isList == '1'?true:false;
  modelData.isSort = formData.isSort == '1'?true:false;
  modelData.sortType = formData.sortType == '1'?true:false;
  modelData.isQuery = formData.isQuery == '1'?true:false;
  modelData.isUnique = formData.isUnique == '1'?true:false;
  const res = await getDictItemList({pageSize: 999, groupCode: 'MES3_FIELD_TYPE'});
  Object.assign(fieldTypeArr, res.records);
  console.log('fieldTypeArr :>> ', fieldTypeArr);
}

/**
 * 新增
 */
async function add(tableId) {
  formData.tableId = tableId
  formData.sort = (await getMaxSort({tableId: tableId})??0)+1;
  formData.isList = true;
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
    init();
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

  //是否启用转换
  // formData.value.isForm = modelData.value.isForm ? 1 : 0
  formData.isList = modelData.isList ? 1 : 0
  formData.isSort = modelData.isSort ? 1 : 0
  formData.sortType = modelData.sortType ? 1 : 0
  if(formData.isSort === 0){
    formData.sortType="";
  }
  formData.isQuery = modelData.isQuery ? 1 : 0
  formData.isUnique = modelData.isUnique ? 1 : 0

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

function getChange() {
  formData.deptCode = '';
  formData.lineCode = '';
}

function getDepartObject(ret) {
  if (ret.memo) {
    formData.orgTypeCode = undefined;
    formData.orgTypeCode = ret.memo;
  }
}

defineExpose({
  add,
  edit,
  submitForm,
});


function onClose(){
  emit('close');
}
</script>

<style lang="less" scoped>
// @import '../../common.less';
</style>