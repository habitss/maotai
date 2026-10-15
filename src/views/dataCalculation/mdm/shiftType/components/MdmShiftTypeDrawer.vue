<template>
  <BasicDrawer v-bind="$attrs" @register="registerDrawer" title="班次维护" width="70%">
    <MdmShiftManageList :shiftTypeRecord/>
  </BasicDrawer>
</template>
<script lang="ts" setup>
import { BasicDrawer, useDrawerInner } from '@/components/Drawer';
import MdmShiftManageList from '../../shiftManage/MdmShiftManageList.vue';
import { ref } from 'vue';

//班次类别数据
const shiftTypeRecord = ref();

//注册抽屉
const [registerDrawer] = useDrawerInner(async (data) => {
  shiftTypeRecord.value = data.record;
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