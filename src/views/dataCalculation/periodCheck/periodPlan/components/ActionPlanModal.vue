<template>
  <BasicModal @register="registerModal" v-bind="$attrs" :title="title" :width="width" @ok="handleSubmit" okText="保存"
               showOkBtn="true" showCancelBtn="true">
    <ActionPlanTrackForm ref="actionPlanRef" @success="handleSuccess" :initRecord="false" :isShowTopForm="false" :isShowSubmitBtn="false"></ActionPlanTrackForm>
  </BasicModal>
</template>

<script lang="ts" setup>
import { ref } from 'vue';
import { BasicModal, useModalInner } from '/@/components/Modal';
import { useI18n } from '/@/hooks/web/useI18n';
import ActionPlanTrackForm from '@/views/dataCalculation/actionPlan/track/ActionPlanTrackForm.vue';
import { list as actionList } from '@/views/dataCalculation/actionPlan/track/ActionPlanTrack.api';
import dayjs from "dayjs";
import { useUserStore } from '/@/store/modules/user';
import {useMessage} from "@/hooks/web/useMessage";

const disabledRef = ref(false);
const title = ref<string>('');
const width = ref<number>(1200);
const actionPlanRef = ref();
const { t } = useI18n();
const record = ref();
const userStore = useUserStore();
const emit = defineEmits(['success']);
//注册modal
const [registerModal, { setModalProps, closeModal }] = useModalInner(async (data: any) => {
  record.value = data.record
  await initAction(data);
  setModalProps({ confirmLoading: false, showCancelBtn: false, showOkBtn: false });
});

async function initAction(data){
  //获取数据
  let datas = [];
  if(data.id){
    const {records} = await actionList({pageSize: 999, dataId: data?.id });
    datas = records;
  }
  //初始化
  await actionPlanRef.value.otherPageInit({raiseDate: dayjs().format('YYYY-MM-DD'),
    problemTypeId: '344',		//问题分类id，引入页面对应的问题id，不知道是多少可以到页面：行动计划->问题分类中、或者数据库mes3_action_plan_problem_type中查找
    problemDescription: data.exception,  //任务/问题描述，且不少于8个字符
    createDept: userStore?.userInfo?.orgCode,
  }, datas, true);
}


//注册Message
const { createMessage: msg } = useMessage();
//表单提交事件
async function handleSubmit() {
  try {

    //关闭弹窗
    closeModal();
    //重置表单
    //刷新外部表格
    emit('success');
  } catch (error) {
    msg.error({ content: '保存失败', key: 'saving' });
  }
}

function handleSuccess(){
  closeModal();
  emit('success');
}
</script>

<style scoped>

</style>
