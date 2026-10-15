<template>
  <BasicDrawer v-bind="$attrs" @register="registerDrawer" :title="title" destroyOnClose width="80%">
    <MdmTaskList ref="taskRef" />
  </BasicDrawer>
</template>
<script lang="ts" setup>
import { BasicDrawer, useDrawerInner } from '@/components/Drawer';
import MdmTaskList from '/@/views/dataCalculation/mdm/task/MdmTaskList.vue';
import { ref } from 'vue';
const taskRef = ref();
const title = ref('');
//注册抽屉
const [registerDrawer] = useDrawerInner(async (data) => {
  title.value = data.record.deviceName+'-'+data.title;
  taskRef.value.reloadData(data.record, data.checkTaskType);
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