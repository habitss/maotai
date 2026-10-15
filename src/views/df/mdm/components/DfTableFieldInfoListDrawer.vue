<template>
  <BasicDrawer v-bind="$attrs" @register="registerDrawer" title="班次维护" width="70%">
    <DfTableFieldInfoList :parentRecord />
  </BasicDrawer>
</template>
<script lang="ts" setup>
import { BasicDrawer, useDrawerInner } from '@/components/Drawer';
import DfTableFieldInfoList from '../DfTableFieldInfoList.vue';
import { ref } from 'vue';

//班次类别数据
const parentRecord = ref();

//注册抽屉
const [registerDrawer] = useDrawerInner(async (data) => {
  parentRecord.value = data.record;
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