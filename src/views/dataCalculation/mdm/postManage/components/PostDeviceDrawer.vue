<template>
  <BasicDrawer v-bind="$attrs" @register="registerDrawer" title="设置填报项" width="50vw">
    <MdmPostDeviceList ref="postDeviceListRef" @success="handleSuccess"></MdmPostDeviceList>
  </BasicDrawer>
</template>
<script lang="ts" setup>
import { BasicDrawer, useDrawerInner } from '@/components/Drawer';
import MdmPostDeviceList from '/@/views/dataCalculation/mdm/postDevice/MdmPostDeviceList.vue';
import { ref } from 'vue';
const emit = defineEmits(['success']);
const postDeviceListRef = ref();
//注册抽屉
const [registerDrawer, { closeDrawer }] = useDrawerInner(async (data) => {
  postDeviceListRef.value.initData(data.record);
});

function handleSuccess(){
  closeDrawer();
  emit('success');
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