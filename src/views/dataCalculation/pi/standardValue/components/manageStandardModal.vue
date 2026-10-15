<template>
  <BasicModal 
    :title="title"
    :width="width"
    :visible="visible"
    
    :destroyOnClose = true
    :okButtonProps="{ class: { 'jee-hidden': disableSubmit } }"
    @cancel="handleCancel"
    :show-ok-btn="false"
    >
    <ManageList ref="maintenanceRef"></ManageList>
  </BasicModal>
</template>
<script lang="ts" setup>
  import ManageList from '../maintenance/standardList.vue';
  import {BasicModal} from '/@/components/Modal';
  import { ref,nextTick } from 'vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  const { t } = useI18n();

  //配置数据
  const visible = ref<boolean>(false);
  const width = ref<number>(1200);
  const maintenanceRef = ref();
  const title = ref();

  function init(data) {
    visible.value = true;
    title.value = "标准-["+data.name+"]";
    nextTick(() => {
      maintenanceRef.value.initData(data);
    })
  }
  /**
   * 取消按钮回调事件
   */
  function handleCancel() {
    visible.value = false;
  }
  defineExpose({
      init
  });
</script>
<style scoped>
  /** 时间和数字输入框样式 */
  :deep(.ant-input-number) {
    width: 100%;
  }

  :deep(.ant-picker) {
    width: 100%;
  }

  /** 下拉框默认文字颜色 */
  :deep(.ant-select-selection-placeholder) {
    color: #BEBEBE !important;
  }
</style>