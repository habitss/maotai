<template>
  <BasicModal 
    @register="registerModal" 
    v-bind="$attrs" 
    :title="`${title}`"
    :cancelText="t('language.closeText')"
    :defaultFullscreen = true
    :canFullscreen = false
    :showOkBtn = "flag !== 'detail'"
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
  import { addData, updateData } from '../reportQueryList.api';
  const { t } = useI18n();
  
  // 声明Emits
  const emit = defineEmits(['success']);

  //标题
  const title = ref();
  //操作标识
  const flag = ref();
  //子组件实例
  const customReportRef = ref();
  //模板数据
  const templateRecord = ref();
  //填报数据
  const record = ref();
  
  //注册modal
  const [registerModal, { closeModal }] = useModalInner(async (data: any) => {
    //设置标题
    title.value = '【'+data.templateRecord.reportName+'】填报';
    //设置模板数据
    templateRecord.value = data.templateRecord;
    //设置填报数据
    record.value = data.record;
    //设置操作标识
    flag.value = data.flag;
    //重置子组件
    customReportRef.value.initData(data.templateRecord, data.flag, data.record);
  });

  //注册Message
  const { createMessage: msg } = useMessage();

  //提交事件
  async function handleSubmit() {
    try {
      //获取模板填报数据
      const reportConfig = customReportRef.value.getConfigData();
      //填报项数据
      const fillInList = reportConfig.fillInList;
      if(null == fillInList || 0 == fillInList.length){
        if(record.value.id){
          return;
        } else {
          msg.error({ content: '填报项不能为空'});
          return;
        }
      }

      const submitObj = {
        fillInList: JSON.stringify(fillInList),
        templateId: templateRecord.value.id,
        tableName: templateRecord.value.tableName,
        recordId: ''
      };
      //提交后台修改
      if(flag.value === 'add'){
        await addData(submitObj);
      }else{
        submitObj.recordId = record.value.id;
        await updateData(submitObj)
      }
      emit('success');
    } catch (error) {
      msg.error({ content: '保存失败' });
      console.log('error :>> ', error);
    }
  }
</script>
