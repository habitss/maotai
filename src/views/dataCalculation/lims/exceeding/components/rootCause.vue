<template>
    <BasicModal :title="title" v-bind="$attrs" @register="registerModal" destroyOnClose width="80vw" :minHeight="300" 
      :bodyStyle="{height: height}" :maskClosable="false" :showOkBtn="false">
      <!--根本原因-->
      <div style="margin: 10px;">
        <div>根本原因：</div>
        <a-textarea v-model:value="limsForm.reason" :disabled="limsForm.reasonStatus =='1'" rows="4"/>
        <div style="margin-top: 5px;text-align: center;" v-if="limsForm.reasonStatus !='1'">
          <a-button type="primary" @click="handleSave()" :loading="saveLoading" style="margin-right: 5px;">{{ saveTitle }}</a-button>
          <a-button type="primary" @click="handleCancel">取消</a-button>
        </div>
      </div>
      <!--5why分析-->
      <div v-if="fiveWhyShow">
        <a-divider>5WHY分析</a-divider>
        <FiveWhyList ref="fiveWhyRef" @close="fiveWhyClose"></FiveWhyList>
      </div>
    </BasicModal>
</template>
  
  <script lang="ts" setup>
  import { nextTick, ref } from 'vue';
  import { BasicModal,useModalInner } from '/@/components/Modal';
  import FiveWhyList from '/@/views/dataCalculation/fiveWhy/components/fiveWhyList.vue';
  import { updateFwhyData } from '/@/views/dataCalculation/lims/exceeding/LimsResultsAbnormal.api';
  import { cloneDeep } from 'lodash-es';
  //消息提示
  import { useMessage } from '/@/hooks/web/useMessage';
  const { createMessage } = useMessage();
  const emit = defineEmits(['success','close']);
  //5w组件ref
  const fiveWhyRef = ref(); 
  const title = ref("5Why分析");
  const limsForm = ref({reason:''});
  const dataParams = ref();
  //保存按钮loading
  const saveLoading = ref(false);
  const fiveWhyShow = ref(false);
  const height = ref("68vh");
  const saveTitle = ref("保存");
  //注册表单model
  // const [registerModal, { openModal }] = useModal();

  // const [registerScheduleModal, { openModal: openSchedule }] = useModal();

  //表单赋值
  const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data) => {
    // title.value = data.params?.fieldName;
    // baseData.value = cloneDeep(data.params);
    // //获取页面数据
    dataParams.value = cloneDeep(data.params);
    limsForm.value['reason'] = dataParams.value.reason;
    limsForm.value['id'] = dataParams.value.dataId;
    limsForm.value['analysisTime'] = dataParams.value.analysisTime;
    limsForm.value['examineStatus'] = dataParams.value.examineStatus;
    limsForm.value['reasonStatus'] = dataParams.value.reasonStatus;
    // //获取页面数据
    // getData();
    //调用5w组件
    if(!dataParams.value.recordType){
      fiveWhyShow.value = true;
      height.value = "68vh"
      title.value = "5Why分析"
      initFiveWhy();
    }else{
      fiveWhyShow.value = false;
      height.value = "20vh"
      title.value = "根本原因"
      if(dataParams.value.examineStatus == '2'){
        saveTitle.value = "重新提交审批"
      }
    }
    // initAbnormalAnalysisText();
    // setEvaluate();
  });
  
  //5w组件保存后的回调
  function fiveWhyClose(){
    // initFiveWhy();
    emit('close');
  }
  //初始化5w组件数据
  function initFiveWhy(){
    //调用5w组件
    nextTick(() => {
      fiveWhyRef.value.init(dataParams.value);
    })
  }


  //保存
  async function handleSave() {
    if(!limsForm.value.reason){
      createMessage.warning("根本原因不为空");
      return false;
    }
    saveLoading.value = true;
    // limsForm.value.fiveWhyStatus = '1';
    // limsForm.value.actionStatus = '1';
	// 根本原因审批状态置为待审批状态
    limsForm.value.reasonStatus = '0'; 
    await updateFwhyData(limsForm.value)
    .then((res) => {
      if (res.success) {
        createMessage.success(res.message);
        emit('close');
      } else {
        createMessage.warning(res.message);
      }
    })
    .finally(() => {
      saveLoading.value = false;
    });
  }


  //取消
  function handleCancel(){
   emit('close'); 
  }
  </script>
  
  <style lang="less" scoped>
  :deep(.ant-picker),
  :deep(.ant-input-number) {
    width: 100%;
  }
  </style>