<template>
  <BasicDrawer v-bind="$attrs" @register="registerDrawer" title="设置数据匹配项" width="500px" @ok="handleSubmit">
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
import { useMessage } from '/@/hooks/web/useMessage';

//消息提示
const { createConfirm } = useMessage();
// 声明Emits
const emit = defineEmits(['success', 'clearComponent']);
//是否显示清除填报项
const isShowClear = ref(false);
//父级数据
const parentData = ref({} as any);
//填报项字段数据
const tableFieldData = ref();

//注册表单
const [registerForm, { resetFields, updateSchema, getFieldsValue, setFieldsValue, validate }] = useForm({
  labelWidth: 90,
  schemas: [
    {
      label: '查询字段',
      field: 'conditionColumn',
      component: 'Select',
      required: true,
    },
    {
      label: '匹配类型',
      field: 'conditionType',
      component: 'Select',
      componentProps: {
        options: [
          { value: '1', label: '全值匹配' },
          { value: '2', label: '整点时间' },
          { value: '3', label: '时分秒' },
        ],
      },
      required: true,
    },
    {
      label: '匹配值',
      field: 'conditionValue',
      component: 'Input',
      required: true,
    },
    {
      label: '行',
      field: 'row',
      component: 'Input',
      required: true,
      show: false,
    },
    {
      label: '列',
      field: 'col',
      component: 'Input',
      required: true,
      show: false,
    },
  ],
  showActionButtonGroup: false,
});

//注册抽屉
const [registerDrawer, { setDrawerProps, closeDrawer }] = useDrawerInner(async (data) => {
  resetFields();
  parentData.value = data.parentData;
  isShowClear.value = false;
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
  // 判断是否已经设置过填报项
  if(componentObject && componentObject.conditionColumn){
    isShowClear.value = true;
    setFieldsValue(componentObject);
  }else{
    setFieldsValue({row: args.row, col: args.col});
  }
  
  //修改表单配置
  updateSchema([
    {
      label: '查询字段',
      field: 'conditionColumn',
      componentProps: {
        options: data.tableFieldData,
        fieldNames: {label: 'name', value: 'fieldName'},
        filterOption: (input, option) => handleFilterOption(input, option, 'name'),
      }
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

//回调
async function handleSubmit() {
  try {
    const values = await validate();
    values.fieldText = tableFieldData.value.find(item => item.fieldName === values.conditionColumn).name;
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