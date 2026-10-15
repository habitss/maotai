<template>
  <BasicDrawer v-bind="$attrs" @register="registerDrawer" title="设置填报项" width="70vw" @ok="handleSubmit">
    <PiAbnormalAnalysisPointList ref="pointListRef" :isEmbed="true"></PiAbnormalAnalysisPointList>
  </BasicDrawer>
</template>
<script lang="ts" setup>
import { BasicDrawer, useDrawerInner } from '@/components/Drawer';
import PiAbnormalAnalysisPointList from '/@/views/dataCalculation/pi/abnormalPoint/PiAbnormalAnalysisPointList.vue';
import { ref } from 'vue';
import { cloneDeep } from 'lodash-es';
const emit = defineEmits(['closeDrawer']);
const pointListRef = ref();
//注册抽屉
const [registerDrawer, { setDrawerProps, closeDrawer }] = useDrawerInner(async (data) => {
  pointListRef.value.setQueryParams(data.queryParams);
});

function handleSubmit() {
  let newData = [] as any;
  for(const item of cloneDeep(pointListRef.value.getSelectRows())){
    if(item.fieldCode){
      delete item.createTime;
      delete item.createBy;
      delete item.updateTime;
      delete item.updateBy;
      item.pointId = item.id;
      newData.push(item)
    }
  }
  emit('closeDrawer', newData)
  closeDrawer();
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