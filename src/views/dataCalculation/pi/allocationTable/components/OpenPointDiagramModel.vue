<template>
  <BasicModal :title="title" v-bind="$attrs" @register="registerModal" destroyOnClose width="90vw" :minHeight="300" 
    :showCancelBtn="false" :showOkBtn="false" :maskClosable="false" >
    <pointDiagram ref="pointDiagramRef"></pointDiagram>
  </BasicModal>
</template>

<script lang="ts" setup>
import { ref } from 'vue';
import { BasicModal, useModalInner } from '/@/components/Modal';
import pointDiagram from '/@/views/dataCalculation/pi/pointDiagram/pointDiagram.vue'
//标题
const title = ref('点图');
//点图ref对象
const pointDiagramRef = ref();
//表单赋值
const [registerModal] = useModalInner(async (data) => {
  title.value = `${data.params.customReportDeptName}-${data.params.customReportLineName}-点图（${data.params.field}）`;
  pointDiagramRef.value.initData(data.params)
});
</script>

<style scoped>
.textarea_class {
  border: 1px solid #d9d9d9;
  border-radius: 4px;
}
.textarea_disabled_class {
  background: #F7F7F7;
  color: #D6D6D6;
}
</style>