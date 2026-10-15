<!-- 班次下拉组件 -->
<template>
  <a-select
    v-model:value="selectedValue"
    :showSearch=showSearch
    :allowClear="allowClear"
    :placeholder= "placeholder"
    :bordered="bordered"
    style="width: 100%;"
    :options="options"
    :filterOption = filterOption
  >
  </a-select>
</template>

<script setup name="KtShiftSelect">
import { ref, defineProps, defineEmits, watchEffect, watch, onMounted, toRefs, nextTick } from 'vue';
import { list } from '/@/views/dataCalculation/mdm/shiftManage/MdmShiftManage.api';
import { propTypes } from '/@/utils/propTypes';
import { cloneDeep } from 'lodash-es';

// 定义组件的 props
const props = defineProps({
  value: propTypes.string.def(''),
  pageSize: {
    type: Number,
    default: 50,
    // 自定义验证函数，确保 pageSize 是一个正整数
    validator: (value) => Number.isInteger(value) && value > 0,
  },
  allowClear: propTypes.bool.def(true),
  showSearch: propTypes.bool.def(true),
  placeholder: propTypes.string.def('请选择班次'),
  bordered: propTypes.bool.def(true),
  lineCode: propTypes.string.def(null),
});
// 定义组件的 emits
const emit = defineEmits(['update:value']);

// 定义响应式变量
const selectedValue = ref(null); // 当前选中的值
const options = ref([]); // 下拉选项列表

async function initData(){
  options.value = [];
  const { records } = await list({ pageSize: 9999, lineCode: props.lineCode });
  //组装下拉数据,显示文字为班次名称+(部门-产线-班次类型)
  records.forEach((item) => {
    options.value.push({ 
      value: item.id, 
      label: item.shiftName+'（'+item.deptCode_dictText+'-'+item.lineCode_dictText+'-'+item.shiftTypeId_dictText+'）' 
    });
  })
}

function filterOption(input, option){
    return option?.label?.toLowerCase().indexOf(input.toLowerCase()) >= 0;
}

watch(
  () => props.lineCode,
  (newVal, oldva) => {
    initData();
  }
);

watch(
  () => props.value,
  (newVal) => {
    if(newVal){
      selectedValue.value = newVal;
    } else {
      selectedValue.value = null;
    }
  }
);

// 监听 selectedValue 的变化，并通过 emit 通知父组件
watch(
  () => selectedValue.value,
  (newValue) => {
    emit('update:value', newValue);
  }
);
</script>