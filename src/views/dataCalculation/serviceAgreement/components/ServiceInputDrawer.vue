<template>
  <BasicDrawer v-bind="$attrs" @register="registerDrawer" showFooter="false" title="设置填报项" width="70vw">
      <ServiceAgreementInputList ref="serviceListRef" @custom-event="handleEvent" :isEmbed="true"></ServiceAgreementInputList>
  </BasicDrawer>
</template>

<script lang="ts" setup>
import {defineComponent, defineEmits, ref} from 'vue';
import {BasicDrawer, useDrawerInner} from '/@/components/Drawer';
import ServiceAgreementInputList
  from "@/views/dataCalculation/serviceAgreement/ServiceAgreementInputList.vue";

const serviceListRef = ref();
//注册抽屉
const [registerDrawer, { setDrawerProps, closeDrawer }] = useDrawerInner(async (data) => {

});

const emit = defineEmits(['custom-event']);

const handleEvent = (data) => {
  console.log(data)
  emit('custom-event', data); // 触发事件并传递数据
}

</script>

<style scoped>

</style>
