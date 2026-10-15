<template>
    <BasicModal :title="title" v-bind="$attrs" @register="registerModal" destroyOnClose width="80vw" :minHeight="300" 
    :bodyStyle="{height: '68vh'}" :maskClosable="false" :showOkBtn="false">
      <!-- 行动记录 -->
      <ActionPlanTrackList ref="trackListmRef" @success="onSuccess" :dataId="record.analysisId" :showSearch="false"></ActionPlanTrackList>
    </BasicModal>
</template>
  
  <script lang="ts" setup>
  import { ref } from 'vue';
  import { BasicModal,useModalInner } from '/@/components/Modal';
  import { useModal } from '/@/components/Modal';
  import ActionPlanTrackList from '@/views/dataCalculation/actionPlan/track/ActionPlanTrackList.vue'
  import { cloneDeep } from 'lodash-es';
  const emit = defineEmits(['success']);
  
  const record = ref({});
  const trackListmRef = ref();
  const title = ref("行动记录");
  //注册表单model
  // const [registerModal, { openModal }] = useModal();

  //表单赋值
  const [registerModal] = useModalInner(async (data) => {
    // //获取页面数据
    if(data.params && data.params.analysisId){
      record.value = data.params;
    }else{
      record.value["analysisId"] = "-1"; 
    }
    
  });
  </script>
  
  <style lang="less" scoped>
  :deep(.ant-picker),
  :deep(.ant-input-number) {
    width: 100%;
  }
  </style>