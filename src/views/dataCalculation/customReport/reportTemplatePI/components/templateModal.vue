<template>
  <BasicModal 
    @register="registerModal" 
    v-bind="$attrs" 
    :title="`${title}`"
    :cancelText="t('language.closeText')"
    :defaultFullscreen = true
    :canFullscreen = false
    :destroyOnClose="true"
    @ok="handleSubmit">
    <CustomReport v-if="record && record.reportType == 2" ref="customReportRef"></CustomReport>
    <VerticalHeadConfig v-else-if="record && record.reportType == 3" ref="verticalHeadConfigRef"></VerticalHeadConfig>
    <SummaryTemplateConfig v-else-if="record && record.reportType == 4" ref="summaryTemplateConfigRef"></SummaryTemplateConfig>
    <TableHeadConfig  v-else-if="record && record.reportType == 5" ref="tableHeadConfigRef"/>
  </BasicModal>
</template>
<script lang="ts" setup>
  import { BasicModal, useModalInner } from '/@/components/Modal';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { nextTick, ref } from 'vue';
  import { useI18n } from '/@/hooks/web/useI18n';
  import CustomReport from '../../customReport/index.vue';
  import VerticalHeadConfig from '/@/views/dataCalculation/customReport/verticalHead/VerticalHeadConfig.vue';
  import SummaryTemplateConfig from '/@/views/dataCalculation/customReport/summaryTemplate/SummaryTemplateConfig.vue';
  import TableHeadConfig from '/@/views/dataCalculation/customReport/tableHead/TableHeadConfig.vue';
  import { updateData } from '../index.api';
  const { t } = useI18n();
  
  // 声明Emits
  const emit = defineEmits(['success']);

  //标题
  const title = ref();
  //模板配置子组件实例
  const customReportRef = ref();
  //垂直表头子组件实例
  const verticalHeadConfigRef = ref();
  //统计表头子组件实例
  const summaryTemplateConfigRef = ref();
  //实际操作的子组件实例
  const actionRef = ref();
  //数据记录
  const record = ref();
  //表头配置
  const tableHeadConfigRef = ref();
  
  //注册modal
  const [registerModal, { closeModal }] = useModalInner(async (data: any) => {
    //设置标题
    title.value = data.record.reportName;
    //设置数据记录
    record.value = data.record;
    nextTick(() => {
      //初始化子组件
      if(data.record.reportType == 2){
        actionRef.value = customReportRef.value;
      }else if(data.record.reportType == 3){
        actionRef.value = verticalHeadConfigRef.value;
      }else if(data.record.reportType == 4){
        actionRef.value = summaryTemplateConfigRef.value;
      }else if(data.record.reportType == 5){
        actionRef.value = tableHeadConfigRef.value;
      }
      actionRef.value.initData(data.record, 'config');
    });
  });

  //注册Message
  const { createMessage: msg } = useMessage();

  //提交事件
  async function handleSubmit() {
    try {
      let resultData = await actionRef.value.getConfigData();
      if(false === resultData){
        return;
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
