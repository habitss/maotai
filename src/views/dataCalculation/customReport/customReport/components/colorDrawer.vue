<template>
  <BasicDrawer v-bind="$attrs" @register="registerDrawer" title="设置背景颜色" width="500px" @ok="handleSubmit">
    <ant-color-picker-with-title
      v-model:modelValue="primaryColor"
      title="选择颜色"
      description=""
      required
    />
  </BasicDrawer>
</template>
<script lang="ts" setup>
import { ref } from 'vue';
import { useMessage } from '/@/hooks/web/useMessage';
import { BasicDrawer, useDrawerInner } from '@/components/Drawer';
import AntColorPickerWithTitle from './AntColorPickerWithTitle.vue';
//消息提示
const { createConfirm } = useMessage();
// 声明Emits
const emit = defineEmits(['success', 'clearComponent']);
//颜色
const primaryColor = ref('#1890ff');
//父级数据
const parentData = ref({} as any);
// const id = ref({} as any);
// const col = ref({} as any);

//注册抽屉
const [registerDrawer, { setDrawerProps, closeDrawer }] = useDrawerInner(async (data) => {
  parentData.value = data.selectCells
});


//回调
async function handleSubmit() {
  //emit('success', values);
  emit('success', primaryColor.value,parentData.value);
  closeDrawer();
}

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