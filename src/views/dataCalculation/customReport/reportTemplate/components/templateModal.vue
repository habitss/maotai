<template>
  <BasicModal 
    @register="registerModal" 
    v-bind="$attrs" 
    :title="`${title}`"
    :cancelText="t('language.closeText')"
    :defaultFullscreen = true
    :canFullscreen = false
    @ok="handleSubmit">
    <CustomReport ref="customReportRef"></CustomReport>
  </BasicModal>
</template>
<script lang="ts" setup>
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { ref } from 'vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import CustomReport from '../../customReport/index.vue';
  import { updateData } from '../index.api';
  const { t } = useI18n();
  
  // 声明Emits
  const emit = defineEmits(['success']);

  //标题
  const title = ref();

  //子组件实例
  const customReportRef = ref();
  //数据记录
  const record = ref();
  
  //注册modal
  const [registerModal, { closeModal }] = useModalInner(async (data: any) => {
    //设置标题
    title.value = data.record.reportName;
    //设置数据记录
    record.value = data.record;
    //重置子组件
    customReportRef.value.initData(data.record, 'config');
  });

  //注册Message
  const { createMessage: msg } = useMessage();

  //提交事件
  async function handleSubmit() {
    try {
      let resultData = customReportRef.value.getConfigData();
      for(const item of resultData.columns){
        item.style = {
          borderColor: '#2A2A2A',
          borderLineWidth: '1'
        }
      }
      record.value.reportConfig = JSON.stringify(resultData);
      //提交后台修改
      await updateData(record.value);
      //关闭弹窗
      closeModal();
    } catch (error) {
      msg.error({ content: '保存失败', key: 'saving' });
    }
  }
</script>
