<template>
  <BasicDrawer v-bind="$attrs" @register="registerDrawer" title="添加关联检查项" destroyOnClose width="80%">
    <MdmItemList ref="itemRef" :deviceId="deviceId" :isTask="true" @success="saveTaskItem" />
  </BasicDrawer>
</template>
<script lang="ts" setup>
import { BasicDrawer, useDrawerInner } from '@/components/Drawer';
import MdmItemList from '/@/views/dataCalculation/mdm/item/MdmItemList.vue';
import { ref } from 'vue';
import { saveOrUpdate } from '../MdmTaskItem.api';
const emit = defineEmits(['success']);
//子组件对象
const itemRef = ref();
//设备id
const deviceId = ref();
//任务数据
const taskData = ref();
//旧的关联数据
const oldList = ref();
//注册抽屉
const [registerDrawer, {closeDrawer}] = useDrawerInner(async (data) => {
  deviceId.value = data.taskData.deviceId;
  taskData.value = data.taskData;
  oldList.value = data.oldList;
  await itemRef.value.setProps({pagination: false, immediate: false});
  await itemRef.value.reloadData(data.oldList ? data.oldList.map(item => String(item.itemId)) : null, data.checkItemType);
});

async function saveTaskItem(itemData){
  try {
    if(oldList.value.length == 0 && (!itemData || itemData.length === 0)){
      //关闭弹窗
      closeDrawer();
      return;
    }
    itemRef.value.setLoading(true);
    let newList = [] as any;
    for(const item of itemData){
      newList.push({
        factoryCode: taskData.value.factoryCode,
        deptCode: taskData.value.deptCode,
        lineCode: taskData.value.lineCode,
        taskId: taskData.value.id,
        itemId: item.id,
        itemName: item.itemName,
        itemStandard: item.itemStandard,
        itemDetail: item.detail,
        itemResultTypeCode: item.resultTypeCode,
        isUploadImg: item.isUploadImg,
        itemLocation: item.itemLocation
      })
    }
    //提交表单
    await saveOrUpdate(newList);
    //刷新列表
    emit('success');
    //关闭弹窗
    closeDrawer();
  } catch (error) {
  } finally {
    itemRef.value.setLoading(false);
  }
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