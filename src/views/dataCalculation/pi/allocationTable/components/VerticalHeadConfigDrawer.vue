<template>
  <BasicDrawer v-bind="$attrs" @register="registerDrawer" :title width="40%" :destroyOnClose="true">
    <TemplateConfigList ref="templateConfigRef" :parentData></TemplateConfigList>
  </BasicDrawer>
</template>
<script lang="ts" setup>
import { BasicDrawer, useDrawerInner } from '@/components/Drawer';
import { ref } from 'vue';
import TemplateConfigList from '/@/views/dataCalculation/customReport/reportTemplatePI/index.vue';
const templateConfigRef = ref();
const parentData = ref();
const title = ref('');
//注册抽屉
const [registerDrawer] = useDrawerInner(async (data) => {
  data.record.reportType = 3;
  parentData.value = data.record;
  title.value = '垂直表头配置 - '+data.record.reportName;
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