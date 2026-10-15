<template>
  <BasicDrawer v-bind="$attrs" @register="registerDrawer" title="人员分配" width="70%">
    <MdmShiftGroupPeopleList :shiftGroupRecord/>
  </BasicDrawer>
</template>
<script lang="ts" setup>
import { BasicDrawer, useDrawerInner } from '@/components/Drawer';
import MdmShiftGroupPeopleList from '../../shiftGroupPeople/MdmShiftGroupPeopleList.vue';
import { ref } from 'vue';

//班组数据
const shiftGroupRecord = ref();

//注册抽屉
const [registerDrawer] = useDrawerInner(async (data) => {
  shiftGroupRecord.value = data.record;
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