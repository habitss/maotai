<template>
  <BasicModal @register="registerModal" v-bind="$attrs" :title="title" :width="width" @ok="handleSubmit" :minHeight="600"
              :cancelText="t('language.closeText')" :showOkBtn=!disabledRef  :destroyOnClose="true">
    <FiveWhyList ref="fiveWhyRef" @close="fiveWhyClose"></FiveWhyList>
  </BasicModal>
</template>

<script lang="ts" setup>
import {nextTick, ref} from 'vue';
import { BasicModal, useModalInner } from '/@/components/Modal';
import { useMessage } from '/@/hooks/web/useMessage';
import { useI18n } from '/@/hooks/web/useI18n';
import FiveWhyList from '/@/views/dataCalculation/fiveWhy/components/fiveWhyList.vue';

const { t } = useI18n();

// 禁用
const disabledRef = ref(false);

//需修改记录的id
const recordId = ref();
const problem = ref();
//5w组件ref
const fiveWhyRef = ref();
const title = ref<string>('5Why分析');
const width = ref<number>(1200);


const emit = defineEmits(['success']);

//注册modal
const [registerModal, { closeModal }] = useModalInner(async (data: any) => {
  recordId.value = data.record.id;
  problem.value = data.record.exceptionDesc
  //调用5w组件
  initFiveWhy();
});

//注册Message
const { createMessage: msg } = useMessage();


//5w组件保存后的回调
function fiveWhyClose(){
  closeModal();
  emit('success');
}
//初始化5w组件数据
function initFiveWhy(){
  nextTick(() => {
    //调用5w组件
    fiveWhyRef.value.init({dataId: recordId.value, type: 10, problem: problem.value});
  })
}

//表单提交事件
async function handleSubmit(v) {
  fiveWhyRef.value.fiveSave();
}

</script>

<style>
/**隐藏样式-modal确定按钮 */
.jee-hidden {
  display: none !important;
}
</style>
