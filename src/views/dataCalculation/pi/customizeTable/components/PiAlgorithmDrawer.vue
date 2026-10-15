<template>
  <BasicDrawer v-bind="$attrs" @register="registerDrawer" title="算法配置" width="70vw" 
    :destroy-on-close="true" 
    :showFooter="true"
    :maskClosable="false"
    :keyboard="false"
    :ok-text="'保存'"
    @ok=handleOk(true)
  >
    <BasicForm @register="registerForm" name="PiDfTableFieldMappingForm" />
    <PiAlgorithmList v-if="currentAlgorithm === 'InfluxDB'" :departObject="piData"></PiAlgorithmList>
    <PiAdapterList v-else-if="currentAlgorithm === 'Adapter'" :departObject="piData"></PiAdapterList>
    <PiAdapterList v-else-if="currentAlgorithm === 'RA'" :departObject="piData"></PiAdapterList>
    <PiDfAdapterList v-else-if="currentAlgorithm === 'DF'" :parentData="piData"></PiDfAdapterList>
  </BasicDrawer>
</template>
<script lang="ts" setup>
import { ref } from 'vue';
import { BasicDrawer, useDrawerInner } from '@/components/Drawer';
import { BasicForm, useForm } from '/@/components/Form/index';
import { FormSchema } from '/@/components/Table';
import { saveOrUpdate } from '/@/views/dataCalculation/pi/customizeTable/PiCustomizeTableField.api';
import PiAlgorithmList from '../PiAlgorithmList.vue'
import PiAdapterList from '../PiAdapterList.vue'
import PiDfAdapterList from '/@/views/dataCalculation/pi/dfAlgorithm/PiDfAlgorithmList.vue'
import { cloneDeep } from 'lodash-es';
import { useMessage } from '/@/hooks/web/useMessage';
// Emits声明
const emit = defineEmits(['register', 'success']);
const { createConfirm } = useMessage();
//pi数据,上级页面传入
const piData = ref({} as any);
//pi表数据，上级页面你传入
const tableData = ref({} as any);
//当前算法
const currentAlgorithm = ref('');
//判断是否为切换
const isChange = ref(false);

//表单配置
const formSchema: FormSchema[] = [
  {
    label: '字段名',
    field: 'fieldName',
    component: 'Input',
    componentProps: {
      disabled: true,
    },
    colProps: {
      span: 8,
    },
  },
  {
    label: '字段描述',
    field: 'name',
    component: 'Input',
    componentProps: {
      disabled: true,
    },
    colProps: {
      span: 8,
    },
  },
  {
    label: '当前使用算法',
    field: 'currentAlgorithm',
    component: 'JDictSelectTag',
    componentProps: {
      dictCode: "mes3_mdm_dict where group_code = 'PI_ALGORITHM_TYPE' order by sort, item_key, item_val",
      type: 'select',
      labelField: 'item_key',
      valueField: 'item_val',
      showChooseOption: false,
      onChange: handleAlgorithmChange
    },
    required: true,
    colProps: {
      span: 8,
    },
  }
];

//表单配置
const [registerForm, { validate, resetFields, setFieldsValue }] = useForm({
  labelWidth: 150,
  schemas: formSchema,
  showActionButtonGroup: false,
  baseColProps: { span: 24 }
});

//注册抽屉
const [registerDrawer, { closeDrawer, changeOkLoading }] = useDrawerInner(async (data) => {
  //重置切换状态
  isChange.value = false;
  let record = cloneDeep(data.record);
  record.currentAlgorithm = data.record.currentAlgorithm ?? 'InfluxDB';
  piData.value = record;
  tableData.value = data.tableData;
  piData.value.tableName = data.tableData.tableName;
  //重置表单
  await resetFields();
  //表单赋值
  await setFieldsValue({
    ...record,
  });
  handleAlgorithmChange(record.currentAlgorithm);
});

/**
 * 算法改变事件
 */
function handleAlgorithmChange(e){
    //判断是否为切换，初始加载不保存
    if(isChange.value === true){
      const previousAlgorithm = currentAlgorithm.value;
      createConfirm({
        iconType: 'warning',
        title: '提示',
        content: '切换算法后，之前的算法将不再计算，是否继续？',
        onOk: () => {
          updateAlgorithm(e);
          handleOk(false);
        },
        onCancel: () => {
          setFieldsValue({ currentAlgorithm: previousAlgorithm });
        },
      });
      return;
    }
    isChange.value = true;
    updateAlgorithm(e);
}

function updateAlgorithm(e){
    piData.value.algorithmType = '';
    if(e === 'Adapter'){
      piData.value.algorithmType = 1;
    }else if(e === 'RA'){
      piData.value.algorithmType = 2;
    }
    currentAlgorithm.value = e;
}

async function handleOk(isClose){
  try {
    await validate();
    piData.value.currentAlgorithm = currentAlgorithm.value;
    changeOkLoading(true);
    //提交表单
    await saveOrUpdate(piData.value, true);
    if(isClose){
      //关闭弹窗
      closeDrawer();
    }
    //刷新列表
    emit('success');
  } catch ({ errorFields }) {
    if (errorFields) {
      const firstField = errorFields[0];
      if (firstField) {
        scrollToField(firstField.name, { behavior: 'smooth', block: 'center' });
      }
    }
    return Promise.reject(errorFields);
  } finally {
    changeOkLoading(false);
  }
}
</script>
<style lang="less" scoped>
/** 时间和数字输入框样式 */
:deep(.ant-input-number) {
  width: 100%
}

:deep(.ant-calendar-picker) {
  width: 100%
}

:deep(.ant-select-selection-placeholder) {
  color: #BEBEBE !important;
}
</style>