<template>
  <BasicDrawer v-bind="$attrs" @register="registerDrawer" title="已关联检查项" destroyOnClose width="70%">
    <MdmTaskItemList ref="taskItemRef" />
  </BasicDrawer>
</template>
<script lang="ts" setup>
import { BasicDrawer, useDrawerInner } from '@/components/Drawer';
import MdmTaskItemList from '/@/views/dataCalculation/mdm/taskItem/MdmTaskItemList.vue';
import { ref } from 'vue';
const taskItemRef = ref();
//注册抽屉
const [registerDrawer] = useDrawerInner(async (data) => {
  taskItemRef.value.reloadData(data.record, data.checkTaskType);
});
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