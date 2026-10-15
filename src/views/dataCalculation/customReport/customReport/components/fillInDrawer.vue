<template>
  <BasicDrawer v-bind="$attrs" @register="registerDrawer" title="设置填报项" width="500px" @ok="handleSubmit">
    <BasicForm @register="registerForm">
      <!-- 底部插槽 -->
      <template #formFooter>
        <div style="margin: 0 auto" v-if="isShowClear">
          <a-button type="link" danger @click="handleClearConfig()">清除填报项</a-button>
        </div>
      </template>
    </BasicForm>
  </BasicDrawer>
</template>
<script lang="ts" setup>
import { ref } from 'vue';
import { BasicForm, useForm } from '@/components/Form';
import { BasicDrawer, useDrawerInner } from '@/components/Drawer';
import { addFormSchemas } from '../index.data';
import { getDictList } from '../index.api';
import { useMessage } from '/@/hooks/web/useMessage';

//消息提示
const { createConfirm } = useMessage();
// 声明Emits
const emit = defineEmits(['success', 'clearComponent']);
//字典
const dictOption = ref([] as any);
//是否显示清除填报项
const isShowClear = ref(false);
//父级数据
const parentData = ref({} as any);
//填报项字段数据
const tableFieldData = ref();

//填报项组件
const componentOptions = [
  { value: 'input', label: '输入框' },
  { value: 'select', label: '选择框' },
  { value: 'selectUser', label: '用户选择框' },
  { value: 'datepicker', label: '日期选择框', disabled: true },
  { value: 'dateTimePicker', label: '时间选择框', disabled: true },
  { value: 'textarea', label: '超长文本框', disabled: true },
];

//注册表单
const [registerForm, { resetFields, updateSchema, getFieldsValue, setFieldsValue, validate }] = useForm({
  labelWidth: 90,
  schemas: addFormSchemas,
  showActionButtonGroup: false,
});
//注册抽屉
const [registerDrawer, { setDrawerProps, closeDrawer }] = useDrawerInner(async (data) => {
  parentData.value = data.parentData;
  isShowClear.value = false;
  resetFields();
  dictOption.value = await getDictList({pageSize: 999});
  validComponent(data);
});

//校验是否已经设置过填报项
function validComponent(data){
  //选择的单元格数据
  const args = data.args;
  //绑定表的字段数据
  tableFieldData.value = data.tableFieldData;
  //模版组件数据
  const componentObject = data.componentObject;
  
  let ifShowDict = false;
  // 判断是否已经设置过填报项
  if(componentObject){
    isShowClear.value = true;
    ifShowDict = componentObject.componentType === 'select' ? true : false;
    setFieldsValue(componentObject);
  }else{
    setFieldsValue({row: args.row, col: args.col});
  }
  
  //修改表单配置
  updateSchema([
    {
      label: '对应字段',
      field: 'mappingColumn',
      componentProps: {
        options: data.tableFieldData,
        fieldNames: {label: 'name', value: 'fieldName'},
        onChange: handleMappingChange,
        filterOption: (input, option) => handleFilterOption(input, option, 'name'),
      }
    },
    {
      label: '填报项组件',
      field: 'componentType',
      componentProps: {
        options: componentOptions,
        onChange: handleTypeChange,
        disabled: false
      }
    },
    {
      label: '组件字典配置',
      field: 'dictType',
      componentProps: {
        options: dictOption.value.records,
        fieldNames: {label: 'dictGroupName', value: 'dictGroupCode'},
        filterOption: (input, option) => handleFilterOption(input, option, 'dictGroupName'),
        disabled: false
      },
      // ifShow: ifShowDict,
      ifShow: ({values}) => {
        return values.componentType == 'select' ? true : false;
      }
    },
  ]);
}

function handleMappingChange(value, obj){
  if(obj){
    let typeValue = 'input';
    let dictType = '';
    let disabled = false;

    if(obj.type == '4'){
      typeValue = 'datepicker';
      disabled = true;
    }else if(obj.type == '5'){
      typeValue = 'textarea';
      disabled = true;
    }else if(obj.type == '6'){
      typeValue = 'dateTimePicker';
      disabled = true;
    }else if(obj.editWidgetType){
      typeValue = obj.editWidgetType;
      if(obj.dropdownData){
        dictType = obj.dropdownData;
      }
    }
    updateSchema([
      {
        label: '填报项组件',
        field: 'componentType',
        componentProps: {
          disabled
        },
      },
    ]);
    setFieldsValue({
      componentType: typeValue, 
      dictType: dictType
    });
  }
}

function handleTypeChange(value, obj){
  let ifShow = value == 'select' ? true : false;
  updateSchema([
    {
      label: '组件字典配置',
      field: 'dictType',
      ifShow
    },
  ]);
}

//清除填报项
function handleClearConfig () {
  createConfirm({
    title: '清除填报项',
    content: `确定要清除该填报项吗？`,
    iconType: 'warning',
    onOk: function(){
      emit('clearComponent', getFieldsValue().row, getFieldsValue().col);
      closeDrawer();
    },
  });
}

/**
 * pi类型填报项设置
 */
async function piConfigSet(obj){
  //设置表单字段值
  let newData = {
    componentType: obj.editWidgetType,
    dictType: null,
  };
  handleTypeChange(obj.editWidgetType, obj);
  //如果是选择框，显示下拉组件配置
  if(obj.editWidgetType === 'select'){
    newData.dictType = obj.dropdownData;
  }
  setFieldsValue(newData);
}

//回调
async function handleSubmit() {
  try {
    const values = await validate();
    values.fieldText = tableFieldData.value.find(item => item.fieldName === values.mappingColumn).name;
    setDrawerProps({ confirmLoading: true });
    closeDrawer();
    emit('success', values);
  } finally {
    setDrawerProps({ confirmLoading: false });
  }
}

/**
 *下拉框搜索 
 */
function handleFilterOption(input, option, searchField) {
  return option[searchField].toLowerCase().indexOf(input.toLowerCase()) >= 0;
}
</script>


<style lang="less" scoped>
  /** 时间和数字输入框样式 */
  :deep(.ant-input-number){
		width: 100%
	}

	:deep(.ant-calendar-picker){
		width: 100%
	}

  :deep(.ant-select-selection-placeholder) {
    color: #BEBEBE !important;
  }
</style>