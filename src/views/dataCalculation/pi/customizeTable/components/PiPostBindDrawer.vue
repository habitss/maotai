<template>
  <BasicDrawer v-bind="$attrs" @register="registerDrawer" title="设置填报项" width="70vw">
    <PiPostBindList ref="piPostListRef"></PiPostBindList>
  </BasicDrawer>
</template>
<script lang="ts" setup>
import { ref } from 'vue';
import { BasicDrawer, useDrawerInner } from '@/components/Drawer';
import PiPostBindList from '/@/views/dataCalculation/pi/postBind/PiPostList.vue';
const piPostListRef = ref();
//注册抽屉
const [registerDrawer] = useDrawerInner((data) => {
  piPostListRef.value.reloadData(data.record);
});
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