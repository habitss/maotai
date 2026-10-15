<template>
  <BasicModal v-bind="$attrs" @register="registerModal" destroyOnClose :title="title" :width="'80%'"
    :maskClosable="false" :showCancelBtn="false" :showOkBtn="false">
    <PlanOrderList ref="planOrderListRef" />
  </BasicModal>
</template>

<script lang="ts" setup>
import { ref } from 'vue';
import { BasicModal, useModalInner } from '/@/components/Modal';
import PlanOrderList from '../order/PlanOrderList.vue'
// 任务列表ref对象
const planOrderListRef = ref();
//标题
const title = ref('');
//初始化
const [registerModal] = useModalInner(async (data) => {
  title.value = '任务明细-' + data?.record?.planName;
  planOrderListRef.value.reloadData(data.record, data?.taskItemName);
});
</script>

<style lang="less" scoped>
/** 时间和数字输入框样式 */
:deep(.ant-input-number) {
  width: 100%;
}

:deep(.ant-calendar-picker) {
  width: 100%;
}
</style>